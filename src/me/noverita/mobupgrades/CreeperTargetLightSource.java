package me.noverita.mobupgrades;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Lightable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class CreeperTargetLightSource implements Listener {
    private Map<Block, ArmorStand> lights;
    private Set<Material> lightSourceTypes;
    private Map<Creeper, CreeperTrackingData> activeCreepers;
    private static CreeperTargetLightSource instance;

    private CreeperTargetLightSource(JavaPlugin plugin) {
        lights = new HashMap<>();
        lightSourceTypes = new HashSet<>();
        lightSourceTypes.add(Material.CAMPFIRE);
        lightSourceTypes.add(Material.LANTERN);
        lightSourceTypes.add(Material.JACK_O_LANTERN);
        lightSourceTypes.add(Material.TORCH);
        lightSourceTypes.add(Material.REDSTONE_TORCH);
        lightSourceTypes.add(Material.GLOWSTONE);
        activeCreepers = new HashMap<>();

        Bukkit.getPluginManager().registerEvents(this, plugin);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            Bukkit.broadcastMessage("Creeper check");
            for (Block b: lights.keySet()) {
                if (!lightSourceTypes.contains(b.getType())) {
                    ArmorStand as = lights.remove(b);
                    as.remove();
                }
            }

            for (Creeper c: activeCreepers.keySet()) {
                if (c.isDead()) {
                    activeCreepers.remove(c);
                    continue;
                }

                CreeperTrackingData data = activeCreepers.get(c);
                Location creeperLocation = c.getLocation();
                Entity target = c.getTarget();
                if (target != null) {
                    Bukkit.broadcastMessage("distance: " + creeperLocation.distance(data.target) + " prev: " + data.prevDistance);
                    if (creeperLocation.distance(data.target) >= data.prevDistance - 0.05 || creeperLocation.distance(target.getLocation()) < c.getExplosionRadius() * 1.5) {
                        c.explode();
                    } else {
                        Location targetLocation = target.getLocation();
                        data.target = target.getLocation();
                        data.prevDistance = creeperLocation.distance(targetLocation);
                    }
                }
            }
        }, 0, 200);
    }
    public static CreeperTargetLightSource register(JavaPlugin plugin) {
        instance = new CreeperTargetLightSource(plugin);
        return instance;
    }

    @EventHandler
    private void removeCreeperOnDeath(EntityExplodeEvent event) {
        activeCreepers.remove(event.getEntity());
    }

    @EventHandler
    private void onCreeperSpawn(EntitySpawnEvent event) {
        if (event.getEntityType() == EntityType.CREEPER) {
            Creeper creeper = (Creeper) event.getEntity();

            double closest = Double.MAX_VALUE;
            ArmorStand target = null;
            for (ArmorStand marker: lights.values()) {
                double distance = creeper.getLocation().distance(marker.getLocation());
                if (distance < closest) {
                    closest = distance;
                    target = marker;
                }
            }
            creeper.setTarget(target);

            if (target != null) {
                Location targetLocation = creeper.getTarget().getLocation();
                double distance = targetLocation.distance(creeper.getLocation());
                activeCreepers.put(creeper, new CreeperTrackingData(creeper.getTarget().getLocation(), distance));
            }
        }
    }

    @EventHandler
    private void onCreeperDeath(EntityDeathEvent event) {
        if (event.getEntityType() == EntityType.CREEPER) {
            activeCreepers.remove(event.getEntity());
        }
    }

    @EventHandler
    private void onTargetChange(EntityTargetEvent event) {
        if (event.getEntityType() == EntityType.CREEPER && event.getTarget() == null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onPlaceLightSource(BlockPlaceEvent event) {
        Block b = event.getBlock();
        if (lightSourceTypes.contains(b.getType())) {
            ArmorStand marker = (ArmorStand) b.getWorld().spawnEntity(b.getLocation(), EntityType.ARMOR_STAND);
            marker.teleport(marker.getLocation().add(new Vector(0.5,0, 0.5)));
            marker.setInvisible(true);
            marker.setInvulnerable(true);
            marker.setMarker(true);
            marker.setGravity(false);
            marker.setAI(false);
            lights.put(event.getBlock(), marker);
        }
    }

    @EventHandler
    private void onBreakLightSource(BlockBreakEvent event) {
        ArmorStand marker = lights.remove(event.getBlock());
        if (marker != null) {
            marker.remove();
        }
    }

    @EventHandler
    private void onExplosion(BlockExplodeEvent event) {
        ArmorStand marker = lights.remove(event.getBlock());
        if (marker != null) {
            marker.remove();
        }
    }

    private static class CreeperTrackingData {
        Location target;
        double prevDistance;

        CreeperTrackingData(Location target, double prevDistance) {
            this.target = target;
            this.prevDistance = prevDistance;
        }
    }
}
