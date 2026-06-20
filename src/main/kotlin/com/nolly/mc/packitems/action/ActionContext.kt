package com.nolly.mc.packitems.action

import com.nolly.mc.packitems.item.PackItem
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.inventory.ItemStack

data class ActionContext(
	val event: Event,
	val player: Player,
	val item: PackItem,
	val handItem: ItemStack?,
	val block: Block?,
	val entity: Entity?,
	val trigger: Trigger
)
