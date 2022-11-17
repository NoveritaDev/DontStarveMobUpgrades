package me.noverita.mobupgrades;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class FastHunger implements Runnable {
    @Override
    public void run() {
        for (Player p: Bukkit.getOnlinePlayers()) {
            float foodReduction = 1.0f;
            float saturation = p.getSaturation();
            if (saturation > 0) {
                if (saturation - foodReduction < 0) {
                    foodReduction -= saturation;
                    p.setSaturation(0);
                } else {
                    p.setSaturation(saturation - foodReduction);
                    foodReduction = 0;
                }
            }

            if (foodReduction > 0) {
                p.setFoodLevel((int) (p.getFoodLevel() - foodReduction));
            }
        }
    }
}
