package ir.OZyroX.cTLender.Listeners


import ir.OZyroX.cTLender.manager.Lang
import ir.OZyroX.cTLender.manager.Lang.prefix
import ir.OZyroX.cTLender.manager.LoanManager
import ir.OZyroX.cTLender.manager.LoanManager.addToPlayer
import ir.OZyroX.cTLender.utils.ItemUtils
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.*
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable

class Listeners : Listener {
    @EventHandler
    fun onInventoryClick(e: InventoryClickEvent) {
        val item = e.currentItem ?: return
        if (!ItemUtils.isLoaned(item)) return

        val topInventory = e.view.topInventory
        val clickedInventory = e.clickedInventory ?: return

        val isOnlyPlayerInventory =
            topInventory.type == InventoryType.CRAFTING || topInventory.type == InventoryType.PLAYER

        if (!isOnlyPlayerInventory) {
            e.isCancelled = true
        }

        if (e.isShiftClick && clickedInventory == e.view.bottomInventory) {
            e.isCancelled = true
            return
        }

        if (e.rawSlot < topInventory.size) {
            e.isCancelled = true
        }
    }

    val allowedTypes = setOf(
        Material.TRIDENT,
        Material.BOW,
        Material.WOODEN_SHOVEL,
        Material.STONE_SHOVEL,
        Material.GOLDEN_SHOVEL,
        Material.IRON_SHOVEL,
        Material.DIAMOND_SHOVEL,
        Material.NETHERITE_SHOVEL,
        Material.WOODEN_HOE,
        Material.STONE_HOE,
        Material.GOLDEN_HOE,
        Material.IRON_HOE,
        Material.DIAMOND_HOE,
        Material.NETHERITE_HOE,
        Material.WOODEN_AXE,
        Material.STONE_AXE,
        Material.GOLDEN_AXE,
        Material.IRON_AXE,
        Material.DIAMOND_AXE,
        Material.NETHERITE_AXE,
        Material.WOODEN_PICKAXE,
        Material.STONE_PICKAXE,
        Material.GOLDEN_PICKAXE,
        Material.IRON_PICKAXE,
        Material.DIAMOND_PICKAXE,
        Material.NETHERITE_PICKAXE,
    )


    @EventHandler
    fun onPlayerInteractEntity(e: PlayerInteractEntityEvent) {
        val item = e.player.inventory.itemInMainHand
        if (ItemUtils.isLoaned(item) && item.type !in allowedTypes) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerInteract(e: PlayerInteractEvent) {
        val item = e.item ?: return
        if (ItemUtils.isLoaned(item) && item.type !in allowedTypes) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onItemConsume(e: PlayerItemConsumeEvent) {
        if (ItemUtils.isLoaned(e.item) && e.item.type !in allowedTypes) {
            e.isCancelled = true
        }
    }






    @EventHandler
    fun onInventoryDrag(e: InventoryDragEvent) {
        val item = e.oldCursor ?: return
        if (!ItemUtils.isLoaned(item)) return

        val topSize = e.view.topInventory.size
        for (slot in e.rawSlots) {
            if (slot < topSize) {
                e.isCancelled = true
                return
            }
        }
    }

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        val uuid = e.player.uniqueId
        val player = e.player
        val addToPlayer = LoanManager.addToPlayer
        val removeFromPlayer = LoanManager.removeFromPlayer

        addToPlayer[uuid]?.let { items ->
            val failedItems = mutableListOf<ItemStack>()

            for (item in items.toList()) {
                val result = player.inventory.addItem(item)
                if (result.isNotEmpty()) {
                    failedItems.addAll(result.values)
                } else {
                    player.sendMessage(Lang.get("item-give", "prefix" to prefix, "item" to LoanManager.getItemName(item)))
                }
            }

            if (failedItems.isNotEmpty()) {
                player.sendMessage(Lang.get("item-give-fail", "prefix" to prefix))
                addToPlayer[uuid] = failedItems
            } else {
                addToPlayer.remove(uuid)
            }
        }

        removeFromPlayer[uuid]?.let { itemsToRemove ->
            for (item in itemsToRemove.toList()) {
                var amountToRemove = item.amount

                for (invItem in player.inventory.contents.filterNotNull()) {
                    if (invItem.isSimilar(item)) {
                        val removeCount = minOf(invItem.amount, amountToRemove)
                        invItem.amount -= removeCount
                        amountToRemove -= removeCount

                        if (invItem.amount <= 0) {
                            player.inventory.removeItem(invItem)
                        }

                        if (amountToRemove <= 0) break
                    }
                }

                if (amountToRemove <= 0) {
                    player.sendMessage(Lang.get("item-remove", "prefix" to prefix, "item" to LoanManager.getItemName(item)))
                }
            }

            removeFromPlayer.remove(uuid)
        }
    }




    @EventHandler
    fun onDrop(e: PlayerDropItemEvent) {
        if (ItemUtils.isLoaned(e.itemDrop.itemStack)) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onItemDamage(event: PlayerItemDamageEvent) {
        val player = event.player
        val item = event.item

        if (!ItemUtils.isLoaned(item)) return

        val loan = LoanManager.getLoanByItem(item, player) ?: return

        val maxDurability = item.type.maxDurability
        val currentDurability = (item.itemMeta as? Damageable)?.damage ?: 0
        val remaining = maxDurability - currentDurability - event.damage

        val updatedItem = item.clone()
        ItemUtils.setLoanTag(updatedItem)
        ItemUtils.setLoanUUID(updatedItem, loan.itemUUID)
        (updatedItem.itemMeta as? Damageable)?.damage = currentDurability + event.damage
        loan.item = updatedItem

        if (remaining <= loan.durability) {
            event.isCancelled = true
            player.inventory.removeItem(item)
            player.sendMessage(Lang.get("removeItemForDurability", "prefix" to prefix, "item" to LoanManager.getItemName(updatedItem)))

            val lender = Bukkit.getPlayer(loan.lender)
            val itemToReturn = item.clone()
            ItemUtils.removeLoanTag(itemToReturn)
            ItemUtils.removeLoanUUID(itemToReturn)

            val i = LoanManager.getItemName(itemToReturn)

            if (lender != null && lender.isOnline) {
                val result = lender.inventory.addItem(itemToReturn)
                if (result.isNotEmpty()) {
                    lender.sendMessage(Lang.get("item-give-fail", "item" to i,"prefix" to prefix))
                    val list = LoanManager.addToPlayer.getOrPut(loan.lender) { mutableListOf() }
                    list.add(itemToReturn)
                } else {
                    lender.sendMessage(Lang.get("item-give","item" to i, "prefix" to prefix))
                }
            } else {
                val list = LoanManager.addToPlayer.getOrPut(loan.lender) { mutableListOf() }
                list.add(itemToReturn)
            }

            LoanManager.removeLoan(loan)
        }
    }





}