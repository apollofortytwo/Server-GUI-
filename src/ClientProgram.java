import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import javax.swing.JFrame;

import com.esotericsoftware.kryo.serializers.JavaSerializer;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class ClientProgram extends Listener {

	// Our client object.
	static Client client;
	// IP to connect to.
	static String ip = "10.20.17.126";
	// Ports to connect on.
	static int tcpPort = 27962, udpPort = 27962;
	static String nickname = "null";
	// A boolean value.
	static boolean messageReceived = false;

	static ClientFrame clientF;

	public static void main(String[] args) throws Exception {
		Scanner sc = new Scanner(System.in);
		System.out.println("Connecting to the server...");
		// Create the client.
		client = new Client();

		// Register the packet object.
		client.getKryo().register(PacketMessage.class);
		client.getKryo().register(Character.class);
		client.getKryo().register(CharacterMove.class);
		client.getKryo().register(Color.class, new JavaSerializer());
		
		client.getKryo().setAutoReset(false);
		
		

		// Start the client
		client.start();
		// The client MUST be started before connecting can take place.

		clientF = new ClientFrame(nickname);

		// Connect to the server - wait 5000ms before failing.
		client.connect(5000, ip, tcpPort, udpPort);

		// Add a listener
		client.addListener(new ClientProgram());

		System.out.println("Connected!\n");

		while (sc.hasNextLine()) {

			PacketMessage packetMessage = new PacketMessage();
			packetMessage.message = nickname + ": " + sc.nextLine();

			client.sendTCP(packetMessage);

		}

	}

	// I'm only going to implement this method from Listener.class because I
	// only need to use this one.
	public void received(Connection c, Object p) {
		client.getKryo().register(Color.class, new JavaSerializer());
		
		// Is the received packet the same class as PacketMessage.class?
		if (p instanceof PacketMessage) {
			// Cast it, so we can access the message within.
			PacketMessage packet = (PacketMessage) p;
			System.out.println(packet.message);
		}

		if (p instanceof Character) {
			
			Character update = (Character) p;
			
			
			if (inList(update)) {
				for (Character localChar : clientF.localCharacter) {
					if (localChar.user == update.user) {
						localChar.x = update.x;
						localChar.y = update.y;
					}
				}
			} else {
				clientF.localCharacter.add(update);
			}
			clientF.paintComponent(clientF.getGraphics());
		}

	}

	public boolean inList(Character character) {
		for (Character localChar : clientF.localCharacter) {
			if (localChar.user == character.user) {
				return true;
			}
		}
		return false;

	}
}

class ClientFrame extends JFrame implements KeyListener {
	Color color;
	public ArrayList<Character> localCharacter = new ArrayList<Character>();

	ClientFrame(String nickname) {
		this.setTitle("Drawing Graphics in thisrames");
		this.setBounds(0, 0, 1000, 1000);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
		this.paintComponent(getGraphics());
		this.addKeyListener(this);
		
		
		
	}

	protected void paintComponent(Graphics g) {
		super.paintComponents(g);
		g.setColor(Color.GRAY);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		for (Character character : localCharacter) {
			g.setColor(character.color);
			g.drawRect(character.x, character.y, character.width, character.height);
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void keyPressed(KeyEvent e) {
		CharacterMove cm = new CharacterMove();
		int speed = 5;
		if (e.getKeyCode() == (KeyEvent.VK_W)) {
			cm.x += 0;
			cm.y += -speed;
		}
		if (e.getKeyCode() == (KeyEvent.VK_A)) {
			cm.x += -speed;
			cm.y += 0;
		}
		if (e.getKeyCode() == (KeyEvent.VK_S)) {
			cm.x += 0;
			cm.y += speed;
		}
		if (e.getKeyCode() == (KeyEvent.VK_D)) {
			cm.x += speed;
			cm.y += 0;
		}
		ClientProgram.client.sendUDP(cm);
	}

	@Override
	public void keyReleased(KeyEvent e) {

	}
}
