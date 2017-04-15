package aiPlanner;

import eventManager.EventHandler;
import shared.Color;
import goals.Goal;
import goals.GoalFactory;
import goals.GoalType;
import interfaces.SignalListener;
import interfaces.WaitProvider;
import itemManager.CentralIntelligenceService;
import itemManager.EyeOfMarvin;
import itemManager.FakeServer;
import itemManager.Server;
import lejos.hardware.ev3.LocalEV3;
import motorsManager.Engine;
import motorsManager.GraberManager;
import positionManager.AreaManager;
import positionManager.DirectionCalculator;
import positionManager.PositionCalculator;
import positionManager.VisionSensor;

import java.util.Iterator;
import java.util.Map;
import java.util.Deque;

@SuppressWarnings("javadoc")
/**
 * Classe principale du programme de navigation et de Décision du robot, 
 * gère les objectif à accomplir ainsi que les signaux envoyer par les différent gestionnaire d'évennement et de navigtion
 */
public class Marvin implements SignalListener, WaitProvider{
	
	/**
	 * Pile d'objectif à accomplir
	 */
	private	final Deque<Goal> 					goals;
	private	final EyeOfMarvin 					itemManager;
	private	final EventHandler 					eventManager;
	private	final PositionCalculator 			positionManager;
	private	final FakeServer					server;
	private	final GraberManager 				graber;
	private	final Engine 						engine;
	private final GoalFactory 					GFactory;
	private	final AreaManager					areaManager;
	private final SoundManager					audio;
	private final DirectionCalculator			directionCalculator;
	private final CentralIntelligenceService	cis;
	private final VisionSensor					radar;
	
	private boolean		allowMoreGoal		= true;
	private int			linearSpeed			= Main.CRUISE_SPEED;
	private boolean 	allowInterrupt 		= false;
	
	/**
	 * initialise une instance complète du système de navigation et de décision de Marvin
	 */
	public Marvin(){
		
		Main.printf("Not that anyone cares what I say, but the restaurant is at the *other* end of the Universe.");
		
		/**********************************************************/
		
		/*
		 *                       __    __    __    __
		 *                      /  \  /  \  /  \  /  \     This is a Boa Constructor
		 * ____________________/  __\/  __\/  __\/  __\_____________________________
		 * ___________________/  /__/  /__/  /__/  /________________________________
		 *                    | / \   / \   / \   / \  \____
		 *                    |/   \_/   \_/   \_/   \    o \
		 *                                            \_____/--<
		 */
		
		/**********************************************************/
		
		VisionSensor radarTemp 		= null;
		try {
			radarTemp				= new VisionSensor();
		} catch (Exception e) {
			Main.printf(e.getMessage());
			System.exit(1);
		}
		
		this.radar 					= radarTemp;
		this.engine 				= new Engine(this);
		this.eventManager 			= new EventHandler(this,this.radar);
		this.graber 				= new GraberManager();
		this.positionManager		= new PositionCalculator(this.engine.getPilot(), this.radar, this);
		this.directionCalculator 	= new DirectionCalculator(this.positionManager);
		this.itemManager 			= new EyeOfMarvin(this.positionManager);
		this.areaManager			= new AreaManager(this.positionManager);
		this.server 				= new FakeServer(this.itemManager);
		this.audio					= new SoundManager();
		this.cis					= new CentralIntelligenceService(this.itemManager, this.positionManager);

		/**********************************************************/
		
		this.directionCalculator.addEom(this.itemManager);
		this.positionManager.addItemGiver(this.itemManager);
		this.positionManager.addAreaManager(this.areaManager);
		
		this.engine.addMoveListener(this.eventManager);
		this.engine.addMoveListener(this.positionManager);
		
		/**********************************************************/
		
		this.GFactory 				= new GoalFactory(this,this.positionManager, this.itemManager, this.radar, this.cis);
		this.goals 					= this.GFactory.initializeStartGoals();
		
		/**********************************************************/
	}
	
