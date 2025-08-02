package net.bitbylogic.sr.listener;

import com.gmail.nossr50.api.AbilityAPI;
import net.bitbylogic.sr.SaplingReplanter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class MCMMOListener implements Listener {

    private final SaplingReplanter plugin;

    public MCMMOListener(SaplingReplanter plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onTreeFeller(BlockBreakEvent event) {
        if(!plugin.isTreefellerActivationOnly() || !AbilityAPI.treeFellerEnabled(event.getPlayer())) {
            return;
        }

        plugin.attemptReplant(event.getPlayer(), event.getBlock().getLocation(), event.getBlock().getType());
    }

}
