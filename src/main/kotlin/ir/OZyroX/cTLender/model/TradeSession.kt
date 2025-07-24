package ir.OZyroX.cTLender.model

import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

data class TradeSession(
    val player1: Player,
    val player2: Player,
    val inventory: Inventory,
    var confirmed1: Boolean = false,
    var confirmed2: Boolean = false,
    var time : Int = 1,
    var durability : Double = 10.0,
    val p1Slots: List<Int> = listOf(10,11,12,19,20,21,28,29,30,37,38,39),
    val p2Slots: List<Int> = listOf(14,15,16,23,24,25,32,33,34,41,42,43)
)
