package com.mukunda.goodshop.commands;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.mukunda.cmdhandler.CommandGroup;
import com.mukunda.cmdhandler.CommandHandler;
import com.mukunda.loremeta.LoreMeta;

//-------------------------------------------------------------------------------------------------
public class PricetagCommand extends CommandHandler {
	
	private static final String BUY_TAG = ChatColor.AQUA + "BUY: ";
	private static final String SELL_TAG = ChatColor.YELLOW + "SELL: ";

	//-------------------------------------------------------------------------------------------------
	public PricetagCommand( CommandGroup parent ) {
		
		super(parent, "pricetag", 1, true );
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public void printSyntax() {
		reply( "/goodshop pricetag <buyprice> <sellprice> <slot>" );
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public void printUsage() {
		reply( "/goodshop pricetag <buyprice> <sellprice> <slot>" );
		reply( "Buy price is how much the player spends when buying the item. "+
				"Put 0 to make it not buyable, and \"FREE\" to make it FREE." );
		reply( "Sell price is how much money the player gets when selling the item. "+
				"Put 0 to make it not sellable." );
		reply( "<slot> is which slot the item will prefer in the inventory window. "+
				"If the slot is taken by other items, this item will not show up." );
	}

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("deprecation")
	public void giveStoreTag( Player player, Float buyprice, Float sellprice, Integer slot ) {
		//Inventory test = getServer().createInventory( null, 9 , "Magic Shop");
		//player.openInventory( test );
		ItemStack item = new ItemStack(Material.COBBLESTONE);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName( ChatColor.GREEN+"#SHOPTAG" );
		ArrayList<String> lore = new ArrayList<String>();
		
		if( buyprice != null ) {
			if( buyprice != 0.0f ) {
				lore.add( String.format( "##" +  BUY_TAG + "%.2f", buyprice ) );
				
			} else {
				lore.add( String.format( "##" + BUY_TAG + "FREE" ) );
			}
		}
		if( sellprice != null ) {
			lore.add( String.format( "##" + SELL_TAG + "%.2f", sellprice ) );
		}
		
		if( slot != null ) {
			lore.add( "@@[B:SLOT:" + slot + "]" );
		}
		
		meta.setLore( lore );
		item.setItemMeta( meta );
		LoreMeta.initialize( item );
		
		player.getInventory().addItem( item );
		player.updateInventory();
		
		player.sendMessage( ChatColor.GREEN + "Here you go.");
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public void run( String[] args ) {

		Player player = getPlayer();
		Float buyprice, sellprice;
		Integer slot;
		
		try {
			if( args[0].equalsIgnoreCase("free") ) {
				buyprice = 0.0f;
			} else {
				buyprice = Float.parseFloat( args[1] );
				if( buyprice < 0.0f ) {
					throw new NumberFormatException();
				} else if( buyprice == 0.0f ) {
					buyprice = null;
				}
			}
			
			if( args.length >= 2 ) {
				sellprice = Float.parseFloat( args[2] );
				if( sellprice < 0.0f ) {
					throw new NumberFormatException();
				} else if( sellprice == 0.0f ) {
					sellprice = null;
				}
			} else {
				sellprice = null;
			}
			
			if( args.length >= 3 ) {
				slot = Integer.parseInt(args[3] );
				if( slot < 0 || slot > 255 ) 
					throw new NumberFormatException();
			} else {
				slot = null;
			}

			// check if the tag has either buy or sell and not neither
			// and don't let them do something stupid.
			if( buyprice != null && sellprice != null && sellprice > buyprice ) {
				throw new NumberFormatException();
			}

			if( ((buyprice != null && buyprice == 0.0f) && sellprice != null) ||
				(buyprice == null && sellprice == null)	) {
				
				throw new NumberFormatException();
			}
			
		} catch( NumberFormatException e ) {
			reply( "Invalid argument(s)." );
			return;
		}
		
		giveStoreTag( player, buyprice, sellprice, slot );
	}

}
