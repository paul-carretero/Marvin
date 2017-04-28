package aiPlanner;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.robotics.navigation.Pose;
import lejos.utility.Delay;
import shared.IntPoint;
import shared.Timer;

/**
 * Classe utilitaire regroupant les informations initial du robot, son état, 
 * les informations fixes sur l'environnement et le terrain et des informations fixes sur les capteurs
 * Instancie et lance un Objet Marvin pour initialiser le programme
 * @see Marvin
 * @author paul.carretero, florent.chastagner
 */
public class Main{
	
	/**
	 * Defini si le radar est en etat de fonctionner ou non
	 */
	public static       boolean	USE_RADAR			= true;
	
	/**
	 * Defini si l'on doit utiliser les sons
	 */
	public static final boolean	USE_SOUND			= true;
	
	/**
	 * Defini si l'on doit utiliser les couleurs verte ou bleu dans les area<br/>
	 * Defini si l'on doit utiliser le calculateur de direction ennemies<br/>
	 * FORTEMENT DECONSEILLE, tres peu fiable et/ou succeptible de provoquer des erreurs
	 */
	public static boolean I_ALSO_LIKE_TO_LIVE_DANGEROUSLY = false;
	
	/**
	 * position en x intiale du robot
	 */
	public static 		int X_INITIAL 				= 1000;
	/**
	 * position en y initiale du robot
	 */
	public static       int Y_INITIAL 				= 2800;
	/**
	 * position en y initiale du robot par rapport à la ligne blanche (environ 10cm)
	 */
	public static final int Y_INITIAL_DECAL			= 100;
	/**
	 * orientation initiale du robot
	 */
	public static       int H_INITIAL 				= -90;

	/**
	 * Ligne d'objectif (ou l'on marque)
	 */
	public static       int Y_OBJECTIVE_WHITE		= 300;
	/**
	 * Ligne de notre camps (que l'on defend)
	 */
	public static       int Y_DEFEND_WHITE 			= 2700;
	
	/**
	 * Ligne blanche ayant le y mimum
	 */
	public static final int Y_BOTTOM_WHITE			= 300;
	/**
	 * Ligne blanche ayant le y maximum
	 */
	public static final int Y_TOP_WHITE 			= 2700;
	
	/**
	 * Y de la ligne verte
	 */
	public static final int Y_GREEN_LINE 			= 900;
	/**
	 * Y de la ligne bleu
	 */
	public static final int Y_BLUE_LINE 			= 2100;
	/**
	 * Y de la ligne noire
	 */
	public static final int Y_BLACK_LINE 			= 1500;
	
	/**
	 * X de la ligne rouge
	 */
	public static final int X_RED_LINE 				= 1500;
	/**
	 * X de la ligne jaune
	 */
	public static final int X_YELLOW_LINE 			= 500;
	/**
	 * X de la ligne noire
	 */
	public static final int X_BLACK_LINE 			= 1000;
	
	/**
	 * Vrai si le capteur de pression à été actionné récement, faux sinon
	 */
	public volatile static boolean 	PRESSION		= false;
	/**
	 * Vrai si les pinces du robot sont ouvertes, faux sinon
	 */
	public volatile static boolean	HAND_OPEN 		= false;
	/**
	 * Vrai si le robot (pense) posseder un palet
	 */
	public          static boolean	HAVE_PALET		= false;
	
	/**
	 * porte maximale du radar pour detecter un palet
	 */
	public static final int RADAR_MAX_RANGE			= 1000;
	/**
	 * Porte minimale du radar pour detecter un palet
	 */
	public static final int RADAR_MIN_RANGE			= 400;
	/**
	 * doit être fiable +/- 100 ... (le reste pas trop)
	 */
	public static final int	RADAR_DEFAULT_RANGE		= 550;
	/**
	 *  distance où on est sur de ne pas avoir de palet, et suffisamant petite pour éviter les faux-positifs
	 */
	public static final int RADAR_WALL_DETECT		= 250;
	/**
	 * Arbitraire, absence de donnee radar
	 */
	public static final int RADAR_OUT_OF_BOUND		= 9999;
	
	/**
	 * Port du capteur de couleur
	 */
	public static final String COLOR_SENSOR 		= "S2";
	/**
	 * Port du capteur de pression
	 */
	public static final String TOUCH_SENSOR 		= "S3";
	/**
	 * Port du capteur ultrason (radar)
	 */
	public static final String US_SENSOR    		= "S4";
	
