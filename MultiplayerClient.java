package core;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class MultiplayerClient {
    private Socket tcpSocket;
    private DatagramSocket udpSocket;
    private String serverIp;
    private int serverPort;
    private boolean connected = false;
    private int playerId = -1;

    private BufferedReader tcpIn;
    private PrintWriter tcpOut;
    private BlockingQueue<String> receivedMessages = new LinkedBlockingQueue<>();
    private Thread tcpListenerThread;
    private Thread udpListenerThread;

    public MultiplayerClient(String serverIp, int serverPort) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
    }

    public void connect() throws IOException {
        System.out.println("🔗 Connecting to server " + serverIp + ":" + serverPort);

        // TCP kapcsolat
        tcpSocket = new Socket(serverIp, serverPort);
        tcpSocket.setTcpNoDelay(true);

        tcpIn = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
        tcpOut = new PrintWriter(tcpSocket.getOutputStream(), true);

        // UDP socket
        udpSocket = new DatagramSocket();
        udpSocket.setSoTimeout(1000);

        connected = true;

        // Indítsd el a listener szálakat
        startTCPListener();
        startUDPListener();

        System.out.println("✅ Connected to server successfully");
    }

    private void startTCPListener() {
        tcpListenerThread = new Thread(() -> {
            try {
                String message;
                while (connected && (message = tcpIn.readLine()) != null) {
                    System.out.println("📨 TCP Received: " + message);
                    receivedMessages.offer(message);
                }
            } catch (IOException e) {
                if (connected) {
                    System.err.println("❌ TCP listener error: " + e.getMessage());
                }
            } finally {
                disconnect();
            }
        });
        tcpListenerThread.setDaemon(true);
        tcpListenerThread.start();
    }

    private void startUDPListener() {
        udpListenerThread = new Thread(() -> {
            byte[] buffer = new byte[1024];

            while (connected && !udpSocket.isClosed()) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    udpSocket.receive(packet);

                    String message = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
                    System.out.println("📨 UDP Received: " + message);
                    receivedMessages.offer(message);

                } catch (SocketTimeoutException e) {
                    // Timeout ok, folytatjuk
                } catch (IOException e) {
                    if (connected) {
                        System.err.println("❌ UDP listener error: " + e.getMessage());
                    }
                }
            }
        });
        udpListenerThread.setDaemon(true);
        udpListenerThread.start();
    }

    public void sendJoinGame(String playerData) {
        sendTCPMessage("JOIN_GAME:" + playerData);
    }

    public void sendPlayerInput(String inputData) {
        if (playerId != -1) {
            sendUDPMessage(playerId + ":PLAYER_INPUT:" + inputData);
        }
    }

    public void sendPlayerPosition(float x, float y) {
        if (playerId != -1) {
            sendUDPMessage(playerId + ":PLAYER_POSITION:" + x + "," + y);
        }
    }

    public void sendPlayerAction(String action, String data) {
        if (playerId != -1) {
            sendTCPMessage("PLAYER_ACTION:" + playerId + ":" + action + ":" + data);
        }
    }

    private void sendTCPMessage(String message) {
        if (connected && tcpOut != null) {
            tcpOut.println(message);
            System.out.println("📤 TCP Sent: " + message);
        }
    }

    private void sendUDPMessage(String message) {
        if (connected && udpSocket != null) {
            try {
                byte[] data = message.getBytes("UTF-8");
                InetAddress address = InetAddress.getByName(serverIp);
                DatagramPacket packet = new DatagramPacket(data, data.length, address, serverPort + 1);
                udpSocket.send(packet);
                System.out.println("📤 UDP Sent: " + message);
            } catch (IOException e) {
                System.err.println("❌ UDP send error: " + e.getMessage());
            }
        }
    }

    public void registerUDP() {
        if (playerId != -1) {
            sendTCPMessage("UDP_REGISTER:" + playerId + ":" + udpSocket.getLocalPort());
        }
    }

    public List<String> getReceivedMessages() {
        List<String> messages = new ArrayList<>();
        receivedMessages.drainTo(messages);
        return messages;
    }

    public boolean hasMessages() {
        return !receivedMessages.isEmpty();
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
        registerUDP();
    }

    public int getPlayerId() {
        return playerId;
    }

    public boolean isConnected() {
        return connected && tcpSocket != null && !tcpSocket.isClosed();
    }

    public void disconnect() {
        connected = false;

        try {
            if (tcpSocket != null && !tcpSocket.isClosed()) {
                tcpSocket.close();
            }
            if (udpSocket != null && !udpSocket.isClosed()) {
                udpSocket.close();
            }
        } catch (IOException e) {
            System.err.println("❌ Error during disconnect: " + e.getMessage());
        }

        System.out.println("🔌 Disconnected from server");
    }
}