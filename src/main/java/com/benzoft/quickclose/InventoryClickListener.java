package com.benzoft.quickclose;

import com.benzoft.quickclose.files.ConfigFile;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class InventoryClickListener implements Listener {

    private final Map<UUID, Supplier<Boolean>> clickCountMap = new HashMap<>();

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        final InventoryView openInventory = event.getWhoClicked().getOpenInventory();

        if (event.getWhoClicked() instanceof Player &&                                                                                        // Is player
                event.getSlotType() == InventoryType.SlotType.OUTSIDE &&                                                                      // Is Outside
                event.getClick() == ConfigFile.getInstance().getClickTypeToClose() &&                                                         // Is correct click type
                ConfigFile.getInstance().isCloseableInventoryType(openInventory.getType()) &&                                                 // Is closeable InventoryType
                (!ConfigFile.getInstance().isEmptyHandOnly() || event.getCursor() == null || event.getCursor().getType() == Material.AIR) &&  // Hand check
                (ConfigFile.getInstance().getClicksToClose() == 1 || isFinalMultiClick(event.getWhoClicked().getUniqueId()))                  // Is correct click amount
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
