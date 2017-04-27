package aiPlanner;

import eventManager.EventHandler;
import goals.Goal;
import goals.GoalFactory;
import goals.GoalType;
import interfaces.SignalListener;
import interfaces.WaitProvider;
import itemManager.CentralIntelligenceService;
import itemManager.EyeOfMarvin;
import itemManager.Server;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.robotics.navigation.Pose;
import motorsManager.Engine;
import motorsManager.Graber;
import positionManager.DirectionCalculator;
import positionManager.PositionCalculator;
import positionManager.VisionSensor;

import java.util.Iterator;
import java.util.Map;

import area.AreaManager;

import java.util.Deque;

/**
 * Classe principale du programme de navigation et de Décision du robot, 
 * gère les objectif à accomplir ainsi que les signaux envoyer par les différent gestionnaire d'évennement et de navigtion
 * @author paul.carretero
 */
public class Marvin implements SignalListener, WaitProvider{
	
	/**
	 * Pile d'objectif à accomplir
	 */
	private	final Deque<Goal> 					goals;
	/**
	 * EyeOfMarvin, gestionnaire des item sur le terrain
	 */
	private	final EyeOfMarvin 					itemManager;
	/**
	 * Gestionnaire d'evenements et des interaction de l'exterieur vers le robot
	 */
	private	final EventHandler 					eventManager;
	/**
	 * Gestionnaire de position, calcul la position après chaque deplacement
	 */
	private	final PositionCalculator 			positionManager;
	/**
	 * Serveur permettant de receptionner les donnes fourni par la camera
	 */
	private	final Server						server;
	/**
	 * Gere les pinces du robot
	 */
	private	final Graber 						graber;
	/**
	 * Gere les deplacement du robot
	 */
	private	final Engine 						engine;
	/**
	 * GoalFactory permettant d'encaspuler la creation des objectifs avec des primitives simples
	 */
	private final GoalFactory 					GFactory;
	/**
	 * Gestionnaires des Area et des couleurs (lignes sur le terrain)
	 */
	private	final AreaManager					areaManager;
	/**
	 * Thread optionnel permettant de gerer des flux audio
	 */
	private final SoundManager					audio;
	/**
	 * Gestionnaire de la direction, calcul la direction du robot entre chaque deplacement
	 */
	private final DirectionCalculator			directionCalculator;
	/**
	 * Thread optionnel permettant de detecter les mouvement eventuel du robot ennemi et de l'intercepter
	 */
	private final CentralIntelligenceService	cis;
	/**
	 * Gestionnaire du radar a ultrason
	 */
	private final VisionSensor					radar;
	
	/**
	 * Vitesse a utiliser par le robot
	 */
	private int		linearSpeed		= Main.CRUISE_SPEED;
	/**
	 * Autorise ou non les interruptions lors de la detection d'une pression
	 */
	private boolean allowInterrupt 	= false;
	
	/**
	 * initialise une instance complète du système de navigation et de décision de Marvin
	 */
	public Marvin(){
		
		Main.printf("[MARVIN]                : Not that anyone cares what I say, but the restaurant is at the *other* end of the Universe.");
		
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
		this.graber 				= new Graber();
		this.itemManager 			= new EyeOfMarvin();
		this.areaManager			= new AreaManager();
		this.positionManager		= new PositionCalculator(this.engine.getPilot(), this.radar, this, this.itemManager, this.areaManager);
		this.directionCalculator 	= new DirectionCalculator(this.positionManager);
		
		this.server 				= new Server(this.itemManager);
		this.audio					= new SoundManager();
		this.cis					= new CentralIntelligenceService(this.itemManager, this.positionManager);

		/**********************************************************/
		
		this.engine.addMoveListener(this.eventManager);
		
		this.positionManager.addPoseListener(this.areaManager);
		this.positionManager.addPoseListener(this.itemManager);
		
		/**********************************************************/
		
		this.GFactory 				= new GoalFactory(this,this.positionManager, this.itemManager, this.radar, this.cis, this.areaManager);
		this.goals 					= this.GFactory.initializeStartGoals();
		
		/**********************************************************/
		
	}
	
	/********************************************************
	 * Phases de lancement et d'arret du programme principal
	 *******************************************************/
	
