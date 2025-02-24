package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`

import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.utils.math.Dice
import net.perfectdreams.loritta.common.utils.math.MathUtils
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.declarations.RollCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.declarations.CalculatorCommand
import kotlin.random.Random

class RollExecutor(loritta: LorittaBot) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val dices = optionalString("dices", RollCommand.I18N_PREFIX.Options.Dices)

        val expression = optionalString("expression", RollCommand.I18N_PREFIX.Options.Expression)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val dicesAsString = args[options.dices]
        val mathematicalExpression = args[options.expression]

        val dices = try {
            // First we will parse only the dices, math expressions will be calculated later!
            val dices = Dice.parse(dicesAsString ?: "6", 100)

            if (dices.isEmpty())
                throw UnsupportedOperationException("No valid dices found!")

            dices
        } catch (e: Dice.Companion.TooManyDicesException) {
            context.failEphemerally(
                context.i18nContext.get(RollCommand.I18N_PREFIX.TooManyDices),
                Emotes.LoriSob
            )
        } catch (e: Dice.Companion.LowerBoundHigherThanUpperBoundException) {
            context.failEphemerally(
                context.i18nContext.get(RollCommand.I18N_PREFIX.InvalidBound),
                Emotes.LoriShrug
            )
        } catch (e: IllegalArgumentException) {
            context.failEphemerally(
                context.i18nContext.get(RollCommand.I18N_PREFIX.InvalidBound),
                Emotes.LoriShrug
            )
        } catch (e: UnsupportedOperationException) {
            context.failEphemerally(
                context.i18nContext.get(RollCommand.I18N_PREFIX.InvalidBound),
                Emotes.LoriShrug
            )
        }

        val rolledSides = mutableListOf<Long>()

        var response = ""
        for (dice in dices) {
            val rolledSide = loritta.random.nextLong(dice.lowerBound, dice.upperBound + 1)
            rolledSides.add(rolledSide)
        }

        response = rolledSides.joinToString(" + ")

        var finalResult = 0F

        rolledSides.forEach {
            finalResult += it
        }


        if (mathematicalExpression != null) {
            try {
                response += " = ${finalResult.toInt()} `${mathematicalExpression.trim()}"

                finalResult = MathUtils.evaluate(finalResult.toString() + mathematicalExpression).toFloat()

                response += " = ${finalResult.toInt()}`"
            } catch (e: Exception) {
                // TODO: Fix stripCodeMarks
                context.failEphemerally(
                    context.i18nContext.get(
                        CalculatorCommand.I18N_PREFIX.Invalid(
                            mathematicalExpression
                        )
                    ),
                    prefix = Emotes.LoriHm
                )
            }
        }

        response = if (rolledSides.size == 1 && mathematicalExpression == null) {
            ""
        } else {
            "`${finalResult.toInt()}` **»** $response"
        }

        // All the dices have the same lower and upper bound, so it doesn't matter what dice we choose
        val firstDice = dices.first()
        val lowerBound = firstDice.lowerBound
        val upperBound = firstDice.upperBound

        context.sendMessage {
            styled(
                context.i18nContext.get(
                    RollCommand.I18N_PREFIX.Result(
                        // Not showing the lower bound is confusing, because the user may think that the lower bound was never recognized
                        // So, if the lower bound is set, we will show it to the user!
                        diceExpression = if (lowerBound != 1L)
                            "${dices.size}d${lowerBound}..$upperBound"
                        else
                            "${dices.size}d$upperBound",
                        result = finalResult.toInt()
                    )
                ),
                prefix = Emotes.LoriGameDie
            )

            if (response.isNotEmpty())
                styled(content = response, prefix = "\uD83E\uDD13")
        }
    }
}