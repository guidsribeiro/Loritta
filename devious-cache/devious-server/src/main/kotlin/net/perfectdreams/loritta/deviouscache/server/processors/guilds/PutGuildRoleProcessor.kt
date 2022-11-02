package net.perfectdreams.loritta.deviouscache.server.processors.guilds

import mu.KotlinLogging
import net.perfectdreams.loritta.deviouscache.requests.PutGuildRoleRequest
import net.perfectdreams.loritta.deviouscache.responses.DeviousResponse
import net.perfectdreams.loritta.deviouscache.responses.OkResponse
import net.perfectdreams.loritta.deviouscache.server.DeviousCache
import net.perfectdreams.loritta.deviouscache.server.utils.GuildKey
import net.perfectdreams.loritta.deviouscache.server.utils.SnowflakeMap

class PutGuildRoleProcessor(val m: DeviousCache) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun process(request: PutGuildRoleRequest): DeviousResponse {
        m.withLock(GuildKey(request.guildId)) {
            m.awaitForEntityPersistenceModificationMutex()

            logger.info { "Updating guild role with ID ${request.role.id} on guild ${request.guildId}" }

            val currentRoles = m.roles[request.guildId]
            // Expected 1 because we will insert the new role
            m.roles[request.guildId] = (currentRoles ?: SnowflakeMap(1))
                .also {
                    it[request.role.id] = request.role
                }
                m.dirtyRoles.add(request.guildId)

            return OkResponse
        }
    }
}