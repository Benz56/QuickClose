package com.benzoft.quickclose;

import com.benzoft.quickclose.files.ConfigFile;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
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
    private static final Map<String, Set<String>> DEFAULT_NAME_MAP = ImmutableMap.<String, Set<String>>builder()
            .put("CHEST", ImmutableSet.of("Chest", "Large Chest", "container.chest", "container.chestDouble", "Minecart with Chest", "Beacon")) // Yea. A Beacon is a Chest in 1.14 -.-
            .put("DISPENSER", ImmutableSet.of("Dispenser", "container.dispenser"))
            .put("DROPPER", ImmutableSet.of("Dropper", "container.dropper"))
            .put("FURNACE", ImmutableSet.of("Furnace", "container.furnace"))
            .put("WORKBENCH", ImmutableSet.of("container.crafting", "Crafting"))
            .put("CRAFTING", ImmutableSet.of("container.crafting", "Crafting"))
            .put("ENCHANTING", ImmutableSet.of("Enchanting", "Enchant"))
            .put("BREWING", ImmutableSet.of("Brewing", "Brewing Stand", "container.brewing"))
            .put("PLAYER", Collections.singleton("Player"))
            .put("MERCHANT", ImmutableSet.of("mob.villager", "None", "Armorer", "Butcher", "Cartographer", "Cleric", "Farmer", "Fisherman", "Fletcher", "Leatherworker", "Librarian", "Mason", "Nitwit", "Shepherd", "Toolsmith", "Weaponsmith"))
            .put("ENDER_CHEST", ImmutableSet.of("Ender Chest", "container.enderchest"))
            .put("ANVIL", ImmutableSet.of("Repairing", "Repair"))
            .put("BEACON", ImmutableSet.of("container.beacon", "Beacon"))
            .put("HOPPER", ImmutableSet.of("Item Hopper", "Minecart with Hopper", "container.hopper"))
            .put("SHULKER_BOX", ImmutableSet.of("Shulker Box", "container.shulkerBox"))
            .put("BARREL", Collections.singleton("Barrel"))
            .put("BLAST_FURNACE", Collections.singleton("Blast Furnace"))
            .put("SMOKER", Collections.singleton("Smoker"))
            .put("LOOM", Collections.singleton("Loom"))
            .put("CARTOGRAPHY", Collections.singleton("Cartography Table"))
            .put("GRINDSTONE", Collections.singleton("Repair & Disenchant"))
            .put("STONECUTTER", Collections.singleton("Stonecutter")).build();

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
