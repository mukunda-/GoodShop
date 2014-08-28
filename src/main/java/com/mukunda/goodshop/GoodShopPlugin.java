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
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
 
//-------------------------------------------------------------------------------------------------
public class GoodShopPlugin extends JavaPlugin implements Listener {
	
    public static Economy economy = null;
	
	private static final String BUY_TAG = ChatColor.AQUA + "BUY:";
	private static final String SELL_TAG = ChatColor.YELLOW + "SELL:";
	
	private static final String BUY_META = ChatColor.COLOR_CHAR + "\u0117";
	
	private static String formatBuyMeta( int index ) {
		if( index > 99 || index < 0 ) index = 99;
		return BUY_META + ChatColor.COLOR_CHAR + (index / 10) + ChatColor.COLOR_CHAR + (index % 10);
	}
	
	private Integer getBuyMeta( ItemStack item ) {
		List<String> lore;
		lore = item.getItemMeta().getLore();
		if( lore == null ) return null;
		for( String str: lore ) {
			int index = str.indexOf( BUY_META );
			if( index == -1 ) continue;
			return Character.getNumericValue(str.charAt( index + 3 )) * 10 + Character.getNumericValue(str.charAt( index + 5 ));
			
		}
		return null;
	}
	
	//-------------------------------------------------------------------------------------------------
	private class ShopHolder implements InventoryHolder {
		
		public Location source;
		
		public ShopHolder( Location source ) {
			this.source = source;
		}

		public Inventory getInventory() {
			
			return null;
		}
		
	}
	
	public class PriceTag {
		public ItemStack item;
		public Float buy;
		public Float sell;
		public Float sellSingle = null; // filled in by user.
		public boolean valid = false;
		
