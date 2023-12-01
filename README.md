# TicTacToeRFC
Group Project Assignment for INFO 314 Networks class

### Start Up Instructions
From the TicTacToeRFC directory, run javac Java/\*.java to compile, java Java/TTTPServer to start the server, and java Java/Client to start a single client.

# RFC

## Abstract
This RFC details a network protocol for two players to play a game of Tic-Tac-Toe over a computer network.

## Status of This Memo
This document is not an Internet Standards Track specification; it is published for informational purposes, primarily for the purpose of students taking my INFO314 class at the University of Washington, Seattle to implement a protocol from RFC and hopefully have a little fun doing it. Hosts on the ARPA Internet that choose to implement a Tic Tac Toe Protocol service are expected to adopt and implement this standard.

This document is not a product of the Internet Engineering Task Force (IETF) and does not in any way represents the consensus of the IETF community. It has received almost no public review, has not been approved for publication by the Internet Engineering Steering Group (IESG), and probably should not be used for production purposes by anyone.

Information about the current status of this document, any errata, and how to provide feedback on it may be obtained at https://github.com/tedneward/INFO314-TicTacToeRFC. 

## Copyright Notice
There is no copyright associated with this document, and anyone is free to use it for whatever purposes they find useful. However, this document also carries with it no assertions or promises, implied or explicit, as to the accuracy or correctness of any statement found herein. In short, this document should not be used for anything important, ever.

## Table of Contents

