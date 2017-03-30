package positionManager;

import aiPlanner.Main;
import area.Area;
import interfaces.AreaGiver;
import interfaces.ModeListener;
import interfaces.PoseGiver;
import shared.Color;
import shared.Mode;

public class AreaManager extends Thread implements ModeListener, AreaGiver {
	
	private Area currentArea;
	private ColorSensor colorSensor;
	private int refreshRate;
	private volatile Mode currentMode;
	private Color currentColor;
	private PoseGiver pg;
	
	public AreaManager(PoseGiver pg){
		this.currentColor	= null;
		this.colorSensor	= new ColorSensor();
		this.refreshRate	= 100;
		this.pg				= pg;
		this.currentArea	= Area.getAreaWithPosition(pg.getPosition());
		
		colorSensor.setCalibration();
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
	
	@SuppressWarnings("incomplete-switch")
	private boolean updateColor(){
		Color checkColor = colorSensor.getCurrentColor();
		if(checkColor != currentColor){
			currentColor = checkColor;
			switch(checkColor){
				case BLACK:
					if(pg.getPosition().getY() < 1250 || pg.getPosition().getY() > 1750 ){
						pg.sendFixY(Main.X_BLACK_LINE);
					}
					else if(pg.getPosition().getX() > 1250 || pg.getPosition().getX() < 750 ){
						pg.sendFixY(Main.Y_BLACK_LINE);
					}
					break;
				case BLUE:
					pg.sendFixY(Main.Y_BLUE_LINE);
					break;
				case GREEN:
					pg.sendFixY(Main.Y_GREEN_LINE);
					break;
				case RED:
					pg.sendFixX(Main.X_RED_LINE);
					break;
				case YELLOW:
					pg.sendFixX(Main.X_YELLOW_LINE);
					break;
				case WHITE:
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
	
	private void syncWait(){
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

	public Area getCurrentArea() {
		return currentArea;
	}

	public void updateArea() {
		currentArea = Area.getAreaWithPosition(pg.getPosition());
	}
}
