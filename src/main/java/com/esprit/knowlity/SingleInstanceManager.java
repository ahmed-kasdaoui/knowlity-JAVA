package com.esprit.knowlity;

import java.io.*;
import java.net.*;
import java.util.function.Consumer;

/**
 * Ensures only one instance of the app runs at a time, and allows passing deeplink arguments
 * to the running instance via a localhost socket.
 */
public class SingleInstanceManager {
    private static final int PORT = 45678; // any unused port
    private static ServerSocket serverSocket;
    private static Thread listenerThread;

    /**
     * Starts the single instance server. If another instance is running, sends the deeplink to it and exits.
     * @param deeplinkArg The deeplink argument (may be null)
     * @param onDeeplinkReceived Callback to handle deeplinks in the running instance
     * @return true if this is the main instance, false if a previous instance handled the deeplink
     */
    public static boolean start(String deeplinkArg, Consumer<String> onDeeplinkReceived) {
        try {
            serverSocket = new ServerSocket(PORT, 10, InetAddress.getByName("127.0.0.1"));
        } catch (IOException e) {
            // Another instance is running; send deeplink to it
            if (deeplinkArg != null) {
                System.out.println("[SingleInstanceManager] About to send argument: " + deeplinkArg);
                System.out.println("[SingleInstanceManager] Attempting to send deeplink to running instance: " + deeplinkArg);
                try (Socket s = new Socket("127.0.0.1", PORT);
                     OutputStream out = s.getOutputStream()) {
                    // Always send the full argument string as received
                    out.write(deeplinkArg.getBytes("UTF-8"));
                    System.out.println("[SingleInstanceManager] Deeplink sent successfully.");
                } catch (IOException ex) {
                    System.out.println("[SingleInstanceManager] Failed to send deeplink: " + ex.getMessage());
                }
            }
            return false;
        }
        // Start listener thread
        listenerThread = new Thread(() -> {
            while (!serverSocket.isClosed()) {
                try (Socket client = serverSocket.accept();
                     InputStream in = client.getInputStream()) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buf = new byte[256];
                    int len;
                    while ((len = in.read(buf)) != -1) {
                        baos.write(buf, 0, len);
                    }
                    String arg = baos.toString("UTF-8");
                    System.out.println("[SingleInstanceManager] Received raw argument: '" + arg + "'");
                    if (!arg.isEmpty() && onDeeplinkReceived != null) {
                        System.out.println("[SingleInstanceManager] Received deeplink: " + arg);
                        javafx.application.Platform.runLater(() -> onDeeplinkReceived.accept(arg));
                    }
                } catch (IOException ignored) {}
            }
        }, "SingleInstanceListener");
        listenerThread.setDaemon(true);
        listenerThread.start();
        return true;
    }

    public static void stop() {
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException ignored) {}
    }
}
