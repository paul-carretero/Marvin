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
		currentColor = -1;
		colorSensor = new ColorSensor();
		this.refreshRate = 500;
		colorSensor.setCalibration();
		this.pg = pg;
		currentArea = Area.getAreaWithPosition(pg.getPosition());
		Main.printf("[AREA MANAGER]          : Initialized");
	}
	
	public void run(){
		colorSensor.lightOn();
		while(!isInterrupted() && currentMode != Mode.END){
			if(updateColor()){
				currentArea = currentArea.colorChange(currentColor, pg.getPosition());
				Main.printf("[AREA MANAGER]          : COLOR DETECTED = " + currentColor + "NEW AREA = " + currentArea.toString());
			}
			syncWait();
		}
		colorSensor.lightOff();
		Main.printf("[AREA MANAGER]          : Finished");
	}
	
	@SuppressWarnings("unused")
	private boolean updateColor(){
		int checkColor = colorSensor.getCurrentColor();
		if(checkColor != currentColor && false){
			currentColor = checkColor;
			switch(checkColor){
				case Main.COLOR_BLACK:
					if(pg.getPosition().getY() < 1250 || pg.getPosition().getY() > 1750 ){
						pg.sendFixY(Main.X_BLACK_LINE);
					}
					else if(pg.getPosition().getX() > 1250 || pg.getPosition().getX() < 750 ){
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
					if(pg.getPosition().getY() < Main.Y_BLACK_LINE){
						pg.sendFixY(Main.Y_BOTTOM_WHITE);
					}
					else{
						pg.sendFixY(Main.Y_TOP_WHITE);
					}
					
					break;
			}
			return true;
		}
		return false;
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
