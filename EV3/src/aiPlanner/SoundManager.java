package aiPlanner;

import java.io.File;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import lejos.hardware.Sound;

/**
 * Classe charger de jouer des son en arri�re plan
 * @author paul.carretero
 */
public class SoundManager extends Thread{
	/**
	 * list FIFO contenant une liste de son � jouer (�ventuellement vide)
	 */
	private Queue<String> audioList;
	
	/**
	 * Retourne une nouvelle instance de la class SoundManager pour g�rer les sons, initialise la liste.
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
				try {
					this.wait();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
		Main.printf("[AUDIO]                 : Finished");
	}
	
	/**
	 * Ajoute le son VictoryTheme dans la liste
	 */
	public void addVictoryTheme(){
		this.audioList.add("victory.wav");
		this.notify();
	}
	
	/**
	 * Ajoute le son order66 dans la liste
	 */
	public void addOrder(){
		this.audioList.add("order66.wav");
		this.notify();
	}
	
	/**
	 * Ajoute le son Bip dans la liste
	 */
	public void addBips() {
		this.audioList.add("bip.wav");
		this.audioList.add("bip.wav");
		this.audioList.add("bip.wav");
		this.notify();
	}
}
