package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.roleplay

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.randomroleplaypictures.client.RandomRoleplayPicturesClient

class RoleplayHeadPatExecutor(
    loritta: LorittaBot,
    client: RandomRoleplayPicturesClient,
) : RoleplayPictureExecutor(
    loritta,
    client,
    RoleplayUtils.HEAD_PAT_ATTRIBUTES
)