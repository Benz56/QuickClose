package com.benzoft.quickclose.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public final class MessageUtil {

    private static String translate(final String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static void send(final Player player, String message) {
        if (!message.isEmpty()) {
            message = translate(message);
            if (player != null) {
                player.sendMessage(message);
            } else Bukkit.getServer().getConsoleSender().sendMessage(message);

        }
    }
}
