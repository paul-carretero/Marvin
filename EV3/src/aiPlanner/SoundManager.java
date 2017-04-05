package aiPlanner;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

import lejos.hardware.Sound;

public class SoundManager extends Thread{
	private Queue<String> audioList;
	
	public SoundManager(){
		this.audioList = new LinkedList<String>();
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
				syncWait(200);
			}
			else{
				syncWait(0);
			}
			
		}
		Main.printf("[AUDIO]                 : Finished");
	}
	
	synchronized public void addIntro(){
		this.audioList.add("lalalalala.wav");
		this.notify();
	}
	
	synchronized public void addVictoryTheme(){
		//AudioList.add("victory.wav");
		this.notify();
	}
	
	synchronized public void addTrololo(){
		this.audioList.add("trollolol.wav");
		this.notify();
	}
	
	synchronized public void addOrder(){
		this.audioList.add("order66.wav");
		this.notify();
	}
	
	synchronized public void addBip() {
		this.audioList.add("bip.wav");
		this.notify();
	}
	
	synchronized private void syncWait(final int t){
		try {
			this.wait(t);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
