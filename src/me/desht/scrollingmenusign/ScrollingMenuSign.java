package me.desht.scrollingmenusign;

import java.io.File;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.*;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class ScrollingMenuSign extends JavaPlugin {
	public static enum MenuRemoveAction { DESTROY_SIGN, BLANK_SIGN, DO_NOTHING };
	public Logger logger = Logger.getLogger("Minecraft");
	public PermissionHandler permissionHandler;
	public static PluginDescriptionFile description;
	public static final String directory = "plugins" + File.separator + "ScrollingMenuSign";
	
	private final SMSPlayerListener signListener = new SMSPlayerListener(this);
	private final SMSBlockListener blockListener = new SMSBlockListener(this);
	private final SMSCommandExecutor commandExecutor = new SMSCommandExecutor(this);
	private final SMSPersistence persistence = new SMSPersistence(this);

	private HashMap<Location, String> menuLocations = new HashMap<Location, String>();
	private HashMap<String, SMSMenu> menus = new HashMap<String, SMSMenu>();
	
	@Override
	public void onDisable() {
		save();
		logger.info(description.getName() + " version " + description.getVersion() + " is disabled!" );
	}

	@Override
	public void onEnable() {
		description = this.getDescription();

		setupPermissions();
		
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_INTERACT, signListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_ITEM_HELD, signListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_DAMAGE, blockListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_PHYSICS, blockListener, Event.Priority.Normal, this);

		getCommand("sms").setExecutor(commandExecutor);
		
		if (!getDataFolder().exists()) getDataFolder().mkdir();

		logger.info(description.getName() + " version " + description.getVersion() + " is enabled!" );
		
		// delayed loading of saved menu files to ensure all worlds are loaded first
		if (getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				load();
			}
		})==-1) {
			log(Level.WARNING, "Couldn't schedule menu loading - multiworld support might not work.");
			load();
		}
	}

	private void setupPermissions() {
		Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");

	      if (permissionHandler == null) {
	          if (permissionsPlugin != null) {
	              permissionHandler = ((Permissions) permissionsPlugin).getHandler();
	              log(Level.INFO, "Permissions detected");
	          } else {
	              log(Level.INFO, "Permissions not detected, using ops");
	          }
	      }
	}

	public Boolean isAllowedTo(Player player, String node) {
		// if Permissions is in force, then it overrides op status
		if (permissionHandler != null) {
			return permissionHandler.has(player, node);
		} else {
			return player.isOp();
		}
	}
	
	public Boolean isAllowedTo(Player player, String node, Boolean okNotOp) {
		// if Permissions is in force, then it overrides op status
		if (permissionHandler != null) {
			return permissionHandler.has(player, node);
		} else {
			return okNotOp ? true : player.isOp();
		}
	}
	
	public void addMenu(String menuName, SMSMenu menu, Boolean updateSign) {
		menus.put(menuName, menu);
		menuLocations.put(menu.getLocation(), menuName);
		if (updateSign) {
			menu.updateSign();
		}
	}
	
	public void removeMenu(String menuName, MenuRemoveAction action) {
		Sign sign = getMenu(menuName).getSign();
		if (sign != null) {
			Location loc = sign.getBlock().getLocation();
			doRemoveMenu(menuName, loc, action);
		} else {
			log(Level.SEVERE, "remove menu: sign for '" + menuName + "' seems to have disappeared!");
		}
	}
	
	public void removeMenu(Location loc, MenuRemoveAction action) {
		String menuName = getMenuName(loc);
		doRemoveMenu(menuName, loc, action);
	}
	
	private void doRemoveMenu(String menuName, Location loc, MenuRemoveAction action) {
		if (action == MenuRemoveAction.DESTROY_SIGN) {
			loc.getBlock().setTypeId(0);
		} else if (action == MenuRemoveAction.BLANK_SIGN) {
			getMenu(menuName).blankSign();
		}
		menuLocations.remove(loc);
		menus.remove(menuName);
	}
	
	public HashMap<String, SMSMenu> getMenus() {
		return menus;
	}
	
	public SMSMenu getMenu(String menuName) {	
		return menus.get(menuName);
	}
	
	public String getMenuName(Location loc) {
		return menuLocations.get(loc);
	}

	public void load() {
		persistence.load();
	}
	
	public void save() {
		persistence.save();
	}

	public void status_message(Player player, String string) {
		player.sendMessage(ChatColor.AQUA + string);
	}

	public void error_message(Player player, String string) {
		player.sendMessage(ChatColor.RED + string);		
	}
	
	public void log(Level level, String message) {
		String logMsg = this.getDescription().getName() + ": " + message;
		logger.log(level, logMsg);
    }

	public String parseColourSpec(Player player, String spec) {
		if (isAllowedTo(player, "scrollingmenusign.coloursigns") || 
				isAllowedTo(player, "scrollingmenusign.colorsigns")) {
			String res = spec.replaceAll("&(?<!&&)(?=[0-9a-fA-F])", "\u00A7");
			return res.replace("&&", "&");
		} else {
			return spec;
		}		
	}
	
	// Return the name of the menu sign that the player is looking at, if any
	public String getTargetedMenuSign(Player player, Boolean complain) {
		Block b = player.getTargetBlock(null, 3);
		if (b.getType() != Material.SIGN_POST && b.getType() != Material.WALL_SIGN) {
			if (complain) error_message(player, "You are not looking at a sign");
			return null;
		}
		String name = getMenuName(b.getLocation());
		if (name == null && complain)
			error_message(player, "There is no menu associated with that sign.");
		return name;
	} 
}
