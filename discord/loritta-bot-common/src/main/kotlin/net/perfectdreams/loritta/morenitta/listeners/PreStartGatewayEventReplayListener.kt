package net.perfectdreams.loritta.morenitta.listeners

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.*
import mu.KotlinLogging
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.PreProcessedRawGatewayEvent
import net.dv8tion.jda.api.events.StatusChangeEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.internal.JDAImpl
import net.dv8tion.jda.internal.entities.SelfUserImpl
import net.dv8tion.jda.internal.requests.WebSocketCode
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.devious.GatewaySessionData
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.LinkedBlockingQueue
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime


/**
 * Replays gateway events when the session is resumed, this should only be triggered on the first resume event received!
 */
class PreStartGatewayEventReplayListener(private val loritta: LorittaBot, private val initialSession: GatewaySessionData?, private val cacheFolder: File, private val state: MutableStateFlow<ProcessorState>) : ListenerAdapter() {
    companion object {
        private const val FAKE_EVENT_FIELD = "fakeout"
        private val logger = KotlinLogging.logger {}
    }

    private val replayCache = LinkedBlockingQueue<DataObject>()

    @OptIn(ExperimentalTime::class)
    override fun onPreProcessedRawGateway(event: PreProcessedRawGatewayEvent) {
        if (event.`package`.getBoolean(FAKE_EVENT_FIELD)) {
            // Events that has the "fakeout" field shouldn't be processed by this listener
            return
        }

        if (state.value == ProcessorState.FINISHED)
            return

        if (state.value == ProcessorState.WAITING_FOR_RESUME) {
            // This is the first boot of this JDA instance that we sent a faked ready event
            // What we need to do about it:
            // * Have we successfully resumed?
            // * If we have successfully resumed, we need to create an event replay cache that should be replayed after we loaded all the cached data
            when (event.`package`.getInt("op")) {
                // Only cancel dispatch events, we don't want the gateway connection to timeout due to not sending heartbeats
                WebSocketCode.DISPATCH -> {
                    if (event.type == "RESUMED") {
                        logger.info("Successfully resumed the gateway connection of shard ${event.jda.shardInfo.shardId}! Loading cached data...")

                        // No need to send the resumed event to JDA because we have sent our own faked READY event
                        event.isCancelled = true

                        val jdaImpl = event.jda as JDAImpl

                        // Indicate on our presence that we are loading the cached data
                        jdaImpl.presence.setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.playing(loritta.createActivityText("\uD83C\uDF6E Loritta is loading... Hang tight!", jdaImpl.shardInfo.shardId)))

                        // Yoru's Fakeout: Discord Edition
                        // (I may or may have not been playing too much VALORANT)
                        // handleEvent is actually protected, which is why we need to use DeviousJDA for this!
                        // We run the events this way to make it easier for us, since JDA will handle the event correctly after relaying it to JDA (yay!)
                        //
                        // Keep in mind that this will block the reading thread, so you need to be FAST to avoid the gateway connection being invalidated!
                        // Technically you need to load your data in less than ~41.5s (which is the current "heartbeat_interval"), but you can take longer
                        // without the connection dropping out (I tested with 60s, and it also worked, but ymmv)
                        // You could be fancier and make the cache stuff happen on a separate thread while keeping heartbeats through,
                        // but that makes the code harder and confusing.
                        val time = measureTime {
                            jdaImpl.guildsView.writeLock().use {
                                File(cacheFolder, "${jdaImpl.shardInfo.shardId}/guilds.json").forEachLine {
                                    // Fill the cache out
                                    jdaImpl.client.handleEvent(
                                        DataObject.fromJson(
                                            """{"op":0,"d":$it,"t":"GUILD_CREATE","$FAKE_EVENT_FIELD":true}"""
                                        )
                                    )
                                }
                            }
                        }

                        // Now replay the events!
                        logger.info { "Successfully sent faked guild create events for shard ${event.jda.shardInfo.shardId}! Took $time" }
                        logger.info { "Replaying ${replayCache.size} events for shard ${event.jda.shardInfo.shardId}.." }
                        while (replayCache.isNotEmpty()) {
                            val cachedEvent = replayCache.poll()

                            // Remove sequence from our cached event because we have already manually updated the sequence before
                            val cachedEventWithoutSequence = cachedEvent.remove("s")
                            // Put our fakeout field to avoid triggering our listener when replaying the event, which causes an infinite loop (whoops)
                            cachedEventWithoutSequence.put(FAKE_EVENT_FIELD, true)

                            (event.jda as JDAImpl).client.handleEvent(cachedEventWithoutSequence)
                        }
                        state.value = ProcessorState.FINISHED
                        logger.info("Successfully replayed events for shard ${event.jda.shardInfo.shardId}!")
                        jdaImpl.presence.setPresence(OnlineStatus.ONLINE, Activity.playing(loritta.createActivityText(loritta.config.loritta.discord.activity.name, event.jda.shardInfo.shardId)))
                        return
                    }

                    if (event.`package`.hasKey("s")) {
                        // Manually update the sequence since using "isCancelled" stops updating the response total, and the current sequence is used for heartbeating
                        val sequence = event.`package`.getInt("s")
                        (event.jda as JDAImpl).setResponseTotal(sequence)
                    }

                    event.isCancelled = true

                    // Add the event to our replay cache, we need to do this because a resume event is AFTER all events were replayed to the client, but we want to apply our faked events BEFORE the replayed events,
                    // to maintain a consistent cache.
                    replayCache.add(event.`package`)
                    return
                }

                WebSocketCode.INVALIDATE_SESSION -> {
                    // Session has been invalidated, clear out the replay cache
                    logger.info { "Session has been invalidated, clearing out ${replayCache.size} events..." }
                    state.value = ProcessorState.FINISHED
                    replayCache.clear()
                }
            }
        }
    }

    override fun onStatusChange(event: StatusChangeEvent) {
        if (state.value == ProcessorState.FINISHED)
            return

        if (event.newStatus == JDA.Status.CONNECTING_TO_WEBSOCKET) {
            if (initialSession != null) {
                logger.info { "Connecting to WebSocket, sending faked READY event..." }

                val jdaImpl = event.jda as JDAImpl

                // Update the current event sequence for resume
                jdaImpl.setResponseTotal(initialSession.sequence.toInt())

                // Send a fake READY event
                jdaImpl.client.handleEvent(
                    DataObject.fromJson(
                        buildJsonObject {
                            this.put("op", 0)
                            this.putJsonObject("d") {
                                this.putJsonArray("guilds") {
                                    for (guildId in initialSession.guilds) {
                                        addJsonObject {
                                            this.put("id", guildId)
                                            this.put("unavailable", true)
                                        }
                                    }
                                }
                                this.putJsonObject("user") {
                                    put("id", event.jda.selfUser.idLong)
                                    put("username", event.jda.selfUser.name)
                                    put("discriminator", event.jda.selfUser.discriminator)
                                    put("avatar", event.jda.selfUser.avatarId)
                                    put("public_flags", event.jda.selfUser.flagsRaw)
                                }
                                this.putJsonObject("application") {
                                    // This requires the verifyToken to be enabled since we need JDA to query the self user before proceeding
                                    // If you aren't using it, store the bot's app ID somewhere and pass it here instead!
                                    put("id", (event.jda.selfUser as SelfUserImpl).applicationId)
                                }
                                this.put("session_id", initialSession.sessionId)
                                this.put("resume_gateway_url", initialSession.resumeGatewayUrl)
                                // This is always empty
                                this.putJsonArray("private_channels") {}
                            }
                            this.put("t", "READY")
                            this.put(FAKE_EVENT_FIELD, true)
                        }.toString()
                    )
                )
                state.value = ProcessorState.WAITING_FOR_RESUME

                // When JDA connects, it will see that it has a non-null session ID and resume gateway URL, which will trigger a resume state instead of a identify... sweet!
            } else {
                // We don't have a gateway session, so just skip the gateway event processing shenanigans
                state.value = ProcessorState.FINISHED
            }
        }
    }

    enum class ProcessorState {
        WAITING_FOR_WEBSOCKET_CONNECTION,
        WAITING_FOR_RESUME,
        FINISHED
    }
}