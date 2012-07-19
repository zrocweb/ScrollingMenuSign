package me.desht.scrollingmenusign.commands;

import java.util.ArrayList;
import java.util.List;

import me.desht.dhutils.MessagePager;
import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.commands.AbstractCommand;
import me.desht.scrollingmenusign.SMSHandler;
import me.desht.scrollingmenusign.SMSMenu;
import me.desht.scrollingmenusign.ScrollingMenuSign;
import me.desht.scrollingmenusign.views.SMSView;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class ListMenusCommand extends AbstractCommand {

	public ListMenusCommand() {
		super("sms l", 0, 1);
		setPermissionNode("scrollingmenusign.commands.list");
		setUsage("/sms list [<menu-name>]");
	}

	@Override
	public boolean execute(Plugin plugin, CommandSender sender, String[] args) {		
		
		MessagePager pager = MessagePager.getPager(sender).clear();
		
		SMSHandler handler = ((ScrollingMenuSign)plugin).getHandler();
		if (args.length > 0) {
			SMSMenu menu = handler.getMenu(args[0]);
			listMenu(pager, menu, true);
		} else {
			List<SMSMenu> menus = handler.listMenus(true);
			if (menus.size() == 0) {
				MiscUtil.statusMessage(sender, "No menu signs exist.");
			} else {
				pager.add("Use &f/sms list <menu-name>&- to see all the views for a menu");
				for (SMSMenu menu : menus) {
					listMenu(pager, menu, false);
				}
			}
		}
		pager.showPage();
		
		return true;
	}

	private void listMenu(MessagePager pager, SMSMenu menu, boolean listViews) {
		List<SMSView> views = SMSView.getViewsForMenu(menu, true);

		ChatColor itemCol = menu.getItemCount() > 0 ? ChatColor.YELLOW : ChatColor.RED;
		ChatColor viewCol = views.size() > 0 ? ChatColor.YELLOW : ChatColor.GOLD;
		String ms = menu.getItemCount() == 1 ? "" : "s";
		String vs = views.size() == 1 ? "" : "s";
		String message = String.format("&4* &f%s \"%s&f\" %s[%d item%s] %s[%d view%s]",
		                               menu.getName(), menu.getTitle(), itemCol, 
		                               menu.getItemCount(), ms,
		                               viewCol.toString(), views.size(), vs);
		List<String> lines = new ArrayList<String>();
		lines.add(message);
		if (listViews) {
			for (SMSView v : views) {
				lines.add(String.format("  &5*&- &f%s&-: &e%s", v.getName(), v.toString()));
			}
		}
		pager.add(lines);
	}
}
