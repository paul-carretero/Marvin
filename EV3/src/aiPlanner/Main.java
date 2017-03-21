/*
 * TODO : implementer un compensateur d'erreur en temps réel.
 * ajouter volatile aux variables qui sont lu partout
 */
package aiPlanner;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;

import area.*;
import lejos.hardware.Sound;
import lejos.hardware.lcd.Image;
import shared.Timer;

public class Main{
	
	// POSITION EN MILIMETRE (oui c'est bête mais pour la "pose" ça évite des convertions multiples => librairie stupide).
	
	public static final int X_INITIAL 				= 1000; // ou 500 ou 1500
	public static final int Y_INITIAL 				= 2700; // 300
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
	public static final int CALIBRATED 				= 2;
	public static final int HAND_OPEN 				= 3;
	public static final int HAVE_PALET				= 4;
	
	public static final int RADAR_MAX_RANGE			= 650;
	public static final int RADAR_MIN_RANGE			= 350;
	public static final int RADAR_RADIUS			= 10;
	
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
	
	public static final float WHEEL_DIAMETER        = 56.5f;
	public static final float DISTANCE_TO_CENTER    = 61.2f;
	public static final String LEFT_WHEEL 			= "C";
	public static final String RIGHT_WHEEL			= "B";
	public static final String GRABER    			= "D";
	public static final float LINEAR_ACCELERATION	= 0.2f;
	
	public static final int   ROTATION_SPEED		= 280;
	public static final int   SEARCH_ROTATION_SPEED = 70;
	
	public static final int   RESEARCH_SPEED		= 120; // mm/s
	public static final int   CRUISE_SPEED			= 240; // mm/s
	public static final int   MAX_SPEED				= 360; // mm/s
	
	public static final int   GRABER_TIMER			= 1500; // 2000 en vrai sa dépan dé foi xDD
	public static final int   GRABER_SPEED			= 500; // 800 en vrai

	public final static boolean[] GLOBALSTATE 		= new boolean[5]; // utiliser la méthode synchronisée pour ecriture
	
	public static final Timer TIMER 				= new Timer();
		
	public static final Area[] AREAS				= {
		new TopArea(0),
		new BorderLeftArea(1,Y_TOP_WHITE,COLOR_WHITE,Y_BLUE_LINE,COLOR_BLUE),
		new InnerArea(2,X_BLACK_LINE,COLOR_BLACK,X_YELLOW_LINE,COLOR_YELLOW,Y_TOP_WHITE,COLOR_WHITE,Y_BLUE_LINE,COLOR_BLUE),
		new InnerArea(3,X_RED_LINE,COLOR_RED,X_BLACK_LINE,COLOR_BLUE,Y_TOP_WHITE,COLOR_WHITE,COLOR_BLACK,Y_BLUE_LINE),
		new BorderRightArea(4,Y_TOP_WHITE,COLOR_WHITE,Y_BLUE_LINE,COLOR_BLUE),	
		new BorderLeftArea(5,Y_BLUE_LINE,COLOR_BLUE,Y_BLACK_LINE,COLOR_BLACK),
		new CenterArea(6),
		new BorderRightArea(7,Y_BLUE_LINE,COLOR_BLUE,Y_BLACK_LINE,COLOR_BLACK),		
		new BorderLeftArea(8,Y_BLACK_LINE,COLOR_BLACK,Y_GREEN_LINE,COLOR_GREEN),
		new BorderRightArea(9,Y_BLACK_LINE,COLOR_BLACK,Y_GREEN_LINE,COLOR_GREEN),
		new BorderRightArea(10,Y_GREEN_LINE,COLOR_GREEN,Y_BOTTOM_WHITE,COLOR_WHITE),
		new InnerArea(11,X_BLACK_LINE,COLOR_BLACK,X_YELLOW_LINE,COLOR_YELLOW,Y_GREEN_LINE,COLOR_GREEN,Y_BOTTOM_WHITE,COLOR_WHITE),
		new InnerArea(12,X_RED_LINE,COLOR_RED,X_BLACK_LINE,COLOR_BLACK,Y_GREEN_LINE,COLOR_GREEN,Y_BOTTOM_WHITE,COLOR_WHITE),
		new BorderRightArea(13,Y_GREEN_LINE,COLOR_GREEN,Y_BOTTOM_WHITE,COLOR_WHITE),
		new BottomArea(14),
		new DefaultArea(15)
	};
	
	public static Area getArea(int id){
		return AREAS[id];
	}
	
	public static float distancePointDroite(float px, float py, float a, float b, float c){
		return (float) ( (Math.abs( (a*px + b*py + c) )) / (Math.sqrt(a*a + b*b)));
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
			InetAddress IPAddress = InetAddress.getByName("192.168.1.76");
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
		//System.out.println("       ___");
		try {
			InputStream img = new ByteArrayInputStream( "test.lni".getBytes() );
			Image.createImage(img);
		} catch (IOException e) {
			e.printStackTrace();
		}
		new Marvin();
		
		printf("@@@ The first ten million years were the worst. And the second ten million: they were the worst, too. The third ten million I didn't enjoy at all. After that, I went into a bit of a decline. @@@");
	}

}
