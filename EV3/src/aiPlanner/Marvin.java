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
	
	private boolean				allowMoreGoal		= true;
	private int					rotationSpeed		= Main.ROTATION_SPEED;
	private int					linearSpeed			= Main.CRUISE_SPEED;
	private boolean 			allowInterrupt 		= false;
	
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
		
		Main.setState(Main.HAS_MOVED, false);
		Main.setState(Main.HAND_OPEN, true);
		Main.setState(Main.HAVE_PALET, false);
		Main.setState(Main.PRESSION, false);
		
		/**********************************************************/
		
		try {
			this.radar				= new VisionSensor();
		} catch (Exception e) {
			Main.printf(e.getMessage());
			System.exit(1);
		}
		
		this.eventManager 			= new EventHandler(this,this.radar);
		this.engine 				= new Engine(this.eventManager,this);
		this.graber 				= new GraberManager();
		this.directionCalculator 	= new DirectionCalculator();
		this.positionManager		= new PositionCalculator(this.engine.getPilot(), this.directionCalculator, this.radar, this);
		this.itemManager 			= new EyeOfMarvin(this.positionManager);
		this.areaManager			= new AreaManager(this.positionManager);
		this.server 				= new Server(this.itemManager);
		this.audio					= new SoundManager();
		this.cis					= new CentralIntelligenceService(this.itemManager, this.positionManager);

		/**********************************************************/
		
		this.directionCalculator.addEom(this.itemManager);
		this.positionManager.addItemGiver(this.itemManager);
		this.positionManager.addAreaManager(this.areaManager);
		
		/**********************************************************/
		
		this.GFactory 				= new GoalFactory(this,this.positionManager, this.itemManager, this.radar);
		this.goals 					= this.GFactory.initializeStartGoals();
		
		/**********************************************************/
		
		LocalEV3.get().getLED().setPattern(3);
		for(int i = 0; i< 5; i++){
			System.out.print("###");
			syncWait(300);
		}
		System.out.print("##");
		
		this.eventManager.setCheckWall(true);
		this.setResearchMode(false);
	}
	
	public void startThreads(){
		this.eventManager.start();
		this.positionManager.start();
		this.graber.start();
		this.server.start();
		this.areaManager.start();
		this.audio.start();
		this.cis.start();
	}
	
	public void run(){
		while(!this.goals.isEmpty() && !(Main.TIMER.getElapsedMin() > 5)){
			this.goals.pop().startWrapper();
		}
		
		cleanUp();
	}
	
	public void setResearchMode(final boolean b) {
		this.eventManager.researchMode(b);
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
	
	synchronized public void tryInterruptEngine(){
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
				this.goals.push(this.GFactory.goalRecalibrate());
			}
			break;
		case STALLED_ENGINE:
			this.engine.stop();
			goBackward(200);
			this.notifyAll();
			break;
		case OBSTACLE:
			this.engine.stop();
			this.notifyAll();
			break;
		case PRESSION_PUSHED:
			tryInterruptEngine();
			break;
		case NO_LOST:
			this.deleteGoals(GoalType.RECALIBRATE);
			break;
		case STOP:
			notifyAll();
			this.engine.stop();
			this.graber.stopGrab();
			this.goals.clear();
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
			
			this.positionManager.interrupt();
			this.positionManager.join();
			
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
		Main.printf("[MARVIN]                : Why should I want to make anything up? Life's bad enough as it is without wanting to invent any more of it.");
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
			this.engine.turnHere(angle, this.rotationSpeed);
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
}
