package com.mukunda.goodshop;

import org.bukkit.inventory.ItemStack;

import com.mukunda.loremeta.LoreMeta;

public class PriceTag {
	
//	public static enum Type {
//		NORMAL,
//		MAGICTAG
//	};
//	public Type type;
	
	public ItemStack item;
	
	public Float buy;
	public Float sell;
	public Float sellSingle = null; // filled in by user.
	public Byte slot;
	
	public boolean valid = false;
	
	public boolean magictag;
	public String action;
	
	public PriceTag( ItemStack item, ItemStack tag ) {
		// read prices from tag item
		buy = null;
		sell = null;
		try {
			String str;
			
			str = LoreMeta.getField( tag, "ACTION" );
			if( str != null ) {
				magictag = true;
				action = str;
			}
			
			str = LoreMeta.getField( tag, "BUY" );
			if( str != null ) {
				if( str.equals("FREE" ) ) {
					buy = 0.0f;
				} else {
					float cost = Float.parseFloat( str );   
					if( cost <= 0.0f ) throw new NumberFormatException();
					buy = cost;
				}
			}
			
			str = LoreMeta.getField( tag, "SELL" );
			if( str != null ) {
				float cost = Float.parseFloat( str );   
				if( cost <= 0.0f ) throw new NumberFormatException();
				sell = cost;
			}
			
			str = LoreMeta.getField( tag, "SLOT" );
			if( str != null ) {
				slot = Byte.parseByte( str );   
				if( slot < 0 ) throw new NumberFormatException();
			}
			
			/*
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
				
			}*/
		} catch( NumberFormatException e ) {
			valid = false;
			return;
		}
		if( sell == null && buy == null ) {
			valid = false;
			return;
		}
		if( sell != null && buy != null && sell > buy ) {
			// invalid price tag.
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
