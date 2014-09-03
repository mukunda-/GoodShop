package com.mukunda.goodshop;

import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Information associated with an open shop window.
 * 
 * Set as the inventory holder.
 * 
 * @author mukunda
 *
 */
public class ShopHolder implements InventoryHolder {
	
	public Location source;
	
	public ShopHolder( Location source ) {
		this.source = source;
	}

	public Inventory getInventory() {
		
		return null;
	}
	
}