package net.bitbylogic.sr.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TreeUtil {

    private static final Set<Material> MEGA_LOGS = Set.of(Material.DARK_OAK_LOG, Material.JUNGLE_LOG, Material.SPRUCE_LOG);
    private static final int[][] MEGA_OFFSETS = {
            {0, 0},
            {-1, 0},
            {0, -1},
            {-1, -1}
    };

    private static final int BASE_SCAN_RADIUS = 4;

    public static List<Location> getSaplingLocations(Location location, Material logType) {
        if(!Tag.LOGS.isTagged(logType)) {
            return new ArrayList<>();
        }

        List<Location> saplingLocations = new ArrayList<>();
        Location treeBase = findTreeBase(location, logType);

        if(treeBase == null) {
            return saplingLocations;
        }

        if(MEGA_LOGS.contains(logType)) {
            Location megaTreeBase = findMegaTreeBase(treeBase, logType);

            if(megaTreeBase == null) {
                saplingLocations.add(treeBase);
                return saplingLocations;
            }

            int baseY = findLowest2x2BaseY(megaTreeBase, logType);

            saplingLocations.add(megaTreeBase.clone().add(0, 0, 0));
            saplingLocations.add(megaTreeBase.clone().add(1, 0, 0));
            saplingLocations.add(megaTreeBase.clone().add(0, 0, 1));
            saplingLocations.add(megaTreeBase.clone().add(1, 0, 1));

            return saplingLocations;
        }

        saplingLocations.add(treeBase);

        return saplingLocations;
    }


    public static Location findTreeBase(Location startLocation, Material logType) {
        Location directBase = traceDownToBase(startLocation, logType);

        if(isValidTreeBase(directBase, logType)) {
            return directBase;
        }

        if(MEGA_LOGS.contains(logType)) {
            return findConnectedTreeBase(startLocation, logType);
        }

        return null;
    }

    public static Location traceDownToBase(Location startLocation, Material logType) {
        Location scannedBlock = startLocation.clone();
        Location lastLog = scannedBlock.clone();

        if(scannedBlock.getWorld() == null) {
            return scannedBlock;
        }

        while(scannedBlock.getBlockY() > scannedBlock.getWorld().getMinHeight()) {
            if(scannedBlock.getBlock().getType() != logType) {
                return lastLog;
            }

            lastLog = scannedBlock.clone();
            scannedBlock.add(0, -1, 0);
        }

        return lastLog;
    }

    public static Location findConnectedTreeBase(Location startLocation, Material logType) {
        for(int radius = 1; radius <= BASE_SCAN_RADIUS; radius++) {
            for(int x = -radius; x <= radius; x++) {
                for(int z = -radius; z <= radius; z++) {
                    if(Math.abs(x) == radius || Math.abs(z) == radius) {
                        Location checkLocation = startLocation.clone().add(x, 0, z);

                        for(int y = -3; y <= 3; y++) {
                            Location testLocation = checkLocation.clone().add(0, y, 0);

                            if(testLocation.getBlock().getType() != logType) {
                                continue;
                            }

                            Location base = traceDownToBase(testLocation, logType);

                            if(!isValidTreeBase(base, logType)) {
                                continue;
                            }

                            if(MEGA_LOGS.contains(logType) && findMegaTreeBase(base, logType) == null) {
                                continue;
                            }

                            return base;
                        }
                    }
                }
            }
        }

        return null;
    }

    public static boolean isValidTreeBase(Location baseLocation, Material logType) {
        if(baseLocation == null) {
            return false;
        }

        Location blockBelow = baseLocation.clone().add(0, -1, 0);

        if(!isValidBaseSurface(logType, blockBelow.getBlock().getType())) {
            return false;
        }

        return hasTreeStructureAbove(baseLocation, logType);
    }

    public static Location findMegaTreeBase(Location treeBase, Material logType) {
        for(int xOffset = 0; xOffset >= -1; xOffset--) {
            for(int zOffset = 0; zOffset >= -1; zOffset--) {
                Location candidateNWCorner = treeBase.clone().add(xOffset, 0, zOffset);

                Location[] potentialBaseLogs = new Location[4];
                boolean allCornersFound = true;
                int lowestYAmongCorners = Integer.MAX_VALUE;

                for(int i = 0; i < 4; i++) {
                    int checkX = candidateNWCorner.getBlockX() + (i % 2);
                    int checkZ = candidateNWCorner.getBlockZ() + (i / 2);
                    Location checkPos = new Location(candidateNWCorner.getWorld(), checkX, treeBase.getY(), checkZ);

                    Location baseLog = findBaseLogAtPosition(checkPos, logType);
                    if (baseLog == null || baseLog.getBlock().getType() != logType) {
                        allCornersFound = false;
                        break;
                    }
                    potentialBaseLogs[i] = baseLog;
                    lowestYAmongCorners = Math.min(lowestYAmongCorners, baseLog.getBlockY());
                }

                if (allCornersFound) {
                    boolean isJungle = logType == Material.JUNGLE_LOG;
                    int maxYDiff = isJungle ? 2 : 1;
                    int currentMaxY = Integer.MIN_VALUE;

                    for(Location log : potentialBaseLogs) {
                        currentMaxY = Math.max(currentMaxY, log.getBlockY());
                    }

                    if (currentMaxY - lowestYAmongCorners <= maxYDiff) {
                        return new Location(candidateNWCorner.getWorld(), candidateNWCorner.getBlockX(), lowestYAmongCorners, candidateNWCorner.getBlockZ());
                    }
                }
            }
        }
        return null;
    }


    public static boolean isValid2x2TreeBase(Location baseLocation, Material logType) {
        Location[] positions = {
                baseLocation.clone(),
                baseLocation.clone().add(1, 0, 0),
                baseLocation.clone().add(0, 0, 1),
                baseLocation.clone().add(1, 0, 1)
        };

        Location[] baseLogs = new Location[4];
        boolean isJungle = logType == Material.JUNGLE_LOG;

        for(int i = 0; i < positions.length; i++) {
            baseLogs[i] = findBaseLogAtPosition(positions[i], logType);

            if(baseLogs[i] == null) {
                if(!isJungle) {
                    return false;
                }
            }
        }

        if(isJungle) {
            int validBases = 0;
            for(Location log : baseLogs) {
                if(log != null) validBases++;
            }
            if(validBases < 2) {
                return false;
            }
        }

        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;

        for(Location log : baseLogs) {
            if(log != null) {
                minY = Math.min(minY, log.getBlockY());
                maxY = Math.max(maxY, log.getBlockY());
            }
        }

        int maxYDiff = isJungle ? 2 : 1;
        if(maxY - minY > maxYDiff) {
            return false;
        }

        return hasTreeStructureAbove(baseLocation, logType);
    }

    public static Location findBaseLogAtPosition(Location position, Material logType) {
        Location baseLog = null;

        for(int y = -2; y <= 2; y++) {
            Location checkPos = position.clone().add(0, y, 0);

            if(checkPos.getBlock().getType() != logType) {
                continue;
            }

            baseLog = traceDownToBase(checkPos, logType);

            if(!isValidTreeBase(baseLog, logType)) {
                continue;
            }

            return baseLog;
        }

        return baseLog;
    }

    public static int findLowest2x2BaseY(Location baseLocation, Material logType) {
        if(baseLocation.getWorld() == null) {
            return baseLocation.getBlockY();
        }

        int lowestY = baseLocation.getWorld().getMaxHeight();

        Location[] positions = {
                baseLocation.clone(),
                baseLocation.clone().add(1, 0, 0),
                baseLocation.clone().add(0, 0, 1),
                baseLocation.clone().add(1, 0, 1)
        };

        boolean foundAnyLog = false;

        for(Location pos : positions) {
            Location baseLog = findBaseLogAtPosition(pos, logType);

            if(baseLog != null) {
                lowestY = Math.min(lowestY, baseLog.getBlockY());
                foundAnyLog = true;
            }
        }

        return foundAnyLog ? lowestY : baseLocation.getBlockY();
    }

    public static boolean hasTreeStructureAbove(Location baseLocation, Material logType) {
        Material leafType = getLeafForLog(logType);
        boolean isJungle = logType == Material.JUNGLE_LOG;

        Location checkUp = baseLocation.clone().add(0, 1, 0);
        boolean hasLogsAbove = false;
        int logHeight = 0;

        if(isJungle && MEGA_LOGS.contains(logType)) {
            for(int x = 0; x <= 1; x++) {
                for(int z = 0; z <= 1; z++) {
                    Location trunkCheck = baseLocation.clone().add(x, 1, z);
                    int currentLogHeight = 0;
                    for(int i = 0; i < 15; i++) {
                        if(trunkCheck.getBlock().getType() == logType) {
                            hasLogsAbove = true;
                            currentLogHeight = i + 1;
                        } else if(hasLogsAbove) {
                            break;
                        }
                        trunkCheck.add(0, 1, 0);
                    }
                    logHeight = Math.max(logHeight, currentLogHeight);
                    if(hasLogsAbove && logHeight >= 4) break;
                }
                if(hasLogsAbove && logHeight >= 4) break;
            }
        } else {
            for(int i = 0; i < 10; i++) {
                if(checkUp.getBlock().getType() == logType) {
                    hasLogsAbove = true;
                    logHeight = i + 1;
                } else if(hasLogsAbove) {
                    break;
                }
                checkUp.add(0, 1, 0);
            }
        }

        int minTrunkHeight = isJungle ? 4 : 3;
        if(!hasLogsAbove || logHeight < minTrunkHeight) {
            return false;
        }

        boolean hasLeaves = false;
        int leafCount = 0;
        int searchRadius = isJungle ? 4 : 3;
        int leafStartHeight = Math.max(2, logHeight - 2);
        int maxHeight = logHeight + 5;
        int minLeaves = isJungle ? 8 : 3;

        for(int x = -searchRadius; x <= searchRadius; x++) {
            for(int y = leafStartHeight; y <= maxHeight; y++) {
                for(int z = -searchRadius; z <= searchRadius; z++) {
                    Location leafCheck = baseLocation.clone().add(x, y, z);
                    Material blockType = leafCheck.getBlock().getType();

                    if(blockType == leafType) {
                        leafCount++;
                        if(leafCount >= minLeaves) {
                            hasLeaves = true;
                            break;
                        }
                    }
                    else if(isJungle && blockType == Material.VINE) {
                        leafCount++;
                        if(leafCount >= minLeaves) {
                            hasLeaves = true;
                            break;
                        }
                    }
                }

                if(hasLeaves) {
                    break;
                }
            }

            if(hasLeaves) {
                break;
            }
        }

        return hasLeaves;
    }

    private static boolean isValidBaseSurface(Material logType, Material blockBelowType) {
        if(logType == Material.CRIMSON_STEM) {
            return blockBelowType == Material.CRIMSON_NYLIUM;
        }
        if(logType == Material.WARPED_STEM) {
            return blockBelowType == Material.WARPED_NYLIUM;
        }

        if(Tag.DIRT.isTagged(blockBelowType)) {
            return true;
        }

        return blockBelowType == Material.GRASS_BLOCK;
    }

    public static Material getSaplingForLog(Material logType) {
        return switch (logType) {
            case BIRCH_LOG -> Material.BIRCH_SAPLING;
            case SPRUCE_LOG -> Material.SPRUCE_SAPLING;
            case JUNGLE_LOG -> Material.JUNGLE_SAPLING;
            case ACACIA_LOG -> Material.ACACIA_SAPLING;
            case DARK_OAK_LOG -> Material.DARK_OAK_SAPLING;
            case CHERRY_LOG -> Material.CHERRY_SAPLING;
            case PALE_OAK_LOG -> Material.PALE_OAK_SAPLING;
            case MANGROVE_LOG -> Material.MANGROVE_PROPAGULE;
            case CRIMSON_STEM -> Material.CRIMSON_FUNGUS;
            case WARPED_STEM -> Material.WARPED_FUNGUS;
            default -> Material.OAK_SAPLING;
        };
    }

    public static Material getLeafForLog(Material logType) {
        return switch (logType) {
            case BIRCH_LOG -> Material.BIRCH_LEAVES;
            case SPRUCE_LOG -> Material.SPRUCE_LEAVES;
            case JUNGLE_LOG -> Material.JUNGLE_LEAVES;
            case ACACIA_LOG -> Material.ACACIA_LEAVES;
            case DARK_OAK_LOG -> Material.DARK_OAK_LEAVES;
            case CHERRY_LOG -> Material.CHERRY_LEAVES;
            case PALE_OAK_LOG -> Material.PALE_OAK_LEAVES;
            case MANGROVE_LOG -> Material.MANGROVE_LEAVES;
            case CRIMSON_STEM -> Material.NETHER_WART_BLOCK;
            case WARPED_STEM -> Material.WARPED_WART_BLOCK;
            default -> Material.OAK_LEAVES;
        };
    }
}