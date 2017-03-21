package aiPlanner;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

import interfaces.ModeListener;
import lejos.hardware.Sound;
import shared.Mode;

public class SoundManager extends Thread implements ModeListener{
	private volatile Queue<String> AudioList;
	private int refreshRate = 1000;
	private volatile Mode currentMode;
	
	public SoundManager(){
		AudioList = new LinkedList<String>();
		currentMode = Mode.ACTIVE;
		Main.printf("[AUDIO]                 : Initialized");
	}
	
	public void run(){
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		Main.printf("[AUDIO]                 : Started");
		while(!isInterrupted() && currentMode != Mode.END){
			if(!AudioList.isEmpty()){
				try{
					File track = new File(AudioList.poll());
					Sound.playSample(track);
				}
				catch (Exception e) {
					Main.printf("[AUDIO]                 : Impossible de lire le fichier");
					Main.printf("[AUDIO]                 : Erreur : " + e.toString());
				}
			}
			syncWait();
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

	public void setMode(Mode m) {
		this.currentMode = m;
	}
	
	public void syncWait(){
		synchronized (this) {
			try {
				this.wait(refreshRate);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
}
