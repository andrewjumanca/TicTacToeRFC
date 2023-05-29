package Java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class CommandHandler {

    private AtomicInteger gameIdCounter = new AtomicInteger(1);
    private HashMap<Integer, TTTPServer.ClientData> clientList;
    private int currentClient;
    private HashMap<String, ArrayList<String>> games;

    public CommandHandler(HashMap<Integer, TTTPServer.ClientData> clientList, int clientID, HashMap<String, ArrayList<String>> games) {
        this.clientList = clientList;
        this.currentClient = clientID;
        this.games = games;
    }

    public String handleRequest(String command, String[] parameters) {
        switch (command) {
            case ("CREA"):
                return createGame(parameters);
            case ("GDBY"):
                // print Goodbye message from client
                quit(command);
                break;
            case ("HELO"):
                return createSession(parameters);
            case ("JOIN"):
                break;
            case ("LIST"):
                break;
            case ("MOVE"):
                move(command);
                break;
            case ("QUIT"):
                break;
            case ("STAT"):
                break;
            default:
                break;
        }
        return "";
    }

    /**
     * CREA
     * Client-sent message
     * 
     * @param sessionMsg - recieved client session message
     * @return void - instantiates new game session for the client given the
     *         specified individual identifier
     */
    private String createGame(String[] parameters) {
        String clientId = parameters[0];
        String gameId = generateGameId();

        System.out.println("Generated Game ID: " + gameId);
        return "JOND " + clientId + " " + gameId;
    }

    private String generateGameId() {
        int gameId = gameIdCounter.getAndIncrement();
        return "SID" + gameId;
    }

    /**
     * HELO
     * Client-sent message
     * 
     * @return session identifier
     */
    private String createSession(String[] parameters) {
        String version = parameters[0];
        String clientId = parameters[1];

        if (clientList.keySet().contains(currentClient) && clientList.get(currentClient).getClientId().equals("")) {
            for (TTTPServer.ClientData client : clientList.values()) {
                if (client.getClientId().equals(clientId)) {
                    return "ERROR: Identifier already taken";
                }
            }
            return "SESS " + currentClient + " " + clientId;
        }
        return "ERROR: Session Already Created";

    }

    /**
     * JOIN
     * Client-sent message
     * Connects to an on-going, already-created game specified by the gameIdentifier
     * parameter.
     * 
     * @param gameIdentifier - recieved client session message
     * @return void - instantiates new game session for the client given the
     *         specified individual identifier
     */
    private void joinGame(String gameIdentifier) {

    }

    private String selectedGame() {
        // Select from the listed games
        return "";
    }

    /**
     * LIST
     * Client-sent message
     * 
     * @return HashSet<String>
     */
    private HashSet<String> listAvailableGames() {
        // TODO: Need To Create Globa HashSet of all The Games
        // return avaliableGames;
        return null;
    }

    /**
     * MOVE
     * Storing Cleint Move
     * 
     * @param moveRequest
     */

    private void move(String movRequest) {

    }

    /**
     * QUIT (GDBY)
     * Client-sent message
     * 
     * @param gameIdentifier -
     */
    private void quit(String gameIdentifier) {

    }

    /**
     * STAT:
     * Client-sent message
     * 
     * @param gameIdentifier -
     */

    // private String[] stats(String gameIdentifier){
    // return [] ;
    // }

}