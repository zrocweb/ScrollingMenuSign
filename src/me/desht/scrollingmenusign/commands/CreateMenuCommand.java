package me.desht.scrollingmenusign.commands;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import me.desht.scrollingmenusign.SMSException;
import me.desht.scrollingmenusign.SMSHandler;
import me.desht.scrollingmenusign.SMSMenu;
import me.desht.scrollingmenusign.SMSUtils;
import me.desht.scrollingmenusign.ScrollingMenuSign;

public class CreateMenuCommand extends AbstractCommand {

	CreateMenuCommand() {
		super("sms c", 1, 1);
		setPermissionNode("scrollingmenusign.commands.create");
		setUsage(new String[] { 
				"Usage: /sms create <menu> <title>",
				"       /sms create <menu> from <other-menu>",
		});
	}

	@Override
	public boolean execute(ScrollingMenuSign plugin, Player player, String[] args) throws SMSException {
		String menuName = args[0];
		
		SMSHandler handler = plugin.getHandler();
				
		if (handler.checkMenu(menuName)) {
			throw new SMSException("A menu called '" + menuName + "' already exists.");
		}

		Location loc = null;
		String owner = "&console";	// dummy owner if menu created from console

		if (player != null) {
			Block b = player.getTargetBlock(null, 3);
			if (b.getType() == Material.SIGN_POST || b.getType() == Material.WALL_SIGN) {
				if (plugin.getHandler().getMenuNameAt(b.getLocation()) != null) {
					throw new SMSException("There is already a menu attached to that sign.");
				}
				owner = player.getName();
				loc = b.getLocation();
			}			
		}

		SMSMenu menu = null;
		if (args.length == 3 && args[1].equals("from")) {
			SMSMenu otherMenu = plugin.getHandler().getMenu(args[2]);
			menu = handler.createMenu(menuName, otherMenu, owner);
		} else if (args.length >= 2) {
			String menuTitle = SMSUtils.parseColourSpec(player, CommandManager.combine(args, 1));
			menu = handler.createMenu(menuName, menuTitle, owner);
		}
		menu.addSign(loc, true);
		SMSUtils.statusMessage(player, "Created new menu &e" + menuName + "&- " +
				(loc == null ? " with no signs" : " with sign @ &f" + SMSUtils.formatLocation(loc)));
		
		return true;
	}

}
