package info.kgeorgiy.ja.minko.walk;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Walk {
    private static final String HASH_VALUE_FOR_ERROR = "0".repeat(64);

    private static void writeHashToFile(String hash, BufferedWriter bufferedWriter, String nameOfFile) {
        try {
            bufferedWriter.write(String.format("%s %s%s", hash, nameOfFile, System.lineSeparator()));
        } catch (IOException e) {
            System.err.println("Can't write to output file: " + e.getMessage());
        }
    }

    private static void processingFile(MessageDigest messageDigest, BufferedReader bufferedReader,
                                       BufferedWriter bufferedWriter) {
        String nameOfFile;
        try {
            while ((nameOfFile = bufferedReader.readLine()) != null) {
                Path pathFile;
                try {
                    pathFile = Path.of(nameOfFile);
                } catch (InvalidPathException e) {
                    writeHashToFile(HASH_VALUE_FOR_ERROR, bufferedWriter, nameOfFile);
                    continue;
                }
                try (InputStream inputStream = Files.newInputStream(pathFile)) {
                    byte[] arrOfBytes = new byte[1024];
                    int counterOfBytes = inputStream.read(arrOfBytes);
                    while (counterOfBytes != -1) {
                        messageDigest.update(arrOfBytes, 0, counterOfBytes);
                        counterOfBytes = inputStream.read(arrOfBytes);
                    }
                    byte[] hash = messageDigest.digest();
                    String hashString = String.format("%0" + (hash.length << 1) + "x", new BigInteger(1, hash));
                    writeHashToFile(hashString, bufferedWriter, nameOfFile);
                } catch (IOException e) {
                    writeHashToFile(HASH_VALUE_FOR_ERROR, bufferedWriter, nameOfFile);
                }
            }
        } catch (IOException e) {
            System.err.println("An error occurred while reading from a input file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("Incorrect format of program arguments");
            return;
        }

        final Path pathForRead;
        final Path pathForWrite;
        try {
            pathForRead = Path.of(args[0]);
        } catch (InvalidPathException e) {
            System.err.println("Invalid path for input file");
            return;
        }
        try {
            pathForWrite = Path.of(args[1]);
            if (pathForWrite.getParent() != null && Files.notExists(pathForWrite.getParent())) {
                Files.createDirectory(pathForWrite.getParent());
            }
        } catch (InvalidPathException e) {
            System.err.println("Path string cannot be converted to a Path: " + e.getMessage());
            return;
        } catch (IOException e) {
            System.err.println("Failed in creating directory");
            return;
        }

        final MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Invalid hash algorithm");
            return;
        }

        try (BufferedReader bufferedReader = Files.newBufferedReader(pathForRead);
             BufferedWriter bufferedWriter = Files.newBufferedWriter(pathForWrite)) {
            processingFile(messageDigest, bufferedReader, bufferedWriter);
        } catch (IOException e) {
            System.err.println("Problem with file processing: " + e.getMessage());
        }
    }
}
