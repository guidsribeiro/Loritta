package net.perfectdreams.loritta.deviousfun.entities

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import kotlinx.datetime.toJavaInstant
import net.perfectdreams.loritta.deviouscache.data.DeviousMemberData
import net.perfectdreams.loritta.deviouscache.data.toKordSnowflake
import net.perfectdreams.loritta.deviousfun.DeviousShard
import net.perfectdreams.loritta.deviousfun.PermissionsWrapper
import net.perfectdreams.loritta.deviousfun.utils.PermissionInteractionUtils
import java.time.OffsetDateTime
import java.time.ZoneOffset

class Member(val deviousShard: DeviousShard, val member: DeviousMemberData, val guild: Guild, val user: User) : Mentionable,
    IdentifiableSnowflake {
    override val idSnowflake: Snowflake
        get() = user.idSnowflake
    val nickname: String?
        get() = member.nick
    val isOwner: Boolean
        get() = guild.ownerIdSnowflake == user.idSnowflake
    val effectiveName: String
        get() = nickname ?: user.name
    val roleIds by member::roles
    val roles: List<Role>
        get() = guild.roles.filter {
            it.idSnowflake in member.roles.map { it.toKordSnowflake() }
        }
    val timeBoosted: OffsetDateTime?
        get() = member.premiumSince?.toJavaInstant()?.atOffset(ZoneOffset.UTC)
    val timeJoined: OffsetDateTime
        get() = member.joinedAt.toJavaInstant().atOffset(ZoneOffset.UTC)
    override val asMention: String
        get() = "<@${idSnowflake}>"

    suspend fun hasPermission(vararg permissions: Permission): Boolean {
        // Yes.
        // *refuses to elaborate*
        if (guild.ownerIdSnowflake == idSnowflake)
            return true

        return getPermissions().hasPermission(*permissions)
    }

    suspend fun hasPermission(channel: Channel, vararg permissions: Permission): Boolean {
        // Yes.
        // *refuses to elaborate*
        if (guild.ownerIdSnowflake == idSnowflake)
            return true

        return getPermissions(channel).hasPermission(*permissions)
    }

    suspend fun hasPermission(channel: Channel, permissions: List<Permission>) =
        hasPermission(channel, *permissions.toTypedArray())

    suspend fun getPermissions(): PermissionsWrapper {
        return PermissionsWrapper(deviousShard.loritta.cache.getPermissions(guild.idSnowflake, user.idSnowflake).permissions)
    }

    suspend fun getPermissions(channel: Channel): PermissionsWrapper {
        return PermissionsWrapper(deviousShard.loritta.cache.getPermissions(guild.idSnowflake, channel.idSnowflake, user.idSnowflake).also { println(it) } .permissions)
    }

    suspend fun canInteract(member: Member): Boolean {
        return PermissionInteractionUtils.canInteract(guild, listOf(this.user), listOf(member.user))
            .all { it.value.all { it.result == PermissionInteractionUtils.InteractionCheckResult.SUCCESS } }
    }

    fun canInteract(role: Role): Boolean {
        return this.roles.maxOf { it.positionRaw } > role.positionRaw
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Member)
            return false

        return this.guild.idSnowflake == other.guild.idSnowflake && this.idSnowflake == other.idSnowflake
    }

    override fun hashCode() = this.idSnowflake.hashCode()
}