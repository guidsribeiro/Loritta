package net.perfectdreams.loritta.cinnamon.discord.interactions.commands

import dev.kord.rest.builder.message.create.MessageCreateBuilder
import kotlinx.serialization.json.JsonNull.content
import net.perfectdreams.discordinteraktions.common.builder.message.MessageBuilder
import net.perfectdreams.loritta.common.emotes.Emote
import net.perfectdreams.loritta.cinnamon.emotes.Emotes

/**
 * Appends a Loritta-styled formatted message to the builder's message content.
 *
 * By default, Loritta-styled formatting looks like this: `[prefix] **|** [content]`.
 *
 * If there's already content present in the builder, a new line will be inserted before the styled replied!
 *
 * @param content the content of the styled message
 * @param prefix  the emote prefix of the styled message
 */
fun MessageBuilder.styled(content: String, prefix: Emote) = styled(content, prefix.asMention)

/**
 * Appends a Loritta-styled formatted message to the builder's message content.
 *
 * By default, Loritta-styled formatting looks like this: `[prefix] **|** [content]`.
 *
 * If there's already content present in the builder, a new line will be inserted before the styled replied!
 *
 * @param content the content of the styled message
 * @param prefix  the emote prefix of the styled message
 */
fun MessageBuilder.styled(content: String, prefix: String = Emotes.DefaultStyledPrefix.asMention) = styled(
    net.perfectdreams.loritta.cinnamon.entities.LorittaReply(content, prefix)
)

/**
 * Appends a Loritta-styled formatted message to the builder's message content.
 *
 * By default, Loritta-styled formatting looks like this: `[prefix] **|** [content]`.
 *
 * If there's already content present in the builder, a new line will be inserted before the styled replied!
 *
 * @param reply the already built LorittaReply
 */
fun MessageBuilder.styled(reply: net.perfectdreams.loritta.cinnamon.entities.LorittaReply) {
    val styled = createStyledContent(reply)

    if (content != null) {
        content += "\n"
        content += styled
    } else {
        content = styled
    }
}

/**
 * Appends a Loritta-styled formatted message to the builder's message content.
 *
 * By default, Loritta-styled formatting looks like this: `[prefix] **|** [content]`.
 *
 * If there's already content present in the builder, a new line will be inserted before the styled replied!
 *
 * @param content the content of the styled message
 * @param prefix  the emote prefix of the styled message
 */
fun MessageCreateBuilder.styled(content: String, prefix: Emote) = styled(content, prefix.asMention)

/**
 * Appends a Loritta-styled formatted message to the builder's message content.
 *
 * By default, Loritta-styled formatting looks like this: `[prefix] **|** [content]`.
 *
 * If there's already content present in the builder, a new line will be inserted before the styled replied!
 *
 * @param content the content of the styled message
 * @param prefix  the emote prefix of the styled message
 */
fun MessageCreateBuilder.styled(content: String, prefix: String = Emotes.DefaultStyledPrefix.asMention) = styled(
    net.perfectdreams.loritta.cinnamon.entities.LorittaReply(content, prefix)
)

/**
 * Appends a Loritta-styled formatted message to the builder's message content.
 *
 * By default, Loritta-styled formatting looks like this: `[prefix] **|** [content]`.
 *
 * If there's already content present in the builder, a new line will be inserted before the styled replied!
 *
 * @param reply the already built LorittaReply
 */
fun MessageCreateBuilder.styled(reply: net.perfectdreams.loritta.cinnamon.entities.LorittaReply) {
    val styled = createStyledContent(reply)

    if (content != null) {
        content += "\n"
        content += styled
    } else {
        content = styled
    }
}

/**
 * Creates a Loritta-styled formatted content.
 *
 * By default, Loritta-styled formatting looks like this: `[prefix] **|** [content]`.
 *
 * @param content the content of the styled message
 * @param prefix  the emote prefix of the styled message
 */
fun createStyledContent(content: String, prefix: Emote) = createStyledContent(
    net.perfectdreams.loritta.cinnamon.entities.LorittaReply(content, prefix.asMention)
)

/**
 * Creates a Loritta-styled formatted content.
 *
 * By default, Loritta-styled formatting looks like this: `[prefix] **|** [content]`.
 *
 * @param content the content of the styled message
 * @param prefix  the emote prefix of the styled message
 */
fun createStyledContent(content: String, prefix: String = Emotes.DefaultStyledPrefix.asMention) = createStyledContent(
    net.perfectdreams.loritta.cinnamon.entities.LorittaReply(content, prefix)
)

/**
 * Creates a Loritta-styled formatted content.
 *
 * By default, Loritta-styled formatting looks like this: `[prefix] **|** [content]`.
 *
 * @param reply the already built LorittaReply
 */
fun createStyledContent(reply: net.perfectdreams.loritta.cinnamon.entities.LorittaReply) = "${reply.prefix} **|** ${reply.content}"