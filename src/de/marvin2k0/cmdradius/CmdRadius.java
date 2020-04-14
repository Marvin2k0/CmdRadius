package de.marvin2k0.cmdradius;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class CmdRadius extends JavaPlugin implements Listener, CommandExecutor
{
    List<String> commands = new ArrayList<String>();

    @Override
    public void onEnable()
    {
        this.getConfig().options().copyDefaults(true);
        this.getConfig().addDefault("commands", commands);
        this.saveConfig();

        this.getServer().getPluginManager().registerEvents(this, this);
        this.getCommand("cmdradius").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (!(sender instanceof Player))
        {
            sender.sendMessage("§cNur fuer Spieler");

            return true;
        }
        // /cr delete say
        // /cr say 5
        // /cr list
        if (args.length == 0)
        {
            sendHelp(sender);

            return true;
        }

        if (args[0].equalsIgnoreCase("delete"))
        {
            if (args.length != 2)
            {
                sendHelp(sender);

                return true;
            }

            List<String> temp = this.getConfig().getStringList("commands");

            for (String str : temp)
            {
                if (str.startsWith(args[1]))
                {
                    temp.remove(str);
                    this.getConfig().set("commands", temp);
                    this.saveConfig();

                    sender.sendMessage("§cRegion §e" + str + " §cwurde gelöscht!");

                    return true;
                }
            }

            sender.sendMessage("§cRegion §e" + args[1] + "§c wurde nicht gefunden!");

            return true;
        }
        else if (args[0].equalsIgnoreCase("list"))
        {
            this.commands = this.getConfig().getStringList("commands");

            for (String str : commands)
            {
                String output = str.split("_")[0] + "_" + str.split("_")[1];

                sender.sendMessage("§7- " + output);
            }

            sender.sendMessage("§cDas waren alle!");

            return true;
        }

        if (args.length != 2)
        {
            sender.sendMessage("§cBitte benutze /cr <befehl> <radius>");

            return true;
        }

        this.commands = this.getConfig().getStringList("commands");
        List<String> temp = this.getConfig().getStringList("commands");
        List<String> a = new ArrayList<String>();
        int radius;
        Location loc = ((Player) sender).getLocation();

        try
        {
            radius = Integer.valueOf(args[1]);
        }
        catch (Exception e)
        {
            sender.sendMessage("§cBitte benutze /cr <befehl> <radius>");

            return true;
        }

        for (String str : temp)
        {
            if (str.contains(args[0]))
            {
                int count = Integer.valueOf(str.split("_")[1]) + 1;
                a.add(args[0] + "_" + (count) + "_" + radius + "_" + loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ() + "_" + loc.getWorld().getName());

                for (String string : temp)
                    a.add(string);

                this.getConfig().set("commands", a);
                this.saveConfig();
                this.reloadConfig();

                sender.sendMessage(str.split("_")[1] + " " + count);
                sender.sendMessage("§aRegion als " + str.split("_")[0] + "_" + count + " gespeichert!");

                return true;
            }
        }

        a.add(args[0] + "_" + 0 + "_" + radius + "_" + loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ() + "_" + loc.getWorld().getName());

        for (String string : temp)
            a.add(string);

        this.getConfig().set("commands", a);
        this.saveConfig();
        this.reloadConfig();

        sender.sendMessage("§aRegion als " + args[0] + "_0" + " gespeichert!");

        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent event)
    {
        String cmd = event.getMessage().split(" ")[0].replace("/", "");

        if (isCommandBlocked(cmd))
        {
            event.getPlayer().sendMessage(cmd + " ist blockiert!");
            //TODO: check if player is inside of one of the radius

            for (String str : this.getConfig().getStringList("commands"))
            {
                if (str.contains(cmd))
                {
                    String world;
                    int x, y, z, radius;

                    world = str.split("_")[6];
                    x = Integer.valueOf(str.split("_")[3]);
                    y = Integer.valueOf(str.split("_")[4]);
                    z = Integer.valueOf(str.split("_")[5]);
                    radius = Integer.valueOf(str.split("_")[2]);

                    Location location = new Location(Bukkit.getWorld(world), x, y, z);

                    if (!insideLocation(event.getPlayer().getLocation(), location, radius))
                    {
                        event.setCancelled(true);
                        event.getPlayer().sendMessage("§c" + cmd + " ist hier nicht erlaubt!");
                    }
                }
            }



        }
    }

    private boolean insideLocation(Location playerLocation, Location loc, int radius)
    {
        boolean checkWorld = playerLocation.getWorld().getName() == loc.getWorld().getName();
        boolean checkX = Math.max(playerLocation.getBlockX(), loc.getBlockX()) - Math.min(playerLocation.getBlockX(), loc.getBlockX()) <= radius;
        boolean checkY = Math.max(playerLocation.getBlockY(), loc.getBlockY()) - Math.min(playerLocation.getBlockY(), loc.getBlockY()) <= radius;
        boolean checkZ = Math.max(playerLocation.getBlockZ(), loc.getBlockZ()) - Math.min(playerLocation.getBlockZ(), loc.getBlockZ()) <= radius;

        return checkWorld && checkX && checkY && checkZ;
    }

    private void sendHelp(CommandSender sender)
    {
        sender.sendMessage("§c===============================================");
        sender.sendMessage("§e/cr <befehl> <radius> §f- §eZum Bereich setzen");
        sender.sendMessage("§e/cr delete <name> §f- §eZum Bereich löschen");
        sender.sendMessage("§e/cr list §f- §eUm alle Bereiche zu sehen");
        sender.sendMessage("§c===============================================");
    }

    private boolean isCommandBlocked(String command)
    {
        for (String str : this.getConfig().getStringList("commands"))
        {
            if (str.split("_")[0].equalsIgnoreCase(command))
                return true;
        }

        return false;
    }
}
