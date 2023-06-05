package it.dominick.tbackup;

import it.dominick.tbackup.commands.BackupCommand;
import it.dominick.tbackup.events.BackupListener;
import it.dominick.tbackup.manager.BackupManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class TreasureBackup extends JavaPlugin {
    private BackupManager backupManager;

    @Override
    public void onEnable() {
        backupManager = new BackupManager(this);

        getServer().getPluginManager().registerEvents(new BackupListener(backupManager), this);

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        getCommand("backupnow").setExecutor(new BackupCommand(this, backupManager));
    }

    @Override
    public void onDisable() {
        backupManager.stopBackupTask();
    }

    public boolean startBackup() {
        BackupTask backupTask = new BackupTask(this);
        backupTask.runTaskAsynchronously(this);
        return true;
    }
}
