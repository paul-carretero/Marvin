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
import lejos.hardware.ev3.LocalEV3;
import motorsManager.Engine;
import motorsManager.GraberManager;
import positionManager.AreaManager;
import positionManager.DirectionCalculator;
import positionManager.PositionCalculator;
import positionManager.VisionSensor;

import java.util.Iterator;
import java.util.Deque;

@SuppressWarnings("javadoc")
/**
 * Classe principale du programme de navigation et de D�cision du robot, 
 * g�re les objectif � accomplir ainsi que les signaux envoyer par les diff�rent gestionnaire d'�vennement et de navigtion
 */
public class Marvin implements SignalListener, WaitProvider{
	
	/**
	 * Pile d'objectif � accomplir
	 */
	private	final Deque<Goal> 					goals;
	private	final EyeOfMarvin 					itemManager;
	private	final EventHandler 					eventManager;
	private	final PositionCalculator 			positionManager;
	private	final Server						server;
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
	 * initialise une instance compl�te du syst�me de navigation et de d�cision de Marvin
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
		
		VisionSensor radar_temp 	= null;
		try {
			radar_temp				= new VisionSensor();
		} catch (Exception e) {
			Main.printf(e.getMessage());
			System.exit(1);
		}
		this.radar = radar_temp;
		
		this.engine 				= new Engine(this);
		this.eventManager 			= new EventHandler(this,this.radar);
		this.graber 				= new GraberManager();
		this.positionManager		= new PositionCalculator(this.engine.getPilot(), this.radar, this);
		this.directionCalculator 	= new DirectionCalculator(this.positionManager);
		this.itemManager 			= new EyeOfMarvin(this.positionManager);
		this.areaManager			= new AreaManager(this.positionManager);
		this.server 				= new Server(this.itemManager);
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
	 * Attends un peu plus de 2 seconde afin de r�cup�rer les donn�es du serveur, les calibrer et les marquer comme palet.
	 */
	public void startThreads(){
		this.eventManager.start();
		this.graber.start();
		this.server.start();
		this.areaManager.start();
		this.audio.start();
		this.cis.start();
		
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		
		LocalEV3.get().getLED().setPattern(3);
		for(int i = 0; i< 6; i++){
			System.out.print("#");
			syncWait(180);
		}
		
		this.itemManager.calibrateSensor();
		
		for(int i = 0; i< 10; i++){
			System.out.print("#");
			syncWait(180);
		}
		
		System.out.print("#");
		Main.printf("[MARVIN]                : radar : " + this.radar.getRadarDistance());
		Main.printf("[MARVIN]                : eom position : " + this.itemManager.getMarvinPosition());
	}
	
	/**
	 * Lance l'ex�cution des objectifs de la pile durant le d�lai imparti
	 * Une foit fini, termine proprement le programme.
	 */
	public void run(){
		
		this.positionManager.initPose();
		
		/*while(!this.goals.isEmpty() && (Main.TIMER.getElapsedMin() < 5)){
			this.goals.pop().startWrapper();
		}*/
		
		goForward(3000);
		
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
	 * @param name nom d'un objectif � supprimer de la pile
	 */
	synchronized private void deleteGoals(final GoalType name){
		for(Iterator<Goal> itr = this.goals.iterator() ; itr.hasNext() ; )  {
			if(itr.next().getName().equals(name)){
				itr.remove();
			}
		}
	}
	
	/**
	 * Tente d'interrompre les moteurs si l'action est autoris� par la variable allowInterrupt
	 */
	synchronized public void tryInterruptEngine(){
		if(this.allowInterrupt){
			this.engine.stop();
			notifyAll();
		}
	}
	
	synchronized public void signalLost(){
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
		Main.printf("Obstacle detected");
		this.engine.stop();
		this.notifyAll();
	}
	
	public void signalPression(){
		tryInterruptEngine();
	}
	
	public void signalStop(){
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
			
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		Main.printf("[MARVIN]                : I told you this would all end in tears.");
		syncWait(1000);
	}

	/**
	 * Ajoute un objectif au sommet de la pile
	 * @param g un Goal � ajouter dans la pile si possible
	 */
	synchronized public void pushGoal(final Goal g){
		//Main.printf("[MARVIN]                : Why should I want to make anything up? Life's bad enough as it is without wanting to invent any more of it.");
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
	 * Fonction permettant de parcourir une distance donn� vers l'avant (encapsule les traitement de navigation)
	 * @param distance distance � parcourir
	 */
	public void goForward(final int distance){
		if(distance > 0 && this.linearSpeed > 0){
			
			Main.printf("go forward 1111");
			
			this.directionCalculator.startLine();
			this.positionManager.setIsMovingForward(true);
			
			Main.printf("go forward 2222");
			
			this.engine.goForward(distance, this.linearSpeed);
			
			Main.printf("go forward 3333");
			
			this.directionCalculator.reset();
			this.positionManager.setIsMovingForward(false);
			
			Main.printf("go forward 4444");
			
		}
	}
	
	/**
	 * Fonction permettant de parcourir une distance donn� vers l'arri�re
	 * @param distance distance � parcourir
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
	 * Permet de tourner sur place (encapsule les traitement de navigation, ainsi que la pr�sence ou non du palet)
	 * @param angle angle en degr�s de rotation par rapport � la position courrante, compris entre -180 et 180
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
	 * Ouvre les pinces du graber si possible, imm�diatement si possible
	 */
	public void open() {
		this.graber.open();
	}

	/**
	 * Ferme les pinces du graber si possible, imm�diatement si possible
	 */
	public void grab() {
		this.graber.close();
	}
	
	/**
	 * @param value vrai si l'objectif autorise l'ia � interrompre un ordre moteur, faux sinon
	 */
	public void setAllowInterrupt(final boolean value){
		this.allowInterrupt = value;
	}

	/**
	 * @param speed nouvelle vitesse lin�aire � utiliser
	 */
	public void setSpeed(int speed) {
		this.linearSpeed = speed;
	}
}
