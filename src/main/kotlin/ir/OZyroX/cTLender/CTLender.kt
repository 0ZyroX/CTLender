package ir.OZyroX.cTLender

import ir.OZyroX.cTLender.Listeners.Listeners
import ir.OZyroX.cTLender.Listeners.TradeListener
import ir.OZyroX.cTLender.commands.LoanCommand
import ir.OZyroX.cTLender.commands.LendCommand
import ir.OZyroX.cTLender.commands.LenderCommand
import ir.OZyroX.cTLender.commands.tabComplete.lendTabComplete
import ir.OZyroX.cTLender.commands.tabComplete.lenderTabComplete
import ir.OZyroX.cTLender.commands.tabComplete.loanTabComplete
import ir.OZyroX.cTLender.gui.LoanStatusGUI
import ir.OZyroX.cTLender.manager.Lang
import ir.OZyroX.cTLender.manager.LoanManager
import ir.OZyroX.cTLender.manager.TradeManager.pendingAccepts
import ir.OZyroX.cTLender.manager.TradeManager.startTrade
import ir.OZyroX.cTLender.manager.TradeManager.tradeRequests
import ir.OZyroX.cTLender.utils.ItemUtils
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.*

class CTLender : JavaPlugin() {


    companion object {
        lateinit var instance: CTLender
    }

    override fun onEnable() {
        instance = this

        config.options().copyDefaults(true)
        saveDefaultConfig()
        load(this)

        server.pluginManager.registerEvents(Listeners(), this)
        server.pluginManager.registerEvents(TradeListener(), this)
        server.pluginManager.registerEvents(LoanStatusGUI, this)
        getCommand("lend")?.setExecutor(LendCommand())
        getCommand("loan")?.setExecutor(LoanCommand())
        getCommand("lender")?.setExecutor(LenderCommand())
        getCommand("loan")?.tabCompleter = loanTabComplete()
        getCommand("lend")?.tabCompleter = lendTabComplete()
        getCommand("lender")?.tabCompleter = lenderTabComplete()
        LoanManager.loadAll()
        LoanManager.loadPlayers()
        LoanManager.startScheduler()
        logger.info("Loading Language...")
        Lang.load(this)
        logger.info("CTLender Enabled")
    }

    override fun onDisable() {
        LoanManager.saveAll()
        LoanManager.savePlayers()
        logger.info("CTLender Disabled")
    }

    fun load(plugin: JavaPlugin) {
        val list = plugin.config.getStringList("blacklist-items")
        ItemUtils.blacklistedItems.clear()
        list.mapNotNullTo(ItemUtils.blacklistedItems) { Material.matchMaterial(it.uppercase()) }
    }


}
