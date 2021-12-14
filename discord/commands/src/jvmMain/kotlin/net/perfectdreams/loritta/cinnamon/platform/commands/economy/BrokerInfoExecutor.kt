package net.perfectdreams.loritta.cinnamon.platform.commands.economy

import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.LorittaBovespaBrokerUtils
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.declarations.BrokerCommand
import net.perfectdreams.loritta.cinnamon.pudding.data.BrokerTickerInformation

class BrokerInfoExecutor : CommandExecutor() {
    companion object : CommandExecutorDeclaration(BrokerInfoExecutor::class)

    override suspend fun execute(context: ApplicationCommandContext, args: CommandArguments) {
        context.deferChannelMessage() // Defer because this sometimes takes too long

        val stockInformations = context.loritta.services.bovespaBroker.getAllTickers()

        context.sendMessage {
            brokerBaseEmbed(context) {
                // TODO: Localization
                title = "${Emotes.LoriStonks} ${context.i18nContext.get(BrokerCommand.I18N_PREFIX.Info.Embed.Title)}"
                description = context.i18nContext.get(
                    BrokerCommand.I18N_PREFIX.Info.Embed.Explanation(
                        loriSob = Emotes.LoriSob,
                        tickerOutOfMarket = Emotes.DoNotDisturb,
                        openTime = LorittaBovespaBrokerUtils.TIME_OPEN_DISCORD_TIMESTAMP,
                        closingTime = LorittaBovespaBrokerUtils.TIME_CLOSING_DISCORD_TIMESTAMP
                    )
                ).joinToString("\n")

                for (stockInformation in stockInformations.sortedBy(BrokerTickerInformation::ticker)) {
                    val tickerId = stockInformation.ticker
                    val tickerName = LorittaBovespaBrokerUtils.trackedTickerCodes[tickerId]
                    val currentPrice = LorittaBovespaBrokerUtils.convertReaisToSonhos(stockInformation.value)
                    val buyingPrice = LorittaBovespaBrokerUtils.convertToBuyingPrice(currentPrice) // Buying price
                    val sellingPrice = LorittaBovespaBrokerUtils.convertToSellingPrice(currentPrice) // Selling price
                    val changePercentage = stockInformation.dailyPriceVariation

                    val fieldTitle = "`$tickerId` ($tickerName) | ${"%.2f".format(changePercentage)}%"

                    if (stockInformation.status != LorittaBovespaBrokerUtils.MARKET) {
                        field {
                            name = "${Emotes.DoNotDisturb} $fieldTitle"
                            value = context.i18nContext.get(BrokerCommand.I18N_PREFIX.Info.Embed.PriceBeforeMarketClose(currentPrice))
                            inline = true
                        }
                    } else if (LorittaBovespaBrokerUtils.checkIfTickerDataIsStale(stockInformation.lastUpdatedAt)) {
                        field {
                            name = "${Emotes.Idle} $fieldTitle"
                            value = """${context.i18nContext.get(BrokerCommand.I18N_PREFIX.Info.Embed.BuyPrice(buyingPrice))}
                                |${context.i18nContext.get(BrokerCommand.I18N_PREFIX.Info.Embed.SellPrice(sellingPrice))}
                            """.trimMargin()
                            inline = true
                        }
                    } else {
                        field {
                            name = "${Emotes.Online} $fieldTitle"
                            value = """${context.i18nContext.get(BrokerCommand.I18N_PREFIX.Info.Embed.BuyPrice(buyingPrice))}
                                |${context.i18nContext.get(BrokerCommand.I18N_PREFIX.Info.Embed.SellPrice(sellingPrice))}
                            """.trimMargin()
                            inline = true
                        }
                    }
                }
            }
        }
    }
}