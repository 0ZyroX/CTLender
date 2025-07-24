package ir.OZyroX.cTLender.commands

import ir.OZyroX.cTLender.gui.LoanStatusGUI
import ir.OZyroX.cTLender.manager.Lang
import ir.OZyroX.cTLender.manager.LoanManager
import ir.OZyroX.cTLender.utils.ItemUtils
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class LoanCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return false
        if (args.isEmpty()) {
            sender.sendMessage("")
            sender.sendMessage("§8» §eLender §c1.1 §7| Developed By OZyroX")
            sender.sendMessage("§8» §a/loan Status §8| ${Lang.get("cmd.loan.help")}")
            sender.sendMessage("")
            return true
        }
        when (args[0].lowercase()) {

            "status" -> {
                LoanStatusGUI.openStatusGUI(sender)
                return true
            }
        }
        return true
    }
}