1. [Status of this memo](#status-of-this-memo)
2. [History](#history)
3. [Rules of Tic-Tac-Toe](#rules-of-tic-tac-toe)
    1. [Determination of which player begins](#determination-of-which-player-begins)
    2. [Restrictions around token placement](#restrictions-around-token-placement)
    3. [Game termination](#game-termination)
4. [Detailed Description](#detailed-description)
    1. [Description of terms](#description-of-terms)
    2. [TCP Based Service](#tcp-based-tic-tac-toe-service)
    3. [UDP Based Service](#udp-based-tic-tac-toe-service)
    4. [Session](#session)
    5. [Creating a game](#creating-a-game)
    6. [Finding a game](#finding-a-game)
    7. [Joining a game](#joining-a-game)
    8. [Making a move](#making-a-move)
    9. [Getting game status](#getting-game-status)
    10. [Timeout](#timeout)
5. [Message reference](#message-reference)
    * [BORD](#bord)
    * [CREA](#crea)
    * [GAMS](#gams)
    * [GDBY](#gdby)
    * [HELO](#helo)
    * [JOIN](#join)
    * [JOND](#jond)
    * [LIST](#list)
    * [MOVE](#move)
    * [QUIT](#quit)
    * [SESS](#sess)
    * [STAT](#stat)
    * [TERM](#term)
    * [YRMV](#yrmv)
5. [References](#references)

## History
An early variation of the game was played in the Roman Empire, around the 1st century B.C. It was called "terni lapilli," which means "three pebbles at a time." The game's grid markings have been found chalked all over Roman ruins. Evidence of the game was also found in ancient Egyptian ruins.

The first print reference to "noughts and crosses," the British name for the game, appeared in 1864. The first print reference to a game called "tick-tack-toe" occurred in 1884 but referred to a children's game played on a slate.

## Rules of Tic-Tac-Toe
The game of Tic-Tac-Toe, also known as "Naughts and Crosses", is played between two players on a 3x3 square grid. The goal is for each player to create a contiguous straight (in a row) placement of three of their tokens (labeled either "X" or "O") on the grid. Players alternate turns placing a token on the grid in one of the nine spaces.

### Determination of which player begins
Various means are used to determine which player plays first: a coin flip is common, although other means are also common. In some cases, players may agree to allow the player losing the previous game to go first, since going first is commonly considered to be an advantage in the game.

### Restrictions around token placement
Players may place their token in any unoccupied square; that is, they may not place a token in a square occupied by an opponent.

### Game termination
The game is over as soon as one player has achieved three-in-a-row; not of the grid need be filled.

If all nine squares are filled and no player has three-in-a-row, the game is a stalemate (also sometimes called a "cats" game, for reasons lost to history), and no winner is declared.

## Detailed Description
The following describes the expectations of both client and server in the Tic-Tac-Toe Protocol (abbreviated "TTTP" in places where the full name would be unwieldy).

### Description of terms
This protocol assumes the existence of a process running on a network that acts as the primary arbiter of the process of playing the game; this process is called the "server", and is referred to as such in this document. Similarly, processes that represent the players of the games are referred to as "clients". Note that this document makes no prescriptive judgment as to whether the "server" and "client" are separate processes; it is entirely feasible for one process to serve as both "client" and "server" simultaneously, at least for one client.

Clients interact with the server in uniquely-identifed "sessions". These are indentified by unique strings of non-whitespace ASCII characters and are called "session identifiers". The server is permitted complete freedom in how it generates session identifiers, save that they must be unique at any time, and cannot exceed 80 characters in length.

Clients play each other in games that are also uniquely-identified. These "game identifiers" are similar to session identifiers in that they are strings of non-whitespace ASCII characters that cannot exceed 80 characters in length.

### TCP Based Tic Tac Toe Service
A Tic-Tac-Toe service is defined as a connection based application on TCP listening for TCP connections on TCP port 3116[1](#1). Once a [connection is established](#session) a Tic Tac Toe session is considered to have started and remains in an "alive" state until either the client or the server chooses to [close the session](). In the TCP session, either client or server may initiate the sending of messages in a fully-duplexed fashion.

A TicTacToe URL using TCP is denoted using the scheme `t3tcp:` followed by the host and optional port. `t3tcp://localhost` would reference the server on the local machine on the default TCP port (3116). If a different port were specified, it would read as `t3tcp://localhost:4567`.

### UDP Based Tic Tac Toe Service
Another Tic-Tac-Toe service is defined as a datagram based application on UDP listening for UDP datagrams on UDP port 3116[1](#1). When a datagram is received, a Tic Tac Toe session is considered to have started, and remains in an "alive" state until either the client or server [sends a "close" message](#gdby) or the server [has not heard from the client over a period of time](#timeout). All datagram communication occurs over port 3116[1](#1).

A TicTacToe URL using TCP is denoted using the scheme `t3udp:` followed by the host and optional port. `t3udp://localhost` would reference the server on the local machine on the default UDP port (3116). If a different port were specified, it would read as `t3udp://localhost:4567`.

### Session
A client must connect with a server before any game can be created. This is called "establishing a session" and requires the client to send a [greeting message](#helo) to the server, sending the protocol version the client understands and self-identifying the client with a unique string to be used as part of the protocol later. Once the client has identified, the server [acknowledges](#sess) receipt, including the version of the protocol that will be used (see the section on version negotiation) a "session identifier" which uniquely identifies this session.

The server is permitted to use any sort of scheme for session identifiers, so long as they do not include whitespace. Thus, values of "1234" (integer values), "0cb8d694-3999-4bc6-8351-0e978b62a08d" (GUIDs), or "57.34" (floating point values) would all be acceptable. Some servers may choose the floating-point scheme to allow sessions to indicate relationships to one another; for example, two players may be in sessions "4.1" and "4.2", respectively, with observers on sessions "4.3" and "4.4". However, each of these sessions is considered unique, has no special relationship to one another, and is purely left as a server implementation detail.

### Creating a game
A client can choose to create a new game (in essence looking for another player) by sending a [create](#crea) message to the server. This will create a new, unique game on the server that is still missing a player, and the server will respond with a [joined-game](#jond) message; this is the same response as if the client [joined](#join) an existing game. Since there is no second player yet in this game, the client will receive no further messages from the server until a (different) client joins the game.

### Finding a game
A client can also ask to see a list of open games on the server by sending a [list](#list) message to the server. The server will respond with a [list of open games](#gams). From there, the client can select a game by identifier and ask to [join](#join) it.

### Joining a game
A client can ask to join an open game by sending a [join](#join) message to the server.

Assuming the game is now filled with its minimum number of players, the server will respond with a message to all clients indicating whose move (referenced by client identifier) is first. That player will be given the moniker "X".

### Making a move
A client can make a move on its turn by sending a [move](#move) message indiciating the position on the board it wishes to occupy. The server will respond with a [board](#bord) message indicating the current status of the game board; if the move is accepted, the player's move will be present, if it was illegal in some fashion, the board status will remain the same.

### Getting game status
A client can request a complete status of the game by sending a [game status](#stat) message, which sends a response that includes 

### Timeout
In the event that the server has not received a message from either of its player clients in a configurable period of time, the server is free to send a [close](#term) message to remaining players and  

### Version negotiation
In the event that the client and server are each using different versions of this protocol, it is expected that the higher-versioning participant will degrade gracefully to use the lower-protocol version asked for by its opposite. Thus, if a version 2 client contacts a version 1 server, the client should degrade to version 1; similarly, if the version 1 client contacts a version 2 server, the server should degrade to version 1 for that client. This should not affect other clients on that server; the server is free to use the highest-understood version for each client independently of others.

## Message Reference
Messages sent in this protocol consist of a 4-letter ASCII command phrase, with additional information following, ending in a CRLF terminator. All messages are assumed to be sent using the 7-bit ASCII character set except where otherwise specified.

**Field Delimiters:** The parts of TTTP messages are delimited by whitespace characters (space) or (tab). Multiple whitespace characters will be treated as a single field delimiter.

**Newlines:** TTTP uses ␍ followed by ␊ (␍␊, 0x0D0A) to terminate protocol messages, at the end of any protocol message body parameters.

**Character Encoding:** Session, client, and game identifiers should be ASCII characters for maximum interoperability. Due to language constraints and performance, some clients may support UTF-8 subject names, as may the server, but no guarantees of non-ASCII support are provided.

**Case:** All TTTP message command headers are case-insensitive, but it is recommended that all-upper-case be used for consistency.

### BORD
This message is sent by the server to a client to indicate the current status of a game. If there is not enough players to be playing this game, the command will respond solely with the game-identifier and the client-identifier of the other player. If there are two players in the game, the message will include: the game identifier; the client-identifier of the two players (the X player--that is the player who went first--comes first in the list); the client-identifier of the player to move next; and a linear representation of the board, with player tokens (`X`, `O`, or `*` if neither player has played there) separated by pipe (`|`) symbols, from upper-left to lower-right in a left-to-right, top-to-bottom fashion. If a winner of the game has been determined, it will appear after the game board information. Example: `BORD GID1 CID1 CID2 CID1 |*|*|*|X|O|X|*|*|*|` for a game that is currently as-yet still playing; that same game may later look like `BORD GID1 CID1 CID2 CID2 |X|*|O|X|O|X|X|*|O| CID1` to indicate the X player's victory after that player's move. Notice that the "next player to move" is listed as CID2 even though the game is terminated; the "next player to move" value is expected to be ignored by clients in the event that the game is over.

### CREA
Sent by a client to the server to create a new game. The body of this message must include the client's client identifier. This client is assumed to be one of the players. The server should respond with a [join](#join) message.

### GAMS
Sent by the server in response to a LIST request. This is a list of all games that are currently looking for players. (If the client specified additional games, the server will include those in the response.) The response packet will be the command `GAMS`, followed by a whitespace-separated list of game identifiers currently meeting the requested criteria.

### GDBY
Sent by either the client or the server to its counterpart to indicate it is finished with the session. If this is a client who is sending the message, it is assumed to implicitly be sending a [quit](#quit) message to the game(s) in which it is a player.

### HELO
Sent by a client to a server to initiate a session with the server. The command is expected to include the version of the protocol understood by the client, and an identifier by which the client identifies itself--an email address for a human, for example, or a GUID for an autonomous agent. Examples: `HELO 1 ted@tedneward.com` or `HELO 1 0cb8d694-3999-4bc6-8351-0e978b62a08d`. The server is expected to respond with a [session initiation](#sess) command.

### JOIN
Sent by the client to the server to join a given game identified in the body by the game-identifier. The server will respond with a "JOND" message to indicate successful join.

### JOND
Sent by the server to the client to indicate the client has successfully joined the game. The message will include the client identifier of the client making the request, and the game identifier. If the game now has its necessary number of players, the server will then also send out a YRMV message to all clients indicating the player whose move it is.

### LIST
This is sent by the client to the server to ask it for a list of all the currently-open games. "Open" games are those that do not have a full complement of players. If the LIST message is sent with a body of `CURR` following it, then the server responds with a list of all games currently open and in-play. If the LIST message is sent with a body of `ALL`, the server responds with a list of all games it currently holds: open, in-play, and finished. The server responds with a [game list](#gams) message listing all of the games that meet the client-specified criteria.

### MOVE
This is sent by the client to the server to attempt to make a move. The message is followed by a representation of the board, either a linear value (counting the squares from the upper-left, going left-to-right then top-to-bottom) or a "X,Y" cartesian representation with the origin in the lower-left (so that the center square is "2,2", the lower-left is "1,1" and the upper-right is "3,3"). Either representation must be accepted by the server. The server is responsible for evaluating the correctness (legality, among other validation) of the move. Regardless of the move's legality, the server responds with a `BORD` message, indicating the current state of the game; if the player's move was successful, the `BORD` will reflect that it is another player's turn, and if it was not, the game state will be unchanged. If the move was successful, after the `BORD` message the server should notify all players by sending a `YRMV` message indicating whose move it is.

### QUIT
Sent by the client to indicate that the player wishes to abandon the game without terminating the session. The QUIT message is expected to include the game identifier of the game being quit. The player opposite the quitting player is immediately declared the winner of the game, and the game is considered to be concluded/finished.

### SESS
This is sent by the server to the client to indicate the server has officially created a unique session between it and the client. The command is expected to include the version of the protocol the server will use with the client, and the unique session identifier itself. These values will be separated by whitespace. Examples: `SESS 1 57` or `SESS 1 0cb8d694-3999-4bc6-8351-0e978b62a08d`.

### STAT
This message is sent by a client to the server; it expects the client to pass a game-identifier body, indicating the game whose status is requested.

### TERM
This message indicates the termination of a game. The message includes the game-identifier, and the client-identifier of the player who is declared the winner. For games which are stalemate, no client-identifier is sent after the game-identifier. The last parameter in the body is a constant string, "KTHXBYE", indicating the end of the TERM message.

### YRMV
This message is sent by the server to al of the participant clients in a game to indicate which player's move is currently accepted. This message always includes the command, the game identifier, and the client identifier whose move is currently accepted. Once this message is sent, the server will not accept any [move](#move) commands from a client other than the one whose identifier was included in this message.

### Client-sent messages

* [CREA](#crea)
* [GDBY](#gdby)
* [HELO](#helo)
* [JOIN](#join)
* [LIST](#list)
* [MOVE](#move)
* [QUIT](#quit)
* [STAT](#stat)

### Server-sent messages

* [BORD](#bord)
* [GAMS](#gams)
* [GDBY](#gdby)
* [JOND](#jond)
* [SESS](#sess)
* [TERM](#term)
* [YRMV](#yrmv)

## Example of use

Two clients, CID1 and CID2, are going to play a game of TicTacToe using this protocol. The first client connects to the server.

> CID1 -> server: `HELO 1 CID1`

The server responds with a new session identifier.

> server -> CID1: `SESS SID1 CID1`

CID1 wants to create a game.

> CID1 -> server: `CREA CID1`

The server responds, indicating the new game's identifier and that CID1 is already joined to it.

> server -> CID1: `JOND CID1 GID1`

CID1 will now not receive any additional messages from the server until another client joins its open game. Meanwhile, CID2 connects to the server.

> CID2 -> server: `HELO 1 CID2`

The server responds by creating a new session and connecting CID2 to it.

> server -> CID2: `SESS SID2 CID2`

CID2 doesn't know the identifier of the open game, so they ask for a list of all open games.

> CID2 -> server: `LIST`

The server has only one open game, so it sends back that list.

> server -> CID2: `GAMS GID1`

Is this the game CID2 wants?

> CID2 -> server: `STAT GID1`

Server responds with the game's status, which hasn't yet started, so there's not much to display yet.

> server -> CID2: `BORD GID1 CID1`

Yep, that's the one. Let's join it.

> CID2 -> server: `JOIN GID1`

Server says that's OK, you're joined.

> server -> CID2: `JOND CID2 GID1`

Server now responds with a YRMV message to each of the players indicating that it is CID1's turn.

> server -> CID1: `YRMV GID1 CID1`

> server -> CID2: `YRMV GID1 CID1`

CID1 considers carefully all of the available options, embraces the conventional opening, and chooses center square.

> CID1 -> server: `MOVE GID1 5`

Server checks the move, which is legal, and sends out a board-update message in response.

> server -> CID1: `BORD GID1 CID1 CID2 CID2 |*|*|*|*|X|*|*|*|*|`

Since it's now CID2's turn, server now sends out messages to each of the players.

> server -> CID1: `YRMV GID1 CID2`

> server -> CID2: `YRMV GID1 CID2`

(To be continued...)

## Footnotes

### 1
This port is chosen because it is the combination of "3" and "116", the ASCII code for the character "t"; in short, "3t", referencing the name starts with three simultaneous "t" characters.

## References

* https://www.exploratorium.edu/brain_explorer/tictactoe.html

* https://www.thesprucecrafts.com/tic-tac-toe-game-rules-412170