	/**
	 * Lance tout les Threads utilitaires. 
	 * Attends un peu plus de 2 seconde afin de récupérer les données du serveur, 
	 * les calibrer et les marquer comme palet.
	 */
	public void startThreads(){
		this.server.start();
		this.eventManager.start();
		this.areaManager.start();
		if(Main.I_ALSO_LIKE_TO_LIVE_DANGEROUSLY){
			this.cis.start();
		}
		if(Main.USE_SOUND){
			this.audio.start();
		}
		
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
	 * Termine proprement les Threads de l'application et termine le programe
	 */
	private void cleanUp(){
		Main.log("[MARVIN]                : cleanUp");
		LocalEV3.get().getLED().setPattern(3);
		this.goals.clear();
		this.audio.addVictoryTheme();
		syncWait(2000);
		Main.printf("[MARVIN]                : I'm just trying to die.");
		
		try {
			
			this.server.interrupt();
			this.server.join();
			
			this.eventManager.interrupt();
			this.eventManager.join();
			
			this.areaManager.interrupt();
			this.areaManager.join();
			
			this.audio.interrupt();
			this.audio.join();
			
			this.cis.interrupt();
			this.cis.join();
			
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		Main.printf("[MARVIN]                : I told you this would all end in tears.");
		syncWait(1000);
	}

	/********************************************************
	 * Primitives de navigation
	 *******************************************************/
	
	/**
	 * Fonction permettant de parcourir une distance donné vers l'avant (encapsule les traitement de navigation)
	 * @param distance distance à parcourir
	 */
	synchronized public void goForward(final float distance){
		if(distance > 0 && this.linearSpeed > 0){	
						
			if(distance > Main.FIABLE_DIST){
				this.directionCalculator.startLine();
			}
			this.positionManager.startLine(distance);
			this.areaManager.setDistance(distance);
			
			boolean everythingFine = this.engine.goForward(distance, this.linearSpeed);
			
			this.directionCalculator.reset();
			this.areaManager.setDistance(0);

			this.positionManager.endLine(everythingFine);
		}
	}
	
	/**
	 * Fonction permettant de parcourir une distance donné vers l'arrière
	 * @param distance distance à parcourir
	 */
	synchronized public void goBackward(final float distance){
		if(distance > 0 && this.linearSpeed > 0){
			
			Pose myFuturePose = this.positionManager.getPosition();
			myFuturePose.moveUpdate((-1) * distance);
			
			this.engine.goBackward(distance, this.linearSpeed);
			this.positionManager.setPose(myFuturePose, false);
		}
	}
	
	/**
	 * Permet de tourner sur place (encapsule les traitement de navigation, ainsi que la présence ou non du palet)
	 * @param f angle en degrès de rotation par rapport à la position courrante, compris entre -180 et 180
	 */
	synchronized public void turnHere(final float f){
		if(f != 0){
			this.engine.updateWheelOffset();
			if(Main.HAVE_PALET){
				this.engine.turnHere(f, Main.SAFE_ROTATION_SPEED);
			}
			else{
				this.engine.turnHere(f, Main.ROTATION_SPEED);
			}
		}
	}

	/********************************************************
	 * Primitives pour la configuration des objectifs
	 *******************************************************/
	
	/**
	 * @param value vrai si l'objectif autorise l'ia à interrompre un ordre moteur, faux sinon
	 */
	synchronized public void setAllowInterrupt(final boolean value){
		this.allowInterrupt = value;
	}

	/**
	 * @param speed nouvelle vitesse linéaire à utiliser
	 */
	synchronized public void setSpeed(int speed) {
		this.linearSpeed = speed;
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
	
	/********************************************************
	 * Gestion de la pile d'objectif
	 *******************************************************/
	
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
	 * Ajoute un objectif au sommet de la pile<br/>
	 * N'ajoutera pas l'objectif si le prochaine objectif a etre executer est l'objectif de recalibration
	 * @param g un Goal à ajouter dans la pile si possible
	 */
	public void pushGoal(final Goal g){
		Goal peek = this.goals.peek();
		if(peek != null && peek.getName() != GoalType.RECALIBRATE){
			this.goals.push(g);
		}
	}
	
	/********************************************************
	 * Gestion des Area/couleurs
	 *******************************************************/
	
	/**
	 * Indique que l'on souhaite est interrompu lorsque l'on croise une ligne de couleur
	 */
	public void addMeWakeUpOnColor(){
		this.areaManager.addWakeUp(this);
	}
	
	/**
	 * indique au gestionnaire de couleur de ne plus interrompre lorsque l'on
	 */
	public void removeMeWakeUpOnColor(){
		this.areaManager.removeWakeUp();
	}
	
	/********************************************************
	 * Gestion des signaux
	 *******************************************************/
	
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
		Sound.beep();
		Main.printf("[MARVIN]                : MAYDAY MAYDAY MAYDAY");
		if(noTypeOfGoal(GoalType.RECALIBRATE)){
			this.goals.push(this.GFactory.goalRecalibrate());
		}
	}
	
	public void signalNoLost(){
		this.deleteGoals(GoalType.RECALIBRATE);
	}
	
	synchronized public void signalStalled(){
		Main.log("[MARVIN]                : signalStalled");
		syncWait(200);
		this.engine.stop();
		goBackward(200);
		this.notifyAll();
	}
	
	synchronized public void signalObstacle(){
		syncWait(200);
		this.engine.stop();
		Pose myPose = this.positionManager.getPosition();
		// si c'est un mur
		if(myPose.getX() < 150 || myPose.getX() > 1850 || myPose.getY() < 150 || myPose.getY() > 2850){
			Main.log("[MARVIN]                : signalObstacle : Mur");
			goBackward(100);
		}
		else{
			Main.log("[MARVIN]                : signalObstacle : Obstacle");
			goBackward(200);
			turnHere(90);
			goBackward(200);
			turnHere(90);
		}
		this.notifyAll();
	}
	
	public void signalPression(){
		Main.log("[MARVIN]                : signalPression");
		tryInterruptEngine();
	}
	
	public void signalStop(){
		Main.log("[MARVIN]                : signalStop");
		this.graber.stop();
		this.engine.stop();
		if(Main.PRINT_LOG){
			debug();
		}
		System.exit(2);
	}
	
	/********************************************************
	 * Divers
	 *******************************************************/
	
	synchronized public void syncWait(final int ms){
		try {
			this.wait(ms);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
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
}
