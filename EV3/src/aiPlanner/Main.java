package aiPlanner;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import area.*;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.robotics.navigation.Pose;
import shared.Color;
import shared.IntPoint;
import shared.Timer;

@SuppressWarnings("javadoc")
/**
 * Classe utilitaire regroupant les informations initial du robot, son état, 
 * les informations fixes sur l'environnement et le terrain et des informations fixes sur les capteurs
 * Instancie et lance un Objet Marvin pour initialiser le programme
 * @see Marvin
 */
public class Main{
	
	public static final boolean	ARE_SENSORS_BUGGED	= true;
	
	public static final int X_INITIAL 				= 1000;
	public static final int Y_INITIAL 				= 2700;
	public static final int H_INITIAL 				= -90;

	public static final int Y_OBJECTIVE_WHITE		= 1400; // 300
	public static final int Y_DEFEND_WHITE 			= 2700;
	
	public static final int Y_BOTTOM_WHITE			= 300;
	public static final int Y_TOP_WHITE 			= 2700;
	
	public static final int Y_GREEN_LINE 			= 900;
	public static final int Y_BLUE_LINE 			= 2100;
	public static final int Y_BLACK_LINE 			= 1500;
	
	public static final int X_RED_LINE 				= 1500;
	public static final int X_YELLOW_LINE 			= 500;
	public static final int X_BLACK_LINE 			= 1000;
	
	public volatile static boolean 	PRESSION		= false;
	public volatile static boolean	HAS_MOVED 		= false;
	public volatile static boolean	HAND_OPEN 		= false;
	public volatile static boolean	HAVE_PALET		= false;
	
	public static final int RADAR_MAX_RANGE			= 1000;
	public static final int RADAR_MIN_RANGE			= 400;
	/**
	 * doit être fiable +/- 100 ... (le reste pas trop)
	 */
	public static final int	RADAR_DEFAULT_RANGE		= 550;
	/**
	 *  distance où on est sur de ne pas avoir de palet, et suffisamant petite pour éviter les faux-positif
	 */
	public static final int RADAR_WALL_DETECT		= 250;
	public static final int RADAR_OUT_OF_BOUND		= 9999;
	
	public static final String COLOR_SENSOR 		= "S2";
	public static final String TOUCH_SENSOR 		= "S3";
	public static final String US_SENSOR    		= "S4";
	
	public static final float WHEEL_DIAMETER        = 55.3f;
	public static final float DISTANCE_TO_CENTER	= 62.5f;
	public static final float DISTANCE_TO_CENTER_P	= 65f;
	public static final String LEFT_WHEEL 			= "C";
	public static final String RIGHT_WHEEL			= "B";
	public static final String GRABER    			= "D";
	public static final float LINEAR_ACCELERATION	= 10.0f;
	
	public static final int   ROTATION_SPEED		= 240;
	public static final int   SAFE_ROTATION_SPEED 	= 120;
	
	/**
	 * En mm/s
	 */
	public static final int   RESEARCH_SPEED		= 120;
	
	/**
	 * En mm/s
	 */
	public static final int   CRUISE_SPEED			= 240;
	
	/**
	 * En mm/s
	 */
	public static final int   MAX_SPEED				= 360;
	
	public static final int   GRABER_TIMER			= 1200;
	public static final int   GRABER_SPEED			= 800;
	
	public static final String	IP					= "192.168.0.9";
	public static final Timer 	TIMER 				= new Timer();
	
	/**
	 * Représente les positions initiales des 9 palet, notament utilisé pour calibrer la Map des item
	 */
	public static final IntPoint[] INITIAL_PALETS	= {
		new IntPoint(500, 2100),
		new IntPoint(1000, 2100),
		new IntPoint(1500, 2100),
		new IntPoint(500, 1500),
		new IntPoint(1000, 1500),
		new IntPoint(1500, 1500),
		new IntPoint(500, 900),
		new IntPoint(1000, 900),
		new IntPoint(1500, 900)
	};
		
	/**
	 * tableau comprenant l'ensemble des 16 Area du terrain (séparée par les lignes de couleurs)
	 */
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
	
