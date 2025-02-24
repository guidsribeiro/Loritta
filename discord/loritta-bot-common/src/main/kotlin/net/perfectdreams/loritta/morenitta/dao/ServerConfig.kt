package net.perfectdreams.loritta.morenitta.dao

import com.github.benmanes.caffeine.cache.Caffeine
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.tables.DonationKeys
import net.perfectdreams.loritta.morenitta.tables.GuildProfiles
import net.perfectdreams.loritta.morenitta.tables.ServerConfigs
import net.perfectdreams.loritta.morenitta.utils.LorittaPermission
import net.perfectdreams.loritta.morenitta.utils.LorittaUser
import net.perfectdreams.loritta.morenitta.utils.extensions.getOrNull
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.AutoroleConfig
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.EconomyConfig
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.EventLogConfig
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.InviteBlockerConfig
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.LevelConfig
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.MiscellaneousConfig
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.ModerationConfig
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.StarboardConfig
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.WelcomerConfig
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.math.ceil
import kotlin.reflect.KMutableProperty1

class ServerConfig(id: EntityID<Long>) : Entity<Long>(id) {
	companion object : EntityClass<Long, ServerConfig>(ServerConfigs)

	val guildId = this.id.value
	var commandPrefix by ServerConfigs.commandPrefix
	var localeId by ServerConfigs.localeId
	var deleteMessageAfterCommand by ServerConfigs.deleteMessageAfterCommand
	var warnOnMissingPermission by ServerConfigs.warnOnMissingPermission
	var warnOnUnknownCommand by ServerConfigs.warnOnUnknownCommand
	var blacklistedChannels by ServerConfigs.blacklistedChannels
	var warnIfBlacklisted by ServerConfigs.warnIfBlacklisted
	var blacklistedWarning by ServerConfigs.blacklistedWarning
	var disabledCommands by ServerConfigs.disabledCommands
	// var donationKey by DonationKey optionalReferencedOn ServerConfigs.donationKey
	var donationConfig by DonationConfig optionalReferencedOn ServerConfigs.donationConfig
	var economyConfig by EconomyConfig optionalReferencedOn ServerConfigs.economyConfig
	var levelConfig by LevelConfig optionalReferencedOn ServerConfigs.levelConfig
	var starboardConfig by StarboardConfig optionalReferencedOn ServerConfigs.starboardConfig
	var miscellaneousConfig by MiscellaneousConfig optionalReferencedOn ServerConfigs.miscellaneousConfig
	var eventLogConfig by EventLogConfig optionalReferencedOn ServerConfigs.eventLogConfig
	var autoroleConfig by AutoroleConfig optionalReferencedOn ServerConfigs.autoroleConfig
	var inviteBlockerConfig by InviteBlockerConfig optionalReferencedOn ServerConfigs.inviteBlockerConfig
	var welcomerConfig by WelcomerConfig optionalReferencedOn ServerConfigs.welcomerConfig
	var moderationConfig by ModerationConfig optionalReferencedOn ServerConfigs.moderationConfig
	var migrationVersion by ServerConfigs.migrationVersion

	// Loading guild roles loritta permissions is very expensive, is called on *every* message and 99% of the times the permissions are *always* the same.
	// So it would be better to just cache them in memory and invalidate when needed, avoiding expensive calls to the database on every message.
	//
	// Of course, this means that cache *must* be invalidated when the permissions are updated! If not, the cache will have inconsistencies.
	private var guildRolesLorittaPermissions: Map<Long, EnumSet<LorittaPermission>>? = null
	private val guildRolesLorittaPermissionsMutex = Mutex()

	// We need to synchronize user profile calls to avoid creating multiple profiles being created due to Loritta processing multiple messages
	// by the same user at the same time as another message.
	//
	// For more information, check this issue: https://github.com/LorittaBot/Loritta/issues/2258
	private val creatingGuildUserProfileMutexes = Caffeine.newBuilder()
			.expireAfterAccess(1, TimeUnit.MINUTES)
			.build<Long, Mutex>()
			.asMap()

