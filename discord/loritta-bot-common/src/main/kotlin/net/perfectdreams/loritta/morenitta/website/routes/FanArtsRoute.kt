package net.perfectdreams.loritta.morenitta.website.routes

import io.ktor.server.application.*
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.utils.extensions.redirect

class FanArtsRoute(loritta: LorittaBot) : LocalizedRoute(loritta, "/fanarts") {
    override val isMainClusterOnlyRoute = true

    override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {
        redirect("https://fanarts.perfectdreams.net/", true)
    }
}