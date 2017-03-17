package itemManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import aiPlanner.Main;
import interfaces.ServerListener;
import shared.Item;
import shared.ItemType;
import shared.TimedPoint;

public class Server extends Thread{
	
	private int port = 8888;
	private byte[] buffer = new byte[2048];
	private DatagramSocket dsocket;
	private DatagramPacket packet;
	
	private List<Item> lastPointsReceived;
	private int lastReceivedTimer = 0;
	private volatile boolean stop = false;
	private ServerListener eom;

	@Override
	public void run() {
		Main.printf("[SERVER]                : Started");
		while(! isInterrupted() && !stop){
			try {
				dsocket.receive(packet);
				lastReceivedTimer = Main.TIMER.getElapsedMs();
			} catch (IOException e) {
				Main.printf("[SERVER]                : Socket Closed");
				stop = true;
			}
			
			String msg = new String(buffer, 0, packet.getLength());
			String[] items = msg.split("\n");
			lastPointsReceived = new ArrayList<Item>();
			for (int i = 0; i < items.length; i++) 
	        {
				String[] coord = items[i].split(";");
				if(coord.length == 3){
		        	int x = Integer.parseInt(coord[1]);
		        	int y = Integer.parseInt(coord[2]);
		        	lastPointsReceived.add(new Item(x, y, lastReceivedTimer, ItemType.UNDEFINED));		        	
				}
	        }
			eom.receiveRawPoints(lastPointsReceived);
			packet.setLength(buffer.length);
		}
		Main.printf("[SERVER]                : Finished");
	}
	
	@Override
	public void interrupt(){
		this.dsocket.close();
		stop = true;
	}
	
	public Server(ServerListener sl){
		this.eom = sl;
		try {
			dsocket = new DatagramSocket(port);
		} catch (SocketException e1) {
			Main.printf("[SERVER]                : Erreur, DatagramSocket non initialisé");
			e1.printStackTrace();
		}
		packet = new DatagramPacket(buffer, buffer.length);
		Main.printf("[SERVER]                : Initialized");
	}
}
      