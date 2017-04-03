package itemManager;

import aiPlanner.Main;
import interfaces.ItemGiver;
import interfaces.PoseGiver;
import lejos.robotics.geometry.Point;
import shared.IntPoint;
import shared.Item;

public class CentralIntelligenceService extends Thread{
	private ItemGiver			eom;
	private PoseGiver 			pg;
	private IntPoint			interceptionTarget;
	private Item				firstContact;
	private Item				confirmationContact;
	private volatile Object		notifyOnUptate;
	private static final int	SIGNIFICATIVE_DISTANCE = 70; // 7cm
	
	public CentralIntelligenceService(ItemGiver eom, PoseGiver pg){
		this.pg						= pg;
		this.interceptionTarget		= null;
		this.firstContact 			= null;
		this.confirmationContact	= null;
		this.notifyOnUptate			= null;
		this.eom					= eom;
		Main.printf("[CIS]                   : Initialized");
	}
	
	@Override
	public void run(){
		Main.printf("[CIS]                   : started");
		this.setPriority(MIN_PRIORITY);
		while(!isInterrupted()){
			
			// si on a pas de premier contact alors on tente d'en repérer un.
			if(this.firstContact == null){
				updateInterceptionTarget(null);
				this.firstContact = this.eom.getPossibleEnnemy();
			}
			else{
				this.confirmationContact = this.eom.getPossibleEnnemy();
				
				// si le contact n'a pas bougé ou si il a disparu, on reset
				if(this.confirmationContact != this.firstContact && this.confirmationContact != null){
					
					// on vérifie que la distance est significative pour pouvoir extrapoler à une droite
					if(this.firstContact.getDistance(this.confirmationContact) > SIGNIFICATIVE_DISTANCE){
						IntPoint intersection = this.confirmationContact.getIntersection(this.firstContact.computeVector(this.confirmationContact));
						// on vérifie que l'ennemy avance vers l'intersection et que l'intersection est bien dans le terrain
						if(intersection != null && intersection.x() > 0 && intersection.x() < 3000 && this.confirmationContact.getDistance(intersection) < this.firstContact.getDistance(intersection)){
							updateInterceptionTarget(intersection);
						}
						else{
							updateInterceptionTarget(null);
						}
					}
					else{
						updateInterceptionTarget(null);
					}
				}
				else{
					updateInterceptionTarget(null);
				}
				
				// on réinitialise firstcontact à confirmationcontact (si il change de trajectoire ou autre)
				this.firstContact = this.confirmationContact;
				this.confirmationContact = null;
			}
			syncWait();
		}
		Main.printf("[CIS]                   : Finished");
	}
	
	public void addEnnemyMoveNotifier(Object o){
		this.notifyOnUptate = o;
	}
	
	public void resetEnnemyMoveNotifier(){
		this.notifyOnUptate = null;
	}
	
	private void updateInterceptionTarget(IntPoint target){
		if(target != null){
			if(!target.equals(this.interceptionTarget)){
				this.interceptionTarget = target;
				if(this.notifyOnUptate != null){
					synchronized (this.notifyOnUptate) {
						this.notifyOnUptate.notifyAll();
					}
				}
			}
		}
		else{
			this.interceptionTarget = null;
		}
	}
	
	synchronized private void syncWait(){
		try {
			this.wait(800);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	public Point getInterceptionLocation(){
		Point ennemy = this.eom.getPossibleEnnemy().toLejosPoint();
		if(this.interceptionTarget != null && ennemy != null
			&& (this.pg.getPosition().distanceTo(this.interceptionTarget.toLejosPoint()) < ennemy.distance(this.interceptionTarget.toLejosPoint()))){
			
			return this.interceptionTarget.toLejosPoint();
		}
		return null;
	}
}