		public PriceTag( ItemStack item, ItemStack tag ) {
			// read prices from tag item
			buy = null;
			sell = null;
			try {
				List<String> tagData = tag.getItemMeta().getLore();
				for( String s: tagData ) {
					if( s.startsWith(BUY_TAG) ) {
						String str = s.substring( BUY_TAG.length() );
						if( str.equals("FREE" ) ) {
							buy = 0.0f;
							continue;
						}
						
						float cost = Float.parseFloat( str );   
						
						if( cost <= 0.0f ) continue;
						buy = cost; 
						
					} else if( s.startsWith(SELL_TAG) ) {
						float cost = Float.parseFloat( s.substring( SELL_TAG.length() ) );
						if( cost <= 0.0f ) continue;

						sell = cost; 
					}
					
				}
			} catch( NumberFormatException e ) {
				buy = null;
				sell = null;
				valid = false;
				return;
			}
			if( sell == null && buy == null ) {
				valid = false;
				return;
			}
			if( sell != null && buy != null && sell > buy ) {
				// invalid price tag.
				buy = null;
				sell = null;
				valid = false;
				return;
			}
			
			this.item = item;
			
			if( sell != null ) {
				sellSingle = sell / item.getAmount();
			}
			valid = true;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean setupEconomy() {
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
		getServer().getPluginManager().registerEvents( this, this );
		
	}
	
	//-------------------------------------------------------------------------------------------------
	public void onDisable() {
		
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("deprecation")
	public void giveStoreTag( Player player, Float buyprice, Float sellprice ) {
		//Inventory test = getServer().createInventory( null, 9 , "Magic Shop");
		//player.openInventory( test );
		ItemStack item = new ItemStack(Material.COBBLESTONE);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName( ChatColor.GREEN+"#SHOPTAG" );
		ArrayList<String> lore = new ArrayList<String>();
		if( buyprice != null ) {
			if( buyprice != 0.0f ) {
				lore.add( String.format( BUY_TAG + "%.2f", buyprice ) );
			} else {
				lore.add( String.format( BUY_TAG + "FREE" ) );
			}
		}
		if( sellprice != null ) {
			lore.add( String.format( SELL_TAG + "%.2f", sellprice ) );
		}
		
		meta.setLore( lore );
		item.setItemMeta( meta );
		player.getInventory().addItem( item );
		player.updateInventory();
		
		player.sendMessage( ChatColor.GREEN + "Here you go.");
	}
	
	//-------------------------------------------------------------------------------------------------
	public boolean onCommand( CommandSender sender, Command cmd, String label, String[] args ) {
		if( cmd.getName().equalsIgnoreCase("test2") ) {
			
			openShop( (Player)sender, "Test Shop", new Location( Bukkit.getWorld("world"), -182, 78, 524 ), 9);
			return true;
		} else if( cmd.getName().equalsIgnoreCase( "shoptag" ) ) {
			if( !(sender instanceof Player) ) { 
				sender.sendMessage( "This is a player only command." );
				return true;
			}
			Player player = (Player)sender;
			
			if( args.length < 1 ) {
				sender.sendMessage( "Usage: /shoptag <buyprice> <sellprice>" );
				sender.sendMessage( "Buy price is how much the player spends when buying the item. Put 0 to make it not buyable, and \"FREE\" to make it FREE." );
				sender.sendMessage( "Sell price is how much money the player gets when selling the item. Put 0 to make it not sellable." );
				return true;
			}
			
			Float buyprice, sellprice;
			
			try {
				if( args[0].equalsIgnoreCase("FREE") ) {
					buyprice = 0.0f;
				} else {
					buyprice = Float.parseFloat( args[0] );
					if( buyprice < 0.0f ) {
						throw new NumberFormatException();
					} else if( buyprice == 0.0f ) {
						buyprice = null;
					}
				}
				
				if( args.length >= 2 ) {
					sellprice = Float.parseFloat( args[1] );
					if( sellprice < 0.0f ) {
						throw new NumberFormatException();
					} else if( sellprice == 0.0f ) {
						sellprice = null;
					}
				} else {
					sellprice = null;
				}

				// check if the tag has either buy or sell and not neither
				// and dont let them do something stupid.
				if( buyprice != null && sellprice != null && sellprice > buyprice ) {
					throw new NumberFormatException();
				}

				if( ((buyprice != null && buyprice == 0.0f) && sellprice != null) ||
					(buyprice == null && sellprice == null)	) {
					
					throw new NumberFormatException();
				}
				
			} catch( NumberFormatException e ) {
				sender.sendMessage( "Invalid argument(s)." );
				return true;
			}
			
			giveStoreTag( player, buyprice, sellprice );
			return true;
		}
		return false;
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean isShopTag( ItemStack item ) {
		if( item == null || item.getType() == Material.AIR ) return false;
		if( item.getType() != Material.COBBLESTONE ) return false;
		String name = item.getItemMeta().getDisplayName();
		if( name == null ) return false;
		
		if( name.equals( ChatColor.GREEN+"#SHOPTAG" ) ) return true;
		return false;
	}
	
	
	
	//-------------------------------------------------------------------------------------------------
	private void loadShopInventory( Inventory dest, Inventory source ) {
		
		int setIndex = 0;
		for( int i = 0; i < source.getSize()-1; i++ ) {
			if( (i%9) == 8 ) continue; // don't allow tags to cross sections
			
			ItemStack item = source.getItem(i);
			if( item == null || item.getType() == Material.AIR ) continue;
			if( isShopTag( item ) ) continue;
			 
			ItemStack tag = source.getItem(i+1);
			if( !isShopTag(tag) ) continue;
			i++;
			
			ItemMeta meta = item.getItemMeta();
			ArrayList<String> lore = new ArrayList<String>();
			
			if( meta.getLore() != null ) lore.addAll( meta.getLore() );
			lore.add(" ");
			
			String buyMeta = formatBuyMeta( i-1 );
			
			PriceTag price = new PriceTag( item, tag );
			if( !price.valid ) continue;
			
			if( price.buy != null ) {
				if( price.buy == 0.0f ) {
					lore.add( buyMeta+ChatColor.WHITE + ChatColor.BOLD + "Buy:" + ChatColor.GREEN + " Free" );
				} else {
					lore.add( String.format( buyMeta+ChatColor.WHITE + ChatColor.BOLD + "Buy: " + ChatColor.GREEN + "%s", economy.format( price.buy ) ) );
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
			
			ItemStack shopItem = item.clone();

			meta.setLore( lore );
			shopItem.setItemMeta( meta );
			
			dest.setItem( setIndex++, shopItem );
			if( setIndex == dest.getSize() ) break;
			
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
		
		Inventory inventory = getServer().createInventory( new ShopHolder( location ), size, name );
		loadShopInventory( inventory, source );
		
		player.openInventory( inventory ); 
	}
	
	private PriceTag getPriceTag( Inventory source, int slot ) {
		ItemStack check = source.getItem(slot);
		if( check == null ) return null;
		ItemStack tag = source.getItem(slot+1);
		if( tag == null ) return null;
		PriceTag price = new PriceTag( check, tag );
		if( !price.valid ) return null;
		return price;
	}
	
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
		
		Integer sourceIndex = getBuyMeta( item );
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
		
		if( player.getInventory().firstEmpty() < 0 ) {
			player.sendMessage( ChatColor.RED + "Your inventory is full." );
			return;
		}
		 
		
		if( economy.getBalance( player ) < priceTag.buy ) {
			player.sendMessage( ChatColor.RED + "You don't have enough money." );
			return;
		}
		  
		EconomyResponse response = economy.withdrawPlayer( player, priceTag.buy );
		if( response.transactionSuccess() ) {
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
			player.sendMessage( "An error occurred; please try again later." );
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
