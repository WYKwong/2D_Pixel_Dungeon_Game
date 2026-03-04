package com.example.myapplication.Model.entities.enemies.normalEnemies;


import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.RectF;

import com.example.myapplication.Model.entities.GameCharacters;
import com.example.myapplication.Model.entities.Character;
import com.example.myapplication.Model.entities.Player.Player;
import com.example.myapplication.Model.entities.Player.projectile.Projectile;
import com.example.myapplication.Model.entities.enemies.enemyStartegy.EnemyStrategy;
import com.example.myapplication.Model.entities.enemies.enemyStates.EnemyStates;
import com.example.myapplication.Model.environments.GameMap;
import com.example.myapplication.Model.environments.MapManager;
import com.example.myapplication.Model.helper.GameConstants;

import java.util.List;
import java.util.Random;

import com.example.myapplication.Model.domain.pathfinding.AStarPathfinder;
import com.example.myapplication.Model.domain.pathfinding.GridAdapter;

public abstract class AbstractEnemy extends Character {
    private long lastDirChange = System.currentTimeMillis();
    private long lastTakeProjectDamage;
    private Random rand = new Random();
    private float baseSpeed;
    private int atk;
    private float currentSpeed;
    private int chaseDistance;
    private int maxHealth;
    private int currentHealth;
    private boolean takeDamageAlready;

    private EnemyStrategy enemyStrategy;
    private EnemyStates currentState;

    private boolean onSkill;
    private boolean alreadyMadeDamageToPlayer;
    private boolean onDeath;

    // A* Pathfinding cache
    private List<int[]> currentPath;
    private int pathIndex;
    private long lastPathCalcTime;
    private static final long PATH_RECALC_INTERVAL_MS = 500; // Recalculate every 500ms


    public AbstractEnemy(
            PointF pos, GameCharacters characterType,
            float baseSpeed, int atk, int health,
            EnemyStrategy enemyStrategy) {

        super(pos, characterType);
        this.baseSpeed = baseSpeed;
        this.atk = atk;


        this.chaseDistance = getDefaultChaseDis();
        this.maxHealth = health;
        this.currentHealth = health;

        this.currentState = EnemyStates.WALK;
        this.enemyStrategy = enemyStrategy;

        takeDamageAlready = false;

        this.onSkill = false;
        this.alreadyMadeDamageToPlayer = false;
        this.onDeath = false;
    }

