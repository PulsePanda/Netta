# Netta

Built in Java, Netta allows easy access to client and server objects. With a couple lines of code, Netta will build and deploy a fully functional and completely customizable multi-client server, single client server, and client.

## Building Netta
Netta is built with a Gradle wrapper. After cloning or downloading the repository, navigate to the root folder and open your command line. Execute `gradlew fatJar`. That will create the jar file in build/libs.

## Using Netta with your software
Netta is designed to be included as a jar in your project. Once your project is set up, include the Netta jar as one of your resources. To initialize the various parts, follow the details below or look on the wiki*.

## Wiki*
The wiki for Netta will have all the details you could ever need for implementing the software into your own projects. However, it is currently unavailable, as denoted with the *. I'm working on getting it up as quickly as possible.

## JavaDocs
For ease of use, Netta comes with Javadoc built into the source code for all necessary files. Simply import the Javadoc from the .jar file, and you're good to go! All information regarding implementation of server and/or client classes are found within the Javadoc for each class constructor, as well within the wiki*.

## Security
Built with my [Kript encryption library](https://github.com/PulsePanda/Kript), Netta utilizes the current RSA standard of network encryption. Both libraries being open source, you can know for yourself that the system is secure.

### Client
Netta provides a client class to be extended by the developer. This class handles everything from socket creation to Input Output
transmittion. The child of this class must override the ThreadAction method within Client to perform actions on received packets. 
For full class documentation, see the wiki.

### Single Client Server
The Single Client Server is designed for a one on one connection only. It accepts the connection, and then listens for packets from the accepted client until told to stop. Whenever a packet is received, the ThreadAction(Packet) method is called. By default, this method does nothing, so to add functionality to your server you must overload it in your own class.

### Single Client Server Creation Example
The point of Netta is to be easy to implement for quick network solutions. With that in mind, I wanted to develop a way to start a threaded server as easily as possible.

To create the server, extend the class SingleClientServer from Netta. The constructor takes an integer for the port, and passes it along to it's parent class. The, to start the server, simply run the class you created as a thread!

Example:
```
private Server s = new Server(1234); // this is the class I created to extend the SingleClientServer super class
Thread t = new Thread(s);
t.start();
```

With those three lines of code, you've created a server!

### Multiple Client Server
The Multi Client Server is designed to act as a multi-point connection hub for clients. Like most servers today, thiss can be used for things such as chat clients, patch servers, or game servers among others. This server starts listening and keeps listening until told otherwise. When a client connects, the ThreadAction(ClientConnection) method is called, with the new client passed. By default, this method does nothing, so to add functionality you must overload it in your own class.
  
  
## Footer - * coming soon
