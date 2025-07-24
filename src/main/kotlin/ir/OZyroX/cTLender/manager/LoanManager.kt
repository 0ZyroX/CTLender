package ir.OZyroX.cTLender.manager

import ir.OZyroX.cTLender.CTLender
import ir.OZyroX.cTLender.manager.Lang.prefix
import ir.OZyroX.cTLender.model.LoanData
import ir.OZyroX.cTLender.utils.ItemUtils
import ir.OZyroX.cTLender.utils.ItemUtils.getLoanUUID
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import java.io.File
import java.util.*

object LoanManager {
    private val dataFile = File(CTLender.instance.dataFolder, "storage/loans.yml")
    private val config = if (dataFile.exists()) YamlConfiguration.loadConfiguration(dataFile) else YamlConfiguration()

    private val playerDataFile = File(CTLender.instance.dataFolder, "storage/players.yml")
    private val playerConfig =
        if (playerDataFile.exists()) YamlConfiguration.loadConfiguration(playerDataFile) else YamlConfiguration()


    private val loans = mutableListOf<LoanData>()
    val removeFromPlayer = mutableMapOf<UUID, MutableList<ItemStack>>()
    val addToPlayer = mutableMapOf<UUID, MutableList<ItemStack>>()

    fun loadAll() {
        config.getConfigurationSection("loans")?.getKeys(false)?.forEach { key ->
            val section = config.getConfigurationSection("loans.$key") ?: return@forEach
            loans.add(
                LoanData(
                    UUID.fromString(section.getString("lender")!!),
                    UUID.fromString(section.getString("borrower")!!),
                    section.getString("item-uuid")!!,
                    section.getItemStack("item")!!,
                    section.getLong("start"),
                    section.getLong("duration"),
                    section.getInt("durability")
                )
            )
        }
    }

    fun loadPlayers() {
        val giveSection = playerConfig.getConfigurationSection("give")
        giveSection?.getKeys(false)?.forEach { key ->
            val section = giveSection.getConfigurationSection(key) ?: return@forEach
            val uuid = section.getString("uuid")?.let { UUID.fromString(it) } ?: return@forEach
            val itemList = section.getList("items")?.filterIsInstance<ItemStack>() ?: return@forEach
            addToPlayer[uuid] = itemList.toMutableList()
        }

        val removeSection = playerConfig.getConfigurationSection("remove")
        removeSection?.getKeys(false)?.forEach { key ->
            val section = removeSection.getConfigurationSection(key) ?: return@forEach
            val uuid = section.getString("uuid")?.let { UUID.fromString(it) } ?: return@forEach
            val itemList = section.getList("items")?.filterIsInstance<ItemStack>() ?: return@forEach
            removeFromPlayer[uuid] = itemList.toMutableList()
        }
    }

    fun savePlayers() {
        playerConfig.set("give", null)
        playerConfig.set("remove", null)

        var index = 0
        addToPlayer.forEach { (uuid, items) ->
            val path = "give.player$index"
            playerConfig.set("$path.uuid", uuid.toString())
            playerConfig.set("$path.items", items)
            index++
        }

        index = 0
        removeFromPlayer.forEach { (uuid, items) ->
            val path = "remove.player$index"
            playerConfig.set("$path.uuid", uuid.toString())
            playerConfig.set("$path.items", items)
            index++
        }

        playerConfig.save(File(CTLender.instance.dataFolder, "players.yml"))
    }


    fun saveAll() {
        config.set("loans", null)
        loans.forEachIndexed { i, loan ->
            val path = "loans.${i}"
            config.set("$path.lender", loan.lender.toString())
            config.set("$path.borrower", loan.borrower.toString())
            config.set("$path.item-uuid", loan.itemUUID)
            config.set("$path.item", loan.item)
            config.set("$path.start", loan.start)
            config.set("$path.duration", loan.duration)
            config.set("$path.durability", loan.durability)
        }
        config.save(dataFile)
    }

    fun getLoanByItem(item: ItemStack, player: Player): LoanData? {
        val uuid = getLoanUUID(item) ?: return null
        return loans.find { it.itemUUID == uuid && it.borrower == player.uniqueId }
    }


    fun addLoan(loan: LoanData) {
        loans.add(loan)
        saveAll()
    }

    fun removeLoan(loan: LoanData) {
        loans.remove(loan)
        saveAll()
    }

    fun getLoansFor(uuid: UUID): List<LoanData> = loans.filter { it.lender == uuid || it.borrower == uuid }

    fun getPendingLoan(borrower: UUID): LoanData? = loans.firstOrNull { it.borrower == borrower && it.start == 0L }

    fun startScheduler() {
        Bukkit.getScheduler().runTaskTimer(CTLender.instance, Runnable {
            val now = System.currentTimeMillis()
            val expired = loans.filter { it.start != 0L && it.start + it.duration <= now }

            expired.forEach { loan ->
                val lender = Bukkit.getOfflinePlayer(loan.lender)
                val borrower = Bukkit.getOfflinePlayer(loan.borrower)

                val itemToReturn = loan.item.clone()
                ItemUtils.removeLoanTag(itemToReturn)
                ItemUtils.removeLoanUUID(itemToReturn)

                if (borrower.isOnline) {
                    val playerInv = borrower.player?.inventory ?: return@forEach
                    val itemsToRemove = playerInv.contents.filter {
                        it != null && ItemUtils.getLoanUUID(it) == loan.itemUUID
                    }

                    itemsToRemove.forEach { playerInv.remove(it) }

                    borrower.player?.sendMessage(Lang.get("item-remove", "prefix" to prefix, "item" to getItemName(itemToReturn)))
                } else {
                    val list = removeFromPlayer.getOrPut(loan.borrower) { mutableListOf() }
                    list.add(loan.item.clone())
                }

                if (lender.isOnline) {
                    val result = lender.player?.inventory?.addItem(itemToReturn)
                    if (result != null && result.isNotEmpty()) {
                        lender.player?.sendMessage(Lang.get("item-give-fail", "item" to getItemName(itemToReturn), "prefix" to prefix))
                        val list = addToPlayer.getOrPut(loan.lender) { mutableListOf() }
                        list.add(itemToReturn)
                    } else {
                        lender.player?.sendMessage(Lang.get("item-give", "prefix" to prefix, "item" to getItemName(itemToReturn)))
                    }
                } else {
                    val list = addToPlayer.getOrPut(loan.lender) { mutableListOf() }
                    list.add(itemToReturn)
                }

                removeLoan(loan)
            }
        }, 20L * 60, 20L * 60)
    }


    fun getItemName(item: ItemStack): String {
        val metaName = item.itemMeta?.displayName
        return if (!metaName.isNullOrBlank()) {
            metaName
        } else {
            item.type.name
                .replace("_", " ")
                .lowercase()
                .split(" ")
                .joinToString(" ") { it.replaceFirstChar { ch -> ch.uppercase() } }
        }
    }


    fun formatRemainingTime(loan: LoanData): String {
        val remaining = loan.duration
        val seconds = remaining / 1000 % 60
        val minutes = remaining / 1000 / 60 % 60
        val hours = remaining / 1000 / 60 / 60

        return "${hours}h ${minutes}m ${seconds}s"
    }

}
