package ir.OZyroX.cTLender.commands.tabComplete

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class loanTabComplete : TabCompleter {
    override fun onTabComplete(
        sender: CommandSender, command: Command, alias: String, args: Array<String>
    ): List<String> {
        if (args.isEmpty()) return emptyList()

        return when {
            args.size == 1 -> listOf("Status")
                .filter { it.startsWith(args[0], ignoreCase = true) }

            else -> emptyList()
        }
    }
}