	/**
	 * Diametre d'une roue (mm)
	 */
	public static final float WHEEL_DIAMETER        = 55.3f;
	/**
	 * Distance des roues par rapport au centre (mm)
	 */
	public static final float DISTANCE_TO_CENTER	= 62.5f;
	/**
	 * Distance des roues par rapport au centre, corrigee de la presence d'un palet
	 */
	public static final float DISTANCE_TO_CENTER_P	= 65f;
	/**
	 * Port du moteur gauche
	 */
	public static final String LEFT_WHEEL 			= "C";
	/**
	 * Port du moteur droit
	 */
	public static final String RIGHT_WHEEL			= "B";
	/**
	 * Port du moteur de graber
	 */
	public static final String GRABER    			= "D";
	/**
	 * Acceleration Lineaire (probablement non utilise)
	 */
	public static final float LINEAR_ACCELERATION	= 300.0f;
	
	/**
	 * Distance (en mm) au délà de laquelle on considère la distance parcourue suffisament fiable pour avoir une calcul précis de l'angle.
	 */
	public static final int FIABLE_DIST	= 375;
	
	/**
	 * Vitesse d'une rotation standard
	 */
	public static final int   ROTATION_SPEED		= 240;
	/**
	 * Vitesse d'une rotation lorsque le robot possede un palet
	 */
	public static final int   SAFE_ROTATION_SPEED 	= 120;
	
	/**
	 * Vitesse minimum de recherche de position En mm/s
	 */
	public static final int   RESEARCH_SPEED		= 120;
	
	/**
	 * Vitesse standard En mm/s
	 */
	public static final int   CRUISE_SPEED			= 240; // 240
	
	/**
	 * Vitesse maximum sans perte de precision En mm/s
	 */
	public static final int   MAX_SPEED				= 300; // 300
	
	/**
	 * Distance maximum considéré comme fiable
	 */
	public static final float MAX_SAFE_DISTANCE		= 1200;
	
	/**
	 * Duree d'un grab
	 */
	public static final int   GRABER_TIMER			= 800;
	/**
	 * Vitesse du moteur du grabber
	 */
	public static final int   GRABER_SPEED			= 800;
	
	/**
	 * IP du pc affichant le détail des log
	 */
	public static final String	IP					= "192.168.0.11";
	/**
	 * Timer du programme
	 */
	public static final Timer 	TIMER 				= new Timer();
	/**
	 * Definie si il faut afficher ou non les logs secondaires
	 */
	public static final boolean PRINT_LOG 			= false;
	