	/**
	 * Lance tout les Threads utilitaires. 
	 * Attends un peu plus de 2 seconde afin de récupérer les données du serveur, 
	 * les calibrer et les marquer comme palet.
	 */
	public void startThreads(){
		this.positionManager.start();
		this.eventManager.start();
		this.graber.start();
		this.server.start();
		this.areaManager.start();
		this.audio.start();
		this.cis.start();
		
		LocalEV3.get().getLED().setPattern(3);
		for(int i = 0; i< 6; i++){
			System.out.print(Main.CHOO_CHOO[i]);
			syncWait(180);
		}
		
		this.itemManager.calibrateSensor();
		
		for(int i = 0; i< 10; i++){
			System.out.println(Main.CHOO_CHOO[i+6]);
			syncWait(180);
		}
		
		System.out.println(Main.CHOO_CHOO[17]);
		
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		
		Main.printf("[MARVIN]                : radar : " + this.radar.getRadarDistance());
		Main.printf("[MARVIN]                : eom position : " + this.itemManager.getMarvinPosition());
	}
	
	/**
	 * Lance l'exécution des objectifs de la pile durant le délai imparti
	 * Une foit fini, termine proprement le programme.
	 */
	public void run(){
		
		this.positionManager.initPose();
		
		Goal newGoal;
		
		while(!this.goals.isEmpty() && (Main.TIMER.getElapsedMin() < 5)){
			newGoal = this.goals.pop();
			if(newGoal != null){
				newGoal.startWrapper();
			}
		}
		
		
		cleanUp();
	}

