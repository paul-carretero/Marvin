package TrillianServer;
import java.io.IOException;
import java.net.*;

/**
 * Server a lancer sur un PC afin de visualiser les log et informations émise par le robot.
 * Il est nécessaire de configurer le robot pour emmetre vers l'IP de ce PC au préalable
 * @author paul.carretero
 */
class TrillianServer{
	
	/**
	 * Couleur console par défaut
	 */
	private static final String ANSI_RESET = "\u001B[0m";
	/**
	 * Couleur console rouge
	 */
	private static final String ANSI_RED = "\u001B[31m";
	/**
	 * Couleur console verte
	 */
	private static final String ANSI_GREEN = "\u001B[32m";
	/**
	 * Couleur console Jaune
	 */
	private static final String ANSI_YELLOW = "\u001B[33m";
	/**
	 * Couleur console bleue
	 */
	private static final String ANSI_BLUE = "\u001B[34m";
	/**
	 * Couleur console violet
	 */
	private static final String ANSI_PURPLE = "\u001B[35m";
	/**
	 * Couleur console cyan
	 */
	private static final String ANSI_CYAN = "\u001B[36m";
	/**
	 * Couleur console blanche
	 */
	private static final String ANSI_WHITE = "\u001B[37m";
	/**
	 * texte normal
	 */
	private static final String setPlainText = "\033[0;0m";
	/**
	 * texte gras
	 */
	private static final String setBoldText = "\033[0;1m";
	/**
	 * port sur lequel ce server ecoutera les donnees du robot
	 */
	private static final int PORT = 4242;
	
	/**
	 * Affiche le logo du serveur
	 */
	private static void logo(){
		System.out.println("\033[H\033[2J");
		System.out.println(" _____ ____  ___ _     _     ___    _    _   _    ____  _____ ______     _______ ____              ____ _____  _    _   _ ____ ___ _   _  ____       ______   __");
		System.out.println("|_   _|  _ \\|_ _| |   | |   |_ _|  / \\  | \\ | |  / ___|| ____|  _ \\ \\   / / ____|  _ \\      _     / ___|_   _|/ \\  | \\ | |  _ \\_ _| \\ | |/ ___|     | __ ) \\ / /");
		System.out.println("  | | | |_) || || |   | |    | |  / _ \\ |  \\| |  \\___ \\|  _| | |_) \\ \\ / /|  _| | |_) |    (_)    \\___ \\ | | / _ \\ |  \\| | | | | ||  \\| | |  _ _____|  _ \\\\ V / ");
		System.out.println("  | | |  _ < | || |___| |___ | | / ___ \\| |\\  |   ___) | |___|  _ < \\ V / | |___|  _ <      _      ___) || |/ ___ \\| |\\  | |_| | || |\\  | |_| |_____| |_) || |  ");
		System.out.println("  |_| |_| \\_\\___|_____|_____|___/_/   \\_\\_| \\_|  |____/|_____|_| \\_\\ \\_/  |_____|_| \\_\\    (_)    |____/ |_/_/   \\_\\_| \\_|____/___|_| \\_|\\____|     |____/ |_|  ");
		System.out.println("\n\n\n\n\n\n\n\n");
		System.out.println(setBoldText + "[TRILLIAN]            : What are you supposed to do with a manically depressed robot?" + setPlainText);
		System.out.println(setBoldText + "[TRILLIAN]            : Listening on UDP port : [" + PORT + "]" + setPlainText);
	}
		
	/**
	 * Lance le serveur et l'était lorsque le robot envoie une phrase de fin valide
	 * @param args unused
	 * @throws IOException lors d'une erreur de communication
	 */
	public static void main(String args[]) throws IOException{
		DatagramSocket serverSocket = new DatagramSocket(PORT);
		logo();
		String sentence = "";
        byte[] receiveData;
        int taille;
        while(!sentence.contains("@@@")){
        	receiveData = new byte[256];
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(receivePacket);
			sentence = new String(receivePacket.getData());
			taille = sentence.indexOf("#");
			if(taille > 0){
				colorPrint(sentence.substring(0, taille));
			}
		}
        serverSocket.close();
	}
	
	/**
	 * Affiche la chaine de caractère dans une couleur en fonction de la classe du robot qui l'a émise
	 * @param sentence une chaine de caractère
	 */
	private static void colorPrint(String sentence) {
		String color = "";
		if(sentence.contains("[MARVIN]")){
			color = ANSI_YELLOW;
		}
		else if(sentence.contains("[GOAL]")){
			color = ANSI_YELLOW;
		}
		else if(sentence.contains("[EVENT MANAGER]")){
			color = ANSI_CYAN;
		}
		else if(sentence.contains("[PRESSION SENSOR]")){
			color = ANSI_CYAN;
		}
		else if(sentence.contains("[EYE OF MARVIN]")){
			color = ANSI_GREEN;
		}
		else if(sentence.contains("[CIS]")){
			color = ANSI_GREEN;
		}
		else if(sentence.contains("[SERVER]")){
			color = ANSI_GREEN;
		}
		else if(sentence.contains("[ENGINE]")){
			color = ANSI_PURPLE;
		}
		else if(sentence.contains("[GRABER]")){
			color = ANSI_PURPLE;
		}
		else if(sentence.contains("[COLOR SENSOR]")){
			color = ANSI_WHITE;
		}
		else if(sentence.contains("[VISION SENSOR]")){
			color = ANSI_BLUE;
		}
		else if(sentence.contains("[DIRECTION CALCULATOR]")){
			color = ANSI_BLUE;
		}
		else if(sentence.contains("[POSITION CALCULATOR]")){
			color = ANSI_BLUE;
		}
		else if(sentence.contains("[GOAL RECALIBRATE]")){
			color = ANSI_RED;
		}
		else if(sentence.contains("[AREA MANAGER]")){
			color = ANSI_CYAN;
		}
		System.out.println(color + sentence + ANSI_RESET);
	}
}
