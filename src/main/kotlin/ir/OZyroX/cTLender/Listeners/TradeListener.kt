package ir.OZyroX.cTLender.Listeners

import ir.OZyroX.cTLender.manager.Lang
import ir.OZyroX.cTLender.manager.Lang.prefix
import ir.OZyroX.cTLender.manager.TradeManager
import ir.OZyroX.cTLender.utils.ItemUtils
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerPickupItemEvent

class TradeListener : Listener {
    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val session = TradeManager.activeTrades[player.uniqueId] ?: return
        if (event.view.topInventory != session.inventory) return

        val slot = event.rawSlot
        val isP1 = player == session.player1
        val allowedSlots = if (isP1) session.p1Slots else session.p2Slots

        if (event.isShiftClick && event.clickedInventory != null && event.clickedInventory == player.inventory) {
            val isP1 = player == session.player1
            val allowedSlots = if (isP1) session.p1Slots else session.p2Slots

            val currentItem = event.currentItem ?: return

            if (ItemUtils.isLoaned(currentItem)) {
                player.sendMessage(Lang.get("trade-cantTradeLoanedItem", "prefix" to prefix))
                event.isCancelled = true
                return
            }

            if (ItemUtils.isBlacklisted(currentItem)) {
                player.sendMessage(Lang.get("trade-cantTradeBlacklistedItem", "prefix" to prefix))
                event.isCancelled = true
                return
            }

            val destinationSlot = allowedSlots.firstOrNull { session.inventory.getItem(it) == null }

            if (destinationSlot == null) {
                event.isCancelled = true
            } else {
                session.inventory.setItem(destinationSlot, currentItem.clone())
                currentItem.amount = 0
                event.isCancelled = true
            }
        }


        if (event.clickedInventory == session.inventory) {
            val slot = event.slot
            val isP1 = player == session.player1
            val allowedSlots = if (isP1) session.p1Slots else session.p2Slots

            if (slot !in allowedSlots) {
                event.isCancelled = true
            }

            if (ItemUtils.isBlacklisted(event.currentItem)) {
                player.sendMessage(Lang.get("trade-cantTradeBlacklistedItem", "prefix" to prefix))
                event.isCancelled = true
                return
            }

            if (slot == 2 && isP1) {
                if (session.confirmed1) {
                    session.confirmed1 = false
                } else {
                    session.confirmed1 = true
                }
            }
            if (slot == 6 && !isP1) {
                if (session.confirmed2) {
                    session.confirmed2 = false
                } else {
                    session.confirmed2 = true
                }
            }

            if (slot == 4) {
                if (event.click == ClickType.LEFT) {
                    session.time += 1
                    TradeManager.updateTimeButton(session)
                    session.confirmed1 = false
                    session.confirmed2 = false
                } else if (event.click == ClickType.RIGHT) {
                    if (session.time > 1) {
                        session.time -= 1
                        TradeManager.updateTimeButton(session)
                        session.confirmed1 = false
                        session.confirmed2 = false
                    }
                }
            }

            if (slot == 31) {
                when (event.click) {
                    ClickType.LEFT -> {
                        session.durability += 1
                        session.confirmed1 = false
                        session.confirmed2 = false
                    }

                    ClickType.SHIFT_LEFT -> {
                        session.durability += 10
                        session.confirmed1 = false
                        session.confirmed2 = false
                    }

                    ClickType.RIGHT -> {
                        if (session.durability > 1) {
                            session.durability -= 1
                            session.confirmed1 = false
                            session.confirmed2 = false
                        }
                    }

                    ClickType.SHIFT_RIGHT -> {
                        if (session.durability > 10) {
                            session.durability -= 10
                            session.confirmed1 = false
                            session.confirmed2 = false
                        }
                    }

                    else -> {}
                }
                TradeManager.updateDurability(session)
            }

            if (slot in allowedSlots) {
                session.confirmed1 = false
                session.confirmed2 = false
            }

            TradeManager.updateConfirmButtons(session)
            if (session.confirmed1 && session.confirmed2) TradeManager.completeTrade(session)
        }

    }


    @EventHandler
    fun onClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        val session = TradeManager.activeTrades[player.uniqueId] ?: return

        TradeManager.cancelTrade(player)
    }

    @EventHandler
    fun onDamage(event: EntityDamageByEntityEvent) {
        val player = event.entity as? Player ?: return
        val session = TradeManager.activeTrades[player.uniqueId] ?: return

        TradeManager.cancelTrade(player)
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        val player = event.entity as? Player ?: return
        val session = TradeManager.activeTrades[player.uniqueId] ?: return

        TradeManager.cancelTrade(player)
    }

    @EventHandler
    fun onPickItem(event: EntityPickupItemEvent) {
        val player = event.entity as? Player ?: return
        val session = TradeManager.activeTrades[player.uniqueId] ?: return

        event.isCancelled = true
    }
}