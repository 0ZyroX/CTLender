package ir.OZyroX.cTLender.utils

import ir.OZyroX.cTLender.CTLender
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object ItemUtils {
    private val key = NamespacedKey(CTLender.instance, "loaned")
    private val loanUUIDKey = NamespacedKey(CTLender.instance, "loanUUID")
    val blacklistedItems = mutableSetOf<Material>()

    fun setLoanUUID(item: ItemStack, uuid: String) {
        val meta = item.itemMeta ?: return
        meta.persistentDataContainer.set(loanUUIDKey, PersistentDataType.STRING, uuid)
        item.itemMeta = meta
    }

    fun getLoanUUID(item: ItemStack): String? {
        val meta = item.itemMeta ?: return null
        return meta.persistentDataContainer.get(loanUUIDKey, PersistentDataType.STRING)
    }

    fun removeLoanUUID(item: ItemStack) {
        val meta = item.itemMeta ?: return
        meta.persistentDataContainer.remove(loanUUIDKey)
        item.itemMeta = meta
    }

    fun isLoanedUUID(item: ItemStack): Boolean {
        return getLoanUUID(item) != null
    }



    fun setLoanTag(item: ItemStack) {
        val meta = item.itemMeta ?: return
        meta.persistentDataContainer.set(key, PersistentDataType.BYTE, 1)
        item.itemMeta = meta
    }

    fun removeLoanTag(item: ItemStack) {
        val meta = item.itemMeta ?: return
        meta.persistentDataContainer.remove(key)
        item.itemMeta = meta
    }

    fun isLoaned(item: ItemStack): Boolean {
        val meta = item.itemMeta ?: return false
        return meta.persistentDataContainer.has(key, PersistentDataType.BYTE)
    }

    fun isBlacklisted(item: ItemStack?): Boolean {
        return item != null && blacklistedItems.contains(item.type)
    }
}
