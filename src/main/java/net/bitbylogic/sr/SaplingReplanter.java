package net.bitbylogic.sr;

import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import net.bitbylogic.sr.command.SaplingReplanterCommand;
import net.bitbylogic.sr.level.MCMMOLevel;
import net.bitbylogic.sr.listener.MCMMOListener;
import net.bitbylogic.sr.listener.TreeListener;
import net.bitbylogic.sr.util.InventoryUtil;
import net.bitbylogic.sr.util.TreeUtil;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public final class SaplingReplanter extends JavaPlugin {

    private static SaplingReplanter instance;

    private static final int METRICS_ID = 26749;

    private final TreeMap<Integer, MCMMOLevel> mcmmoLevels = new TreeMap<>();
    private final Set<String> placedBlocks = new HashSet<>();

    private File placedBlocksFile;
    private YamlConfiguration placedBlocksConfig;

    private boolean mcmmoInstalled;

    private boolean usePermission;
    private boolean consumeSaplings;
    private boolean ignorePlayerPlacedBlocks;
    private boolean mcmmoSupportEnabled;
    private boolean treefellerActivationOnly;

    private double baseReplantChance;

    private String useReplanterPermission;
    private String bypassConsumePermission;
    private String mcmmoLevelSkipPermission;
    private String reloadCommandPermission;

    private MCMMOListener mcmmoListener;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        Metrics metrics = new Metrics(this, METRICS_ID);
        metrics.addCustomChart(new SimplePie("using_mcmmo_integration", () -> mcmmoSupportEnabled ? "True" : "False"));

        mcmmoInstalled = getServer().getPluginManager().isPluginEnabled("McMMO");

        loadSettings();
        loadLevels();

        getServer().getPluginManager().registerEvents(new TreeListener(this), this);

        getCommand("saplingreplanter").setExecutor(new SaplingReplanterCommand(this));

        if(mcmmoInstalled && mcmmoSupportEnabled) {
            getServer().getPluginManager().registerEvents(mcmmoListener = new MCMMOListener(this), this);
        }
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();

        loadPlacedBlocks();
        loadSettings();
        loadLevels();

        if(mcmmoListener != null && !mcmmoSupportEnabled) {
            HandlerList.unregisterAll(mcmmoListener);
            mcmmoListener = null;
        }

        if(mcmmoInstalled && mcmmoSupportEnabled && mcmmoListener == null) {
            getServer().getPluginManager().registerEvents(mcmmoListener = new MCMMOListener(this), this);
        }
    }

    @Override
    public void onDisable() {
        savePlacedBlocks();
    }

    private void loadSettings() {
        ConfigurationSection settingsSection = getConfig().getConfigurationSection("Settings");

        if(settingsSection == null) {
            getLogger().severe("Unable to load plugin settings, plugin may not function.");
            return;
        }

        usePermission = settingsSection.getBoolean("Use-Permission");
        consumeSaplings = settingsSection.getBoolean("Consume-Saplings");
        ignorePlayerPlacedBlocks = settingsSection.getBoolean("Ignore-Player-Placed-Blocks");
        mcmmoSupportEnabled = settingsSection.getBoolean("MCMMO-Support");
        treefellerActivationOnly = settingsSection.getBoolean("Treefeller-Activation-Only");

        baseReplantChance = settingsSection.getDouble("Base-Replant-Chance");

        if(!mcmmoInstalled && mcmmoSupportEnabled) {
            getLogger().warning("MCMMO Support is enabled in the configuration but MCMMO isn't installed. MCMMO support won't function.");
        }

        ConfigurationSection permissionSection = getConfig().getConfigurationSection("Permissions");

        if(permissionSection == null) {
            getLogger().warning("Unable to load plugin permissions, permissions may not function.");
            return;
        }

        useReplanterPermission = permissionSection.getString("Use-Replanter", "replanter.use");
        bypassConsumePermission = permissionSection.getString("Bypass-Consume", "replanter.bypassconsume");
        mcmmoLevelSkipPermission = permissionSection.getString("MCMMO-Level-Skip", "replanter.mcmmoskip");
        reloadCommandPermission = permissionSection.getString("Reload-Permission", "replanter.reload");
    }

    private void loadLevels() {
        mcmmoLevels.clear();

        ConfigurationSection levelsSection = getConfig().getConfigurationSection("MCMMO-Levels");

        if(levelsSection == null) {
            getLogger().warning("Failed to load MCMMO Levels, MCMMO-Levels section missing.");
            return;
        }

        for (String levelId : levelsSection.getKeys(false)) {
            ConfigurationSection levelSection = levelsSection.getConfigurationSection(levelId);

            if(levelSection == null) {
                continue;
            }

            int requiredLevel = levelSection.getInt("Required-Level");
            double chance = levelSection.getDouble("Chance", baseReplantChance);
            boolean consumeSaplings = levelSection.getBoolean("Consume-Saplings", getConfig().getBoolean("Settings.Consume-Saplings", true));

            mcmmoLevels.put(requiredLevel, new MCMMOLevel(levelId, requiredLevel, chance, consumeSaplings));
        }
    }

    private void loadPlacedBlocks() {
        placedBlocksFile = new File(getDataFolder(), "placed_blocks.yml");

        if (!placedBlocksFile.exists()) {
            placedBlocksFile.getParentFile().mkdirs();
            saveResource("placed_blocks.yml", false);
        }

        placedBlocksConfig = YamlConfiguration.loadConfiguration(placedBlocksFile);

        placedBlocks.clear();
        placedBlocks.addAll(placedBlocksConfig.getStringList("Placed-Blocks"));
    }

    private void savePlacedBlocks() {
        placedBlocksConfig.set("Placed-Blocks", new ArrayList<>(placedBlocks));

        try {
            placedBlocksConfig.save(placedBlocksFile);
        } catch (IOException e) {
            getLogger().warning("Failed to save placed blocks: " + e.getMessage());
        }
    }

    public double getReplantChance(Player player) {
        if(mcmmoInstalled && mcmmoSupportEnabled && !player.hasPermission(mcmmoLevelSkipPermission)) {
            int mcmmoLevel = ExperienceAPI.getLevel(player, PrimarySkillType.WOODCUTTING);
            Map.Entry<Integer, MCMMOLevel> level = mcmmoLevels.floorEntry(mcmmoLevel);

            return level == null ? baseReplantChance : level.getValue().chance();
        }

        return baseReplantChance;
    }

    private boolean shouldConsumeSaplings(Player player) {
        if(mcmmoInstalled && mcmmoSupportEnabled && !player.hasPermission(mcmmoLevelSkipPermission)) {
            int mcmmoLevel = ExperienceAPI.getLevel(player, PrimarySkillType.WOODCUTTING);
            Map.Entry<Integer, MCMMOLevel> level = mcmmoLevels.floorEntry(mcmmoLevel);

            return level == null ? consumeSaplings : level.getValue().consumeSaplings();
        }

        return consumeSaplings && (!usePermission || !player.hasPermission(bypassConsumePermission));
    }

    public void attemptReplant(Player player, Location location, Material blockType) {
        if (ignorePlayerPlacedBlocks && isBlockPlayerPlaced(location)) {
            return;
        }

        Material saplingType = TreeUtil.getSaplingForLog(blockType);

        if (saplingType == null) {
            return;
        }

        List<Location> saplingLocations = new ArrayList<>(TreeUtil.getSaplingLocations(location, blockType));

        if (saplingLocations.isEmpty()) {
            return;
        }

        if (usePermission && !player.hasPermission(useReplanterPermission)) {
            return;
        }

        boolean shouldConsume = shouldConsumeSaplings(player);

        if (shouldConsume && !player.getInventory().contains(saplingType, saplingLocations.size())) {
            return;
        }

        if (ThreadLocalRandom.current().nextDouble(100) >= getReplantChance(player)) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(this, () -> {
            saplingLocations.forEach(saplingLoc -> {
                Block targetBlock = saplingLoc.getBlock();

                if(ignorePlayerPlacedBlocks && isBlockPlayerPlaced(saplingLoc) || targetBlock.getType() == saplingType) {
                    return;
                }

                targetBlock.breakNaturally();
                targetBlock.setType(saplingType);

                if(shouldConsume) {
                    InventoryUtil.removeExactMaterial(player, saplingType, 1);
                }
            });
        }, 5);
    }

    public boolean isTreefellerActivationOnly() {
        return treefellerActivationOnly;
    }

    public boolean isIgnorePlayerPlacedBlocks() {
        return ignorePlayerPlacedBlocks;
    }

    public boolean isUsePermission() {
        return usePermission;
    }

    public String getReloadCommandPermission() {
        return reloadCommandPermission;
    }

    public void markBlockPlaced(Block block) {
        placedBlocks.add(blockKey(block.getLocation()));
    }

    public void removeBlockPlaced(Location location) {
        placedBlocks.remove(blockKey(location));
    }

    public boolean isBlockPlayerPlaced(Location location) {
        return placedBlocks.contains(blockKey(location));
    }

    private String blockKey(Location loc) {
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    public static SaplingReplanter getInstance() {
        return instance;
    }

}
