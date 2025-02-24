package net.perfectdreams.loritta.morenitta.commands.vanilla.utils

import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.stripCodeMarks
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.math.MathUtils
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils
import net.perfectdreams.loritta.morenitta.LorittaBot

class CalculadoraCommand(loritta: LorittaBot) : AbstractCommand(loritta, "calc", listOf("calculadora", "calculator", "calcular", "calculate"), net.perfectdreams.loritta.common.commands.CommandCategory.UTILS) {
	companion object {
		const val LOCALE_PREFIX = "commands.command.calc"
	}

	// TODO: Fix Usage

	override fun getDescriptionKey() = LocaleKeyData("$LOCALE_PREFIX.description")
	override fun getExamplesKey() = LocaleKeyData("$LOCALE_PREFIX.examples")

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "calc")

		if (context.args.isNotEmpty()) {
			val expression = context.args.joinToString(" ")
					.replace("_", "")
					.replace(",", "")

			try {
				// Regra de três:tm:
				if (expression.contains("---")) {
					val split = expression.split("/")
					val firstSide = split[0].split("---")
					val secondSide = split[1].split("---")
					val number0 = firstSide[0].trim()
					val number1 = firstSide[1].trim()

					val number2 = secondSide[0].trim()
					val number3 = secondSide[1].trim()

					val resultNumber0 = MathUtils.evaluate(number0)
					val resultNumber1 = MathUtils.evaluate(number1)
					val resultNumber2 = MathUtils.evaluate(number2)

					// resultNumber0 --- resultNumber1
					// resultNumber2 --- x
					context.reply(
                            LorittaReply(
                                    context.locale["$LOCALE_PREFIX.result", (resultNumber2 * resultNumber1) / resultNumber0]
                            )
					)
					return
				}

				val result = MathUtils.evaluate(expression)

				context.reply(
                        LorittaReply(
                                context.locale["$LOCALE_PREFIX.result", result]
                        )
				)
			} catch (e: Exception) {
				context.reply(
                        LorittaReply(
                                context.locale["$LOCALE_PREFIX.invalid", expression.stripCodeMarks()] + " ${Emotes.LORI_CRYING}",
                                Emotes.LORI_HM
                        )
				)
			}
		} else {
			this.explain(context)
		}
	}
}
