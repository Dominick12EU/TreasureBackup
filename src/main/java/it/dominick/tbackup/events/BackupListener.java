package it.dominick.tbackup.events;

import it.dominick.tbackup.manager.BackupManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class BackupListener implements Listener {
    private BackupManager backupManager;

    public BackupListener(BackupManager backupManager) {
        this.backupManager = backupManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Esempio di esecuzione del backup quando un giocatore interagisce con un blocco specifico
        if (event.getClickedBlock() != null && event.getClickedBlock().getType().isInteractable()) {
            backupManager.startBackupTask();
        }
    }
}

