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

public class Server extends Thread{
	
	private int port = 8888;
	private byte[] buffer = new byte[2048];
	private DatagramSocket dsocket;
	private DatagramPacket packet;
	
	private List<Item> lastPointsReceived;
	private int lastReceivedTimer	= 0;
	private volatile boolean stop	= false;
	private ServerListener eom;
	
	private static int		xOffset = 0;
	private static int		yOffset = 0;

	public Server(ServerListener sl){
		this.eom 				= sl;
		this.packet 			= new DatagramPacket(this.buffer, this.buffer.length);
		this.lastPointsReceived	= new ArrayList<Item>();
		
		try {
			this.dsocket = new DatagramSocket(this.port);
		} catch (SocketException e1) {
			Main.printf("[SERVER]                : Erreur, DatagramSocket non initialisé");
			e1.printStackTrace();
		}
		
		Main.printf("[SERVER]                : Initialized");
	}
	
	public static void defineOffset(int x, int y){
		xOffset = x;
		yOffset = y;
	}
	
	@Override
	public void run() {
		Main.printf("[SERVER]                : Started");
		this.setPriority(Thread.NORM_PRIORITY);
		int aJeter = 0;
		while(! isInterrupted() && !this.stop){
			try {
				this.dsocket.receive(this.packet);
				this.lastReceivedTimer = Main.TIMER.getElapsedMs();
			} catch (IOException e) {
				Main.printf("[SERVER]                : Socket Closed");
				this.stop = true;
			}
			if(aJeter == 0){
				String msg = new String(this.buffer, 0, this.packet.getLength());
				String[] items = msg.split("\n");
				this.lastPointsReceived.clear();
				for (int i = 0; i < items.length; i++) 
		        {
					String[] coord = items[i].split(";");
					if(coord.length == 3){
			        	int x = Integer.parseInt(coord[1]);
			        	int y = 300 - Integer.parseInt(coord[2]); // convertion en mode 'genius'
			        	this.lastPointsReceived.add(new Item((x*10) + xOffset, (y*10) + yOffset, this.lastReceivedTimer, ItemType.UNDEFINED));		        	
					}
		        }
				this.eom.receiveRawPoints(this.lastReceivedTimer,this.lastPointsReceived);
				this.packet.setLength(this.buffer.length);
			}
			aJeter ++;
			if(aJeter == 3){
				aJeter = 0;
			}
		}
		Main.printf("[SERVER]                : Finished");
	}
	
	@Override
	public void interrupt(){
		this.dsocket.close();
		this.stop = true;
		//this.interrupt(); ???
	}

}
      