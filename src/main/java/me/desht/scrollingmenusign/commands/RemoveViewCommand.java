package me.desht.scrollingmenusign.commands;

import me.desht.scrollingmenusign.SMSException;
import me.desht.scrollingmenusign.ScrollingMenuSign;
import me.desht.scrollingmenusign.util.MiscUtil;
import me.desht.scrollingmenusign.util.PermissionsUtils;
import me.desht.scrollingmenusign.views.SMSMapView;
import me.desht.scrollingmenusign.views.SMSView;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class RemoveViewCommand extends AbstractCommand {

	public RemoveViewCommand() {
		super("sms b", 0, 2);
		setPermissionNode("scrollingmenusign.commands.break");
		setUsage(new String[] {
				"/sms break",
				"/sms break -loc <x,y,z,world>",
				"/sms break -view <view-name>"
		});
	}

	@Override
	public boolean execute(ScrollingMenuSign plugin, Player player, String[] args) throws SMSException {
		SMSView view = null;

		if (args.length == 2 && args[0].equals("-view")) {
			// detaching a view by view name
			view = SMSView.getView(args[1]);
		} else if (args.length == 2 && args[0].equals("-loc")) {
			// detaching a view by location
			try {
				view = SMSView.getViewForLocation(MiscUtil.parseLocation(args[0], player));
			} catch (IllegalArgumentException e) {
				throw new SMSException(e.getMessage());
			}
			view = SMSView.getViewForLocation(MiscUtil.parseLocation(args[0], player));
		} else if (player != null && (view = SMSMapView.getHeldMapView(player)) != null) {
			// detaching a map view - nothing else to check here
		} else if (args.length == 0) {
			// detaching a view that the player is looking at?
			notFromConsole(player);
			try {
				Block b = player.getTargetBlock(null, 3);
				view = SMSView.getViewForLocation(b.getLocation());
			} catch (IllegalStateException e) {
				// ignore
			}
		}

		if (view == null) {
			throw new SMSException("No suitable view found to remove.");
		} else {
			PermissionsUtils.requirePerms(player, "scrollingmenusign.use." + view.getType());
			view.deletePermanent();
			MiscUtil.statusMessage(player, String.format("Removed &9%s&- view &e%s&- from menu &e%s&-.",
			                                             view.getType(), view.getName(), view.getMenu().getName()));
		}

		return true;
	}
}
