package positionManager;

import aiPlanner.Main;
import area.Area;
import interfaces.AreaGiver;
import interfaces.PoseGiver;
import shared.Color;

/**
 * class g�rant la position du robot sur les 15+1 zones du terrain en fonction des lignes de couleurs.
 */
public class AreaManager extends Thread implements AreaGiver {
	
	/**
	 * Contient l'Area dans laquelle le robot se trouve
	 */
	private Area 		currentArea;
	
	/**
	 * Capteur de couleur
	 */
	private ColorSensor	colorSensor;
	
	/**
	 * repr�sente la derni�re couleur vu par le robot
	 */
	private Color		currentColor;
	
	/**
	 * Interface donnant la position du robot
	 */
	private PoseGiver	pg;
	
	/**
	 * dur�e entre deux v�rification de couleur
	 */
	private static final int REFRESHRATE = 100;
	
	/**
	 * @param pg L'interface du PoseGiver initialis� pr�c�dement
	 */
	public AreaManager(PoseGiver pg){
		this.currentColor	= null;
		this.colorSensor	= new ColorSensor();
		this.pg				= pg;
		this.currentArea	= Area.getAreaWithPosition(pg.getPosition());
		Main.printf("[AREA MANAGER]          : Initialized");
	}
	
	@Override
	public void run(){
		Main.printf("[AREA MANAGER]          : Started");
		this.setPriority(Thread.NORM_PRIORITY);
		this.colorSensor.lightOn();
		while(!isInterrupted()){
			if(updateColor()){
				this.currentArea = this.currentArea.colorChange(this.currentColor, this.pg.getPosition());
				//Main.printf("[AREA MANAGER]          : COLOR DETECTED = " + this.currentColor + "NEW AREA = " + this.currentArea.toString());
			}
			syncWait();
		}
		this.colorSensor.lightOff();
		Main.printf("[AREA MANAGER]          : Finished");
	}
	
	/**
	 * V�rifie si la vouleur � changer par rapport � la derni�re v�rification.
	 * A pour effet de bord d'informer le gestionnaire de position si une ligne est detect� (o� l'on est s�r de ses coordonn�es)
	 * @return vrai si la couleur a chang�, faux sinon.
	 */
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
				default:
					// nothing to do
					break;
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Attends pendant un temps d�termin�
	 */
	synchronized private void syncWait(){
		try {
			this.wait(REFRESHRATE);
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
