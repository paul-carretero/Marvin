package aiPlanner;

import java.io.File;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import lejos.hardware.Sound;

/**
 * Classe charger de jouer des son en arrière plan
 * @author paul.carretero, florent.chastagner
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
		super("SoundManager");
		this.audioList = new ConcurrentLinkedQueue<String>();
		Main.printf("[AUDIO]                 : Initialized");
	}
	
	@Override
	public void run(){
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		Main.printf("[AUDIO]                 : Started");
		while(!isInterrupted()){
			if(!this.audioList.isEmpty()){
				try{
					final File track = new File(this.audioList.poll());
					Sound.playSample(track);
				}
				catch (Exception e) {
					Main.printf("[AUDIO]                 : Impossible de lire le fichier");
					Main.printf("[AUDIO]                 : Erreur : " + e.toString());
				}
			}
			else{
				synchWait();
			}
		}
		Main.printf("[AUDIO]                 : Finished");
	}
	
	/**
	 * Ajoute le son VictoryTheme dans la liste
	 */
	synchronized public void addVictoryTheme(){
		this.audioList.add("victory.wav");
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
	synchronized public void addBips() {
		this.audioList.add("bip.wav");
		this.audioList.add("bip.wav");
		this.audioList.add("bip.wav");
		this.notify();
	}
	
	/**
	 * Attends jusqu'a etre reveiller par un autre Thread
	 */
	synchronized private void synchWait() {
		try {
			this.wait();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
