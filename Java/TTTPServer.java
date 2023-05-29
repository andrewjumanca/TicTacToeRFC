package Java;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class TTTPServer {
    private HashSet<String> availableGames = new HashSet<String>();
    private static HashMap<String, ArrayList<String>> games;
    private static HashMap<Integer, ClientData> clients;
    private static final List<String> COMMANDS = new ArrayList<String>();

    static {
        COMMANDS.add("CREA");
        COMMANDS.add("GDBY");
        COMMANDS.add("HELO");
        COMMANDS.add("JOIN");
        COMMANDS.add("LIST");
        COMMANDS.add("MOVE");
        COMMANDS.add("QUIT");
        COMMANDS.add("STAT");
    }

    private static int port = 3116;
    static ExecutorService exec = null;

    public static void main(String[] args) {
        clients = new HashMap<Integer, ClientData>();
        games = new HashMap<String, ArrayList<String>>();

        exec = Executors.newFixedThreadPool(10);
        exec.submit(() -> handleTCPRequest());
        exec.submit(() -> handleUDPRequest());

    }

    private static void handleTCPRequest() {

        try (ServerSocket server = new ServerSocket(port)) {
            server.setReuseAddress(true);
            System.out.println("TCP server started and listening on port " + port);

            while (true) {
                Socket tcpSocket = server.accept();
                System.out.println("New TCP client connected: " + tcpSocket.getInetAddress().getHostAddress());

                ClientHandlerTCP clientSock = new ClientHandlerTCP(tcpSocket);
                new Thread(clientSock).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleUDPRequest() {
        DatagramSocket udpSocket = null;
        try {
            udpSocket = new DatagramSocket(port);
            System.out.println("UDP server started and listening on port " + port);

            // while (true) {
            byte[] buffer = new byte[256];
            DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length);

            ClientHandlerUDP udpClient = new ClientHandlerUDP(udpSocket, requestPacket);
            new Thread(udpClient).start();

            // }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Example Request:
    // HELO 1 CID1
    // SESS SID2 CID2
    // BORD GID1 CID1 CID2 CID2
    //
    // |*|*|*|
    // |*|X|*|
    // |*|*|*|
    private static String callCommand(String request, int clientID) {
        String[] requestArgs = request.split("\\s+");
        String command = requestArgs[0];
        String[] args = Arrays.copyOfRange(requestArgs, 1, requestArgs.length);

        if (clients.keySet() != null) {
            if (!clients.keySet().contains(clientID)) {
                clients.put(clientID, new ClientData(""));
            }
        } else {
            clients.put(clientID, new ClientData(""));
        }

        CommandHandler ch = new CommandHandler(clients, clientID, games);
        if (COMMANDS.contains(command)) {
            return ch.handleRequest(command, args);
        } else {
            System.out.println("Invalid command: " + command);
            return "Error";
        }
    }

    // ClientHandler class
    static class ClientHandlerTCP implements Runnable {
        private Random random;
        private int id;
        private final Socket clientSocket;

        // Constructor
        public ClientHandlerTCP(Socket socket) {
            this.random = new Random();
            this.id = random.nextInt(Integer.MAX_VALUE) + 1;
            this.clientSocket = socket;
        }

        public void run() {
            PrintWriter out = null;
            BufferedReader in = null;
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    System.out.printf(" Sent from client " + this.id + ": %s\n", line);
                    String response = callCommand(line, this.id);

                    String[] responseArgs = response.split("\\s+");
                    String command = responseArgs[0];
                    String[] args = Arrays.copyOfRange(responseArgs, 1, responseArgs.length);

                    if (command.equals("SESS")) {
                        int sessionId = Integer.parseInt(args[0]);
                        String clientId = args[1];
                        clients.get(sessionId).setClientId(clientId);
                        System.out.println(clients.toString());
                    }

                    out.println(response);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class ClientHandlerUDP implements Runnable {
        private Random random;
        private int id;
        private final DatagramSocket serverSocket;
        private final DatagramPacket receivePacket;

        // Constructor
        public ClientHandlerUDP(DatagramSocket socket, DatagramPacket packet) {
            this.random = new Random();
            this.id = random.nextInt();
            this.serverSocket = socket;
            this.receivePacket = packet;
        }

        public void run() {
            try {
                while (true) {
                    serverSocket.receive(receivePacket);
                    InetAddress clientAddress = receivePacket.getAddress();
                    int clientPort = receivePacket.getPort();

                    byte[] receiveData = receivePacket.getData();
                    int length = receivePacket.getLength();

                    // Convert received data to a string
                    String receivedMessage = new String(receiveData, 0, length);

                    // Process the received message
                    System.out.printf("Received from udp client %d: %s%n", this.id, receivedMessage);

                    // Send a response back to the client
                    String responseMessage = receivedMessage.toUpperCase(); // Example: Convert to uppercase
                    byte[] responseData = responseMessage.getBytes();

                    DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, clientAddress,
                            clientPort);
                    serverSocket.send(responsePacket);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class ClientData {
        private String clientId;
        private String gameId;
    
        public ClientData(String clientId) {
            this.clientId = clientId;
            this.gameId = null; // Initialize game ID as null (optional)
        }
    
        // Getters and setters (optional) for sessionId and gameId
        public String getClientId() {
            return clientId;
        }
    
        public void setClientId(String sessionId) {
            this.clientId = sessionId;
        }
    
        public String getGameId() {
            return gameId;
        }
    
        public void setGameId(String gameId) {
            this.gameId = gameId;
        }
    }
    
}
// }