	/**
	 * Loads the guild role Loritta permissions from the cache or, if it is not present in the cache, loads from the database.
	 *
	 * This uses [LorittaUser.loadGuildRolesLorittaPermissions], but caches the result to a [guildRolesLorittaPermissions] variable.
	 *
	 * @param guild the guild object
	 * @return a map containing all the loritta permissions of the roles in [guild]
	 *
	 * @see LorittaUser.loadGuildRolesLorittaPermissions
	 */
	suspend fun getOrLoadGuildRolesLorittaPermissions(loritta: LorittaBot, guild: Guild): Map<Long, EnumSet<LorittaPermission>> {
		// Needs to be inside of a mutex to avoid synchronization issues (concurrent changes, etc)
		return guildRolesLorittaPermissionsMutex.withLock {
			guildRolesLorittaPermissions ?: run {
				// If we don't have the permissions cached, load it from the database and store in the guildRolesLorittaPermissions map
				val guildPermissions = LorittaUser.loadGuildRolesLorittaPermissions(loritta, this, guild)
				guildRolesLorittaPermissions = guildPermissions
				guildPermissions
			}
		}
	}

	suspend fun getActiveDonationKeys(loritta: LorittaBot) = loritta.newSuspendedTransaction {
		DonationKey.find { DonationKeys.activeIn eq this@ServerConfig.id and (DonationKeys.expiresAt greaterEq System.currentTimeMillis()) }
				.toList()
	}

	suspend fun getActiveDonationKeysValue(loritta: LorittaBot) = getActiveDonationKeys(loritta).sumOf {
		// This is a weird workaround that fixes users complaining that 19.99 + 19.99 != 40 (it equals to 39.38()
		ceil(it.value)
	}

	fun getActiveDonationKeysNested() = DonationKey.find { DonationKeys.activeIn eq this@ServerConfig.id and (DonationKeys.expiresAt greaterEq System.currentTimeMillis()) }
				.toList()

	fun getActiveDonationKeysValueNested() = getActiveDonationKeysNested().sumByDouble { it.value }

	suspend fun getUserData(loritta: LorittaBot, id: Long, isInGuild: Boolean = true): GuildProfile {
		val t = this
		val mutex = creatingGuildUserProfileMutexes.getOrPut(id) { Mutex() }

		return getUserDataIfExistsAsync(loritta, id) ?: mutex.withLock {
			loritta.newSuspendedTransaction {
				GuildProfile.new {
					this.guildId = t.guildId
					this.userId = id
					this.money = BigDecimal(0)
					this.quickPunishment = false
					this.xp = 0
					this.isInGuild = isInGuild
				}
			}
		}
	}

	suspend fun getUserDataIfExistsAsync(loritta: LorittaBot, id: Long): GuildProfile? {
		return loritta.newSuspendedTransaction {
			GuildProfile.find { (GuildProfiles.guildId eq guildId) and (GuildProfiles.userId eq id) }.firstOrNull()
		}
	}

	fun getUserDataIfExistsNested(id: Long) = GuildProfile.find { (GuildProfiles.guildId eq guildId) and (GuildProfiles.userId eq id) }.firstOrNull()

	private val cachedData = ConcurrentHashMap<KMutableProperty1<ServerConfig, *>, Optional<Any>>()

	/**
	 * Gets or retrieves from the database the object you've requested
	 */
	suspend fun <T> getCachedOrRetreiveFromDatabase(loritta: LorittaBot, property: KMutableProperty1<ServerConfig, *>): T {
		if (!cachedData.containsKey(property)) {
			val databaseObject = loritta.pudding.transaction {
				property.call(this@ServerConfig)
			}
			cachedData[property] = Optional.ofNullable(databaseObject)
			return databaseObject as T
		}
		return cachedData[property]?.getOrNull() as T
	}

	/**
	 * Gets or retrieves from the database the object you've requested
	 */
	suspend fun <T> getCachedOrRetreiveFromDatabaseAsync(loritta: LorittaBot, property: KMutableProperty1<ServerConfig, *>): T {
		if (!cachedData.containsKey(property)) {
			val databaseObject = loritta.newSuspendedTransaction {
				property.call(this@ServerConfig)
			}
			cachedData[property] = Optional.ofNullable(databaseObject)
			return databaseObject as T
		}
		return cachedData[property]?.getOrNull() as T
	}

	/**
	 * Gets or retrieves from the database the object you've requested
	 */
	suspend fun <T> getCachedOrRetreiveFromDatabaseDeferred(loritta: LorittaBot, property: KMutableProperty1<ServerConfig, *>): Deferred<T> {
		if (!cachedData.containsKey(property)) {
			val job = loritta.suspendedTransactionAsync {
				val result = property.call(this@ServerConfig)

				cachedData[property] = Optional.ofNullable(result)

				return@suspendedTransactionAsync result
			}
			return job as Deferred<T>
		}
		return GlobalScope.async(loritta.coroutineDispatcher) { cachedData[property]?.getOrNull() as T }
	}
}