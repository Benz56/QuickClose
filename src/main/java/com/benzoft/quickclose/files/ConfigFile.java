package com.benzoft.quickclose.files;

import com.benzoft.quickclose.QuickClose;
import com.benzoft.quickclose.util.MessageUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ConfigFile {

    private static ConfigFile file;

    private final List<InventoryType> inventoryTypeBlackList;
    private final boolean inventoryTypeBlackListInvert;
    private final boolean emptyHandOnly;
    private final int clicksToClose;
    private final long consecutiveClicksTimeFrame;
    private final boolean ignoreNamedInventories;
    private ClickType clickTypeToClose;

    private ConfigFile() {
        QuickClose.getPlugin(QuickClose.class).saveDefaultConfig();
        final FileConfiguration config = QuickClose.getPlugin(QuickClose.class).getConfig();
        inventoryTypeBlackList = config.getStringList("InventoryTypeBlackList").stream().flatMap(s -> {
            try {
                return Stream.of(InventoryType.valueOf(s));
            } catch (final IllegalArgumentException e) {
                MessageUtil.send(null, "&7[&eQuickClose&7] &e" + s + "&c in InventoryTypeBlackList is not a valid InventoryType!");
            }
            return Stream.empty();
        }).collect(Collectors.toList());
        inventoryTypeBlackListInvert = config.getBoolean("InventoryTypeBlackListInvert", false);
        emptyHandOnly = config.getBoolean("EmptyHandOnly", true);
        clicksToClose = config.getInt("ClicksToClose", 1);
        consecutiveClicksTimeFrame = config.getLong("ConsecutiveClicksTimeFrame", 500L);
        try {
            clickTypeToClose = ClickType.valueOf(config.getString("ClickTypeToClose", "RIGHT"));
            if (!Arrays.asList(ClickType.LEFT, ClickType.MIDDLE, ClickType.RIGHT).contains(clickTypeToClose)) throw new IllegalArgumentException();
        } catch (final IllegalArgumentException e) {
            clickTypeToClose = ClickType.RIGHT;
            MessageUtil.send(null, "&7[&eQuickClose&7] &cClickTypeToClose is invalid. Using default \"RIGHT\"! Valid are: LEFT, MIDDLE, RIGHT");
        }
        ignoreNamedInventories = config.getBoolean("IgnoreNamedInventories", false);
    }

    public static ConfigFile getInstance() {
        file = file == null ? new ConfigFile() : file;
        return file;
    }

    public static void reload(final Plugin plugin) {
        plugin.reloadConfig();
        plugin.saveDefaultConfig();
        file = new ConfigFile();
    }

    public boolean isCloseableInventoryType(final InventoryType inventoryType) {
        return inventoryTypeBlackListInvert == inventoryTypeBlackList.contains(inventoryType);
    }

    public int getClicksToClose() {
        return clicksToClose;
    }

    public ClickType getClickTypeToClose() {
        return clickTypeToClose;
    }

    public long getConsecutiveClicksTimeFrame() {
        return consecutiveClicksTimeFrame;
    }

    public boolean isEmptyHandOnly() {
        return emptyHandOnly;
    }

    public boolean isIgnoreNamedInventories() {
        return ignoreNamedInventories;
    }
}
