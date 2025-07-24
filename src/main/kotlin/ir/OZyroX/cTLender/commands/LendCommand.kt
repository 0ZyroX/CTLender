package ir.OZyroX.cTLender.commands

import ir.OZyroX.cTLender.manager.Lang
import ir.OZyroX.cTLender.manager.Lang.prefix
import ir.OZyroX.cTLender.manager.LoanManager
import ir.OZyroX.cTLender.manager.TradeManager
import ir.OZyroX.cTLender.manager.TradeManager.pendingAccepts
import ir.OZyroX.cTLender.manager.TradeManager.startTrade
import ir.OZyroX.cTLender.manager.TradeManager.tradeRequests
import ir.OZyroX.cTLender.model.LoanData
import ir.OZyroX.cTLender.utils.ItemUtils
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.hover.content.Text

class LendCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player || args.size < 1){
            sender.sendMessage("")
            sender.sendMessage("§8» §eLender §c1.1 §7| Developed By OZyroX")
            sender.sendMessage("§8» §a/lend <PlayerName> §8| ${Lang.get("cmd-lend-help")}")
            sender.sendMessage("")
            return true
        }
        val target = Bukkit.getPlayerExact(args[0])
        if(target == null){
            sender.sendMessage(Lang.get("cmd-lend-playerNotFound", "prefix" to prefix))
            return true
        }

        if(target == sender){
            sender.sendMessage(Lang.get("cmd-lend-self", "prefix" to prefix))
            return true
        }

        val senderId = sender.uniqueId
        val targetId = target.uniqueId

        if (pendingAccepts.contains(Pair(targetId, senderId))) {
            pendingAccepts.remove(Pair(targetId, senderId))
            startTrade(sender, target)
        } else {
            tradeRequests[senderId] = targetId
            pendingAccepts.add(Pair(senderId, targetId))
            sender.sendMessage(Lang.get("cmd-lend-requestSend", "target" to target.name, "prefix" to prefix))
            sendTradeRequestMessage(sender,target)
        }


        return true
    }

    fun sendTradeRequestMessage(sender: Player, target: Player) {
        val base = TextComponent(Lang.get("cmd-lend-requestGet", "player" to sender.name, "prefix" to prefix))
        val accept = TextComponent(Lang.get("cmd-lend-requestGetAccept"))
        accept.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/lend ${sender.name}")
        accept.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text(Lang.get("cmd-lend-requestGetAcceptHover")))

        base.addExtra(accept)

        target.spigot().sendMessage(base)
    }
}