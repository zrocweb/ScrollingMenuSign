package me.desht.scrollingmenusign.expector;

import me.desht.scrollingmenusign.SMSException;
import me.desht.scrollingmenusign.SMSMenu;
import me.desht.scrollingmenusign.views.SMSSignView;
import me.desht.scrollingmenusign.views.SMSView;
import me.desht.util.MiscUtil;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ExpectViewCreation extends ExpectData {
	private SMSMenu menu;
	private Location loc;
	private String arg;

	public ExpectViewCreation(SMSMenu menu, String arg) {
		this.menu = menu;
		this.arg = arg;
	}

	public Location getLoc() {
		return loc;
	}

	public void setLoc(Location loc) {
		this.loc = loc;
	}

	@Override
	public void doResponse(Player p) throws SMSException {
		SMSView view = null;
		
		if (arg.equals("-sign")) {
			view = SMSSignView.addSignToMenu(menu, getLoc());
		}

		if (view != null) 
			MiscUtil.statusMessage(p, String.format("Added %s view &e%s&- to menu &e%s&-.",
					view.getType(), view.getName(), menu.getName()));
	}
}
