package ir.OZyroX.cTLender.manager

import ir.OZyroX.cTLender.gui.TradeGUI
import ir.OZyroX.cTLender.manager.Lang.prefix
import ir.OZyroX.cTLender.model.LoanData
import ir.OZyroX.cTLender.model.TradeSession
import ir.OZyroX.cTLender.utils.ItemUtils
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

object TradeManager {

    val tradeRequests = mutableMapOf<UUID, UUID>()
    val pendingAccepts = mutableSetOf<Pair<UUID, UUID>>()
    val activeTrades = mutableMapOf<UUID, TradeSession>()

    fun isPlayerViewingTrade(player: Player): Boolean {
        val session = activeTrades[player.uniqueId] ?: return false
        return player.openInventory.topInventory == session.inventory
    }

    fun updateConfirmButtons(session: TradeSession) {
        val confirm = ItemStack(Material.RED_WOOL).apply {
            itemMeta = itemMeta?.apply { setDisplayName(Lang.get("gui-trade-confirm1")) }
        }
        val wait = ItemStack(Material.LIME_WOOL).apply {
            itemMeta = itemMeta?.apply { setDisplayName(Lang.get("gui-trade-confirm2")) }
        }

        session.inventory.setItem(2, if (session.confirmed1) wait else confirm)
        session.inventory.setItem(6, if (session.confirmed2) wait else confirm)
    }

    fun updateTimeButton(session: TradeSession) {
        session.inventory.setItem(4, ItemStack(Material.CLOCK).apply {
            val meta = itemMeta!!
            meta.setDisplayName(Lang.get("gui-trade-item-duration"))
            meta.lore = Lang.getList("gui-trade-item-duration-lore", "time" to session.time.toString())
            itemMeta = meta
        })
    }

    fun updateDurability(session : TradeSession){
        session.inventory.setItem(31, ItemStack(Material.DIAMOND_CHESTPLATE).apply {
            val meta = itemMeta!!
            meta.setDisplayName(Lang.get("gui-trade-item-durability"))
            meta.lore = Lang.getList("gui-trade-item-durability-lore", "durability" to session.durability.toString())
            itemMeta = meta
        })
    }

    fun completeTrade(session: TradeSession) {
        val inv = session.inventory
        val p1Items = session.p1Slots.mapNotNull { inv.getItem(it) }
        val p2Items = session.p2Slots.mapNotNull { inv.getItem(it) }
        val duration = session.time.toLong().times(3600000)
        p1Items.forEach {
            val uuid = UUID.randomUUID().toString()
            ItemUtils.setLoanTag(it)
            ItemUtils.setLoanUUID(it, uuid)
            session.player2.inventory.addItem(it)
            val data = LoanData(session.player1.uniqueId, session.player2.uniqueId, uuid, it, System.currentTimeMillis(), duration, session.durability.toInt())
            LoanManager.addLoan(data)
        }
        p2Items.forEach {
            val uuid = UUID.randomUUID().toString()
            ItemUtils.setLoanTag(it)
            ItemUtils.setLoanUUID(it, uuid)
            session.player1.inventory.addItem(it)
            val data = LoanData(session.player2.uniqueId, session.player1.uniqueId, uuid,it, System.currentTimeMillis(), duration, session.durability.toInt())
            LoanManager.addLoan(data)
        }

        session.player1.sendMessage(Lang.get("trade-complete", "prefix" to prefix))
        session.player2.sendMessage(Lang.get("trade-complete", "prefix" to prefix))

        session.player1.closeInventory()
        session.player2.closeInventory()

        activeTrades.remove(session.player1.uniqueId)
        activeTrades.remove(session.player2.uniqueId)


    }

    fun startTrade(p1: Player, p2: Player) {


        val session = TradeGUI().open(p1, p2)


        activeTrades[p1.uniqueId] = session
        activeTrades[p2.uniqueId] = session

        p1.openInventory(session.inventory)
        p2.openInventory(session.inventory)
        updateConfirmButtons(session)
    }

    fun cancelTrade(player: Player) {
        val session = activeTrades[player.uniqueId] ?: return
        val inv = session.inventory
        val p1 = session.player1
        val p2 = session.player2

        if (session.confirmed1 && session.confirmed2) return

        session.p1Slots.forEach { slot -> inv.getItem(slot)?.let { item -> session.player1.inventory.addItem(item) } }
        session.p2Slots.forEach { slot -> inv.getItem(slot)?.let { item -> session.player2.inventory.addItem(item) } }

        session.player1.sendMessage(Lang.get("trade-cancel", "prefix" to prefix))
        session.player2.sendMessage(Lang.get("trade-cancel", "prefix" to prefix))


        activeTrades.remove(session.player1.uniqueId)
        activeTrades.remove(session.player2.uniqueId)

        p1.closeInventory()
        p2.closeInventory()
    }

}