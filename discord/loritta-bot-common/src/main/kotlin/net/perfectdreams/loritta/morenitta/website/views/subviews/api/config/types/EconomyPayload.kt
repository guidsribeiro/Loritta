package net.perfectdreams.loritta.morenitta.website.views.subviews.api.config.types

import com.google.gson.JsonObject
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.morenitta.website.session.LorittaJsonWebSession

class EconomyPayload : ConfigPayloadType("economy") {
	override fun process(payload: JsonObject, userIdentification: LorittaJsonWebSession.UserIdentification, serverConfig: ServerConfig, guild: Guild) {
		// applyReflection(payload.economyConfig)
	}
}