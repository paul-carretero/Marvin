package positionManager;

import aiPlanner.Main;
import area.Area;
import interfaces.AreaGiver;
import interfaces.PoseListener;
import lejos.robotics.navigation.Pose;
import shared.Color;

/**
 * class gérant la position du robot sur les 15+1 zones du terrain en fonction des lignes de couleurs.
 */
public class AreaManager extends Thread implements AreaGiver, PoseListener {
	
	/**
	 * Contient l'Area dans laquelle le robot se trouve
	 */
	private Area 				currentArea;
	
	/**
	 * Capteur de couleur
	 */
	private final ColorSensor	colorSensor;
	
	/**
	 * représente la dernière couleur vu par le robot
	 */
	private Color				currentColor;
	
	/**
	 * Interface donnant la position du robot
	 */
	private volatile Pose		myPose;
	
	/**
	 * Objet sur lequelle notifier un thread en attente lorsque l'on detecte une couleur significative
	 */
	private Object				wakeUp;
	
	/**
	 * Couleur de la dernière ligne traversee
	 */
	private Color 				lastLine;
	
	/**
	 * durée entre deux vérification de couleur
	 */
	private static final int REFRESHRATE = 100;
	
	/**
	 * initialise le gestionnaire de couleur
	 */
	public AreaManager(){
		super("AreaManager");
		this.currentColor	= null;
		this.colorSensor	= new ColorSensor();
		this.currentArea	= Area.getAreaWithPosition(new Pose(Main.X_INITIAL, Main.Y_INITIAL, Main.H_INITIAL));
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
					this.currentArea = this.currentArea.colorChange(this.currentColor, this.myPose.getHeading());
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
	 * Vérifie si la vouleur à changer par rapport à la dernière vérification.
	 * A pour effet de bord d'informer le gestionnaire de position si une ligne est detecté (où l'on est sûr de ses coordonnées)
	 * @return vrai si la couleur a changé, faux sinon.
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
	 * réveille un thread ayant demander à être réveiller si l'on passe sur une couleur significative
	 */
	synchronized private void wakeUpOnColor(){
		if(this.wakeUp != null && this.currentColor != Color.GREY && this.currentColor != Color.BLACK && this.currentColor != Color.WHITE){
			synchronized (this.wakeUp) {
				this.wakeUp.notify();
			}
		}
	}
	
	/**
	 * Attends pendant un temps déterminé
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
		this.currentArea = Area.getAreaWithPosition(this.myPose);
	}

	/**
	 * @return la couleur courrante (sur la ou l'on est)
	 */
	synchronized public Color getColor() {
		return this.currentColor;
	}
	
	/**
	 * @return la couleur de la dernière ligne traversee
	 */
	synchronized public Color getLastLine(){
		return this.lastLine;
	}

	synchronized public void setPose(Pose p) {
		this.myPose = p;
	}
}
