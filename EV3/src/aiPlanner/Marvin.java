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
import shared.SignalType;

import java.util.Iterator;
import java.util.Deque;

public class Marvin implements SignalListener, WaitProvider{
	
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
	private VisionSensor						radar;
	
	private boolean		allowMoreGoal		= true;
	private int			linearSpeed			= Main.CRUISE_SPEED;
	private boolean 	allowInterrupt 		= false;
	
	/*
	 * @return initialise une instance complète du système de navigation Marvin
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
		
		try {
			this.radar				= new VisionSensor();
		} catch (Exception e) {
			Main.printf(e.getMessage());
			System.exit(1);
		}
		
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
		
		this.eventManager.setCheckWall(true);
		
		Main.printf("[MARVIN]                : radar : " + this.radar.getRadarDistance());
		Main.printf("[MARVIN]                : eom position : " + this.itemManager.getMarvinPosition());
	}
	
	/**
	 * Lance tout les Threads utilitaires. 
	 * Attends un peu plus de 2 seconde afin de récupérer les données du serveur, les calibrer et les marquer comme palet.
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
	}
	
	/**
	 * Lance l'exécution des objectifs de la pile durant le délai imparti
	 * Une foit fini, termine proprement le programme.
	 */
	public void run(){
		
		this.positionManager.initPose();
		
		while(!this.goals.isEmpty() && (Main.TIMER.getElapsedMin() < 5)){
			this.goals.pop().startWrapper();
		}
		
		cleanUp();
	}

	private boolean noTypeOfGoal(final GoalType name){ 
		for(Iterator<Goal> itr = this.goals.iterator() ; itr.hasNext() ; )  {
			if(itr.next().getName().equals(name)){
				return false;
			}
		}
		return true;
	}
	
	private void deleteGoals(final GoalType name){
		for(Iterator<Goal> itr = this.goals.iterator() ; itr.hasNext() ; )  {
			if(itr.next().getName().equals(name)){
				itr.remove();
			}
		}
	}
	
	public void tryInterruptEngine(){
		if(this.allowInterrupt){
			this.engine.stop();
			notifyAll();
		}
	}
	
	synchronized public void signal(final SignalType e) {
		Main.printf("[MARVIN]                : Signal received : " + e.toString());
		switch (e) {
		case LOST:
			if(noTypeOfGoal(GoalType.RECALIBRATE)){
				//this.goals.push(this.GFactory.goalRecalibrate());
			}
			break;
		case STALLED_ENGINE:
			this.engine.stop();
			goBackward(200);
			this.notifyAll();
			break;
		case OBSTACLE:
			/*this.engine.stop();
			this.notifyAll();*/
			break;
		case PRESSION_PUSHED:
			tryInterruptEngine();
			break;
		case NO_LOST:
			this.deleteGoals(GoalType.RECALIBRATE);
			break;
		case STOP:
			System.exit(2);
			break;
		default:
			break;
		}
	}
	
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
	
	public void goForward(final int distance){
		if(distance > 0 && this.linearSpeed > 0){
			
			this.directionCalculator.startLine();
			
			this.engine.goForward(distance, this.linearSpeed);
			
			this.directionCalculator.reset();
			
		}
	}
	
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
	
	public void turnHere(final int angle){
		if(angle != 0){
			this.engine.updateWheelOffset();
			if(Main.HAVE_PALET){
				this.engine.turnHere(angle, Main.ROTATION_SPEED);
			}
			else{
				this.engine.turnHere(angle, Main.ROTATION_SPEED);
			}
		}
	}

	public void open() {
		this.graber.open();
	}

	public void grab() {
		this.graber.close();
		syncWait(300);
	}
	
	public void setAllowInterrupt(final boolean value){
		this.allowInterrupt = value;
	}

	public void setSpeed(int speed) {
		this.linearSpeed = speed;
	}
}
