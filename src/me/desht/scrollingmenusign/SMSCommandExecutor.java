package me.desht.scrollingmenusign;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import me.desht.scrollingmenusign.ScrollingMenuSign.MenuRemoveAction;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SMSCommandExecutor implements CommandExecutor {
	private ScrollingMenuSign plugin;
	private ArrayList<String> messageBuffer;
	private static int pageSize = 16;
	
	public SMSCommandExecutor(ScrollingMenuSign plugin) {
		this.plugin = plugin;
		messageBuffer = new ArrayList<String>();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}
		
    	if (label.equalsIgnoreCase("sms")) {
    		if (args.length == 0) {
    			return false;
    		}
    		if (!plugin.isAllowedTo(player, "scrollingmenusign.commands." + args[0])) {
    			plugin.error_message(player, "You are not allowed to do that.");
    			return true;
    		}
    		if (args[0].equalsIgnoreCase("create")) {
    			createSMSMenu(player, args);
    		} else if (args[0].equalsIgnoreCase("sync")) {
    			syncSMSSign(player, args);
    		} else if (args[0].equalsIgnoreCase("break")) {
    			breakSMSSign(player, args);
    		} else if (args[0].equalsIgnoreCase("delete")) {
    			deleteSMSMenu(player, args);
    		} else if (args[0].equalsIgnoreCase("list")) {
    			listSMSMenus(player, args);
    		} else if (args[0].equalsIgnoreCase("show")) {
    			showSMSMenu(player, args);
    		} else if (args[0].equalsIgnoreCase("add")) {
    			addSMSItem(player, args);
    		} else if (args[0].equalsIgnoreCase("remove")) {
    			removeSMSItem(player, args);
    		} else if (args[0].equalsIgnoreCase("save")) {
    			saveSigns(player, args);
    		} else if (args[0].equalsIgnoreCase("reload")) {
    			reload(player, args);
    		} else if (args[0].equalsIgnoreCase("getcfg")) {
    			getConfig(player, args);
    		} else if (args[0].equalsIgnoreCase("setcfg")) {
    			setConfig(player, args);
    		} else if (args[0].equalsIgnoreCase("title")) {
    			setMenuTitle(player, args);
    		} else if (args[0].equalsIgnoreCase("page") && args.length > 1) {
    			pagedDisplay(player, args);
    		} else {
    			return false;
    		}
    	}
		return true;
	}

	private void createSMSMenu(Player player, String[] args) {
		if (onConsole(player)) return;
		
		Block b = player.getTargetBlock(null, 3);
		if (args.length < 2) {
			plugin.error_message(player, "Usage: sms create <menu-name> <title>");
			plugin.error_message(player, "   or: sms create <menu-name> from <other-menu-name>");
			return;
		}
		String menuName = args[1];
		
		if (b.getType() != Material.SIGN_POST && b.getType() != Material.WALL_SIGN) {
			plugin.error_message(player, "You are not looking at a sign");
			return;
		}
		if (plugin.getMenu(menuName) != null) {
			plugin.error_message(player, "A menu called '" + menuName + "' already exists.");
			return;
		}
		if (plugin.getMenuName(b.getLocation()) != null) {
			plugin.error_message(player, "There is already a menu attached to that sign.");
			return;
		}
		SMSMenu menu = null;
		if (args.length == 4 && args[2].equals("from")) {
			SMSMenu otherMenu = plugin.getMenu(args[3]);
			if (otherMenu == null) {
				plugin.error_message(player, "No such menu '" + args[3] + "'");
				return;
			}
			menu = new SMSMenu(otherMenu, menuName, player.getName(), b.getLocation());
		} else if (args.length >= 3) {
			String menuTitle = plugin.parseColourSpec(player, combine(args, 2));
			menu = new SMSMenu(menuName, menuTitle, player.getName(), b.getLocation());
		}
		plugin.addMenu(menuName, menu, true);
		plugin.status_message(player, "Added new scrolling menu: " + menuName);
	}

	private void deleteSMSMenu(Player player, String[] args) {
		String menuName = null;
		if (args.length >= 2) {
			menuName = args[1];
			if (plugin.getMenu(menuName) == null) {
				plugin.error_message(player, "Unknown menu name: " + menuName);
				return;
			}
			plugin.removeMenu(menuName, ScrollingMenuSign.MenuRemoveAction.BLANK_SIGN);
		} else {
			if (onConsole(player)) return;
			menuName = plugin.getTargetedMenuSign(player, true);
			if (menuName != null) {
				plugin.removeMenu(menuName, ScrollingMenuSign.MenuRemoveAction.BLANK_SIGN);
			} else {
				plugin.error_message(player, "You are not looking at a sign.");
				return;
			}
		}
		plugin.status_message(player, "Deleted scrolling menu: " + menuName);
	}

	private void breakSMSSign(Player player, String[] args) {
		if (onConsole(player)) return;
		
		String menuName = plugin.getTargetedMenuSign(player, true);
		if (menuName == null) {
			plugin.error_message(player, "You are not looking at a sign.");
			return;
		}
		Block b = player.getTargetBlock(null, 3);
		plugin.removeMenu(b.getLocation(), MenuRemoveAction.BLANK_SIGN);
		plugin.status_message(player, "Removed sign from scrolling menu: " + menuName);
	}

	private void syncSMSSign(Player player, String[] args) {
		if (onConsole(player)) return;
		
		if (args.length < 2) {
			plugin.error_message(player, "Usage: sms sync <menu-name>");
			return;
		}

		Block b = player.getTargetBlock(null, 3);		
		if (b.getType() != Material.SIGN_POST && b.getType() != Material.WALL_SIGN) {
			plugin.error_message(player, "You are not looking at a sign.");
			return;
		}
		String existingMenu = plugin.getMenuName(b.getLocation());
		if (existingMenu != null) {
			plugin.error_message(player, "That sign already belongs to menu '" + existingMenu + "'.");
			return;
		}
		String menuName = args[1];
		plugin.syncMenu(menuName, b.getLocation());
		plugin.status_message(player, "Added sign to scrolling menu: " + menuName);
	}

	private void listSMSMenus(Player player, String[] args) {
		HashMap<String, SMSMenu> menus = plugin.getMenus();
	
		if (menus.size() == 0) {
			plugin.status_message(player, "No menu signs exist.");
			return;
		}
		
		Iterator<String> iter = menus.keySet().iterator();
		messageBuffer.clear();
		while (iter.hasNext()) {
			String k = iter.next();
			SMSMenu menu = menus.get(k);
			Map<Location,Integer> locs = menu.getLocations();
			ChatColor sCol = locs.size() > 0 ? ChatColor.YELLOW : ChatColor.RED;
			String message = ChatColor.YELLOW + k +
				ChatColor.GREEN + " \"" + menu.getTitle() + ChatColor.GREEN + "\"" +
				ChatColor.YELLOW + " [" + menu.getNumItems() + " items]" +
				sCol + " [" + locs.size() + " signs]";
			messageBuffer.add(message);
			for (Location loc: locs.keySet()) {
				messageBuffer.add(" - " + formatLoc(loc));
			}
		}
		pagedDisplay(player, 1);
	}

	private void showSMSMenu(Player player, String[] args) {
		String menuName;
		SMSMenu menu;
		if (args.length >= 2) {
			menuName = args[1];
		} else {
			if (onConsole(player)) return;
			menuName = plugin.getTargetedMenuSign(player, true);
			if (menuName == null)
				return;
		}
		menu = plugin.getMenu(menuName);
		if (menu == null) {
			plugin.error_message(player, "Unknown menu name: " + menuName);
			return;
		}
		messageBuffer.clear();
		messageBuffer.add(ChatColor.YELLOW + "Menu '" + menuName + "': title '" + menu.getTitle() + "'");
		ArrayList<SMSMenuItem> items = menu.getItems();
		int n = 1;
		for (SMSMenuItem item : items) {
			String s = String.format(ChatColor.YELLOW + "%2d)" +
					ChatColor.WHITE + " %s " + ChatColor.WHITE + "[%s] \"%s\"",
					n, item.getLabel(), item.getCommand(), item.getMessage());
			n++;
			messageBuffer.add(s);
		}
		pagedDisplay(player, 1);
	}

	private void setMenuTitle(Player player, String[] args) {
		if (args.length < 3) {
			plugin.error_message(player, "Usage: /sms title <menu-name> <new-title>");
			return;
		}
		plugin.setTitle(player, args[1], combine(args, 2));
	}

	private void setConfig(Player player, String[] args) {
		if (args.length < 3) {
			plugin.error_message(player, "Usage: /sms setcfg <key> <value>");
			return;
		}
		plugin.setConfigItem(player, args[1], args[2]);
	}

	private void getConfig(Player player, String[] args) {
		messageBuffer.clear();
		if (args.length < 2) {
			for (String line : plugin.getConfigList()) {
				messageBuffer.add(line);
			}
			pagedDisplay(player, 1);
		} else {
			String res = plugin.getConfiguration().getString(args[1]);
			if (res != null) {
				plugin.status_message(player, args[1] + " = " + res);
			} else {
				plugin.error_message(player, "No such config item " + args[1]);
			}
		}
	}

	private void addSMSItem(Player player, String[] args) {	
		if (args.length < 3) {
			plugin.error_message(player, "Usage: /sms add <menu-name> <menu-entry>");
			return;
		}
			
		String menuName = args[1];
		String rest = combine(args, 2);
		String sep = plugin.getConfiguration().getString("sms.menuitem_separator", "\\|");
		String[] entry_args = rest.split(sep);
		
		if (entry_args.length < 2) {
			plugin.error_message(player, "menu-entry must include at least entry label & command");
			return;
		}
		
		SMSMenu menu = plugin.getMenu(menuName);
		if (menu == null) {
			plugin.error_message(player, "Unknown menu name: " + menuName);
			return;
		}
		
		String msg = "";
		if (entry_args.length >= 3) {
			msg = entry_args[2];
		}
		String label = plugin.parseColourSpec(player, entry_args[0]);
		menu.add(label, entry_args[1], msg);
		menu.updateSigns();
		plugin.status_message(player, "Menu entry [" + entry_args[0] + "] added to: " + menuName);
	}

	private void removeSMSItem(Player player, String[] args) {
		if (args.length < 3) {
			plugin.error_message(player, "Usage: /sms remove <menu-name> <item-index>");
			return;
		}
		String menuName = args[1];
		int index;
		try {
			index = Integer.parseInt(args[2]);
		} catch (NumberFormatException e) {
			plugin.error_message(player, "Item index must be numeric");
			return;
		}
		
		SMSMenu menu = plugin.getMenu(menuName);
		if (menu == null) {
			plugin.error_message(player, "Unknown menu name: " + menuName);
			return;
		}
		menu.remove(index);
		menu.updateSigns();
		plugin.status_message(player, "Menu entry #" + index + " removed from: " + menuName);
	}

	private void saveSigns(Player player, String[] args) {
		plugin.save();
		plugin.status_message(player, "Scrolling menu signs have been saved.");
	}

	private void reload(Player player, String[] args) {
		plugin.getConfiguration().load();
		plugin.load();
		plugin.status_message(player, "Scrolling menu signs have been reloaded.");
	}

	private String formatLoc(Location loc) {
		StringBuilder str = new StringBuilder(ChatColor.WHITE + "@ " +
			loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + " " +
			loc.getWorld().getName());
		Block b = plugin.getServer().getWorld(loc.getWorld().getName()).getBlockAt(loc);
		if (b.getType() != Material.SIGN_POST && b.getType() != Material.WALL_SIGN) {
			str.append(ChatColor.RED + " [ NO SIGN ]");
		}
		return str.toString();
	}

	private void pagedDisplay(Player player, String[] args) {
		int pageNum = 1;
		try {
			pageNum = Integer.parseInt(args[1]);
			pagedDisplay(player, pageNum);
		} catch (NumberFormatException e) {
			plugin.error_message(player, "invalid argument '" + args[1] + "'");
		}
	}
	
	private void pagedDisplay(Player player, int pageNum) {
		if (player != null) {
			// pretty paged display
			int nMessages = messageBuffer.size();
			plugin.status_message(player, ChatColor.GREEN + "" +  nMessages +
					" lines (page " + pageNum + "/" + (nMessages / pageSize + 1) + ")");
			plugin.status_message(player, ChatColor.GREEN + "---------------");
			for (int i = (pageNum -1) * pageSize; i < nMessages && i < pageNum * pageSize; i++) {
				plugin.status_message(player, messageBuffer.get(i));
			}
			plugin.status_message(player, ChatColor.GREEN + "---------------");
			String footer = (nMessages > pageSize * pageNum) ? "Use /sms page [page#] to see more" : "";
			plugin.status_message(player, ChatColor.GREEN + footer);
		} else {
			// just dump the whole message buffer to the console
			for (String s: messageBuffer) {
				plugin.status_message(null, plugin.deColourise(s));
			}
		}
	}
	
	private static String combine(String[] args, int idx) {
		return combine(args, idx, args.length - 1);
	}
	
	private static String combine(String[] args, int idx1, int idx2) {
		StringBuilder result = new StringBuilder();
		for (int i = idx1; i <= idx2; i++) {
			result.append(args[i]);
			if (i < idx2) {
				result.append(" ");
			}
		}
		return result.toString();
	}

	private boolean onConsole(Player player) {
		if (player == null) {
			plugin.error_message(player, "This command cannot be run from the console.");
			return true;
		} else {
			return false;
		}
	}
}
