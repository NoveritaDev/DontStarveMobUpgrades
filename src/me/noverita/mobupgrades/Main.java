package me.noverita.mobupgrades;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements CommandExecutor {
    private int repeatingTask;
    @Override
    public void onEnable() {
        super.onEnable();

        repeatingTask = -1;
        //CreeperTargetLightSource.register(this);


        getCommand("fasthunger").setExecutor(this);

        Listeners spawnEffect = new Listeners();
        Bukkit.getPluginManager().registerEvents(spawnEffect, this);
        getCommand("spawneffect").setExecutor(spawnEffect);
        getCommand("spawneffect").setTabCompleter(spawnEffect);

        PlayerDamageBoost damageBuff = new PlayerDamageBoost();
        Bukkit.getPluginManager().registerEvents(damageBuff, this);
        getCommand("damagebuff").setExecutor(damageBuff);
        getCommand("damagebuff").setTabCompleter(damageBuff);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            if (repeatingTask != -1) {
                Bukkit.getScheduler().cancelTask(repeatingTask);
            }
            repeatingTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new FastHunger(), Integer.parseInt(args[0]), Integer.parseInt(args[0]));
            return true;
        }
        return false;
    }
}
