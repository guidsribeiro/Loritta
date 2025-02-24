package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure

import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.website.evaluate
import io.ktor.server.application.ApplicationCall
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.RequiresGuildAuthLocalizedRoute
import net.perfectdreams.loritta.morenitta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.morenitta.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import kotlin.collections.set

class ConfigureStarboardRoute(loritta: LorittaBot) : RequiresGuildAuthLocalizedRoute(loritta, "/configure/starboard") {
	override suspend fun onGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig) {
		loritta as LorittaBot

		val starboardConfig = loritta.newSuspendedTransaction {
			serverConfig.starboardConfig
		}

		val variables = call.legacyVariables(loritta, locale)

		variables["saveType"] = "starboard"
		variables["serverConfig"] = FakeServerConfig(
				FakeServerConfig.FakeStarboardConfig(
						starboardConfig?.enabled ?: false,
						starboardConfig?.starboardChannelId?.toString(),
						starboardConfig?.requiredStars ?: 1
				)
		)

		call.respondHtml(evaluate("starboard.html", variables))
	}

	/**
	 * Fake Server Config for Pebble, in the future this will be removed
	 */
	private class FakeServerConfig(val starboardConfig: FakeStarboardConfig) {
		class FakeStarboardConfig(
				val isEnabled: Boolean,
				val starboardId: String?,
				val requiredStars: Int
		)
	}
}