	/**
	 * CHOO CHOO
	 */
	public static final String[] CHOO_CHOO = new String[]{
			"\n\n\n\n=================",
			"\n\n\n\n\n}\n\\================\n\n\n\n",
			"\n\nO\nY\n|}\n\\\\===============\n\n\n\n",
			"\n\n O\n_Y\n_|}\nO\\\\==============\n\n\n\n",
			"\nO\n  O\n__Y\n__|}\nOO\\\\=============\n\n\n\n",
			"\n O\n   O\n'__Y\n|__|}\nOOO\\\\============\n\n\n\n",
			"\n  O\n    O\n_'__Y\n_|__|}\n-OOO\\\\===========\n\n\n",
			"\nO  O\n_    O\n|_'__Y\n|_|__|}\n--OOO\\\\==========\n\n\n",
			"\n O  O\n__    O\n]|_'__Y\n_|_|__|}\no--OOO\\\\=========\n\n\n",
			"\n  O  O\n___    O\n[]|_'__Y\n__|_|__|}\noo--OOO\\\\========\n\n\n",
			"\no  O  O\n____    O\n|[]|_'__Y\n|__|_|__|}\n=oo--OOO\\\\=======\n\n\n",
			"\n o  O  O\n ____    O\n_|[]|_'__Y\n_|__|_|__|}\n==oo--OOO\\\\======\n\n\n",
			"\n  o  O  O\n  ____    O\n\\_|[]|_'__Y\n__|__|_|__|}\no==oo--OOO\\\\=====\n\n\n",
			"\no  o  O  O\n_  ____    O\n \\_|[]|_'__Y\n___|__|_|__|}\noo==oo--OOO\\\\====\n\n\n",
			"\n o  o  O  O\n__  ____    O\nN \\_|[]|_'__Y\n____|__|_|__|}\n-oo==oo--OOO\\\\===\n\n\n",
			"\n  o  o  O  O\n___  ____    O\nMN \\_|[]|_'__Y\n_____|__|_|__|}\n--oo==oo--OOO\\\\==\n\n\n",
			"\n   o  o  O  O\n____  ____    O\n MN \\_|[]|_'__Y\n______|__|_|__|}\no--oo==oo--OOO\\\\=\n\n\n",
			"\n    o  o  O  O\n,____  ____    O\n| MN \\_|[]|_'__Y\n|______|__|_|__|}\noo--oo==oo--OOO\\\\\n\nMARVIN : STAND-BY\n"
	};
	
	/**
	 * @param id l'id de l'area dans le tableau (entre 0 et 15)
	 * @return retourne l'Area ayant l'indice i dans le tableau
	 */
	public static Area getArea(final int id){
		return AREAS[id];
	}
	
	/**
	 * Les capteurs de couleur notament étant légèrement en avant du point a partir duquel nous calculons la position, 
	 * il est nécessaire de convertir les données lu par le capteur avant de les traiter 
	 * @param p la pose représentant la pose réel du robot
	 * @see Pose
	 */
	public static void poseRealToSensor(final Pose p){
		p.moveUpdate(80);
	}
	
	/**
	 * Les capteurs de couleur notament étant légèrement en avant du point a partir duquel nous calculons la position, 
	 * il est nécessaire de convertir les données lu par le capteur avant de les traiter 
	 * @param p la pose représentant la pose calculé par le capteur du robot
	 * @see Pose
	 */
	public static void poseSensorToReal(final Pose p){
		p.moveUpdate(-80);
	}
	
	/**
	 * @param x un entier quelconque
	 * @param y un entier quelconque
	 * @param marge la marge minimum au délà de laquelle x et y sont considéré comme différent
	 * @return retourne vrai si x est environ égal à y, faux sinon
	 */
	public static boolean areApproximatelyEqual(final int x, final int y, final int marge){
		return x < (y + marge) && x > (y - marge);
	}

	/**
	 * envoi un paquet UDP contenant la chaîne vers un affichage distant
	 * @param s une chaine quelconque
	 */
	public static void printf(String s){
		try {
			String str = s + "#";
			DatagramSocket clientSocket = new DatagramSocket();
			InetAddress IPAddress = InetAddress.getByName(IP);
			byte[] sendData = new byte[256];
			sendData = str.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 4242);
			clientSocket.send(sendPacket);
			clientSocket.close();
		} catch (Exception e) {
			System.out.println("[ERREUR] : impossible d'envoyer les données : " + e.getMessage());
		}
	}


	/**
	 * Fonction de lancement du programme
	 * @param args unused
	 */
	public static void main(String[] args) {
		
		LocalEV3.get().getLED().setPattern(2);
		System.out.println(CHOO_CHOO[0]);

		Marvin marvin = new Marvin();
		
		marvin.startThreads();
 		
 		LocalEV3.get().getLED().setPattern(1);
 		
 		
 		int pressButton = Button.waitForAnyPress();
 		
 		if(pressButton == Button.ID_DOWN){
 			HAND_OPEN = false;
 			Sound.beep();
 			Sound.beep();
 			
 			Main.TIMER.resetTimer();
 			marvin.run();
 		}
 		else if(pressButton == Button.ID_UP){
 			HAND_OPEN = true;
 			Sound.beep();
 			
 			Main.TIMER.resetTimer();
 			marvin.run();
 		}		
		
		printf("@@@ The first ten million years were the worst. And the second ten million: they were the worst, too. The third ten million I didn't enjoy at all. After that, I went into a bit of a decline. @@@");
	}

}
