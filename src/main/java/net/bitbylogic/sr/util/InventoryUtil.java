package net.bitbylogic.sr.util;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class InventoryUtil {

    public static boolean removeExactMaterial(Player player, Material type, int amount) {
        int toRemove = amount;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() != type) continue;

            int itemAmount = item.getAmount();

            if (itemAmount <= toRemove) {
                player.getInventory().removeItem(item);
                toRemove -= itemAmount;
            } else {
                item.setAmount(itemAmount - toRemove);
                toRemove = 0;
            }

            if (toRemove <= 0) break;
        }

        return toRemove <= 0;
    }


}
