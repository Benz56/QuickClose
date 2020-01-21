package com.benzoft.quickclose;

import com.benzoft.quickclose.files.ConfigFile;
import com.benzoft.quickclose.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class QuickClose extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        new Metrics(this);
        Bukkit.getPluginManager().registerEvents(new InventoryClickListener(), this);
        ConfigFile.getInstance();
        Objects.requireNonNull(getCommand("quickclose")).setExecutor(this);
        new UpdateChecker(this).checkForUpdate();
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String label, final String[] args) {
        if (args.length >= 1 && Arrays.asList("reload", "rel", "r").contains(args[0].toLowerCase()) && sender.isOp()) {
            ConfigFile.reload(this);
            MessageUtil.send(sender instanceof Player ? (Player) sender : null, "&7[&eQuickClose&7] &aConfiguration file successfully reloaded!");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String alias, final String[] args) {
        return args.length == 1 && "reload".startsWith(args[0].toLowerCase()) && sender.isOp() ? Collections.singletonList("reload") : null;
    }
}
