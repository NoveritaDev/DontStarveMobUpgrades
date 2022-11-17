package me.noverita.mobupgrades;

import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Listeners implements Listener, CommandExecutor, TabCompleter {
    private final Map<EntityType, List<PotionEffect>> potionEffectMap = new HashMap<>();

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        EntityType et = event.getEntityType();
        List<PotionEffect> effects = potionEffectMap.get(et);
        if (effects != null) {
            LivingEntity entity = (LivingEntity) event.getEntity();
            for (PotionEffect effect: effects) {
                entity.addPotionEffect(effect);
            }
            Monster monster = (Monster) entity;
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length == 0) {
            StringBuilder sb = new StringBuilder();
            for (EntityType et: potionEffectMap.keySet()) {
                sb.append(et);
                sb.append(": ");
                for (PotionEffect pe: potionEffectMap.get(et)) {
                    sb.append(pe.getType());
                    sb.append(' ');
                    sb.append(pe.getAmplifier());
                    sb.append(',');
                }
                sb.append('\n');
            }
            commandSender.sendMessage(sb.toString());
            return true;
        } else if (strings.length == 1) {
            EntityType entityType = EntityType.valueOf(strings[0]);
            potionEffectMap.remove(entityType);
            commandSender.sendMessage(strings[0] + " spawn effects cleared.");

        } else if (strings.length == 2) {
            EntityType entityType = EntityType.valueOf(strings[0]);
            PotionEffectType type = PotionEffectType.getByName(strings[1]);
            int power = 0;

            if (!potionEffectMap.containsKey(entityType)) {
                potionEffectMap.put(entityType, new ArrayList<>());
            }
            potionEffectMap.get(entityType).add(new PotionEffect(type, Integer.MAX_VALUE, power, false, false));

            commandSender.sendMessage(strings[0] + " spawn effect added.");

        } else if (strings.length == 3) {
            EntityType entityType = EntityType.valueOf(strings[0]);
            PotionEffectType type = PotionEffectType.getByName(strings[1]);
            int power = Integer.parseInt(strings[2]);

            if (!potionEffectMap.containsKey(entityType)) {
                potionEffectMap.put(entityType, new ArrayList<>());
            }
            potionEffectMap.get(entityType).add(new PotionEffect(type, Integer.MAX_VALUE, power, false, false));

            commandSender.sendMessage(strings[0] + " spawn effect added");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length == 1) {
            List<String> entityTypes = new ArrayList<>();
            for (EntityType et : EntityType.values()) {
                if (et.name().startsWith(strings[0])) {
                    entityTypes.add(et.name());
                }
            }
            return entityTypes;
        } else if (strings.length == 2) {
            List<String> effectTypes = new ArrayList<>();
            for (PotionEffectType et : PotionEffectType.values()) {
                if (et.getName().startsWith(strings[1])) {
                    effectTypes.add(et.getName());
                }
            }
            return effectTypes;
        } else if (strings.length == 3) {
            List<String> amplifiers = new ArrayList<>();
            amplifiers.add("0");
            amplifiers.add("1");
            amplifiers.add("2");
            return amplifiers;
        }
        return new ArrayList<>();
    }
}
