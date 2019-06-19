package com.benzoft.quickclose;

import com.benzoft.quickclose.files.ConfigFile;
import com.benzoft.quickclose.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

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
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final Player player = sender instanceof Player ? (Player) sender : null;
        if (args.length >= 1 && Arrays.asList("reload", "rel", "r").contains(args[0].toLowerCase()) && (player == null || player.isOp())) {
            ConfigFile.reload(this);
            MessageUtil.send(player, "&7[&eQuickClose&7] &aConfiguration file successfully reloaded!");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        return args.length == 1 && "reload".startsWith(args[0].toLowerCase()) && (sender == null || sender.isOp()) ? Collections.singletonList("reload") : null;
    }
}
