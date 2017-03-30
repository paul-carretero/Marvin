package aiPlanner;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

import lejos.hardware.Sound;

public class SoundManager extends Thread{
	private volatile Queue<String> AudioList;
	
	public SoundManager(){
		AudioList = new LinkedList<String>();
		Main.printf("[AUDIO]                 : Initialized");
	}
	
	public void run(){
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		Main.printf("[AUDIO]                 : Started");
		while(!isInterrupted()){
			if(!AudioList.isEmpty()){
				try{
					File track = new File(AudioList.poll());
					Sound.playSample(track);
				}
				catch (Exception e) {
					Main.printf("[AUDIO]                 : Impossible de lire le fichier");
					Main.printf("[AUDIO]                 : Erreur : " + e.toString());
				}
				syncWait(200);
			}
			else{
				syncWait(2000);
			}
			
		}
		Main.printf("[AUDIO]                 : Finished");
	}
	
	public void addIntro(){
		AudioList.add("lalalalala.wav");
	}
	
	public void addVictoryTheme(){
		AudioList.add("victory.wav");
	}
	
	public void addTrololo(){
		AudioList.add("trollolol.wav");
	}
	
	public void addOrder(){
		AudioList.add("order66.wav");
	}
	
	public void syncWait(int t){
		synchronized (this) {
			try {
				this.wait(t);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	public void addBip() {
		synchronized (this) {
			AudioList.add("bip.wav");
			this.notify();
		}
		
	}
}
