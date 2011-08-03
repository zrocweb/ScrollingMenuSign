package me.desht.scrollingmenusign;

import java.util.List;

import me.desht.scrollingmenusign.ScrollingMenuSign.MenuRemoveAction;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SMSHandlerImpl implements SMSHandler {
	private ScrollingMenuSign plugin;
	
	SMSHandlerImpl(ScrollingMenuSign plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public SMSMenu createMenu(String name, String title, String owner) {
		return new SMSMenu(plugin, name, title, owner, null);
	}

	@Override
	public SMSMenu createMenu(String name, SMSMenu otherMenu, String owner) {
		return new SMSMenu(plugin, otherMenu, name, owner, null);
	}

	@Override
	public SMSMenu getMenu(String name) throws SMSNoSuchMenuException {
		return SMSMenu.getMenu(name);
	}

	@Override
	public boolean checkMenu(String name) {
		return checkMenu(name);
	}

	@Override
	public void deleteMenu(String name, MenuRemoveAction action) throws SMSNoSuchMenuException {
		SMSMenu.getMenu(name).deletePermanent(action);
	}

	@Override
	public void deleteMenu(String name) throws SMSNoSuchMenuException {
		SMSMenu.getMenu(name).deletePermanent();
	}

	@Override
	public SMSMenu getMenuAt(Location loc) throws SMSNoSuchMenuException {
		return SMSMenu.getMenuAt(loc);
	}

	@Override
	public String getMenuNameAt(Location loc) {
		return SMSMenu.getMenuNameAt(loc);
	}

	@Override
	public String getTargetedMenuSign(Player player, Boolean complain) throws SMSException {
		return SMSMenu.getTargetedMenuSign(player, complain);
	}

	@Override
	public List<SMSMenu> listMenus() {
		return SMSMenu.listMenus();
	}

	@Override
	public List<SMSMenu> listMenus(boolean isSorted) {
		return SMSMenu.listMenus(isSorted);
	}
}