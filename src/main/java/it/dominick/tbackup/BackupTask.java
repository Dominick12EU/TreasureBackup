package it.dominick.tbackup;

import org.apache.commons.io.FileUtils;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Comparator;

public class BackupTask extends BukkitRunnable {
    private TreasureBackup plugin;

    public BackupTask(TreasureBackup plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        // Directory di destinazione per il backup
        String backupDirectory = "backup";

        // Ottenere la directory del server
        String serverDirectory = plugin.getServer().getWorldContainer().getAbsolutePath();

        // Creare il percorso completo della directory di backup
        Path backupPath = Paths.get(backupDirectory);

        try {
            // Creare la directory di backup se non esiste
            if (Files.notExists(backupPath)) {
                Files.createDirectories(backupPath);
                plugin.getServer().getConsoleSender().sendMessage("Directory di backup creata con successo.");
            }

            // Effettuare il backup solo se la directory di backup esiste
            if (Files.exists(backupPath)) {
                // Ottenere il percorso completo del file session.lock
                Path sessionLockPath = Paths.get(serverDirectory, "session.lock");

                // Verificare se il file session.lock esiste
                File sessionLockFile = sessionLockPath.toFile();
                if (sessionLockFile.exists()) {
                    plugin.getServer().getConsoleSender().sendMessage("Ignorato il file session.lock durante il backup.");
                } else {
                    // Creare una cartella con nome basato sulla data e l'orario attuali
                    SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy_HHmm");
                    String backupFolderName = dateFormat.format(new Date());
                    Path backupFolderPath = backupPath.resolve(backupFolderName);
                    Files.createDirectories(backupFolderPath);

                    // Backup dei file e delle cartelle nella directory del server
                    File serverDirectoryFile = new File(serverDirectory);
                    backupFilesAndFolders(serverDirectoryFile, backupFolderPath);

                    // Comprimere la cartella di backup in un file zip
                    String zipFileName = backupFolderName + ".zip";
                    File zipFile = backupPath.resolve(zipFileName).toFile();
                    zipDirectory(backupFolderPath.toFile(), zipFile);

                    // Eliminare la cartella di backup non compressa
                    FileUtils.deleteDirectory(backupFolderPath.toFile());

                    // Eliminare il backup più vecchio se il numero di file supera 5
                    deleteOldestBackup(backupPath);

                    plugin.getServer().getConsoleSender().sendMessage("Backup completato con successo.");
                }
            } else {
                plugin.getServer().getConsoleSender().sendMessage("La directory di backup non esiste.");
            }
        } catch (IOException e) {
            plugin.getServer().getConsoleSender().sendMessage("Errore durante il backup del server: " + e.getMessage());
        }
    }

    private void backupFilesAndFolders(File source, Path destination) throws IOException {
        // Copia il file o la cartella nella directory di destinazione
        if (source.isDirectory()) {
            Files.createDirectories(destination);
            File[] files = source.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (!file.getName().equals("backup")) {  // Ignora la cartella di backup
                        Path newDestination = destination.resolve(file.getName());
                        backupFilesAndFolders(file, newDestination);
                    }
                }
            }
        } else {
            // Ignora il file session.lock
            if (source.getName().equals("session.lock")) {
                plugin.getServer().getConsoleSender().sendMessage("Ignorato il file " + source.getName() + " durante il backup.");
                return;
            }

            // Ignora il file se non è accessibile
            if (!fileIsAccessible(source)) {
                plugin.getServer().getConsoleSender().sendMessage("Ignorato il file " + source.getName() + " durante il backup.");
                return;
            }

            Files.copy(source.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private boolean fileIsAccessible(File file) {
        try {
            FileUtils.touch(file);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void zipDirectory(File sourceDir, File zipFile) throws IOException {
        try (ZipArchiveOutputStream zipOut = new ZipArchiveOutputStream(new FileOutputStream(zipFile))) {
            addFilesToZip(sourceDir, "", zipOut);
        }
    }

    private void addFilesToZip(File source, String parentPath, ZipArchiveOutputStream zipOut) throws IOException {
        File[] files = source.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    addFilesToZip(file, parentPath + file.getName() + "/", zipOut);
                } else {
                    ArchiveEntry entry = zipOut.createArchiveEntry(file, parentPath + file.getName());
                    zipOut.putArchiveEntry(entry);
                    FileUtils.copyFile(file, zipOut);
                    zipOut.closeArchiveEntry();
                }
            }
        }
    }

    private void deleteOldestBackup(Path backupDirectory) throws IOException {
        File[] backupFiles = backupDirectory.toFile().listFiles();
        if (backupFiles != null && backupFiles.length > 5) {
            // Ordina i file per data di modifica (dal più vecchio al più recente)
            Arrays.sort(backupFiles, Comparator.comparingLong(File::lastModified));

            // Elimina il backup più vecchio
            FileUtils.deleteQuietly(backupFiles[0]);

            plugin.getServer().getConsoleSender().sendMessage("Eliminato il backup più vecchio: " + backupFiles[0].getName());
        }
    }
}
