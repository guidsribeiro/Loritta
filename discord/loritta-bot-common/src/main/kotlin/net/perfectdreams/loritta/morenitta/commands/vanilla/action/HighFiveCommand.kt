package net.perfectdreams.loritta.morenitta.commands.vanilla.action

import net.perfectdreams.loritta.morenitta.LorittaBot
import java.awt.Color

class HighFiveCommand(loritta: LorittaBot): ActionCommand(loritta, listOf("highfive", "hifive", "tocaaqui")) {
    override fun create(): ActionCommandDSL = action {
        emoji = "\uD83D\uDD90"
        color = Color(27, 224, 96)

        response { locale, sender, target ->
            locale["commands.command.highfive.response", sender.asMention, target.asMention]
        }
    }
}