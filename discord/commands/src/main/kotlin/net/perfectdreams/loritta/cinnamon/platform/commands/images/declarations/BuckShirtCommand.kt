package net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.images.BuckShirtExecutor

class BuckShirtCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Buckshirt
    }

    override fun declaration() = slashCommand("buckshirt", CommandCategory.IMAGES, I18N_PREFIX.Description) {
        executor = { BuckShirtExecutor(it, it.gabrielaImageServerClient) }
    }
}