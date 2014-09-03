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

public class MagictagCommand extends CommandHandler {

	//-------------------------------------------------------------------------------------------------
	public MagictagCommand( CommandGroup parent ) {
		super( parent, "magictag", 1, true );
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public void printSyntax() {
		reply( "/goodshop magictag <action> [fee] [slot]" );
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public void printUsage() {
		reply( "/goodshop magictag <action> [fee] [slot]" );
		reply( "Creates a tag for a MagicItem that is used when the player \"purchases\" it. Fee may be 0 for a normal option they can click on." );
		reply( "This feature is mainly used to create options that you can select in the inventory dialog." );
		reply( "The optional [slot] parameter specifies which slot in the inventory the option should appear." );
	}
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public void run( String[] args ) {

		Player player = getPlayer();
		String action;
		Float fee = 0.0f;
		Integer slot;
		
		try {
			action = args[1].trim();
			if( action.isEmpty() ) throw new NumberFormatException();
			
			if( args.length >= 3 ) {
				if( args[2].equalsIgnoreCase("free") ) {
					fee = 0.0f;
				} else {
					fee = Float.parseFloat( args[2] );
					if( fee < 0.0f ) {
						throw new NumberFormatException();
					}
				}
			}
			  
			if( args.length >= 4 ) {
				slot = Integer.parseInt(args[3] );
				if( slot < 0 || slot > 127 ) 
					throw new NumberFormatException();
			} else {
				slot = null;
			}  
			
		} catch( NumberFormatException e ) {
			reply( "Invalid argument(s)." );
			return;
		}
		
		PricetagCommand.giveStoreTag( player, fee, null, slot, action );
	}

}
