package net.perfectdreams.loritta.deviouscache.data

import dev.kord.common.entity.DiscordRole
import dev.kord.common.entity.Permissions
import kotlinx.serialization.Serializable

@Serializable
data class DeviousRoleData(
    val id: LightweightSnowflake,
    val name: String,
    val color: Int,
    val hoist: Boolean,
    val icon: String?,
    val unicodeEmoji: String?,
    val position: Int,
    val permissions: Permissions,
    val managed: Boolean,
    val mentionable: Boolean,
) {
    companion object {
        fun from(role: DiscordRole): DeviousRoleData {
            return DeviousRoleData(
                role.id.toLightweightSnowflake(),
                role.name,
                role.color,
                role.hoist,
                role.icon.value,
                role.unicodeEmoji.value,
                role.position,
                role.permissions,
                role.managed,
                role.mentionable
            )
        }
    }
}