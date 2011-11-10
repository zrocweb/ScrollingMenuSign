package me.desht.scrollingmenusign;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;

import me.desht.scrollingmenusign.enums.SMSMenuAction;
import me.desht.scrollingmenusign.spout.SpoutUtils;
import me.desht.scrollingmenusign.util.MiscUtil;
import me.desht.scrollingmenusign.views.SMSMapView;
import me.desht.scrollingmenusign.views.SMSView;

import org.bukkit.configuration.Configuration;

public class SMSConfig {
	private static File pluginDir;
	private static File dataDir, menusDir, viewsDir, macrosDir;
	private static File commandFile;
	
	private static final String dataDirName = "data";
	private static final String menusDirName = "menus";
	private static final String viewsDirName = "views";
	private static final String macrosDirName = "macros";
	private static final String commandFileName = "commands.yml";

	static void init(ScrollingMenuSign plugin) {
		setupDirectoryStructure();
		initConfigFile();
	}

	private static void setupDirectoryStructure() {
		pluginDir = ScrollingMenuSign.getInstance().getDataFolder();

		commandFile = new File(pluginDir, commandFileName);
		dataDir = new File(pluginDir, dataDirName);
		menusDir = new File(dataDir, menusDirName);
		viewsDir = new File(dataDir, viewsDirName);
		macrosDir = new File(dataDir, macrosDirName);

		createDirectory(pluginDir);
		createDirectory(dataDir);
		createDirectory(menusDir);
		createDirectory(viewsDir);
		createDirectory(macrosDir);
	}

	private static void createDirectory(File dir) {
		if (dir.isDirectory()) {
			return;
		}
		if (!dir.mkdir()) {
			MiscUtil.log(Level.WARNING, "Can't make directory " + dir.getName()); //$NON-NLS-1$
		}
	}

	private static void initConfigFile() {
		boolean saveNeeded = false;
		getConfig().options().copyDefaults(true);
		Configuration config = getConfig();
		
		// check if there is anything in the defaults which isn't in our live config file
		for (String k : config.getDefaults().getKeys(true)) {
			if (!config.contains(k)) {
				saveNeeded = true;
			}
		}

		if (config.getString("sms.menuitem_separator").equals("\\|")) {
			// special case - convert from v0.3 or older where it was a regexp
			config.set("sms.menuitem_separator", "|");
			saveNeeded = true;
		}
		
		if (config.contains("sms.use_any_view")) {
			// migrate the old & confusingly-named "use_any_view" setting
			config.set("sms.ignore_view_ownership", config.getBoolean("sms.use_any_view"));
			config.set("sms.use_any_view", null);
			saveNeeded = true;
		}
		
		if (saveNeeded) {
			ScrollingMenuSign.getInstance().saveConfig();
		}
	}

	public static File getCommandFile() {
		return commandFile;
	}

	public static File getPluginFolder() {
		return pluginDir;
	}

	public static File getDataFolder() {
		return dataDir;
	}

	public static File getMenusFolder() {
		return menusDir;
	}

	public static File getMacrosFolder() {
		return macrosDir;
	}

	public static File getViewsFolder() {
		return viewsDir;
	}

	public static Configuration getConfig() {
		return ScrollingMenuSign.getInstance().getConfig();
	}

	public static void setPluginConfiguration(String key, String val) throws SMSException {
		if (!key.startsWith("sms.")) {
			key = "sms." + key;
		}

		setConfigItem(getConfig(), key, val);
		
		// special hooks
		 
		if (key.equalsIgnoreCase("sms.ignore_view_ownership")) {
			// redraw map views
			for (SMSView v : SMSView.listViews()) {
				if (v instanceof SMSMapView)
					v.update(v.getMenu(), SMSMenuAction.REPAINT);
			}
		}
		
		if (key.startsWith("sms.actions.spout") && ScrollingMenuSign.getInstance().isSpoutEnabled()) {
			// reload & re-cache spout key definitions
			SpoutUtils.loadKeyDefinitions();
		}
		
		ScrollingMenuSign.getInstance().saveConfig();
	}

