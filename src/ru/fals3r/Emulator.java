package ru.fals3r;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public class Emulator {

    public static void main(String[] args) {
        System.out.println("--- WexSide emulator by Fals3R ---");
        System.out.println();

        // ==========| Auth Emulator |==========
        Executors.newSingleThreadExecutor().submit(() -> {
            while (true) {
                ServerSocket serverSocket = new ServerSocket(1999 /* auth port */);
                System.out.println("[ auth emulator ] ready, waiting for connection...");

                Socket connection = serverSocket.accept();
                DataInputStream input = new DataInputStream(connection.getInputStream());
                DataOutputStream output = new DataOutputStream(connection.getOutputStream());
                System.out.println("[ auth emulator ] client connected");

                while (connection.isConnected() && !connection.isClosed()) {
                    if (input.available() > 0) {
                        byte[] bytes = new byte[2048];
                        int bytes_count = input.read(bytes);
                        System.out.println("[ auth emulator ] received " + bytes_count + " bytes from client");

                        byte[] usernameBytes = "EmulatedUser".getBytes(StandardCharsets.UTF_8);
                        byte[] dateBytes = "EmulatedDate".getBytes(StandardCharsets.UTF_8);

                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        bos.write(new byte[] { 0, 0, 0, 0, 22, -120, 17, 118 }); // magic 1

                        bos.write(new byte[] { 0, (byte)usernameBytes.length }); // username length
                        bos.write(usernameBytes); // username

                        bos.write(new byte[] { 0, 1, 49 }); // uid

                        bos.write(new byte[] {0, (byte)dateBytes.length, }); // subscription date length
                        bos.write(dateBytes); // subscription date

                        output.write(bos.toByteArray());
                        output.flush();
                        connection.close();
                        System.out.println("[ auth emulator ] sent info");
                    }
                }

                System.out.println("[ auth emulator ] client disconnected");
                serverSocket.close();
            }
        });

        Executors.newSingleThreadExecutor().submit(() -> {
            while (true) {
                ServerSocket serverSocket = new ServerSocket(2001 /* server port */);
                System.out.println("[ server emulator ] ready, waiting for connection...");

                Socket connection = serverSocket.accept();
                DataInputStream input = new DataInputStream(connection.getInputStream());
                DataOutputStream output = new DataOutputStream(connection.getOutputStream());
                System.out.println("[ server emulator ] client connected");

                while (connection.isConnected() && !connection.isClosed()) {
                    if (input.available() > 0) {
                        byte[] bytes = new byte[2048];
                        int bytes_count = input.read(bytes);
                        System.out.println("[ server emulator ] received " + bytes_count + " bytes from client");

                        int hwidSize = bytes[9];
                        int ipSize = bytes[9 + hwidSize + 2];
                        String serverIP = new String(bytes, 9 + hwidSize + 3, ipSize);

                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        bos.write(new byte[] { 0, (byte) ipSize }); // ip size
                        bos.write(serverIP.getBytes()); // ip
                        bos.write(new byte[] { 0, 0, 0, 0, 122, 87, 111, 47 }); // magic 1
                        bos.write(new byte[] { 0, (byte) ipSize }); // ip size
                        bos.write(serverIP.getBytes()); // ip
                        bos.write(new byte[] { -1, -1, -1, -1, -102, -124, 95, -107 }); // magic 2

                        output.write(bos.toByteArray());
                        output.flush();
                        connection.close();
                        System.out.println("[ server emulator ] sent info");
                    }
                }

                System.out.println("[ server emulator ] client disconnected");
                serverSocket.close();
            }
        });
    }
}
