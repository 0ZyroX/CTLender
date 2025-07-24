package ir.OZyroX.cTLender.model

import org.bukkit.inventory.ItemStack
import java.util.*

data class LoanData(
    val lender: UUID,
    val borrower: UUID,
    val itemUUID: String,
    var item: ItemStack,
    val start: Long,
    val duration: Long,
    var durability: Int
)

