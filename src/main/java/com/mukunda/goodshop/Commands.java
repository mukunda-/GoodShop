package com.mukunda.goodshop;

import org.bukkit.ChatColor;

import com.mukunda.cmdhandler.CommandGroup;
import com.mukunda.goodshop.commands.PricetagCommand;

public class Commands extends CommandGroup {

	public Commands() {
		super( "goodshop", "[" + ChatColor.GREEN + "GoodShop" + ChatColor.RESET + "]" );
		
		new PricetagCommand( this );
	}

}
