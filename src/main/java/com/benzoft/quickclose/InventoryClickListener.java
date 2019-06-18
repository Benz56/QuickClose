package com.benzoft.quickclose;

import com.benzoft.quickclose.files.ConfigFile;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;

import java.util.*;
import java.util.function.Supplier;

public class InventoryClickListener implements Listener {

    // Substitute for getDefaultTitle() which is inconsistent.
    // The key is the InventoryType as String to avoid issues on older versions.
    // Values are the names getTitle() returns on different versions.
    private static final Map<String, Set<String>> DEFAULT_NAME_MAP = new HashMap<>();

    static {
        DEFAULT_NAME_MAP.put("CHEST", new HashSet<>(Arrays.asList("Chest", "container.chest", "Minecart with Chest", "Beacon"))); // Yea. A Beacon is a Chest in 1.14 -.-
        DEFAULT_NAME_MAP.put("DISPENSER", new HashSet<>(Arrays.asList("Dispenser", "container.dispenser")));
        DEFAULT_NAME_MAP.put("DROPPER", new HashSet<>(Arrays.asList("Dropper", "container.dropper")));
        DEFAULT_NAME_MAP.put("FURNACE", new HashSet<>(Arrays.asList("Furnace", "container.furnace")));
        DEFAULT_NAME_MAP.put("WORKBENCH", new HashSet<>(Arrays.asList("container.crafting", "Crafting")));
        DEFAULT_NAME_MAP.put("CRAFTING", new HashSet<>(Arrays.asList("container.crafting", "Crafting")));
        DEFAULT_NAME_MAP.put("ENCHANTING", new HashSet<>(Arrays.asList("Enchanting", "Enchant")));
        DEFAULT_NAME_MAP.put("BREWING", new HashSet<>(Arrays.asList("Brewing", "Brewing Stand", "container.brewing")));
        DEFAULT_NAME_MAP.put("PLAYER", Collections.singleton("Player"));
        DEFAULT_NAME_MAP.put("MERCHANT", new HashSet<>(Arrays.asList("mob.villager", "None", "Armorer", "Butcher", "Cartographer", "Cleric", "Farmer", "Fisherman", "Fletcher", "Leatherworker", "Librarian", "Mason", "Nitwit", "Shepherd", "Toolsmith", "Weaponsmith")));
        DEFAULT_NAME_MAP.put("ENDER_CHEST", new HashSet<>(Arrays.asList("Ender Chest", "container.enderchest")));
        DEFAULT_NAME_MAP.put("ANVIL", new HashSet<>(Arrays.asList("Repairing", "Repair")));
        DEFAULT_NAME_MAP.put("BEACON", new HashSet<>(Arrays.asList("container.beacon", "Beacon")));
        DEFAULT_NAME_MAP.put("HOPPER", new HashSet<>(Arrays.asList("Item Hopper", "Minecart with Hopper", "container.hopper")));
        DEFAULT_NAME_MAP.put("SHULKER_BOX", new HashSet<>(Arrays.asList("Shulker Box", "container.shulkerBox")));
        DEFAULT_NAME_MAP.put("BARREL", Collections.singleton("Barrel"));
        DEFAULT_NAME_MAP.put("BLAST_FURNACE", Collections.singleton("Blast Furnace"));
        DEFAULT_NAME_MAP.put("SMOKER", Collections.singleton("Smoker"));
        DEFAULT_NAME_MAP.put("LOOM", Collections.singleton("Loom"));
        DEFAULT_NAME_MAP.put("CARTOGRAPHY", Collections.singleton("Cartography Table"));
        DEFAULT_NAME_MAP.put("GRINDSTONE", Collections.singleton("Repair & Disenchant"));
        DEFAULT_NAME_MAP.put("STONECUTTER", Collections.singleton("Stonecutter"));
    }

    private final Map<UUID, Supplier<Boolean>> clickCountMap = new HashMap<>();

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        final InventoryView openInventory = event.getWhoClicked().getOpenInventory();
        final InventoryType type = openInventory.getType();

        if (event.getWhoClicked() instanceof Player &&                                                                                                // Is player
                event.getSlotType() == InventoryType.SlotType.OUTSIDE &&                                                                              // Is Outside
                ConfigFile.getInstance().getClickTypeToClose().contains(event.getClick()) &&                                                          // Is correct click type
                ConfigFile.getInstance().isCloseableInventoryType(type) &&                                                                            // Is closeable InventoryType
                (!ConfigFile.getInstance().isIgnoreNamedInventories() || DEFAULT_NAME_MAP.get(type.toString()).contains(openInventory.getTitle())) && // Is Ignore name or unnamed.
                (!ConfigFile.getInstance().isEmptyHandOnly() || event.getCursor() == null || event.getCursor().getType() == Material.AIR) &&          // Hand check
                (ConfigFile.getInstance().getClicksToClose() == 1 || isFinalMultiClick(event.getWhoClicked().getUniqueId()))                          // Is correct click amount
        ) {
            openInventory.close();
        }
    }

    private boolean isFinalMultiClick(final UUID uuid) {
        return clickCountMap.computeIfAbsent(uuid, val -> new Supplier<Boolean>() {
            private long lastClick;
            private int clicks;

            @Override
            public Boolean get() {
                clicks = lastClick + ConfigFile.getInstance().getConsecutiveClicksTimeFrame() < System.currentTimeMillis() ? 1 : clicks + 1;
                lastClick = System.currentTimeMillis();
                return clicks == ConfigFile.getInstance().getClicksToClose();
            }
        }).get();
    }
}
