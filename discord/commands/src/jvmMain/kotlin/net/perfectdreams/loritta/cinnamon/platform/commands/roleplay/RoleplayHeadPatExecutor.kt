package net.perfectdreams.loritta.cinnamon.platform.commands.roleplay

import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.retribute.RetributeHeadPatButtonExecutor
import net.perfectdreams.randomroleplaypictures.client.RandomRoleplayPicturesClient

class RoleplayHeadPatExecutor(
    client: RandomRoleplayPicturesClient,
) : RoleplayPictureExecutor(
    client,
    { gender1, gender2 ->
        headPat(gender1, gender2)
    },
    RetributeHeadPatButtonExecutor.Companion
) {
    companion object : SlashCommandExecutorDeclaration(RoleplayHeadPatExecutor::class) {
        override val options = RoleplayPictureExecutor.Companion.Options
    }
}