	/**
	 * @param name nom d'un objectif
	 * @return retourne vrai si aucun objectif de ce type n'est dans la pile, faux sinon
	 */
	private boolean noTypeOfGoal(final GoalType name){ 
		for(Iterator<Goal> itr = this.goals.iterator() ; itr.hasNext() ; )  {
			if(itr.next().getName().equals(name)){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * @param name nom d'un objectif à supprimer de la pile
	 */
	private void deleteGoals(final GoalType name){
		for(Iterator<Goal> itr = this.goals.iterator() ; itr.hasNext() ; )  {
			if(itr.next().getName().equals(name)){
				itr.remove();
			}
		}
	}
	
	/**
	 * Tente d'interrompre les moteurs si l'action est autorisé par la variable allowInterrupt
	 */
	synchronized public void tryInterruptEngine(){
		if(this.allowInterrupt){
			this.engine.stop();
			this.notifyAll();
		}
	}
	
	public void signalLost(){
		if(noTypeOfGoal(GoalType.RECALIBRATE)){
			this.goals.push(this.GFactory.goalRecalibrate());
		}
	}
	
	public void signalNoLost(){
		this.deleteGoals(GoalType.RECALIBRATE);
	}
	
	synchronized public void signalStalled(){
		this.engine.stop();
		goBackward(200);
		this.notifyAll();
	}
	
	synchronized public void signalObstacle(){
		this.engine.stop();
		goBackward(50);
		this.notifyAll();
	}
	
	public void signalPression(){
		tryInterruptEngine();
	}
	
	public void signalStop(){
		this.graber.stopGrab();
		this.engine.stop();
		debug();
		System.exit(2);
	}
	
	/**
	 * Termine proprement les Threads de l'application et termine le programe
	 */
	synchronized private void cleanUp(){
		this.allowMoreGoal = false;
		LocalEV3.get().getLED().setPattern(3);
		this.goals.clear();
		
		this.audio.addVictoryTheme();
		syncWait(2000);
		Main.printf("[MARVIN]                : I'm just trying to die.");
		
		try {
			
			this.server.interrupt();
			this.server.join();
			
			this.graber.interrupt();
			this.graber.join();
			
			this.eventManager.interrupt();
			this.eventManager.join();
			
			this.areaManager.interrupt();
			this.areaManager.join();
			
			this.audio.interrupt();
			this.audio.join();
			
			this.cis.interrupt();
			this.cis.join();
			
			this.positionManager.interrupt();
			this.positionManager.join();
			
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		Main.printf("[MARVIN]                : I told you this would all end in tears.");
		syncWait(1000);
	}

	/**
	 * Ajoute un objectif au sommet de la pile
	 * @param g un Goal à ajouter dans la pile si possible
	 */
	public void pushGoal(final Goal g){
		if(this.allowMoreGoal){
			this.goals.push(g);
		}
	}
	
	synchronized public void syncWait(final int ms){
		try {
			this.wait(ms);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	/**
	 * Fonction permettant de parcourir une distance donné vers l'avant (encapsule les traitement de navigation)
	 * @param distance distance à parcourir
	 */
	public void goForward(final int distance){
		if(distance > 0 && this.linearSpeed > 0){			
			this.directionCalculator.startLine();
			this.positionManager.setIsMovingForward(true);
			
			this.engine.goForward(distance, this.linearSpeed);

			this.directionCalculator.reset();
			this.positionManager.setIsMovingForward(false);
		}
	}
	
	/**
	 * Fonction permettant de parcourir une distance donné vers l'arrière
	 * @param distance distance à parcourir
	 */
	public void goBackward(final int distance){
		if(distance > 0 && this.linearSpeed > 0){
			
			this.positionManager.swap();
			
			for(int i = 0; i<(distance/this.linearSpeed); i++){
				this.audio.addBip();
			}
			
			this.engine.goBackward(distance, this.linearSpeed);
			
			this.positionManager.swap();
		}
	}
	
	/**
	 * Permet de tourner sur place (encapsule les traitement de navigation, ainsi que la présence ou non du palet)
	 * @param angle angle en degrès de rotation par rapport à la position courrante, compris entre -180 et 180
	 */
	public void turnHere(final int angle){
		if(angle != 0){
			this.engine.updateWheelOffset();
			if(Main.HAVE_PALET){
				this.engine.turnHere(angle, Main.SAFE_ROTATION_SPEED);
			}
			else{
				this.engine.turnHere(angle, Main.ROTATION_SPEED);
			}
		}
	}

	/**
	 * Ouvre les pinces du graber si possible, immédiatement si possible
	 */
	public void open() {
		this.graber.open();
	}

	/**
	 * Ferme les pinces du graber si possible, immédiatement si possible
	 */
	public void grab() {
		this.graber.close();
	}
	
	/**
	 * @param value vrai si l'objectif autorise l'ia à interrompre un ordre moteur, faux sinon
	 */
	public void setAllowInterrupt(final boolean value){
		this.allowInterrupt = value;
	}

	/**
	 * @param speed nouvelle vitesse linéaire à utiliser
	 */
	public void setSpeed(int speed) {
		this.linearSpeed = speed;
	}
	
	public void addMeWakeUpOnColor(){
		this.areaManager.addWakeUp(this);
	}
	
	public void removeMeWakeUpOnColor(){
		this.areaManager.removeWakeUp();
	}
	
	/**
	 * Affiche les stackTraces des différent Threads
	 */
	private static void debug() {
		System.err.println("-------- START of StackLog --------");
	    Map<Thread, StackTraceElement[]> liveThreads = Thread.getAllStackTraces();
	    for (Iterator<Thread> i = liveThreads.keySet().iterator(); i.hasNext(); ) {
	      Thread key = i.next();
	      System.err.println("Thread " + key.getName());
	        StackTraceElement[] trace = liveThreads.get(key);
	        for (int j = 0; j < trace.length; j++) {
	        	System.err.println("\tat " + trace[j]);
	        }
	    }
	    System.err.println("--------- END of StackLog ---------");
	}

	public Color getColor() {
		return this.areaManager.getColor();
	}
}
