/*
 * TODO : implementer un compensateur d'erreur en temps réel.
 * ajouter volatile aux variables qui sont lu partout
 */
package aiPlanner;

import java.net.*;

import shared.Timer;

public class Main{
	
	public static final int X_INITIAL 				= 100; // ou 50 ou 150
	public static final int Y_INITIAL 				= 0; // 30
	public static final int H_INITIAL 				= 90;
	
	/* DEPART COTE 2
	public static final int X_INITIAL 				= 100; // ou 50 ou 150
	public static final int Y_INITIAL 				= 270;
	public static final int H_INITIAL 				= -90;
	*/
	
	public static final int X_OBJECTIVE_WHITE		= 270;
	public static final int X_DEFEND_WHITE 			= 30;
	
	public static final int X_GREEN_LINE 			= 90;
	public static final int X_BLUE_LINE 			= 210;
	public static final int X_BLACK_LINE 			= 150;
	
	public static final int Y_RED_LINE 				= 150;
	public static final int Y_YELLOW_LINE 			= 50;
	public static final int Y_BLACK_LINE 			= 100;
	
	public static final int STARTED 				= 0;
	public static final int HAS_MOVED 				= 1;
	public static final int CALIBRATED 				= 2;
	public static final int HAND_OPEN 				= 3;
	public static final int HAVE_PALET				= 4;
	
	public static final int COLOR_BLUE 				= 0;
	public static final int COLOR_BLACK 			= 1;
	public static final int COLOR_WHITE 			= 2;
	public static final int COLOR_GREY 				= 3;
	public static final int COLOR_YELLOW 			= 4;
	public static final int COLOR_RED 				= 5;
	public static final int COLOR_GREEN 			= 6;
	
	public static final String COLOR_SENSOR 		= "S4";
	public static final String TOUCH_SENSOR 		= "S2";
	public static final String US_SENSOR    		= "S3";
	
	public static final float WHEEL_DIAMETER        = 56;
	public static final float DISTANCE_TO_CENTER    = 62.525f;
	public static final String LEFT_WHEEL 			= "C";
	public static final String RIGHT_WHEEL			= "B";
	public static final String GRABER    			= "D";
	public static final float LINEAR_ACCELERATION	= 0.2f;
	
	public static final int   ROTATION_SPEED		= 280;
	public static final int   SEARCH_ROTATION_SPEED = 70;
	
	public static final int   RESEARCH_SPEED		= 120; // cm/s
	public static final int   CRUISE_SPEED			= 240; // cm/s
	public static final int   MAX_SPEED				= 360; // cm/s
	
	public static final int   RADAR_CALIBRATION		= -9; // cm (ajouté)
	public static final int   RADAR_MIN				= 25; // cm
	public static final int   RADAR_MAX				= 70; // cm
	public static final int   RADAR_RADIUS			= 20;
	
	public static final int   GRABER_TIMER			= 1500; // 2000 en vrai sa dépan dé foi xDD
	public static final int   GRABER_SPEED			= 500; // 800 en vrai

	public final static boolean[] GLOBALSTATE 		= new boolean[5]; // utiliser la méthode synchronisée pour ecriture
	
	public static final Timer TIMER 				= new Timer();
		
	/*public static final Area[] AREAS				= {
		new Area('A',0,COLOR_BLUE,0,COLOR_BLUE,0,COLOR_BLUE,0,COLOR_BLUE),
		new Area('B',0,COLOR_BLUE,0,COLOR_BLUE,0,COLOR_BLUE,0,COLOR_BLUE),
		new Area('C',0,COLOR_BLUE,0,COLOR_BLUE,0,COLOR_BLUE,0,COLOR_BLUE),
		new Area('D',0,COLOR_BLUE,0,COLOR_BLUE,0,COLOR_BLUE,0,COLOR_BLUE),
		new Area('E',0,COLOR_BLUE,0,COLOR_BLUE,0,COLOR_BLUE,0,COLOR_BLUE),
		new Area('F',0,COLOR_BLUE,0,COLOR_BLUE,0,COLOR_BLUE,0,COLOR_BLUE),
		new Area('G',0,COLOR_BLUE,0,COLOR_BLUE,0,COLOR_BLUE,0,COLOR_BLUE),
		new Area('H',0,COLOR_BLUE,0,COLOR_BLUE,0,COLOR_BLUE,0,COLOR_BLUE),
		new Area('I',0,COLOR_BLUE,0,COLOR_BLUE,0,COLOR_BLUE,0,COLOR_BLUE),
		new Area('J',0,COLOR_BLUE,0,COLOR_BLUE,0,COLOR_BLUE,0,COLOR_BLUE),
		new Area('K',0,COLOR_BLUE,0,COLOR_BLUE,0,COLOR_BLUE,0,COLOR_BLUE),
		new Area('L',0,COLOR_BLUE,0,COLOR_BLUE,0,COLOR_BLUE,0,COLOR_BLUE),
		new Area('M',0,COLOR_BLUE,0,COLOR_BLUE,0,COLOR_BLUE,0,COLOR_BLUE),
		new Area('N',0,COLOR_BLUE,0,COLOR_BLUE,0,COLOR_BLUE,0,COLOR_BLUE),
		new Area('O',0,COLOR_BLUE,0,COLOR_BLUE,0,COLOR_BLUE,0,COLOR_BLUE)
	};*/
	
	synchronized public static void setState(int i, boolean state){
		GLOBALSTATE[i] = state;
	}
	
	public static boolean getState(int i){
		return GLOBALSTATE[i];
	}

	public static void printf(String str){
		try {
			str = str + "#";
			DatagramSocket clientSocket = new DatagramSocket();
			InetAddress IPAddress = InetAddress.getByName("192.168.43.58");
			byte[] sendData = new byte[256];
			sendData = str.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 4242);
			clientSocket.send(sendPacket);
			clientSocket.close();
		} catch (Exception e) {
			System.out.println("[MAIN] ERREUR : impossible d'envoyer les données");
		}
	}


	public static void main(String[] args) {
		System.out.println("       ___");
		try{
			new Marvin(0);
		}
		catch (Exception e) {
			printf(e.getMessage());
		}
		
		printf("@@@ The first ten million years were the worst. And the second ten million: they were the worst, too. The third ten million I didn't enjoy at all. After that, I went into a bit of a decline. @@@");
	}

}
