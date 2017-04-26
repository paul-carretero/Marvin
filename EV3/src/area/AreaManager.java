package area;

import java.util.ArrayList;
import java.util.List;

import aiPlanner.Main;
import interfaces.AreaGiver;
import interfaces.PoseListener;
import lejos.robotics.navigation.Pose;
import positionManager.ColorSensor;
import shared.Color;

/**
 * class g�rant la position du robot sur les 15+1 zones du terrain en fonction des lignes de couleurs.
 */
public class AreaManager extends Thread implements AreaGiver, PoseListener {
	
	/**
	 * Contient l'Area dans laquelle le robot se trouve
	 */
	private final List<Area> 	areas;
	
	/**
	 * Capteur de couleur
	 */
	private final ColorSensor	colorSensor;
	
	/**
	 * repr�sente la derni�re couleur vu par le robot
	 */
	private Color				currentColor;
	
	/**
	 * Couleur de la derni�re ligne traversee
	 */
	private Color 				lastLine;
	
	/**
	 * Interface donnant la position du robot
	 */
	private volatile Pose		myPose;
	
	/**
	 * Objet sur lequelle notifier un thread en attente lorsque l'on detecte une couleur significative
	 */
	private volatile Object		wakeUp;
	
	/**
	 * dur�e entre deux v�rification de couleur
	 */
	private static final int REFRESHRATE = 100;
	
	/**
	 * initialise le gestionnaire de couleur
	 */
	public AreaManager(){
		super("AreaManager");
		this.currentColor	= null;
		this.colorSensor	= new ColorSensor();
		this.myPose 		= new Pose(Main.X_INITIAL, Main.Y_INITIAL, Main.H_INITIAL);
		this.areas			= new ArrayList<Area>();
		
		this.areas.add(new XArea(Color.YELLOW));
		this.areas.add(new XArea(Color.RED));
		this.areas.add(new YArea(Color.BLUE));
		this.areas.add(new YArea(Color.GREEN));
		
		updateArea(true);
		
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
					if(this.currentColor != Color.GREY){
						this.lastLine = this.currentColor;
						for(Area area : this.areas){
							area.colorChange(this.currentColor, this.myPose.getHeading());
						}
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

	synchronized public void updateArea(boolean force) {
		for(Area area : this.areas){
			area.updateAreaWithPosition(this.myPose , force);
		}
	}

	synchronized public Color getColor() {
		return this.currentColor;
	}
	
	synchronized public Color getLastLine(){
		return this.lastLine;
	}

	synchronized public void setPose(Pose p) {
		this.myPose = p;
	}
	
	synchronized public void updatePose(Pose p) {
		for(Area area : this.areas){
			area.updatePose(p);
		}
	}
}
