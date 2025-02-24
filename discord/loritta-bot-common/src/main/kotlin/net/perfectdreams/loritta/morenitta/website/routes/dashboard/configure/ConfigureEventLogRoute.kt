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

class ConfigureEventLogRoute(loritta: LorittaBot) : RequiresGuildAuthLocalizedRoute(loritta, "/configure/event-log") {
	override suspend fun onGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig) {
		loritta as LorittaBot

		val eventLogConfig = loritta.newSuspendedTransaction {
			serverConfig.eventLogConfig
		}

		val variables = call.legacyVariables(loritta, locale)

		variables["saveType"] = "event_log"
		variables["serverConfig"] = FakeServerConfig(
				FakeServerConfig.FakeEventLogConfig(
						eventLogConfig?.enabled ?: false,
						eventLogConfig?.eventLogChannelId?.toString(),
						eventLogConfig?.memberBanned ?: false,
						eventLogConfig?.memberUnbanned ?: false,
						eventLogConfig?.messageEdited ?: false,
						eventLogConfig?.messageDeleted ?: false,
						eventLogConfig?.nicknameChanges ?: false,
						eventLogConfig?.avatarChanges ?: false,
						eventLogConfig?.voiceChannelJoins ?: false,
						eventLogConfig?.voiceChannelLeaves ?: false
				)
		)

		call.respondHtml(evaluate("event_log.html", variables))
	}

	/**
	 * Fake Server Config for Pebble, in the future this will be removed
	 */
	private class FakeServerConfig(val eventLogConfig: FakeEventLogConfig) {
		class FakeEventLogConfig(
				val isEnabled: Boolean,
				val eventLogChannelId: String?,
				val memberBanned: Boolean,
				val memberUnbanned: Boolean,
				val messageEdit: Boolean,
				val messageDeleted: Boolean,
				val nicknameChanges: Boolean,
				val avatarChanges: Boolean,
				val voiceChannelJoins: Boolean,
				val voiceChannelLeaves: Boolean
		)
	}
}