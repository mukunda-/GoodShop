/*
 * GoodShop
 *
 * Copyright (c) 2014 Mukunda Johnson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mukunda.goodshop;

import java.util.ArrayList;
import java.util.List;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory; 
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
 




import com.mukunda.loremeta.LoreMeta;
import com.mukunda.loremeta.MetaKeyByte;
import com.mukunda.loremeta.MetaKeyFlag;
import com.mukunda.loremeta.MetaKeyText;
import com.mukunda.magicitems.MagicItems;
import com.mukunda.magicitems.NotAMagicItem;
 
//-------------------------------------------------------------------------------------------------
public class GoodShopPlugin extends JavaPlugin implements Listener {
	
    public static Economy economy = null;

    // is this safe to setup like this in a plugin?
	private final String BUY_METAKEY_NAME = "BUYX";
	private final String ACTION_METAKEY_NAME = "USEX";
	private final MetaKeyByte BUY_METAKEY = new MetaKeyByte( BUY_METAKEY_NAME );
	private final MetaKeyFlag ACTION_METAKEY = new MetaKeyFlag( ACTION_METAKEY_NAME );
	
	private Commands commands;
	/*
	private static final String BUY_META = ChatColor.COLOR_CHAR + "\u0117";
	
	private static String formatBuyMeta( int index ) {
		if( index > 99 || index < 0 ) index = 99;
		return BUY_META + ChatColor.COLOR_CHAR + (index / 10) + ChatColor.COLOR_CHAR + (index % 10);
	}*/
	
	private Byte getBuyMeta( ItemStack item ) {
		List<String> lore;
		lore = item.getItemMeta().getLore();
		if( lore == null ) return null;
		
		return LoreMeta.getData( item, BUY_METAKEY );
		/*
		for( String str: lore ) {
			int index = str.indexOf( BUY_META );
			if( index == -1 ) continue;
			return Character.getNumericValue(str.charAt( index + 3 )) * 10 + Character.getNumericValue(str.charAt( index + 5 ));
			
		}*/
		//return null;
	}
	 
	//-------------------------------------------------------------------------------------------------
	private boolean setupEconomy() {
		// link with vault
		
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }
	
	//-------------------------------------------------------------------------------------------------
	public void onEnable() {
		if( !setupEconomy() ) {
			getLogger().warning( "No Vault dependency found. Disabling." );
			getServer().getPluginManager().disablePlugin( this );
			return;
		}
		
		commands = new Commands();
		getServer().getPluginManager().registerEvents( this, this );
		
	}
	
	//-------------------------------------------------------------------------------------------------
	public void onDisable() {
		
	}
	
	
	//-------------------------------------------------------------------------------------------------
	public boolean onCommand( CommandSender sender, Command cmd, String label, String[] args ) {
		// TODO testing function.
		if( cmd.getName().equalsIgnoreCase("test2") ) {
			
			openShop( (Player)sender, "Test Shop", new Location( Bukkit.getWorld("world"), -182, 78, 524 ), 9);
			return true;
		}
		
		return commands.onCommand( sender, cmd, label, args );
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean isShopTag( ItemStack item ) {
		if( !itemExists(item) ) return false;
		if( item.getType() != Material.COBBLESTONE ) return false;
		String name = item.getItemMeta().getDisplayName();
		if( name == null ) return false;
		
		if( name.equals( ChatColor.GREEN+"#SHOPTAG" ) ) return true;
		return false;
	}

	//-------------------------------------------------------------------------------------------------
	private boolean itemExists( ItemStack item ) {
		if( item == null ) return false;
		if( item.getType() == Material.AIR ) {
			return false;
		}
		return true;
	}
	
	//-------------------------------------------------------------------------------------------------
	private void loadShopInventory( Player forWho, Inventory dest, Inventory source ) {
		
		for( int i = 0; i < source.getSize()-1; i++ ) {
			if( (i%9) == 8 ) continue; // don't allow tags to cross sections
			
			ItemStack item = source.getItem(i);
			if( !itemExists(item) ) continue;
			if( isShopTag( item ) ) continue;
			 
			ItemStack tag = source.getItem(i+1);
			if( !isShopTag(tag) ) continue;
			i++;
			
			ItemMeta meta = item.getItemMeta();
			ArrayList<String> lore = new ArrayList<String>();
			
			if( meta.getLore() != null ) lore.addAll( meta.getLore() );
			lore.add(" ");
			
			
			PriceTag price = new PriceTag( item, tag );
			if( !price.valid ) continue; 
			if( price.slot != null ) {
				if( price.slot >= dest.getSize() || itemExists(dest.getItem( price.slot ) ) ) {
					continue; // the desired slot is already used.
				}
			}
			
			if( !price.magictag ) {
				
				if( price.buy != null ) {
					lore.add( "@@[B:"+BUY_METAKEY_NAME+":" + (i-1)+"]" );
					if( price.buy == 0.0f ) {
						lore.add( "" + ChatColor.WHITE + ChatColor.BOLD + "Buy:" + ChatColor.GREEN + " Free" );
					} else {
						lore.add( String.format( "" + ChatColor.WHITE + ChatColor.BOLD + "Buy: " + ChatColor.GREEN + "%s", economy.format( price.buy ) ) );
					}
				}
				if( price.sell != null ) {
					String sellText = String.format( ""+ChatColor.WHITE + ChatColor.BOLD + "Sell: "+ChatColor.YELLOW + "%s", economy.format(price.sell) );
					if( item.getAmount() > 1 ) {
						sellText += String.format( " (%s ea.)", economy.format(price.sellSingle) );
						
					}
					lore.add( sellText ); 
				}

				lore.add(" ");
				
				if( price.buy != null ) {
					lore.add( ChatColor.GRAY + "Right-click to purchase." );
				}
				if( price.sell != null ) {
					lore.add( ChatColor.GRAY + "Drag your items up here") ;
					lore.add( ChatColor.GRAY + "or shift-right-click to");
					lore.add( ChatColor.GRAY + "sell them.");
				}
			} else {
				if( price.buy == null ) continue; // price needs to be set for magictag
				
				lore.add( "@@[B:"+BUY_METAKEY_NAME+":" + (i-1)+"]" );
				lore.add( "@@[T:"+ACTION_METAKEY_NAME+":" + price.action + "]" );
				
				Object newfee = MagicItems.getAPI().fireCustomAction( price.item, forWho, "getFee", price.action );
				if( newfee != null && newfee instanceof Float ) {
					price.buy = (Float)newfee;
					lore.add( String.format( "@@[T:CUSTOMFEE:%.2f]",price.buy) );
				}
				
				if( price.buy == 0.0f ) {
					//lore.add( "" + ChatColor.WHITE + ChatColor.BOLD + "Buy:" + ChatColor.GREEN + " Free" );
				} else {
					lore.add( String.format( "" + ChatColor.YELLOW + ChatColor.BOLD + "Fee: " + ChatColor.GREEN + "%s", economy.format( price.buy ) ) );
				}
				
				lore.add(" ");
				lore.add( ChatColor.GRAY + "Right-click to select." );
				
			}
			
			ItemStack shopItem = item.clone();

			meta.setLore( lore );
			shopItem.setItemMeta( meta );
			LoreMeta.initialize( shopItem );
			
			if( price.slot == null ) {
				dest.addItem( shopItem );
			} else {
				dest.setItem( price.slot, shopItem );
			}
			
			if( dest.firstEmpty() == -1 ) break;
			
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private Inventory getSourceInventory( Location location ) {
		Block block = location.getBlock();
		if( block == null ) {
			getLogger().warning( "Tried to get an inventory but the chest is missing, location=" + location.toString() );
			return null;
		}
		BlockState blockState = block.getState();
		if( !(blockState instanceof Chest) ) {
			getLogger().warning( "Tried to get an inventory but the chest is missing, location=" + location.toString() );
			return null;
		}
		return ((Chest)blockState).getInventory();
	}
	
	//-------------------------------------------------------------------------------------------------
	public void openShop( Player player, String name, Location location, int size ) {
		Inventory source = getSourceInventory( location );
		if( source == null ) return;
		
		Inventory inventory = getServer().createInventory( new ShopHolder( location ), size, name );
		loadShopInventory( player, inventory, source );
		
		player.openInventory( inventory ); 
	}
	
	//-------------------------------------------------------------------------------------------------
	private PriceTag getPriceTag( Inventory source, int slot ) {
		ItemStack check = source.getItem(slot);
		if( check == null ) return null;
		ItemStack tag = source.getItem(slot+1);
		if( tag == null ) return null;
		PriceTag price = new PriceTag( check, tag );
		if( !price.valid ) return null;
		return price;
	}
	
	//-------------------------------------------------------------------------------------------------
	private PriceTag findPriceTag( Inventory source, ItemStack item, boolean mustHaveSell, Boolean mustHaveBuy ) {
		for( int i = 0; i < source.getSize()-1; i++ ) {
			if( (i%9) == 8 ) continue; // dont allow tags to cross sections
			ItemStack check = source.getItem(i);
			if( check == null ) continue;
			if( check.isSimilar( item ) ) {
				ItemStack tag = source.getItem(i+1);
				if( isShopTag(tag) ) {
					i++;
					PriceTag price = new PriceTag( check, tag );
					if( !price.valid ) continue;
					if( mustHaveSell && price.sell == null ) continue;
					if( mustHaveBuy && price.buy == null ) continue; 
					return price;// price.sell / check.getAmount();
				}
			}
		}
		return null;
	}
	
	
	//-------------------------------------------------------------------------------------------------
	
	private void tryBuyItem( InventoryClickEvent event ) {
		ItemStack item = event.getCurrentItem();
		if( item == null ) return;
		
		Byte sourceIndex = getBuyMeta( item );
		if( sourceIndex == null ) return; 
		
		ShopHolder holder = (ShopHolder)event.getInventory().getHolder();
		Inventory source = getSourceInventory( holder.source );
		if( source == null ) return;
		
		Player player = (Player)event.getWhoClicked();
		
		PriceTag priceTag = getPriceTag( source, sourceIndex );
		
		if( priceTag == null || priceTag.buy == null ) {
			player.sendMessage( ChatColor.RED + "That item is no longer available." );
			return;
		}
		
		if( !priceTag.magictag ) {
			if( player.getInventory().firstEmpty() < 0 ) {
				player.sendMessage( ChatColor.RED + "Your inventory is full." );
				return;
			}
		} else {
			String newfee = LoreMeta.getData( item, new MetaKeyText("CUSTOMFEE") );
			if( newfee != null ) {
				priceTag.buy = Float.parseFloat(newfee);
			}
		}
		
		if( !economy.isEnabled() ) {
			player.sendMessage( "An error occurred; please try again later." );
			return;
		}
		 
		if( economy.getBalance( player ) < priceTag.buy ) {
			player.sendMessage( ChatColor.RED + "You don't have enough money." );
			return;
		}
		 
			
		if( !priceTag.magictag ) {
			
			economy.withdrawPlayer( player, priceTag.buy );
			
			player.getInventory().addItem( priceTag.item.clone() );
			int quantity = priceTag.item.getAmount();
			if( quantity == 1 ) {
				player.sendMessage( String.format( 
						ChatColor.GREEN + "You bought %s for %s" + ChatColor.GREEN + ".",
						
						priceTag.item.getType().toString(), 
						economy.format( priceTag.buy ) ) 
						);
			} else {
				player.sendMessage( String.format( 
						ChatColor.GREEN + "You bought %d %s for %s"+ ChatColor.GREEN + ".", 
						quantity, 
						priceTag.item.getType().toString(), 
						economy.format( priceTag.buy ) ) 
						);
			}
		} else {
			
			
			// fire MagicItem event!
			Object result = MagicItems.getAPI().fireCustomAction( 
					priceTag.item, player, priceTag.action );
			
			if( result instanceof NotAMagicItem ) {
				
				player.sendMessage( "An error occurred; please try again later." );
				return;
			} else if( result == null ) {
				
				// the action returns null if he can't use the option, and a fee
				// won't be charged.
				return;
			} else {
				
				if( priceTag.buy != 0.0f ) {
					economy.withdrawPlayer( player, priceTag.buy );
					player.sendMessage( 
							String.format( ChatColor.GREEN + "You spent %s" + ChatColor.GREEN + ".",
									economy.format( priceTag.buy ) ) );
				}
			}
		}
		
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("deprecation")
	private void trySellItem( InventoryClickEvent event, boolean cursor, boolean single ) {
		ItemStack item = cursor ? event.getCursor() : event.getCurrentItem();
		if( item == null ) return;
		
		ShopHolder holder = (ShopHolder)event.getInventory().getHolder();
		Inventory source = getSourceInventory( holder.source );
		if( source == null ) return;
		
		PriceTag priceTag = findPriceTag( source, item, true, false );
		
		
		Player player = (Player)event.getWhoClicked();
		
		if( priceTag == null ) {
			
			player.sendMessage( ChatColor.RED + "You can't sell that item here." );
			return;
		}
		
		float sellPrice = priceTag.sellSingle;
		int quantity = single ? 1 : 
				(cursor ? event.getCursor().getAmount() : event.getCurrentItem().getAmount());
		if( quantity < priceTag.item.getAmount() ) {
			player.sendMessage( String.format( ChatColor.RED + "You need to sell at least %d of that item.", priceTag.item.getAmount() ) );
			return;
		}
		
		float amount = sellPrice * quantity;
		EconomyResponse response = economy.depositPlayer( player, amount );
		if( response.transactionSuccess() ) {
			player.sendMessage( String.format( ChatColor.GREEN + "You sold %d %s for %s"+ ChatColor.GREEN + ".", quantity, item.getType().toString(), economy.format( amount ) ) );
			
			// remove item.
			if( item.getAmount() == quantity ) {
				if( cursor ) {
					event.setCursor( null );
				} else {
					event.setCurrentItem( null );
				}
			} else {
				item.setAmount( item.getAmount() - quantity );
				if( cursor ) {
					event.setCursor( item );
				} else {
					event.setCurrentItem( item );
				}
			}
		} else {
			player.sendMessage( ChatColor.RED + "An error occurred; please try again later." );
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@EventHandler( priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onInventoryClick( InventoryClickEvent event ) {
		 
		Inventory inv = event.getInventory();
		Bukkit.broadcastMessage( "---3" );
		Bukkit.broadcastMessage( ChatColor.AQUA + inv.getHolder().getClass().toString() );
		Bukkit.broadcastMessage( ChatColor.WHITE + event.getAction().toString() );
		Bukkit.broadcastMessage( ChatColor.GREEN + event.getClick().toString() );
		Bukkit.broadcastMessage( ""+ChatColor.BLUE + event.getHotbarButton() + "," + event.getRawSlot() + "," + event.getSlot() + "," + event.getSlotType().toString() );
		//Bukkit.broadcastMessage( ""+ChatColor.GREEN + event.getClick().toString() );
		
		if( inv.getHolder() instanceof ShopHolder ) {
			if( !(event.getWhoClicked() instanceof Player) ) {
				event.setCancelled(true);
				return;
			}
			
			if( event.getRawSlot() >= 0 && event.getRawSlot() < inv.getSize() ) {
				// clicking on upper area, interact with shop			
				
				if( event.getAction() == InventoryAction.PICKUP_HALF &&
						event.getClick() == ClickType.RIGHT ) {
					
					Bukkit.broadcastMessage( ChatColor.AQUA + "ACTION = PURCHASE ITEM." );
					tryBuyItem( event  );
					
				} else if( event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY &&
						event.getClick() == ClickType.SHIFT_RIGHT ) {
					
					//Bukkit.broadcastMessage( ChatColor.AQUA + "ACTION = PURCHASE ITEM LOTS." );
					//tryBuyItem( event, true );
					
					
				} else if( event.getAction() == InventoryAction.PLACE_ALL ) {
					Bukkit.broadcastMessage( ChatColor.AQUA + "ACTION = SELL ALL." );
					trySellItem( event, true, false );

				} else if( event.getAction() == InventoryAction.PLACE_ONE ) {
					Bukkit.broadcastMessage( ChatColor.AQUA + "ACTION = SELL ONE." );

					trySellItem( event, true, true );
					
				}
				event.setCancelled(true);
			} else { 
				
				switch( event.getAction() ) {
					case PICKUP_ALL:
					case PICKUP_HALF:
					case COLLECT_TO_CURSOR:
					case DROP_ALL_CURSOR:
					case DROP_ONE_CURSOR:
					case DROP_ONE_SLOT:
					case DROP_ALL_SLOT: 
					case PICKUP_ONE:
					case PICKUP_SOME:
					case PLACE_ALL:
					case PLACE_ONE:
					case PLACE_SOME:
					case SWAP_WITH_CURSOR:
						return;
					default:
					
				}
				
				if( event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY && 
						event.getClick() == ClickType.SHIFT_RIGHT ) {
					// sell item

					trySellItem( event, false, false );
				}

				event.setCancelled(true);
			}
			
		}
		
	}
	
	//-------------------------------------------------------------------------------------------------
	@EventHandler( priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onInventoryDrag( InventoryDragEvent event ) {

		Inventory inv = event.getInventory();
		if( inv.getHolder() instanceof ShopHolder ) {
			event.setCancelled(true);				
		}
	}
	
	//-------------------------------------------------------------------------------------------------
}
