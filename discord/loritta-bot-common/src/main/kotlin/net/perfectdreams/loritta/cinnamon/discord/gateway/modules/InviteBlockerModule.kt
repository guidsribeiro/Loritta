package net.perfectdreams.loritta.cinnamon.discord.gateway.modules

import com.github.benmanes.caffeine.cache.Caffeine
import dev.kord.common.entity.*
import dev.kord.common.entity.optional.Optional
import dev.kord.core.cache.data.MemberData
import dev.kord.core.cache.data.UserData
import dev.kord.gateway.*
import dev.kord.rest.builder.message.create.actionRow
import dev.kord.rest.builder.message.create.allowedMentions
import dev.kord.rest.request.KtorRequestException
import io.ktor.http.*
import mu.KotlinLogging
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.utils.LorittaPermission
import net.perfectdreams.loritta.common.utils.text.TextUtils.stripCodeBackticks
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.discord.gateway.GatewayEventContext
import net.perfectdreams.loritta.cinnamon.discord.utils.metrics.DiscordGatewayEventsProcessorMetrics
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.interactiveButton
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.loriEmoji
import net.perfectdreams.loritta.cinnamon.discord.interactions.inviteblocker.ActivateInviteBlockerBypassButtonClickExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.inviteblocker.ActivateInviteBlockerData
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordInviteUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.MessageUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.hasLorittaPermission
import net.perfectdreams.loritta.cinnamon.discord.utils.sources.UserTokenSource
import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher
import java.util.regex.Pattern

class InviteBlockerModule(val m: LorittaBot) : ProcessDiscordEventsModule() {
    companion object {
        private val URL_PATTERN = Pattern.compile("[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[A-z]{2,7}\\b([-a-zA-Z0-9@:%_+.~#?&/=]*)")
        private val logger = KotlinLogging.logger {}
    }

    private val cachedInviteLinks = Caffeine.newBuilder().expireAfterAccess(30L, TimeUnit.MINUTES)
        .build<Snowflake, Set<String>>()
        .asMap()

    override suspend fun processEvent(context: GatewayEventContext): ModuleResult {
        when (val event = context.event) {
            is MessageCreate -> {
                val author = event.message.author
                val guildId = event.message.guildId.value ?: return ModuleResult.Continue // Not in a guild
                val member = event.message.member.value ?: return ModuleResult.Continue // The member isn't in the guild
                val channelId = event.message.channelId

                return handleMessage(
                    guildId,
                    channelId,
                    event.message.id,
                    author,
                    member,
                    event.message.content,
                    event.message.embeds
                )
            }
            is MessageUpdate -> {
                val author = event.message.author.value ?: return ModuleResult.Continue // Where's the user?
                val guildId = event.message.guildId.value ?: return ModuleResult.Continue // Not in a guild
                val member = event.message.member.value ?: return ModuleResult.Continue // The member isn't in the guild
                val channelId = event.message.channelId
                val content = event.message.content.value ?: return ModuleResult.Continue // Where's the message content?
                val embeds = event.message.embeds.value ?: return ModuleResult.Continue // Where's the embeds? (This is an empty list even if the message doesn't have any embeds)

                return handleMessage(
                    guildId,
                    channelId,
                    event.message.id,
                    author,
                    member,
                    content,
                    embeds
                )
            }
            // Delete invite list from cache when a server invite is created or deleted
            is InviteCreate -> event.invite.guildId.value?.let { cachedInviteLinks.remove(it) }
            is InviteDelete -> event.invite.guildId.value?.let { cachedInviteLinks.remove(it) }
            else -> {}
        }
        return ModuleResult.Continue
    }

