package ir.OZyroX.cTLender.gui

import ir.OZyroX.cTLender.manager.Lang
import ir.OZyroX.cTLender.model.TradeSession
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

class TradeGUI {
    fun open(p1: Player, p2: Player): TradeSession {

        val inv = Bukkit.createInventory(null, 54, Lang.get("gui-trade-title", "p1" to p1.name, "p2" to p2.name))
        val session = TradeSession(p1, p2, inv)

        for (i in 0 until 54) {
            if (i in session.p1Slots || i in session.p2Slots) continue
            inv.setItem(i, ItemStack(Material.GRAY_STAINED_GLASS_PANE).apply {
                itemMeta = itemMeta?.apply { setDisplayName("§7") }
            })
        }

        inv.setItem(4, ItemStack(Material.CLOCK).apply {
            val meta = itemMeta!!
            meta.setDisplayName(Lang.get("gui-trade-item-duration"))
            meta.lore = Lang.getList("gui-trade-item-duration-lore", "time" to session.time.toString())
            itemMeta = meta
        })

        inv.setItem(0, ItemStack(Material.PLAYER_HEAD).apply {
            val meta = itemMeta as SkullMeta
            meta.setOwningPlayer(p1)
            meta.setDisplayName("§e${p1.name}")
            itemMeta = meta
        })

        inv.setItem(8, ItemStack(Material.PLAYER_HEAD).apply {
            val meta = itemMeta as SkullMeta
            meta.setOwningPlayer(p2)
            meta.setDisplayName("§e${p2.name}")
            itemMeta = meta
        })

        inv.setItem(31, ItemStack(Material.DIAMOND_CHESTPLATE).apply {
            val meta = itemMeta!!
            meta.setDisplayName(Lang.get("gui-trade-item-durability"))
            meta.lore = Lang.getList("gui-trade-item-durability-lore", "durability" to session.durability.toString())
            itemMeta = meta
        })


        return session
    }
}