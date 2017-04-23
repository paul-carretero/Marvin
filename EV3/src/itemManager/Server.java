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

/**
 * Thread g�rant la reception des position des item du terrain de la cam�ra.
 * Cr�er une liste de point qui sera trait�e par EyeOfMarvin.
 * @see Item
 */
public class Server extends Thread{
	
	/**
	 * port sur lequel recevoir les positions des item du terrain fournies par la cam�ra
	 */
	private static final int PORT	= 8888;
	
	/**
	 * buffer pour la reception des donn�es
	 */
	private byte[] buffer			= new byte[2048];
	
	/**
	 * temps en ms de la derni�re reception des positions
	 */
	private int lastReceivedTimer	= 0;
	
	/**
	 * vrai si le Thread doit se terminer, faux sinon
	 */
	private volatile boolean stop	= false;
	
	/**
	 * Socket du serveur
	 */
	private DatagramSocket dsocket;
	
	/**
	 * paquet UDP re�u contenant les positions des items
	 */
	private DatagramPacket packet;
	
	/**
	 * Liste d'item contenant les points (bruts) re�u de la cam�ra.
	 */
	private List<Item> lastPointsReceived;
	
	
	/**
	 * EyeOfMarvin traitant la liste de position g�n�r�e
	 */
	private ServerListener eom;
	
	/**
	 * d�placement sur l'axe des X a appliquer aux donn�es re�ues
	 */
	private static int		xOffset = 0;
	
	/**
	 * d�placement sur l'axe des Y a appliquer aux donn�es re�ues
	 */
	private static int		yOffset = 0;

	/**
	 * @param sl un objet (EyeOfMarvin dans ce cas) permettant de traiter la reception de la liste de points.
	 */
	public Server(ServerListener sl){
		super("Server");
		this.eom 				= sl;
		this.packet 			= new DatagramPacket(this.buffer, this.buffer.length);
		this.lastPointsReceived	= new ArrayList<Item>();
		
		try {
			this.dsocket = new DatagramSocket(PORT);
		} catch (SocketException e1) {
			Main.printf("[SERVER]                : Erreur, DatagramSocket non initialis�");
			e1.printStackTrace();
		}
		
		Main.printf("[SERVER]                : Initialized");
	}
	
	/**
	 * @param x d�placement sur l'axe des X a appliquer aux donn�es re�ues
	 * @param y d�placement sur l'axe des Y a appliquer aux donn�es re�ues
	 */
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
      