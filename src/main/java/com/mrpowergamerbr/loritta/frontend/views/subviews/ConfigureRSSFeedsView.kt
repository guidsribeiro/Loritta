package com.mrpowergamerbr.loritta.frontend.views.subviews

import com.github.salomonbrys.kotson.set
import com.google.gson.JsonArray
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.frontend.evaluate
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.oauth2.TemmieDiscordAuth
import net.dv8tion.jda.core.entities.Guild
import org.jooby.Request
import org.jooby.Response
import kotlin.collections.set

class ConfigureRSSFeedsView : ConfigureView() {
	override fun handleRender(req: Request, res: Response, variables: MutableMap<String, Any?>): Boolean {
		super.handleRender(req, res, variables)
		return req.path().matches(Regex("^/dashboard/configure/[0-9]+/feeds"))
	}

	override fun renderConfiguration(req: Request, res: Response, variables: MutableMap<String, Any?>, discordAuth: TemmieDiscordAuth, guild: Guild, serverConfig: ServerConfig): String {
		variables["saveType"] = "feeds"

		val feeds = JsonArray()
		serverConfig.rssFeedConfig.feeds.forEach {
			val json = Loritta.gson.toJsonTree(it)
			json["textChannelName"] = guild.getTextChannelById(it.repostToChannelId).name
			feeds.add(json)
		}

		variables["feeds"] = feeds.toString()

		return evaluate("configure_rssfeeds.html", variables)
	}
}