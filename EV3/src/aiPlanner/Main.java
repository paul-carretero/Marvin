/*
 * TODO : implementer un compensateur d'erreur en temps réel.
 * ajouter volatile aux variables qui sont lu partout
 */
package aiPlanner;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import area.*;
import lejos.hardware.ev3.LocalEV3;
import lejos.robotics.navigation.Pose;
import shared.Color;
import shared.Timer;

public class Main{
	
	// POSITION EN MILIMETRE (oui c'est bête mais pour la "pose" ça évite des convertions multiples => librairie stupide).
	
	public static final int X_INITIAL 				= 1500;
	public static final int Y_INITIAL 				= 2700;
	public static final int H_INITIAL 				= -90;

	
	public static final int Y_OBJECTIVE_WHITE		= 300;
	public static final int Y_DEFEND_WHITE 			= 2700;
	
	public static final int Y_BOTTOM_WHITE			= 300;
	public static final int Y_TOP_WHITE 			= 2700;
	
	public static final int Y_GREEN_LINE 			= 900;
	public static final int Y_BLUE_LINE 			= 2100;
	public static final int Y_BLACK_LINE 			= 1500;
	
	public static final int X_RED_LINE 				= 1500;
	public static final int X_YELLOW_LINE 			= 500;
	public static final int X_BLACK_LINE 			= 1000;
	
	public static final int PRESSION				= 0;
	public static final int HAS_MOVED 				= 1;
	public static final int HAND_OPEN 				= 2;
	public static final int HAVE_PALET				= 3;
	
	public static final int RADAR_MAX_RANGE			= 650;
	public static final int RADAR_MIN_RANGE			= 350;
	public static final int RADAR_RADIUS			= 10;
	
	public static final String COLOR_SENSOR 		= "S4";
	public static final String TOUCH_SENSOR 		= "S2";
	public static final String US_SENSOR    		= "S3";
	
	public static final float WHEEL_DIAMETER        = 57f;
	public static final float DISTANCE_TO_CENTER    = 61.2f;
	public static final String LEFT_WHEEL 			= "C";
	public static final String RIGHT_WHEEL			= "B";
	public static final String GRABER    			= "D";
	public static final float LINEAR_ACCELERATION	= 10.0f;
	
	public static final int   ROTATION_SPEED		= 280;
	public static final int   SEARCH_ROTATION_SPEED = 70;
	
	public static final int   RESEARCH_SPEED		= 120; // mm/s
	public static final int   CRUISE_SPEED			= 240; // mm/s
	public static final int   MAX_SPEED				= 360; // mm/s
	
	public static final int   GRABER_TIMER			= 1500; // 2000 en vrai sa dépan dé foi xDD
	public static final int   GRABER_SPEED			= 800; // 800 en vrai
	public static final int   DROP_DELAY			= 500;

	public final static boolean[] GLOBALSTATE 		= new boolean[4]; // utiliser la méthode synchronisée pour ecriture
	
	public static final Timer TIMER 				= new Timer();
		
	public static final Area[] AREAS				= {
		new TopArea(0),
		new BorderLeftArea(1,Y_TOP_WHITE,Color.WHITE,Y_BLUE_LINE,Color.BLUE),
		new InnerArea(2,X_BLACK_LINE,Color.BLACK,X_YELLOW_LINE,Color.YELLOW,Y_TOP_WHITE,Color.WHITE,Y_BLUE_LINE,Color.BLUE),
		new InnerArea(3,X_RED_LINE,Color.RED,X_BLACK_LINE,Color.BLUE,Y_TOP_WHITE,Color.WHITE,Y_BLUE_LINE,Color.BLUE),
		new BorderRightArea(4,Y_TOP_WHITE,Color.WHITE,Y_BLUE_LINE,Color.BLUE),	
		new BorderLeftArea(5,Y_BLUE_LINE,Color.BLUE,Y_BLACK_LINE,Color.BLACK),
		new CenterArea(6),
		new BorderRightArea(7,Y_BLUE_LINE,Color.BLUE,Y_BLACK_LINE,Color.BLACK),		
		new BorderLeftArea(8,Y_BLACK_LINE,Color.BLACK,Y_GREEN_LINE,Color.GREEN),
		new BorderRightArea(9,Y_BLACK_LINE,Color.BLACK,Y_GREEN_LINE,Color.GREEN),
		new BorderLeftArea(10,Y_GREEN_LINE,Color.GREEN,Y_BOTTOM_WHITE,Color.WHITE),
		new InnerArea(11,X_BLACK_LINE,Color.BLACK,X_YELLOW_LINE,Color.YELLOW,Y_GREEN_LINE,Color.GREEN,Y_BOTTOM_WHITE,Color.WHITE),
		new InnerArea(12,X_RED_LINE,Color.RED,X_BLACK_LINE,Color.BLACK,Y_GREEN_LINE,Color.GREEN,Y_BOTTOM_WHITE,Color.WHITE),
		new BorderRightArea(13,Y_GREEN_LINE,Color.GREEN,Y_BOTTOM_WHITE,Color.WHITE),
		new BottomArea(14),
		new DefaultArea(15)
	};
	
	public static Area getArea(int id){
		return AREAS[id];
	}
	
	/*public static float distancePointDroite(float px, float py, float a, float b, float c){
		return (float) ( (Math.abs( (a*px + b*py + c) )) / (Math.sqrt(a*a + b*b)));
	}*/
	
	public static void poseRealToSensor(Pose p){
		p.moveUpdate(100);
	}
	
	public static void poseSensorToReal(Pose p){
		p.moveUpdate(-100);
	}
	
	public static boolean areApproximatlyEqual(int x, int y, int marge){
		return (x < (y + marge) && x > (y - marge) );
	}
	
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
			InetAddress IPAddress = InetAddress.getByName("192.168.1.10");
			byte[] sendData = new byte[256];
			sendData = str.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 4242);
			clientSocket.send(sendPacket);
			clientSocket.close();
		} catch (Exception e) {
			System.out.println("[ERREUR] : impossible d'envoyer les données");
		}
	}


	public static void main(String[] args) {
		LocalEV3.get().getLED().setPattern(2);
		System.out.println("       ___");
		System.out.println(" _____/_o_\\_____");
		System.out.println("(==(/_______\\)==)");
		System.out.println(" \\==\\/     \\/==/");
		new Marvin();
		printf("@@@ The first ten million years were the worst. And the second ten million: they were the worst, too. The third ten million I didn't enjoy at all. After that, I went into a bit of a decline. @@@");
	}

}
