package positionManager;

import aiPlanner.Main;
import interfaces.ModeListener;
import shared.Mode;

public class AreaManager extends Thread implements ModeListener {
	
	private Area currentArea;
	private ColorSensor colorSensor;
	private int refreshRate;
	private volatile Mode currentMode;
	private int currentColor;
	
	public AreaManager(){
		colorSensor = new ColorSensor();
		this.refreshRate = 150;
		colorSensor.setCalibration();
		Main.printf("[AREA MANAGER]          : Initialized");
	}
	
	public void run(){
		Main.printf("[AREA MANAGER]          : Started");
		colorSensor.lightOn();
		while(!isInterrupted() && currentMode != Mode.END){
			updateColor();
			syncWait();
		}
		colorSensor.lightOff();
		Main.printf("[AREA MANAGER]          : Finished");
	}
	
	private void updateColor(){
		int checkColor = colorSensor.getCurrentColor();
		if(checkColor != currentColor){
			currentColor = checkColor;
			//Main.printf("Detected Color : " + currentColor);
		}
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

	public void setMode(Mode m) {
		this.currentMode = m;
	}
}
