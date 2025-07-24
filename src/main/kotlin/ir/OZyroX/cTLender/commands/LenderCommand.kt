package ir.OZyroX.cTLender.commands

import ir.OZyroX.cTLender.CTLender
import ir.OZyroX.cTLender.gui.LoanStatusGUI
import ir.OZyroX.cTLender.manager.Lang
import ir.OZyroX.cTLender.manager.Lang.prefix
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class LenderCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return false
        if(!sender.hasPermission("CTLender.reload")){
            sender.sendMessage("§cYou Don't Have Permission")
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage("")
            sender.sendMessage("§8» §eLender §c1.1 §7| Developed By OZyroX")
            sender.sendMessage("§8» §a/lender Reload §8| §7Reload Plugin")
            sender.sendMessage("")
            return true
        }
        when (args[0].lowercase()) {

            "reload" -> {
                CTLender.instance.reloadConfig()
                Lang.load(CTLender.instance)
                CTLender.instance.load(CTLender.instance)
                sender.sendMessage(prefix + " §aReloaded!")
                return true
            }
        }
        return true
    }
}