package com.seanfos.ytdl_rss.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.*;

public class FileDeletionService {

    public static void main(String[] args) {
        Path directoryPath = Paths.get("path/to/your/directory");
        
        try {
            deleteOldFilesExceptNewest(directoryPath, 10);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteOldFilesExceptNewest(Path directoryPath, int numberOfFilesToKeep) throws IOException {
        // Ensure the directory exists
        if (!Files.isDirectory(directoryPath)) {
            System.out.println("The provided path is not a valid directory.");
            return;
        }

        // List all files in the directory
        try (Stream<Path> paths = Files.walk(directoryPath)) {
            List<Path> files = paths
                .filter(Files::isRegularFile) // Keep only regular files, ignore directories
                .sorted((p1, p2) -> {
                    try {
                        return Long.compare(Files.getLastModifiedTime(p2).toMillis(), Files.getLastModifiedTime(p1).toMillis());
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                })
                .collect(Collectors.toList());

            // Keep the newest `numberOfFilesToKeep` files, delete the rest
            List<Path> filesToDelete = files.subList(numberOfFilesToKeep, files.size());

            for (Path file : filesToDelete) {
                try {
                    Files.delete(file);
                    System.out.println("Deleted: " + file);
                } catch (IOException e) {
                    System.err.println("Failed to delete " + file + ": " + e.getMessage());
                }
            }

        }
    }
}

