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
	private Area 				currentArea;
	
	/**
	 * Capteur de couleur
	 */
	private final ColorSensor	colorSensor;
	
	/**
	 * repr�sente la derni�re couleur vu par le robot
	 */
	private Color				currentColor;
	
	/**
	 * Interface donnant la position du robot
	 */
	private final PoseGiver		pg;
	
	/**
	 * Objet sur lequelle notifier un thread en attente lorsque l'on detecte une couleur significative
	 */
	private Object				wakeUp;
	
	/**
	 * Couleur de la derni�re ligne traversee
	 */
	private Color 				lastLine;
	
	/**
	 * dur�e entre deux v�rification de couleur
	 */
	private static final int REFRESHRATE = 100;
	
	/**
	 * @param pg L'interface du PoseGiver initialis� pr�c�dement
	 */
	public AreaManager(PoseGiver pg){
		super("AreaManager");
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
				synchronized(this){
					this.currentArea = this.currentArea.colorChange(this.currentColor, this.pg.getPosition().getHeading());
					Main.log("[AREA MANAGER]          : Couleur detecte : " + this.currentColor);
					Main.printf("[AREA MANAGER]          : AREA = " + this.currentArea.toString());
					if(this.currentColor != Color.GREY){
						this.lastLine = this.currentColor;
					}
				}
				wakeUpOnColor();
			}
			syncWait();
		}
		this.colorSensor.lightOff();
		Main.printf("[AREA MANAGER]          : Finished");
	}
	
	/**
	 * @param w un objet moniteur
	 */
	public void addWakeUp(final Object w){
		this.wakeUp = w;
	}
	
	/**
	 * supprime le moniteur
	 */
	public void removeWakeUp(){
		this.wakeUp = null;
	}
	
	/**
	 * V�rifie si la vouleur � changer par rapport � la derni�re v�rification.
	 * A pour effet de bord d'informer le gestionnaire de position si une ligne est detect� (o� l'on est s�r de ses coordonn�es)
	 * @return vrai si la couleur a chang�, faux sinon.
	 */
	synchronized private boolean updateColor(){
		Color checkColor = this.colorSensor.getCurrentColor();
		if(checkColor != this.currentColor){
			this.currentColor = checkColor;
			return true;
		}
		return false;
	}
	
	/**
	 * r�veille un thread ayant demander � �tre r�veiller si l'on passe sur une couleur significative
	 */
	synchronized private void wakeUpOnColor(){
		if(this.wakeUp != null && this.currentColor != Color.GREY && this.currentColor != Color.BLACK && this.currentColor != Color.WHITE){
			synchronized (this.wakeUp) {
				this.wakeUp.notify();
			}
		}
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

	synchronized public Area getCurrentArea() {
		return this.currentArea;
	}

	synchronized public void updateArea() {
		this.currentArea = Area.getAreaWithPosition(this.pg.getPosition());
	}

	/**
	 * @return la couleur courrante (sur la ou l'on est)
	 */
	synchronized public Color getColor() {
		return this.currentColor;
	}
	
	/**
	 * @return la couleur de la derni�re ligne traversee
	 */
	synchronized public Color getLastLine(){
		return this.lastLine;
	}
}
