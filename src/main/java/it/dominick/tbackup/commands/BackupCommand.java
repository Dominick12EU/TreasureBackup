package it.dominick.tbackup.commands;

import it.dominick.tbackup.TreasureBackup;
import it.dominick.tbackup.manager.BackupManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BackupCommand implements CommandExecutor {

    private TreasureBackup plugin;

    public BackupCommand(TreasureBackup plugin, BackupManager backupManager) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("backupnow")) {
            sender.sendMessage("Avvio del backup in corso...");

            if (plugin.startBackup()) {
                sender.sendMessage("Backup avviato con successo!");
            } else {
                sender.sendMessage("Errore durante l'avvio del backup. Controlla la console per ulteriori informazioni.");
            }

            return true;
        }
        return false;
    }
}
