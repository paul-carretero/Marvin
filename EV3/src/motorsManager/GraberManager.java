package motorsManager;

import java.util.LinkedList;
import java.util.Queue;

import aiPlanner.Main;
import interfaces.WaitProvider;

/**
 * Manager du Graber, permet d'ajouter des opérations a affectuer par les pinces du robot de manière asynchrone.
 * @see Graber
 */
public class GraberManager extends Thread implements WaitProvider{
	
	/**
	 * représente une action (ordre) du graber
	 */
	private enum Action {
	    /**
	     * Instruction pour ouvrir les pinces
	     */
	    OPEN,
	    /**
	     * Instruction pour fermer les pinces
	     */
	    CLOSE
	}
	
	/**
	 * Objet fournissant les primitives pour l'utilisation d'un graber
	 */
	private final Graber graber;
	
	/**
	 * File (FIFO) des actions à faire effectuer par le graber
	 */
	private final Queue<Action> actionList;
	
	/**
	 * Initialise un graberManager permettant d'ajouter et d'effectuer des opérations de grab de manière asynchrone
	 */
	public GraberManager(){
		super("GraberManager");
		this.graber		= new Graber(this);
		this.actionList = new LinkedList<Action>();
		Main.printf("[GRABER]                : Initialized");
	}
	
	@Override
	public void run(){
		Main.printf("[GRABER]                : Started");
		this.setPriority(Thread.NORM_PRIORITY);
		while(! isInterrupted()){
			synchronized(this){
				while(!this.actionList.isEmpty()){
					Action todo = this.actionList.poll();
					if(todo == Action.CLOSE && Main.HAND_OPEN){
						Main.HAND_OPEN = false;
						this.graber.close();
					}
					else if(todo == Action.OPEN && !Main.HAND_OPEN){
						Main.HAND_OPEN = true;
						this.graber.open();
					}
				}
			}
			syncWait(0);
		}
		Main.printf("[GRABER]                : Finished");
	}
	
	/**
	 * Ajoute un ordre de fermeture des pinces dans la file d'action à effectuer.
	 */
	synchronized public void close(){
			if(this.actionList.isEmpty()){
				this.actionList.add(Action.CLOSE);
				this.notifyAll();
			}
			else{
				this.actionList.add(Action.CLOSE);
			}
	}
	
	/**
	 * Ajoute un ordre d'ouverture des pinces dans la file d'action à effectuer.
	 */
	synchronized public void open(){
		if(this.actionList.isEmpty()){
			this.actionList.add(Action.OPEN);
			this.notifyAll();
		}
		else{
			this.actionList.add(Action.OPEN);
		}
	}
	
	/**
	 * Arrête l'action du grabber et supprimes les éventuelles actions en attente
	 * Peut rendre l'état du grabber inconsistant (non fermé et non ouvert)
	 */
	synchronized public void stopGrab(){
		this.graber.stop();
		this.actionList.clear();
		this.notifyAll();
	}
	
	synchronized public void syncWait(int t){
		try {
			this.wait(t);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