    private suspend fun handleMessage(
        guildId: Snowflake,
        channelId: Snowflake,
        messageId: Snowflake,
        author: DiscordUser,
        member: DiscordGuildMember,
        content: String,
        embeds: List<DiscordEmbed>
    ): ModuleResult {
        // Ignore messages sent by bots
        if (author.bot.discordBoolean)
            return ModuleResult.Continue

        val strippedContent = content
            // We need to strip the code marks to avoid this:
            // https://cdn.discordapp.com/attachments/513405772911345664/760887806191992893/invite-bug.png
            .stripCodeBackticks()
            .replace("\u200B", "")
            // https://discord.gg\loritta is actually detected as https://discord.gg/loritta on Discord
            // So we are going to flip all \ to /
            .replace("\\", "/")
            // https://discord.gg//loritta is actually detected as https://discord.gg/loritta on Discord
            // (yes, two issues, wow)
            // So we are going to replace all /+ to /, so https://discord.gg//loritta becomes https://discord.gg/loritta
            .replace(Regex("/+"), "/")

        val validMatchers = mutableListOf<Matcher>()
        val contentMatcher = getMatcherIfHasInviteLink(strippedContent)
        if (contentMatcher != null)
            validMatchers.add(contentMatcher)

        if (!isYouTubeLink(strippedContent)) {
            for (embed in embeds) {
                val descriptionMatcher = getMatcherIfHasInviteLink(embed.description)
                if (descriptionMatcher != null)
                    validMatchers.add(descriptionMatcher)

                val titleMatcher = getMatcherIfHasInviteLink(embed.title)
                if (titleMatcher != null)
                    validMatchers.add(titleMatcher)

                val urlMatcher = getMatcherIfHasInviteLink(embed.url)
                if (urlMatcher != null)
                    validMatchers.add(urlMatcher)

                val footerMatcher = getMatcherIfHasInviteLink(embed.footer.value?.text)
                if (footerMatcher != null)
                    validMatchers.add(footerMatcher)

                val authorNameMatcher = getMatcherIfHasInviteLink(embed.author.value?.name)
                if (authorNameMatcher != null)
                    validMatchers.add(authorNameMatcher)

                val authorUrlMatcher = getMatcherIfHasInviteLink(embed.author.value?.url)
                if (authorUrlMatcher != null)
                    validMatchers.add(authorUrlMatcher)

                val fields = embed.fields.value
                if (fields != null) {
                    for (field in fields) {
                        val fieldMatcher = getMatcherIfHasInviteLink(field.value)
                        if (fieldMatcher != null)
                            validMatchers.add(fieldMatcher)
                    }
                }
            }
        }

        // There isn't any matched links in the message!
        if (validMatchers.isEmpty())
            return ModuleResult.Continue

        // We will only get the configuration and stuff after checking if we should act on the message
        val serverConfig = m.pudding.serverConfigs.getServerConfigRoot(guildId.value)
        val inviteBlockerConfig = serverConfig
            ?.getInviteBlockerConfig()
            ?: return ModuleResult.Continue
        if (inviteBlockerConfig.whitelistedChannels.contains(channelId.toLong()))
            return ModuleResult.Continue

        val i18nContext = m.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)

        // Can the user bypass the invite blocker check?
        val canBypass = m.pudding.serverConfigs.hasLorittaPermission(guildId, member, LorittaPermission.ALLOW_INVITES)
        if (canBypass)
            return ModuleResult.Continue

        // Para evitar que use a API do Discord para pegar os invites do servidor toda hora, nós iremos *apenas* pegar caso seja realmente
        // necessário, e, ao pegar, vamos guardar no cache de invites
        val lorittaPermissions = m.cache.getLazyCachedLorittaPermissions(guildId, channelId)

        val allowedInviteCodes = mutableSetOf<String>()
        if (inviteBlockerConfig.whitelistServerInvites) {
            val cachedGuildInviteLinks = cachedInviteLinks[guildId]
            if (cachedGuildInviteLinks == null) {
                val guildInviteLinks = mutableSetOf<String>()

                if (lorittaPermissions.hasPermission(Permission.ManageGuild)) {
                    try {
                        logger.info { "Querying guild $guildId's vanity invite..." }
                        val vanityInvite = m.rest.guild.getVanityInvite(guildId)
                        val vanityInviteCode = vanityInvite.code
                        if (vanityInviteCode != null)
                            guildInviteLinks.add(vanityInviteCode)
                        else
                            logger.info { "Guild $guildId has the vanity invite feature, but they haven't set the invite code!" }
                    } catch (e: KtorRequestException) {
                        // Forbidden = The server does not have the feature enabled
                        if (e.httpResponse.status != HttpStatusCode.Forbidden)
                            throw e
                        else
                            logger.info { "Guild $guildId does not have the vanity invite feature..." }
                    }

                    logger.info { "Querying guild $guildId normal invites..." }
                    val invites = m.rest.guild.getGuildInvites(guildId) // This endpoint does not return the vanity invite
                    val codes = invites.map { it.code }
                    guildInviteLinks.addAll(codes)
                } else {
                    logger.warn { "Not querying guild $guildId invites because I don't have permission to manage the guild there!" }
                }

                allowedInviteCodes.addAll(guildInviteLinks)
            } else {
                allowedInviteCodes.addAll(cachedGuildInviteLinks)
            }

            cachedInviteLinks[guildId] = allowedInviteCodes
        }

        logger.info { "Allowed invite codes in guild $guildId: $allowedInviteCodes" }

