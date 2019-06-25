package com.benzoft.quickclose.files;

import com.benzoft.quickclose.QuickClose;
import com.benzoft.quickclose.util.MessageUtil;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public final class ConfigFile {

    private static ConfigFile file;

    @Getter(AccessLevel.NONE)
    private final List<InventoryType> inventoryTypeBlackList;
    @Getter(AccessLevel.NONE)
    private final boolean inventoryTypeBlackListInvert;
    private final boolean emptyHandOnly;
    private final int clicksToClose;
    private final long consecutiveClicksTimeFrame;
    private final boolean ignoreNamedInventories;
    private final boolean updateCheckerEnabled;
    private final boolean updateCheckerPermissionOnly;
    private final Set<ClickType> clickTypeToClose = new HashSet<>();

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
        Stream.of(Objects.requireNonNull(config.getString("ClickTypeToClose", "")).replaceAll(" ", "").split(",")).filter(s -> !s.isEmpty()).map(String::toUpperCase).forEach(type -> {
            try {
                final ClickType clickType = ClickType.valueOf(type);
                if (!Arrays.asList(ClickType.LEFT, ClickType.MIDDLE, ClickType.RIGHT).contains(clickType)) throw new IllegalArgumentException();
                clickTypeToClose.add(clickType);
            } catch (final IllegalArgumentException e) {
                MessageUtil.send(null, "&7[&eQuickClose&7] &e" + type + "&c in ClickTypeToClose is not a valid ClickType. Valid are: LEFT, MIDDLE, RIGHT");
            }
        });
        if (clickTypeToClose.isEmpty()) {
            MessageUtil.send(null, "&7[&eQuickClose&7] &cNo valid ClickTypes found in ClickTypeToClose. Using default \"RIGHT\"");
            clickTypeToClose.add(ClickType.RIGHT);
        }
        ignoreNamedInventories = config.getBoolean("IgnoreNamedInventories", false);
        updateCheckerEnabled = config.getBoolean("UpdateCheckerEnabled", true);
        updateCheckerPermissionOnly = config.getBoolean("UpdateCheckerPermissionOnly", false);
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
}
