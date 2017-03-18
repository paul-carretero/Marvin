package positionManager;

import aiPlanner.Main;
import area.Area;
import interfaces.ModeListener;
import interfaces.PoseGiver;
import shared.Mode;

public class AreaManager extends Thread implements ModeListener {
	
	private Area currentArea;
	private ColorSensor colorSensor;
	private int refreshRate;
	private volatile Mode currentMode;
	private int currentColor;
	private PoseGiver pg;
	
	public AreaManager(PoseGiver pg){
		colorSensor = new ColorSensor();
		this.refreshRate = 150;
		colorSensor.setCalibration();
		this.pg = pg;
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
			switch(checkColor){
				case Main.COLOR_BLACK:
					if(pg.getPosition().getY() < 125 || pg.getPosition().getY() > 175 ){
						pg.sendFixY(Main.X_BLACK_LINE);
					}
					else if(pg.getPosition().getX() > 125 || pg.getPosition().getX() < 75 ){
						pg.sendFixY(Main.Y_BLACK_LINE);
					}
					break;
				case Main.COLOR_BLUE:
					pg.sendFixY(Main.Y_BLUE_LINE);
					break;
				case Main.COLOR_GREEN:
					pg.sendFixY(Main.Y_GREEN_LINE);
					break;
				case Main.COLOR_RED:
					pg.sendFixX(Main.X_RED_LINE);
					break;
				case Main.COLOR_YELLOW:
					pg.sendFixX(Main.X_YELLOW_LINE);
					break;
				case Main.COLOR_WHITE:
					if(pg.getPosition().getY() < 150){
						pg.sendFixY(Main.Y_BOTTOM_WHITE);
					}
					else{
						pg.sendFixY(Main.Y_TOP_WHITE);
					}
					
					break;
			}
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
