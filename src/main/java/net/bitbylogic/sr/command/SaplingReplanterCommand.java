package net.bitbylogic.sr.command;

import net.bitbylogic.sr.SaplingReplanter;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class SaplingReplanterCommand implements CommandExecutor {

    private final SaplingReplanter plugin;

    public SaplingReplanterCommand(SaplingReplanter plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(!sender.hasPermission(plugin.getReloadCommandPermission())) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.No-Permission", "&cNo permission.")));
            return true;
        }

        plugin.reloadConfig();
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.Reloaded", "&aSapling Replanter successfully reloaded.")));
        return true;
    }

}
