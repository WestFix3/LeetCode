package core;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class MultiplayerClient {
    private static final int UDP_PORT = 5556;

    private Socket tcpSocket;
    private DatagramSocket udpSocket;
    private PrintWriter tcpOut;
    private BufferedReader tcpIn;

    private String serverIp;
    private int serverPort;
    private boolean connected = false;

    private BlockingQueue<String> receivedMessages = new LinkedBlockingQueue<>();
    private Thread tcpListenerThread;
    private Thread udpListenerThread;

    public MultiplayerClient(String serverIp, int serverPort) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
    }

    public void connect() throws IOException {
        // TCP kapcsolat
        tcpSocket = new Socket(serverIp, serverPort);
        tcpOut = new PrintWriter(tcpSocket.getOutputStream(), true);
        tcpIn = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));

        // UDP socket
        udpSocket = new DatagramSocket();

        connected = true;

        // TCP üzenetek fogadása
        tcpListenerThread = new Thread(this::listenTCP);
        tcpListenerThread.setDaemon(true);
        tcpListenerThread.start();

        // UDP üzenetek fogadása
        udpListenerThread = new Thread(this::listenUDP);
        udpListenerThread.setDaemon(true);
        udpListenerThread.start();

        System.out.println("🔗 Connected to multiplayer server: " + serverIp + ":" + serverPort);
    }

    private void listenTCP() {
        try {
            String message;
            while (connected && (message = tcpIn.readLine()) != null) {
                receivedMessages.offer(message);
                System.out.println("📨 TCP from server: " + message);
            }
        } catch (IOException e) {
            if (connected) {
                System.err.println("❌ TCP connection error: " + e.getMessage());
            }
        }
    }

    private void listenUDP() {
        byte[] buffer = new byte[1024];

        while (connected) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                udpSocket.receive(packet);

                String message = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
                receivedMessages.offer(message);
                System.out.println("📨 UDP from server: " + message);

            } catch (IOException e) {
                if (connected) {
                    System.err.println("❌ UDP receive error: " + e.getMessage());
                }
            }
        }
    }

    public void sendJoinGame(String playerData) {
        sendTCPMessage("JOIN_GAME:" + playerData);
    }

    public void sendPlayerInput(String inputData) {
        sendUDPMessage("PLAYER_INPUT:" + inputData);
    }

    public void sendPlayerReady() {
        sendTCPMessage("PLAYER_READY");
    }

    public void sendChatMessage(String message) {
        sendTCPMessage("CHAT_MESSAGE:" + message);
    }

    private void sendTCPMessage(String message) {
        if (connected && tcpOut != null) {
            tcpOut.println(message);
            System.out.println("📤 TCP to server: " + message);
        }
    }

    private void sendUDPMessage(String message) {
        if (connected && udpSocket != null) {
            try {
                byte[] data = message.getBytes("UTF-8");
                InetAddress address = InetAddress.getByName(serverIp);
                DatagramPacket packet = new DatagramPacket(data, data.length, address, UDP_PORT);
                udpSocket.send(packet);
                System.out.println("📤 UDP to server: " + message);
            } catch (IOException e) {
                System.err.println("❌ UDP send error: " + e.getMessage());
            }
        }
    }

    public List<String> getReceivedMessages() {
        List<String> messages = new ArrayList<>();
        receivedMessages.drainTo(messages);
        return messages;
    }

    public boolean isConnected() {
        return connected;
    }

    public void disconnect() {
        connected = false;

        try {
            if (tcpSocket != null) tcpSocket.close();
            if (udpSocket != null) udpSocket.close();
        } catch (IOException e) {
            System.err.println("❌ Error disconnecting: " + e.getMessage());
        }

        System.out.println("🔌 Disconnected from multiplayer server");
    }
}