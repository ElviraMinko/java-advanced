package info.kgeorgiy.ja.minko.walk;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Walk {
    private static void writeHashToFile(String hash, BufferedWriter bufferedWriter, String nameOfFile) {
        try {
            bufferedWriter.write(hash + " " + nameOfFile + System.lineSeparator());
        } catch (IOException e) {
            System.err.println("Can't write to output file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("Incorrect format of program arguments");
        } else {
            Path pathForRead;
            Path pathForWrite;
            try {
                pathForRead = Path.of(args[0]);
                pathForWrite = Path.of(args[1]);
            } catch (InvalidPathException e) {
                System.err.println("Path string cannot be converted to a Path: " + e.getMessage());
                return;
            }
            try (BufferedReader bufferedReader = Files.newBufferedReader(pathForRead)) {
                try (BufferedWriter bufferedWriter = Files.newBufferedWriter(pathForWrite)) {
                    String nameOfFile;
                    try {
                        while ((nameOfFile = bufferedReader.readLine()) != null) {
                            Path pathFile;
                            try {
                                pathFile = Path.of(nameOfFile);
                            } catch (InvalidPathException e) {
                                writeHashToFile("0".repeat(64), bufferedWriter, nameOfFile);
                                continue;
                            }
                            try (InputStream inputStream = Files.newInputStream(pathFile)) {
                                try {
                                    MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                                    byte[] arrOfBytes = new byte[1024];
                                    int counterOfBytes = inputStream.read(arrOfBytes);
                                    while (counterOfBytes != -1) {
                                        messageDigest.update(arrOfBytes, 0, counterOfBytes);
                                        counterOfBytes = inputStream.read(arrOfBytes);
                                    }
                                    byte[] hash = messageDigest.digest();
                                    String hashString = String.format("%0" + (hash.length << 1) + "x", new BigInteger(1, hash));
                                    writeHashToFile(hashString, bufferedWriter, nameOfFile);
                                } catch (NoSuchAlgorithmException e) {
                                    throw new RuntimeException("Invalid hashing algorithm");
                                }
                            } catch (IOException e) {
                                writeHashToFile("0".repeat(64), bufferedWriter, nameOfFile);
                            }
                        }
                    } catch (IOException e) {
                        System.err.println("An error occurred while reading from a input file: " + e.getMessage());
                    }
                } catch (IOException e) {
                    System.err.println("Problem with writing in file: " + e.getMessage());
                }
            } catch (IOException e) {
                System.err.println("Problem with file's reading: " + e.getMessage());
            }
        }
    }
}