	public static <T> void setPluginConfiguration(String key, List<T> list) throws SMSException {
		if (!key.startsWith("sms.")) {
			key = "sms." + key;
		}
		
		setConfigItem(getConfig(), key, list);
		
		ScrollingMenuSign.getInstance().saveConfig();
	}

	/**
	 * Get a formatted list of all plugin configuration items.
	 * 
	 * @return	A list of strings, each representing a key/value pair.
	 */
	public static List<String> getPluginConfiguration() {
		ArrayList<String> res = new ArrayList<String>();
		Configuration config = getConfig();
		for (String k : config.getDefaults().getKeys(true)) {
			if (config.isConfigurationSection(k))
				continue;
			res.add("&f" + k.replaceAll("^sms\\.", "") + "&- = '&e" + config.get(k) + "&-'");
		}
		Collections.sort(res);
		return res;
	}

	public static Object getPluginConfiguration(String key) throws SMSException {
		if (!key.startsWith("sms.")) {
			key = "sms." + key;
		}
		if (!SMSConfig.getConfig().contains(key)) {
			throw new SMSException("No such config item: " + key);
		}
		
		return getConfig().get(key);
	}

	/**
	 * Sets a configuration item in the given config object.  The key and value are both strings; the value
	 * will be converted into an object of the correct type, if possible (where the type is discovered from
	 * the config's default object).  The type's class must provide a constructor which takes a single string
	 * or an exception will be thrown.
	 * 
	 * @param config	The configuration object
	 * @param key		The configuration key
	 * @param val		The value
	 * @throws SMSException	if the key is unknown or a bad numeric value is passed
	 */
	public static void setConfigItem(Configuration config, String key, String val) throws SMSException {
		Configuration defaults = config.getDefaults();
		if (!defaults.contains(key)) {
			throw new SMSException("No such config key '" + key + "'");
		}
		if (defaults.get(key) instanceof List<?>) {
			List<String>list = new ArrayList<String>(1);
			list.add(val);
			handleListValue(config, key, list);
		} else if (defaults.get(key) instanceof String) {
			// should be marginally quicker than going through the following method...
			config.set(key, val);
		} else {
			// the class we're converting to needs to have a constructor taking a single String argument
			Class<?> c = null;
			try {
				c = defaults.get(key).getClass();
				Constructor<?> ctor = c.getDeclaredConstructor(String.class);
				config.set(key, ctor.newInstance(val));
			} catch (NoSuchMethodException e) {
				throw new SMSException("Don't know how to convert '" + val + "' into a " + c.getName());
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				if (e.getCause() instanceof NumberFormatException) {
					throw new SMSException("Invalid numeric value: " + val);
				} else {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static <T> void setConfigItem(Configuration config, String key, List<T> list) throws SMSException {
		if (config.getDefaults().get(key) == null) {
			throw new SMSException("No such key '" + key + "'");
		}
		if (!(config.getDefaults().get(key) instanceof List<?>)) {
			throw new SMSException("Key '" + key + "' does not accept a list of values");
		}
		handleListValue(config, key, list);
	}

	@SuppressWarnings("unchecked")
	private static <T> void handleListValue(Configuration config, String key, List<T> list) {
		HashSet<T> current;
		
		if (list.get(0).equals("-")) {
			// remove specifed item from list
			list.remove(0);
			current = new HashSet<T>(config.getList(key));
			current.removeAll(list);
		} else if (list.get(0).equals("=")) {
			// replace list
			list.remove(0);
			current = new HashSet<T>(list);
		} else if (list.get(0).equals("+")) {
			// append to list
			list.remove(0);
			current = new HashSet<T>(config.getList(key));
			current.addAll(list);
		} else {
			// append to list
			current = new HashSet<T>(config.getList(key));
			current.addAll(list);
		}
		
		config.set(key, new ArrayList<T>(current));
	}
}