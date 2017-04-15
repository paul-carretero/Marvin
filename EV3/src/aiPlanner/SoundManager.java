package aiPlanner;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

import lejos.hardware.Sound;

/**
 * Classe charger de jouer des son en arrière plan
 */
public class SoundManager extends Thread{
	/**
	 * list FIFO contenant une liste de son à jouer (éventuellement vide)
	 */
	private Queue<String> audioList;
	
	/**
	 * Retourne une nouvelle instance de la class SoundManager pour gérer les sons, initialise la liste.
	 */
	public SoundManager(){
		this.audioList = new LinkedList<String>();
		Main.printf("[AUDIO]                 : Initialized");
	}
	
	@Override
	public void run(){
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		Main.printf("[AUDIO]                 : Started");
		while(!isInterrupted()){
			synchronized (this) {
				if(!this.audioList.isEmpty()){
					try{
						final File track = new File(this.audioList.poll());
						//Sound.playSample(track);
					}
					catch (Exception e) {
						Main.printf("[AUDIO]                 : Impossible de lire le fichier");
						Main.printf("[AUDIO]                 : Erreur : " + e.toString());
					}
					try {
						this.wait(200);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
				else{
					try {
						this.wait(0);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			}
		}
		Main.printf("[AUDIO]                 : Finished");
	}
	
	/**
	 * Ajoute le son Inro dans la liste
	 */
	synchronized public void addIntro(){
		this.audioList.add("lalalalala.wav");
		this.notify();
	}
	
	/**
	 * Ajoute le son VictoryTheme dans la liste
	 */
	synchronized public void addVictoryTheme(){
		this.audioList.add("victory.wav");
		this.notify();
	}
	
	/**
	 * Ajoute le son Trololo dans la liste
	 */
	synchronized public void addTrololo(){
		this.audioList.add("trollolol.wav");
		this.notify();
	}
	
	/**
	 * Ajoute le son order66 dans la liste
	 */
	synchronized public void addOrder(){
		this.audioList.add("order66.wav");
		this.notify();
	}
	
	/**
	 * Ajoute le son Bip dans la liste
	 */
	synchronized public void addBip() {
		this.audioList.add("bip.wav");
		this.notify();
	}
}