	/**
	 * Fournit des boolean et chiffre aléatoire entre autre,
	 * Une seule initialisation pour éviter les erreurs
	 */
	public static final Random RANDOMIZER			= new Random();
	
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
	 * CHOO CHOO
	 */
	public static final String[] CHOO_CHOO = new String[]{
			"\n\n\n\n\n=================",
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
			byte[] sendData = str.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 4242);
			clientSocket.send(sendPacket);
			clientSocket.close();
		} catch (Exception e) {
			System.out.println("[ERREUR] : impossible d'envoyer les données : " + e.getMessage());
		}
	}
	
	/**
	 * @param s une chaine de log secondaire a afficher (ou non)
	 */
	public static void log(String s){
		if(PRINT_LOG){
			printf(s);
		}
	}
	
	/**
	 * Menu d'initialisation du robot permettant de choisir sa position initiale
	 * Initialise les variables globales de position initiale.
	 */
	private static void menu(){
		
		String stringPose[] = new String[]{
			"\n  -------------\n  |[ ] [ ] [ ]|\n  |           |\n  |           |\n  |           |\n  |[M] [ ] [ ]|\n  -------------\n 0",
			"\n  -------------\n  |[ ] [ ] [ ]|\n  |           |\n  |           |\n  |           |\n  |[ ] [M] [ ]|\n  -------------\n 0",
			"\n  -------------\n  |[ ] [ ] [ ]|\n  |           |\n  |           |\n  |           |\n  |[ ] [ ] [M]|\n  -------------\n 0",
			"\n  -------------\n  |[M] [ ] [ ]|\n  |           |\n  |           |\n  |           |\n  |[ ] [ ] [ ]|\n  -------------\n 0",
			"\n  -------------\n  |[ ] [M] [ ]|\n  |           |\n  |           |\n  |           |\n  |[ ] [ ] [ ]|\n  -------------\n 0",
			"\n  -------------\n  |[ ] [ ] [M]|\n  |           |\n  |           |\n  |           |\n  |[ ] [ ] [ ]|\n  -------------\n 0",
		};
		
		int startPoses[][] = new int[][]{
			new int[]{500,2800,-90,300,2700},
			new int[]{1000,2800,-90,300,2700},
			new int[]{1500,2800,-90,300,2700},
			new int[]{500,200,90,2700,300},
			new int[]{1000,200,90,2700,300},
			new int[]{1500,200,90,2700,300},
		};
		
		int current = 1;
		
		System.out.print(stringPose[current]);
		
		int pressButton = Button.waitForAnyPress();
		
		while(pressButton != Button.ID_ENTER){
			switch (pressButton) {
				case Button.ID_UP:
					if(current == 0){
						current = 3;
						
					}
					if(current == 1){
						current = 4;
					}
					if(current == 2){
						current = 5;
					}
					break;
				case Button.ID_DOWN:
					if(current == 3){
						current = 0;
					}
					if(current == 4){
						current = 1;
					}
					if(current == 5){
						current = 2;
					}
					break;
				case Button.ID_LEFT:
					if(current == 1 || current == 2 || current == 4 || current == 5){
						current--;
					}
					break;
				case Button.ID_RIGHT:
					if(current == 0 || current == 1 || current == 3 || current == 4){
						current++;
					}
					break;
				default:
					break;
			}
			System.out.print(stringPose[current]);
			Delay.msDelay(200);
			pressButton = Button.waitForAnyPress();
		}
		
		X_INITIAL = startPoses[current][0];
		Y_INITIAL = startPoses[current][1];
		H_INITIAL = startPoses[current][2];
		Y_OBJECTIVE_WHITE = startPoses[current][3];
		Y_DEFEND_WHITE = startPoses[current][4];
	}
	
	/**
	 * Lance le programme principale
	 * Chargera le fichier de calibration couleur depuis le fichier conf.txt
	 */
	private static void runMainProgram(){
		menu();
		LocalEV3.get().getLED().setPattern(2);
		
		System.out.println(CHOO_CHOO[0]);

		Marvin marvin = new Marvin();
		
		marvin.startThreads();
 		
 		LocalEV3.get().getLED().setPattern(1);
 		
 		System.out.print("\n [SAFE HAND OPEN]\n\n[UNSAFE] [UNSAFE]\n[OPEN]    [CLOSE]\n\n[SAFE HAND CLOSE]\n-----------------\nMarvin Start Menu");
 		int pressButton = Button.waitForAnyPress();
 		
 		if(pressButton == Button.ID_DOWN){
 			HAND_OPEN = false;
 			Sound.beep();
 		}
 		else if(pressButton == Button.ID_UP || pressButton == Button.ID_ENTER){
 			HAND_OPEN = true;
 			Sound.beep();
 		}
 		else if(pressButton == Button.ID_RIGHT){
 			HAND_OPEN = false;
 			I_ALSO_LIKE_TO_LIVE_DANGEROUSLY = true;
 			Sound.beep();
 		}
 		else if(pressButton == Button.ID_LEFT){
 			HAND_OPEN = true;
 			I_ALSO_LIKE_TO_LIVE_DANGEROUSLY = true;
 			Sound.beep();
 		}
 		
 		if(pressButton != Button.ID_ESCAPE){
	 		Main.TIMER.resetTimer();
			marvin.run();
 		}
	}


	/**
	 * Fonction de lancement du programme
	 * @param args unused
	 */
	public static void main(String[] args) {
		
		LocalEV3.get().getLED().setPattern(1);
		
		System.out.print("\n [DEFAULT START]\n\n[CALIBRATE COLOR]\n\n   [CALIBRATE]\n   [AND START]\n-----------------\nMarvin Start Menu");
		int pressButton = Button.waitForAnyPress();
		
		if(pressButton == Button.ID_DOWN){
			System.out.println("Lancement de la calibration couleur");
			Delay.msDelay(300);
			ColorCalibrator.Calibrate();
			runMainProgram();
		}
		if(pressButton == Button.ID_ENTER){
			System.out.println("Lancement de la calibration couleur");
			Delay.msDelay(300);
			ColorCalibrator.Calibrate();
		}
		if(pressButton == Button.ID_UP){
			runMainProgram();
		}
		
		printf("@@@ The first ten million years were the worst. And the second ten million: they were the worst, too. The third ten million I didn't enjoy at all. After that, I went into a bit of a decline. @@@");
	}

}