    public void takePjtDamage(Projectile p) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTakeProjectDamage >= 500) {
            lastTakeProjectDamage = currentTime;

            Player.getInstance().projectileHitEnemy(p);

            this.currentHealth = Math.max(currentHealth - p.getDamage(), 0);
            setToHurt();
            if (currentHealth == 0) {
                setToDeath();
            }

        }
    }

    public void takeDamage() {
        if (!takeDamageAlready) {
            takeDamageAlready = true;
            this.currentHealth = Math.max(currentHealth
                    - Player.getInstance().getCurrentDamage(), 0);
            if (currentHealth == 0) {
                setToDeath();
            } else {
                if (!onSkill) {
                    hitBack(GameConstants.Sprite.SIZE / 4f);
                }
                setToHurt();

            }
        }
    }

    private void hitBack(float dis) {
        if (Player.getInstance().getFaceDir() == GameConstants.FaceDir.LEFT) {
            dis *= -1;
        }
        hitBox.left += dis;
        hitBox.right += dis;
    }


    public void initWithDiff(int difficulty) {
        this.currentHealth *= difficulty;
        this.maxHealth = currentHealth;
    }

    public void update(double delta, MapManager mapManager, PointF playerPos) {

        if (goingUpdateMove()) {
            updateMove(delta, mapManager, playerPos);
        }


        updateEnemyAnimation();


        updateChase();
    }


    private void updateEnemyAnimation() {
        aniTick++;
        if (aniTick >= GameConstants.Animation.ANI_SPEED) {
            aniTick = 0;
            aniIndex++;
            if (aniIndex >= enemyStrategy.getAnimMaxIndex(currentState)) {
                if (onSkill) {
                    onSkill = false;
                    alreadyMadeDamageToPlayer = false;
                    setToWalk();
                }
                if (onDeath) {
                    active = false;
                }
                aniIndex = 0;
            }
        }
    }

    private void updateChase() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTakeProjectDamage <= 10000) {
            chaseDistance = 20;
        } else if (chaseDistance == 20) {
            chaseDistance = getDefaultChaseDis();
        }
    }

    abstract int getDefaultChaseDis();

    private void updateMove(double delta, MapManager mapManager, PointF playerPos) {
        GameMap gameMap = mapManager.getCurrentMap();

        currentSpeed = (float) delta * baseSpeed;

        float distanceX = hitBox.centerX() - playerPos.x;
        float distanceY = hitBox.centerY() - playerPos.y;

        double distance = Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));

        if (distance <= chaseDistance * GameConstants.Sprite.SIZE) {
            chaseMoveMode(distanceX, distanceY, gameMap, playerPos);
        } else {
            currentPath = null; // Clear path when out of range
            defaultMoveMode(gameMap);
        }

    }

    /**
     * A*-based chase mode: computes a pathfinding route around obstacles
     * and follows it node-by-node. Falls back to direct chase if A*
     * cannot find a valid path.
     */
    private void chaseMoveMode(
            float distanceX,
            float distanceY,
            GameMap gameMap,
            PointF playerPos) {

        // Recalculate A* path at intervals for performance
        long now = System.currentTimeMillis();
        if (currentPath == null || pathIndex >= currentPath.size()
                || now - lastPathCalcTime > PATH_RECALC_INTERVAL_MS) {
            recalculatePath(gameMap, playerPos);
            lastPathCalcTime = now;
        }

        // Follow A* path if available
        if (currentPath != null && pathIndex < currentPath.size()) {
            followPath(gameMap);
        } else {
            // Fallback: direct chase (original behavior)
            directChase(distanceX, distanceY, gameMap);
        }
    }

    /**
     * Recalculates the A* path from the enemy's current position to the player.
     */
    private void recalculatePath(GameMap gameMap, PointF playerPos) {
        boolean[][] grid = GridAdapter.toWalkableGrid(gameMap);
        AStarPathfinder pathfinder = new AStarPathfinder(grid);

        int startX = GridAdapter.pixelToGridX(hitBox.centerX());
        int startY = GridAdapter.pixelToGridY(hitBox.centerY());
        int goalX = GridAdapter.pixelToGridX(playerPos.x);
        int goalY = GridAdapter.pixelToGridY(playerPos.y);

        currentPath = pathfinder.findPath(startX, startY, goalX, goalY);
        pathIndex = 0;
    }

    /**
     * Moves the enemy along the pre-computed A* path, one waypoint at a time.
     */
    private void followPath(GameMap gameMap) {
        int[] target = currentPath.get(pathIndex);
        float targetPixelX = GridAdapter.gridToPixelX(target[0]);
        float targetPixelY = GridAdapter.gridToPixelY(target[1]);

        float dx = targetPixelX - hitBox.centerX();
        float dy = targetPixelY - hitBox.centerY();
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        // Reached waypoint — advance to the next one
        if (dist < currentSpeed * 2) {
            pathIndex++;
            if (pathIndex >= currentPath.size()) {
                return;
            }
            target = currentPath.get(pathIndex);
            targetPixelX = GridAdapter.gridToPixelX(target[0]);
            targetPixelY = GridAdapter.gridToPixelY(target[1]);
            dx = targetPixelX - hitBox.centerX();
            dy = targetPixelY - hitBox.centerY();
            dist = (float) Math.sqrt(dx * dx + dy * dy);
        }

        if (dist > 0) {
            float moveX = (dx / dist) * currentSpeed;
            float moveY = (dy / dist) * currentSpeed;

            // Update face direction
            if (dx > 1) {
                faceDir = GameConstants.FaceDir.RIGHT;
            } else if (dx < -1) {
                faceDir = GameConstants.FaceDir.LEFT;
            }

            // Update move direction for animation
            if (Math.abs(dx) > Math.abs(dy)) {
                moveDir = dx > 0 ? GameConstants.MoveDir.RIGHT : GameConstants.MoveDir.LEFT;
            } else {
                moveDir = dy > 0 ? GameConstants.MoveDir.DOWN : GameConstants.MoveDir.UP;
            }

            moveX(gameMap, moveX);
            moveY(gameMap, moveY);
        }
    }

    /**
     * Direct-line chase fallback when A* cannot find a path.
     * Preserves the original chase behavior for robustness.
     */
    private void directChase(float distanceX, float distanceY, GameMap gameMap) {
        float ratio = Math.abs(distanceY) / Math.abs(distanceX);
        double angle = Math.atan(ratio);
        float xSpeed = (float) Math.cos(angle);
        float ySpeed = (float) Math.sin(angle);
        float deltaX = xSpeed * currentSpeed;
        float deltaY = ySpeed * currentSpeed;

        if (xSpeed > ySpeed) {
            if (distanceX > 0) {
                moveDir = GameConstants.MoveDir.LEFT;
            } else {
                moveDir = GameConstants.MoveDir.RIGHT;
            }
        } else {
            if (distanceY > 0) {
                moveDir = GameConstants.MoveDir.UP;
            } else {
                moveDir = GameConstants.MoveDir.DOWN;
            }
        }

        if (distanceX
                > (float) (Player.getInstance().getHitBoxWidth() / 2 + getHitBoxWidth() / 2)) {
            deltaX *= -1;
            moveX(gameMap, deltaX);
        } else if (distanceX
                < -(float) (Player.getInstance().getHitBoxWidth() / 2 + getHitBoxWidth() / 2)) {
            moveX(gameMap, deltaX);
        }
        if (distanceX > 1) {
            faceDir = GameConstants.FaceDir.LEFT;
        } else if (distanceX < -1) {
            faceDir = GameConstants.FaceDir.RIGHT;
        }

        if (distanceY > 1) {
            deltaY *= -1;
            moveY(gameMap, deltaY);
        } else if (distanceY < -1) {
            moveY(gameMap, deltaY);
        }
    }


    private void moveX(GameMap gameMap, float deltaX) {
        float xL = hitBox.left + deltaX;
        float xR = hitBox.right + deltaX;
        float currYTop = hitBox.top;
        float currYBottom = hitBox.bottom;

        if (gameMap.canMoveHere(xL, currYTop, currYBottom)
                && gameMap.canMoveHere(xR, currYTop, currYBottom)
        ) {
            hitBox.left += deltaX;
            hitBox.right += deltaX;
        }
    }

    private void moveY(GameMap gameMap, float deltaY) {
        float xL = hitBox.left;
        float xR = hitBox.right;
        float currYTop = hitBox.top + deltaY;
        float currYBottom = hitBox.bottom + deltaY;

        if (gameMap.canMoveHere(xL, currYTop, currYBottom)
                && gameMap.canMoveHere(xR, currYTop, currYBottom)
        ) {
            hitBox.top += deltaY;  //updating hitBox
            hitBox.bottom += deltaY;
        }
    }








    private void defaultMoveMode(GameMap gameMap) {
        if (System.currentTimeMillis() - lastDirChange >= 3000) { // 距离上次改变方向，3000毫秒即3秒后改变怪物方向
            moveDir = rand.nextInt(4);
            lastDirChange = System.currentTimeMillis(); //更新改变方向的时间
        }
        if (moveDir == 0 || moveDir == 1) {
            faceDir = moveDir;       //若新方向为左或右，则改变为对应动画，若为上或下则保留原先的数值
        }

        //检测是否碰撞到屏幕边缘，如果是则将方向翻转(1，将坐标位置改变，2.通过改变Face_Dir改变动画行数，变为对应目标的动画
        if (moveDir == GameConstants.MoveDir.DOWN) {
            hitBox.top += currentSpeed;  //updating hitBox
            hitBox.bottom += currentSpeed; //300 is the speed
            if (!(gameMap.canMoveHereTwoX(hitBox.left, hitBox.right, hitBox.bottom))) { //2220为屏幕像素
                moveDir = GameConstants.MoveDir.UP;
            }
        } else if (moveDir == GameConstants.MoveDir.UP) {
            hitBox.top -= currentSpeed;
            hitBox.bottom -= currentSpeed;
            if (!(gameMap.canMoveHereTwoX(hitBox.left, hitBox.right, hitBox.top))) {
                moveDir = GameConstants.MoveDir.DOWN;
            }
        } else if (moveDir == GameConstants.MoveDir.RIGHT) {
            hitBox.left += currentSpeed;
            hitBox.right += currentSpeed;
            if (!(gameMap.canMoveHere(hitBox.right, hitBox.top, hitBox.bottom))) {
                moveDir = GameConstants.MoveDir.LEFT;
                faceDir = GameConstants.FaceDir.LEFT;
            }
        } else if (moveDir == GameConstants.MoveDir.LEFT) {
            hitBox.left -= currentSpeed;
            hitBox.right -= currentSpeed;
            if (!(gameMap.canMoveHere(hitBox.left, hitBox.top, hitBox.bottom))) {
                moveDir = GameConstants.MoveDir.RIGHT;
                faceDir = GameConstants.FaceDir.RIGHT;
            }
        }
    }







    public void setBaseSpeed(float baseSpeed) {
        this.baseSpeed = baseSpeed;
    }



    public int getAtk() {
        return atk;
    }


    public int getCurrentHealth() {
        return currentHealth;
    }

    public void setTakeDamageAlready(boolean takeDamageAlready) {
        this.takeDamageAlready = takeDamageAlready;
    }

    public double getHealthPercentage() {
        return (((double) currentHealth / (double) maxHealth) * 100.0);
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void startPreparingAtk() {
        setToAtk();
    }
    public void setToAtk() {
        if (!onSkill) {
            onSkill = true;
            resetAnimation();
            currentState = EnemyStates.ATK;
        }

    }
    public void setToDeath() {
        if (currentState != EnemyStates.DEATH) {
            onSkill = true;
            resetAnimation();
            currentState = EnemyStates.DEATH;
            onDeath = true;
            Player.getInstance().increaseScore(enemyStrategy.getScore());
        }


    }

    public void setToHurt() {
        if (!onSkill) {
            onSkill = true;
            resetAnimation();
            currentState = EnemyStates.HURT;
        }
    }

    private void setToWalk() {
        resetAnimation();
        currentState = EnemyStates.WALK;
    }


    private boolean goingUpdateMove() {
        return currentState == EnemyStates.WALK;
    }





    public RectF getAtkHitBox() {
        return getRectBySize(enemyStrategy.getAtkHitBoxSize());

    }

    public RectF getAtkRange() {
        return enemyStrategy.getAtkDetectBox(hitBox, faceDir, (float) (gameCharType.getScale()));

    }

    private RectF getRectBySize(PointF size) {
        if (faceDir == GameConstants.FaceDir.RIGHT) {
            return new RectF(
                    hitBox.right,
                    hitBox.bottom - size.y,
                    hitBox.right + size.x,
                    hitBox.bottom
            );
        } else {
            return new RectF(
                    hitBox.left - size.x,
                    hitBox.bottom - size.y,
                    hitBox.left,
                    hitBox.bottom
            );
        }
    }



    public int getAnimRow() {
        return currentState.getAnimRow() + faceDir;
    }


    public float getEnemyLeft() {

        if (faceDir == GameConstants.FaceDir.RIGHT) {
            return hitBox.left - getHitBoxOffsetX()
                    + enemyStrategy.adjustX(currentState, (float) gameCharType.getScale());
        } else {
            return hitBox.left
                    - enemyStrategy.adjustX(currentState, (float) gameCharType.getScale());
        }

    }

    public float getEnemyTop() {
        return hitBox.top
                - getHitBoxOffsetY()
                + enemyStrategy.adjustY(currentState, (float) gameCharType.getScale());
    }
    public Bitmap getEnemySprite() {
        return gameCharType.getSprite(
                getAnimRow(),
                aniIndex
        );
    }

    public boolean isOnDeath() {
        return onDeath;
    }


    public boolean isMakingDamage() {
        if (!enemyStrategy.isMakingDamage(currentState, aniIndex)) {
            alreadyMadeDamageToPlayer = false;
            return false;
        } else {
            return !alreadyMadeDamageToPlayer;
        }
    }

    public void setAlreadyMadeDamageToPlayer(boolean alreadyMadeDamageToPlayer) {
        this.alreadyMadeDamageToPlayer = alreadyMadeDamageToPlayer;
    }


}