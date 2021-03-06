package fr.xephi.authme.task;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import fr.xephi.authme.AuthMe;
import fr.xephi.authme.ConsoleLogger;
import fr.xephi.authme.cache.auth.PlayerCache;
import fr.xephi.authme.cache.backup.FileCache;
import fr.xephi.authme.cache.limbo.LimboCache;
import fr.xephi.authme.cache.limbo.LimboPlayer;
import fr.xephi.authme.listener.AuthMePlayerListener;
import fr.xephi.authme.settings.Messages;

public class TimeoutTask implements Runnable {

    private AuthMe plugin;
    private String name;
    private Messages m = Messages.getInstance();
    private FileCache playerCache;

    public TimeoutTask(AuthMe plugin, String name) {
        this.plugin = plugin;
        this.name = name;
        this.playerCache = new FileCache(plugin);
    }

    public String getName() {
        return name;
    }

    @Override
    public void run() {
        if (PlayerCache.getInstance().isAuthenticated(name))
            return;

        for (final Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.getName().toLowerCase().equals(name)) {
                if (LimboCache.getInstance().hasLimboPlayer(name)) {
                    LimboPlayer inv = LimboCache.getInstance().getLimboPlayer(name);
                    inv.getMessageTaskId().cancel();
                    inv.getTimeoutTaskId().cancel();
                    if (playerCache.doesCacheExist(player)) {
                        playerCache.removeCache(player);
                    }
                }
                final GameMode gm = AuthMePlayerListener.gameMode.get(name);
                if (gm != null) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

                        @Override
                        public void run() {
                            player.setGameMode(gm);
                        }

                    });
                    ConsoleLogger.info("Set " + player.getName() + " to gamemode: " + gm.name());
                }
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

                    @Override
                    public void run() {
                        if (player.isOnline())
                            player.kickPlayer(m.send("timeout")[0]);
                    }
                });
                return;
            }
        }
    }
}
