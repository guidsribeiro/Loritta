package net.perfectdreams.loritta.morenitta.website.routes.api.v1.callbacks

import io.ktor.server.application.*
import mu.KotlinLogging
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.utils.extensions.hostFromHeader
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.sequins.ktor.BaseRoute
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

class CreateWebhookRoute(val loritta: LorittaBot) : BaseRoute("/api/v1/callbacks/discord-webhook") {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override suspend fun onRequest(call: ApplicationCall) {
		val hostHeader = call.request.hostFromHeader()
		val code = call.parameters["code"]

		val auth = TemmieDiscordAuth(
			loritta.config.loritta.discord.applicationId.toString(),
			loritta.config.loritta.discord.clientSecret,
			code,
			"https://$hostHeader/api/v1/callbacks/discord-webhook",
			listOf("webhook.incoming")
		)

		val authExchangePayload = auth.doTokenExchange()
		call.respondJson(authExchangePayload["webhook"])
	}
}