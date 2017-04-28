package area;

import java.util.ArrayList;
import java.util.List;

import aiPlanner.Main;
import interfaces.AreaGiver;
import interfaces.PoseListener;
import lejos.robotics.navigation.Pose;
import shared.Color;

/**
 * class gérant la position du robot par rapport aux 4 lignes de couleurs (jaune, rouge, verte et bleu).
 * @author paul.carretero, florent.chastagner
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
	 * représente la dernière couleur vu par le robot
	 */
	private volatile Color		currentColor;
	
	/**
	 * Couleur de la dernière ligne traversee
	 */
	private volatile Color 		lastLine;
	
	/**
	 * Interface donnant la position du robot
	 */
	private volatile Pose		myPose;
	
	/**
	 * Distance que le robot est en train de parcourir en avant
	 */
	private volatile float		distance = 0;
	
	/**
	 * Objet sur lequelle notifier un thread en attente lorsque l'on detecte une couleur significative
	 */
	private volatile Object		wakeUp;
	
	/**
	 * durée entre deux vérification de couleur
	 */
	private static final int REFRESHRATE = 100;
	
	/**
	 * initialise le gestionnaire de couleur
	 * N'initialise pas les couleur verte et bleu car trop d'erreur de lecture...
	 */
	public AreaManager(){
		super("AreaManager");
		setColor(null);
		this.colorSensor	= new ColorSensor();
		this.myPose 		= new Pose(Main.X_INITIAL, Main.Y_INITIAL, Main.H_INITIAL);
		this.areas			= new ArrayList<Area>();
		
		this.areas.add(new XArea(Color.YELLOW,this));
		this.areas.add(new XArea(Color.RED,this));
		
		if(Main.I_ALSO_LIKE_TO_LIVE_DANGEROUSLY){
			this.areas.add(new YArea(Color.BLUE,this));
			this.areas.add(new YArea(Color.GREEN,this));
		}
		
		
		updateArea(true);
		
		Main.printf("[AREA MANAGER]          : Initialized");
	}
	
	@Override
	public void run(){
		Main.printf("[AREA MANAGER]          : Started");
		this.setPriority(Thread.NORM_PRIORITY);
		this.colorSensor.lightOn();
		Color tColor;
		
		while(!isInterrupted()){
			if(updateColor()){
				tColor = getColor();
				if(tColor != Color.GREY){
					setLastLine(tColor);
					for(Area area : this.areas){
						area.colorChange(tColor, this.myPose.getHeading(), this.distance);
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
	 * @return vrai si la couleur a changé, faux sinon.
	 */
	private boolean updateColor(){
		Color checkColor = this.colorSensor.getCurrentColor();
		if(checkColor != getColor()){
			setColor(checkColor);
			return true;
		}
		return false;
	}
	
	/**
	 * réveille un thread ayant demander à être réveiller si l'on passe sur une couleur significative
	 */
	private void wakeUpOnColor(){
		Color tColor = getColor();
		if(this.wakeUp != null && tColor != Color.GREY && tColor != Color.BLACK && tColor != Color.WHITE){
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

	public void updateArea(final boolean force) {
		for(Area area : this.areas){
			area.updateAreaWithPosition(this.myPose , force);
		}
	}

	public Color getColor() {
		return this.currentColor;
	}
	
	public Color getLastLine(){
		return this.lastLine;
	}

	/**
	 * Definie la couleur courrante
	 * @param c une couleur
	 */
	public void setColor(Color c) {
		this.currentColor = c;
	}
	
	/**
	 * définie la couleur de la dernière ligne
	 * @param c une couleur
	 */
	public void setLastLine(Color c){
		this.lastLine = c;
	}
	
	public void setPose(final Pose p) {
		this.myPose = p;
	}
	
	public void updatePose(final Pose p) {
		for(Area area : this.areas){
			area.updatePose(p);
		}
	}
	
	/**
	 * @return la dernière pose connue par l'areamanager
	 */
	protected Pose getLastPose(){
		return this.myPose;
	}
	
	/**
	 * @param distance la distance que le robot est en train de parcourir en avant
	 */
	public void setDistance(final float distance){
		this.distance = distance;
	}
}
