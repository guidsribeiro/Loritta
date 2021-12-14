package net.perfectdreams.loritta.cinnamon.microservices.brokertickersupdater

import io.ktor.client.*
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.common.utils.config.ConfigUtils
import net.perfectdreams.loritta.cinnamon.microservices.brokertickersupdater.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import kotlin.concurrent.thread

object BrokerTickersUpdaterLauncher {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        val rootConfig = ConfigUtils.loadAndParseConfigOrCopyFromJarAndExit<RootConfig>(BrokerTickersUpdaterLauncher::class, System.getProperty("brokertickersupdater.config", "broker-tickers-updater.conf"))
        logger.info { "Loaded Loritta's configuration file" }

        val http = HttpClient {
            expectSuccess = false
        }

        val services = Pudding.createPostgreSQLPudding(
            rootConfig.pudding.address ?: error("Missing database address!"),
            rootConfig.pudding.database ?: error("Missing database!"),
            rootConfig.pudding.username ?: error("Missing database username!"),
            rootConfig.pudding.password ?: error("Missing database password!")
        )

        Runtime.getRuntime().addShutdownHook(
            thread(false) {
                // Shutdown services when stopping the application
                // This is needed for the Pudding Tasks
                services.shutdown()
            }
        )

        logger.info { "Started Pudding client!" }

        val loritta = BrokerTickersUpdater(
            rootConfig,
            services,
            http
        )

        loritta.start()
    }
}