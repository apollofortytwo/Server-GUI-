import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

public class ServerProgram extends Listener {

	// Server object
	static Server server;
	// Ports to listen on
	static int udpPort = 27962, tcpPort = 27962;
	ArrayList<Character> localCharList = new ArrayList<Character>();

	public static void main(String[] args) throws Exception {
		Scanner sc = new Scanner(System.in);
		System.out.println("Creating the server...");
		// Create the server
		server = new Server();

		// Register a packet class.
		server.getKryo().register(PacketMessage.class);
		server.getKryo().register(Character.class);
		server.getKryo().register(CharacterMove.class);

		// We can only send objects as packets if they are registered.

		// Bind to a port
		server.bind(tcpPort, udpPort);

		// Start the server
		server.start();

		// Add the listener
		server.addListener(new ServerProgram());

		System.out.println("Server is operational!");
		while (sc.hasNextLine()) {

			PacketMessage packetMessage = new PacketMessage();
			packetMessage.message = "host: " + sc.nextLine();

			server.sendToAllTCP(packetMessage);
		}
	}

	// This is run when a connection is received!
	public void connected(Connection c) {
		System.out.println("Received a connection from " + c.getRemoteAddressTCP().getHostString());
		System.out.println("Connection Id:" + c.getID());
		// Create a message packet.
		PacketMessage packetMessage = new PacketMessage();
		// Assign the message text.
		packetMessage.message = "Hello friend! The time is: " + new Date().toString();

		Character character = new Character();
		character.user = c.getID();
		character.x = 50;
		character.y = 50;
		character.width = 50;
		character.height = 50;
		this.localCharList.add(character);

		// Send the message
		c.sendTCP(packetMessage);
		for (Character localChar : localCharList) {
			c.sendTCP(localChar);
		}
		
		server.sendToAllTCP(character);

		// Alternatively, we could do:
		// c.sendUDP(packetMessage);
		// To send over UDP.
	}

	// This is run when we receive a packet.
	public void received(Connection c, Object p) {
		if (p instanceof PacketMessage) {
			// System.out.println("new message");
			// Cast it, so we can access the message within.
			PacketMessage packet = (PacketMessage) p;
			System.out.println(packet.message);
			server.sendToAllTCP(packet);
		}

		if (p instanceof CharacterMove) {
			// System.out.println("recieved an order to move");
			CharacterMove cm = (CharacterMove) p;
			cm.id = c.getID();

			for (Character localChar : localCharList) {
				if (localChar.user == cm.id) {
					localChar.x += cm.x;
					localChar.y += cm.y;
				}
				server.sendToAllTCP(localChar);
			}

		}
	}

	// This is run when a client has disconnected.
	public void disconnected(Connection c) {
		System.out.println("A client disconnected!");
	}
}
