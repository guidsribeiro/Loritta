package net.perfectdreams.loritta.morenitta.gateway

import net.dv8tion.jda.internal.JDAImpl
import net.perfectdreams.loritta.cinnamon.discord.gateway.LorittaDiscordGatewayManager
import net.perfectdreams.loritta.morenitta.utils.LorittaShards

class JDAToKordDiscordGatewayManager(val lorittaShards: LorittaShards) : LorittaDiscordGatewayManager(lorittaShards.loritta.config.loritta.discord.maxShards) {
    val proxiedKordGateways = mutableMapOf<Int, JDAProxiedKordGateway>()
    override val gateways: Map<Int, JDAProxiedKordGateway>
        get() = proxiedKordGateways

    override fun getGatewayForShardOrNull(shardId: Int) = gateways[shardId]
}