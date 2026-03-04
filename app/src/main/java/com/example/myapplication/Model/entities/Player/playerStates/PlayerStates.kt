package com.example.myapplication.Model.entities.Player.playerStates

/**
 * Player animation states — migrated from Java enum to Kotlin enum class.
 *
 * Maintains identical API surface for seamless Java interop:
 *   - `PlayerStates.IDLE`, `PlayerStates.ATTACK`, etc.
 *   - `getAnimRow()` method accessible from Java unchanged.
 *
 * @property animRow the sprite sheet row index for this animation state
 */
enum class PlayerStates(val animRow: Int) {
    IDLE(0),
    WALK(2),
    RUNNING(4),
    ATTACK(6),
    PROJECTILE(8),
    DASH(14),
    HURT(16),
    SKILL_ONE(22)
}
