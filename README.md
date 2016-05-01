# Netta

Designed for ease of use, this Java library handles both Server side and Client side.

## JavaDocs
This library comes with JavaDocs in the exported versions. Simply include the docs within your project for easier integration use.

### Client
Netta provides a client class to be extended by the developer. This class handles everything from socket creation to Input Output
transmittion. The child of this class must override the ThreadAction method within Client to perform actions on received packets. 
For full class documentation, see the wiki.

### Single Client Server
The Single Client Server is designed for a one on one connection only. It accepts the connection, and then listens for packets from the accepted client until told to stop. Whenever a packet is received, the ThreadAction(Packet) method is called. By default, this method does nothing, so to add functionality to your server you must overload it in your own class.

### Mutliple Client Server
The Multi Client Server is designed to act as a multi-point connection hub for clients. Like most servers today, thiss can be used for things such as chat clients, patch servers, or game servers among others. This server starts listening and keeps listening until told otherwise. When a client connects, the ThreadAction(ClientConnection) method is called, with the new client passed. By default, this method does nothing, so to add functionality you must overload it in your own class.
