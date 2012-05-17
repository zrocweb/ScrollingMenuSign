package me.desht.scrollingmenusign;

import java.io.File;

import me.desht.dhutils.LogUtils;

public class DirectoryStructure {
	private static File pluginDir;
	private static File dataDir, menusDir, viewsDir, macrosDir, imgCacheDir;
	private static File commandFile;

	private static final String dataDirName = "data";
	private static final String menusDirName = "menus";
	private static final String viewsDirName = "views";
	private static final String macrosDirName = "macros";
	private static final String imgCacheDirName = "imagecache";
	private static final String commandFileName = "commands.yml";

	static void setupDirectoryStructure() {
		pluginDir = ScrollingMenuSign.getInstance().getDataFolder();

		commandFile = new File(pluginDir, commandFileName);
		dataDir = new File(pluginDir, dataDirName);
		menusDir = new File(dataDir, menusDirName);
		viewsDir = new File(dataDir, viewsDirName);
		macrosDir = new File(dataDir, macrosDirName);
		imgCacheDir = new File(pluginDir, imgCacheDirName);

		createDirectory(pluginDir);
		createDirectory(dataDir);
		createDirectory(menusDir);
		createDirectory(viewsDir);
		createDirectory(macrosDir);
		createDirectory(imgCacheDir);
	}

	private static void createDirectory(File dir) {
		if (dir.isDirectory()) {
			return;
		}
		if (!dir.mkdir()) {
			LogUtils.warning("Can't make directory " + dir.getName()); //$NON-NLS-1$
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

	public static File getImgCacheFolder() {
		return imgCacheDir;
	}
}
