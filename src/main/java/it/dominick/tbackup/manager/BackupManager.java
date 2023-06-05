package it.dominick.tbackup.manager;

import it.dominick.tbackup.BackupTask;
import it.dominick.tbackup.TreasureBackup;
import org.bukkit.Bukkit;

public class BackupManager {
    private TreasureBackup plugin;
    private BackupTask backupTask;

    public BackupManager(TreasureBackup plugin) {
        this.plugin = plugin;
    }

    public boolean startBackupTask() {
        // Ottieni l'ora di esecuzione del backup dal file di configurazione
        int backupHour = plugin.getConfig().getInt("backupHour", 3);

        // Crea il task pianificato per il backup
        backupTask = new BackupTask(plugin);
        backupTask.runTaskTimer(plugin, calculateDelay(backupHour), 24 * 60 * 60 * 20);

        // Restituisci true per indicare che il backup è stato avviato correttamente
        return true;
    }

    public boolean instantBackupTask() {
        BackupTask backupTask = new BackupTask(plugin);

        // Esegui il backup immediato
        backupTask.run();

        // Puoi aggiungere ulteriori azioni o feedback dopo l'esecuzione del backup
        plugin.getServer().getConsoleSender().sendMessage("Backup istantaneo eseguito con successo.");
        // Restituisci true per indicare che il backup è stato avviato correttamente
        return true;
    }

    public void stopBackupTask() {
        if (backupTask != null) {
            backupTask.cancel();
            backupTask = null;
        }
    }

    private long calculateDelay(int backupHour) {
        long currentTime = Bukkit.getWorlds().get(0).getFullTime();
        long targetTime = currentTime - (currentTime % (24 * 60 * 60 * 20)) + (backupHour * 60 * 60 * 20);

        return targetTime - currentTime;
    }
}
