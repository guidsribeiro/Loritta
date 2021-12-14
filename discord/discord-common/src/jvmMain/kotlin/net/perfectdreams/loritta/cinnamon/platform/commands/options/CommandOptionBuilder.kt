package net.perfectdreams.loritta.cinnamon.platform.commands.options

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData

open class CommandOptionBuilder<T>(
    // We need to store the command option type due to type erasure
    val type: CommandOptionType,
    val name: String,
    val description: StringI18nData,
    val choices: MutableList<CommandChoice<T>>
) {
    fun choice(value: T, name: StringI18nData): CommandOptionBuilder<T> {
        choices.add(
            LocalizedCommandChoice(
                type,
                name,
                value
            )
        )
        return this
    }

    fun choice(value: T, name: String): CommandOptionBuilder<T> {
        choices.add(
            RawCommandChoice(
                type,
                name,
                value
            )
        )
        return this
    }
}