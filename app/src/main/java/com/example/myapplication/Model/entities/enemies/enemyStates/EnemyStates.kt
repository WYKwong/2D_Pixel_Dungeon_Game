package com.example.myapplication.Model.entities.enemies.enemyStates

/**
 * Enemy animation states — migrated from Java enum to Kotlin enum class.
 *
 * Maintains identical API surface for seamless Java interop:
 *   - `EnemyStates.WALK`, `EnemyStates.ATK`, etc.
 *   - `getAnimRow()` method accessible from Java unchanged.
 *
 * @property animRow the sprite sheet row index for this animation state
 */
enum class EnemyStates(val animRow: Int) {
    WALK(0),
    IDLE(2),
    ATK(4),
    HURT(6),
    DEATH(8)
}
