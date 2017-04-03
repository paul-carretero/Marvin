package positionManager;

import aiPlanner.Main;
import area.Area;
import interfaces.AreaGiver;
import interfaces.PoseGiver;
import shared.Color;

public class AreaManager extends Thread implements AreaGiver {
	
	private Area currentArea;
	private ColorSensor colorSensor;
	private int refreshRate;
	private Color currentColor;
	private PoseGiver pg;
	
	public AreaManager(PoseGiver pg){
		this.currentColor	= null;
		this.colorSensor	= new ColorSensor();
		this.refreshRate	= 100;
		this.pg				= pg;
		this.currentArea	= Area.getAreaWithPosition(pg.getPosition());
		this.colorSensor.setCalibration();
		Main.printf("[AREA MANAGER]          : Initialized");
	}
	
	@Override
	public void run(){
		this.colorSensor.lightOn();
		while(!isInterrupted()){
			if(updateColor()){
				this.currentArea = this.currentArea.colorChange(this.currentColor, this.pg.getPosition());
				Main.printf("[AREA MANAGER]          : COLOR DETECTED = " + this.currentColor + "NEW AREA = " + this.currentArea.toString());
			}
			syncWait();
		}
		this.colorSensor.lightOff();
		Main.printf("[AREA MANAGER]          : Finished");
	}
	
	@SuppressWarnings("incomplete-switch")
	private boolean updateColor(){
		Color checkColor = this.colorSensor.getCurrentColor();
		if(checkColor != this.currentColor){
			this.currentColor = checkColor;
			switch(checkColor){
				case BLACK:
					if(this.pg.getPosition().getY() < 1250 || this.pg.getPosition().getY() > 1750 ){
						this.pg.sendFixY(Main.X_BLACK_LINE);
					}
					else if(this.pg.getPosition().getX() > 1250 || this.pg.getPosition().getX() < 750 ){
						this.pg.sendFixY(Main.Y_BLACK_LINE);
					}
					break;
				case BLUE:
					this.pg.sendFixY(Main.Y_BLUE_LINE);
					break;
				case GREEN:
					this.pg.sendFixY(Main.Y_GREEN_LINE);
					break;
				case RED:
					this.pg.sendFixX(Main.X_RED_LINE);
					break;
				case YELLOW:
					this.pg.sendFixX(Main.X_YELLOW_LINE);
					break;
				case WHITE:
					if(this.pg.getPosition().getY() < Main.Y_BLACK_LINE){
						this.pg.sendFixY(Main.Y_BOTTOM_WHITE);
					}
					else{
						this.pg.sendFixY(Main.Y_TOP_WHITE);
					}
					break;
			}
			return true;
		}
		return false;
	}
	
	synchronized private void syncWait(){
		try {
			this.wait(this.refreshRate);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public Area getCurrentArea() {
		return this.currentArea;
	}

	public void updateArea() {
		this.currentArea = Area.getAreaWithPosition(this.pg.getPosition());
	}
}
