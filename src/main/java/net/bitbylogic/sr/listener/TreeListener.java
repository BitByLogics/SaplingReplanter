package net.bitbylogic.sr.listener;

import net.bitbylogic.sr.SaplingReplanter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class TreeListener implements Listener {

    private final SaplingReplanter plugin;

    public TreeListener(SaplingReplanter plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if(plugin.isTreefellerActivationOnly()) {
            if(plugin.isIgnorePlayerPlacedBlocks() && plugin.isBlockPlayerPlaced(event.getBlock().getLocation())) {
                plugin.removeBlockPlaced(event.getBlock().getLocation());
            }

            return;
        }

        plugin.attemptReplant(event.getPlayer(), event.getBlock().getLocation(), event.getBlock().getType());

        if(plugin.isIgnorePlayerPlacedBlocks() && plugin.isBlockPlayerPlaced(event.getBlock().getLocation())) {
            plugin.removeBlockPlaced(event.getBlock().getLocation());
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (!plugin.isIgnorePlayerPlacedBlocks() || !plugin.getConfig().getStringList("Log-Blocks").contains(event.getBlock().getType().name())) {
            return;
        }

        plugin.markBlockPlaced(event.getBlock());
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        if(plugin.isIgnorePlayerPlacedBlocks() && plugin.isBlockPlayerPlaced(event.getBlock().getLocation())) {
            plugin.removeBlockPlaced(event.getBlock().getLocation());
        }
    }

}