        for (matcher in validMatchers) {
            val urls = mutableSetOf<String>()
            while (matcher.find()) {
                var url = matcher.group()
                if (url.startsWith("discord.gg", true)) {
                    url = "discord.gg" + matcher.group(1).replace(".", "")
                }
                urls.add(url)
            }

            val inviteCodes = urls.mapNotNull { DiscordInviteUtils.getInviteCodeFromUrl(it) }
            val disallowedInviteCodes = inviteCodes.filter { it !in allowedInviteCodes }

            if (disallowedInviteCodes.isNotEmpty()) {
                logger.info { "Invite Blocker triggered in guild $guildId! Invite Codes: $disallowedInviteCodes" }

                DiscordGatewayEventsProcessorMetrics.invitesBlocked
                    .labels(guildId.toString())
                    .inc()

                if (inviteBlockerConfig.deleteMessage && lorittaPermissions.hasPermission(Permission.ManageMessages)) {
                    try {
                        // Discord does not log messages deleted by bots, so providing an audit log reason is pointless
                        m.rest.channel.deleteMessage(
                            channelId,
                            messageId
                        )
                    } catch (e: KtorRequestException) {
                        // Maybe the message was deleted by another bot?
                        if (e.httpResponse.status != HttpStatusCode.NotFound)
                            throw e
                        else
                            logger.warn { "I tried deleting the message ${messageId} on $guildId, but looks like another bot deleted it... Let's just ignore it and pretend it was successfully deleted" }
                    }
                }

                val warnMessage = inviteBlockerConfig.warnMessage

                if (inviteBlockerConfig.tellUser && !warnMessage.isNullOrEmpty()) {
                    if (lorittaPermissions.canTalk()) {
                        logger.info { "Sending blocked invite message in $channelId on $guildId..." }

                        val userData = UserData.from(author)
                        val memberData = MemberData.from(userData.id, guildId, member)

                        val toBeSent = MessageUtils.createMessage(
                            m,
                            guildId,
                            warnMessage,
                            listOf(
                                UserTokenSource(m.kord, userData, memberData)
                            ),
                            emptyMap()
                        )

                        val sentMessage = m.rest.channel.createMessage(channelId) {
                            toBeSent.apply(this)
                        }

                        if (m.cache.hasPermission(guildId, channelId, author.id, Permission.ManageGuild)) {
                            // If the user has permission to enable the invite bypass permission, make Loritta recommend to enable the permission
                            val topRole = m.cache.getRoles(guildId, member)
                                .asSequence()
                                .sortedByDescending { it.position }
                                .filter { !it.isManaged }
                                .filter { it.idLong != guildId.value.toLong() } // If it is the role ID == guild ID, then it is the @everyone role!
                                .firstOrNull()

                            if (topRole != null) {
                                // A role has been found! Tell the user about enabling the invite blocker bypass
                                m.rest.channel.createMessage(channelId) {
                                    this.failIfNotExists = false
                                    this.messageReference = sentMessage.id

                                    styled(
                                        i18nContext.get(I18nKeysData.Modules.InviteBlocker.ActivateInviteBlockerBypass("<@&${topRole.id}>")),
                                        Emotes.LoriSmile
                                    )

                                    styled(
                                        i18nContext.get(I18nKeysData.Modules.InviteBlocker.HowToReEnableLater("<${m.config.loritta.website.url}guild/${author.id}/configure/permissions>")),
                                        Emotes.LoriHi
                                    )

                                    actionRow {
                                        interactiveButton(
                                            ButtonStyle.Primary,
                                            i18nContext.get(I18nKeysData.Modules.InviteBlocker.AllowSendingInvites),
                                            ActivateInviteBlockerBypassButtonClickExecutor,
                                            m.encodeDataForComponentOrStoreInDatabase(
                                                ActivateInviteBlockerData(
                                                    author.id,
                                                    Snowflake(topRole.id)
                                                )
                                            )
                                        ) {
                                            loriEmoji = Emotes.LoriPat
                                        }
                                    }

                                    // Empty allowed mentions because we don't want to mention the role
                                    allowedMentions {}
                                }
                            }
                        }
                    } else {
                        logger.warn { "I wanted to tell about the blocked invite in $channelId on $guildId, but I can't talk there!" }
                    }
                }

                // Invite has been found, exit!
                return ModuleResult.Cancel
            } else {
                logger.info { "Invite Blocker triggered in guild $guildId, but we will ignore it because it is on the invite code allowed list... Invite Codes: $inviteCodes" }
                continue
            }
        }

        return ModuleResult.Continue
    }

    /**
     * Checks if [content] contains a YouTube Link
     *
     * @param content the content
     * @return if the link contains a YouTube URL
     */
    private fun isYouTubeLink(content: String?): Boolean {
        if (content.isNullOrBlank())
            return false

        val matcher = URL_PATTERN.matcher(content)
        return if (matcher.find()) {
            val everything = matcher.group(0)
            val afterSlash = matcher.group(1)
            val uri = everything.replace(afterSlash, "")
            uri.endsWith("youtube.com") || uri.endsWith("youtu.be")
        } else {
            false
        }
    }

    private fun getMatcherIfHasInviteLink(optionalString: Optional<String>?) = optionalString?.let {
        getMatcherIfHasInviteLink(it.value)
    }

    private fun getMatcherIfHasInviteLink(content: String?): Matcher? {
        if (content.isNullOrBlank())
            return null

        val matcher = URL_PATTERN.matcher(content)
        return if (matcher.find()) {
            matcher.reset()
            matcher
        } else {
            null
        }
    }
}