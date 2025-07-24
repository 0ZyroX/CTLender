package ir.OZyroX.cTLender.gui

import ir.OZyroX.cTLender.manager.Lang
import ir.OZyroX.cTLender.manager.LoanManager
import ir.OZyroX.cTLender.model.LoanData
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

object LoanStatusGUI : Listener {
    fun openStatusGUI(p: Player) {
        val loans = LoanManager.getLoansFor(p.uniqueId)
        val inv = Bukkit.createInventory(LoanStatusHolder(), 54, Lang.get("gui-loanStatus-title"))

        for ((index, loan) in loans.withIndex()) {
            if (index >= 54) break
            val item = loan.item.clone()
            val meta = item.itemMeta!!
            val lore = meta.lore?.toMutableList() ?: mutableListOf()
            Lang.getList(
                "gui-loanStatus-item-lore",
                "lender" to Bukkit.getOfflinePlayer(loan.lender).name!!,
                "borrower" to Bukkit.getOfflinePlayer(loan.borrower).name!!,
                "status" to if (loan.start == 0L) "Pending" else "Active",
                "time" to formatRemainingTime(loan)
            ).forEach { msg ->
                lore.add(msg)
            }


            meta.lore = lore
            item.itemMeta = meta
            inv.setItem(index, item)
        }

        p.openInventory(inv)
    }

    @EventHandler
    fun handleClick(e: InventoryClickEvent) {
        val holder = e.inventory.holder
        if (holder is LoanStatusHolder) {
            e.isCancelled = true
        }
    }

    private fun formatRemainingTime(loan: LoanData): String {
        return if (loan.start == 0L) "-" else {
            val remaining = (loan.start + loan.duration) - System.currentTimeMillis()
            val seconds = remaining / 1000 % 60
            val minutes = remaining / 1000 / 60 % 60
            val hours = remaining / 1000 / 60 / 60
            "${hours}h ${minutes}m ${seconds}s"
        }
    }

    class LoanStatusHolder : InventoryHolder {
        override fun getInventory(): Inventory = Bukkit.createInventory(this, 54)
    }
}