package goals;

import aiPlanner.Main;
import aiPlanner.Marvin;
import interfaces.ItemGiver;
import shared.IntPoint;

public class GoalGrabAndDropPalet extends Goal {
	
	protected final GoalType NAME = GoalType.GRAB_AND_DROP;
	private ItemGiver 		 eom;

	public GoalGrabAndDropPalet(GoalFactory gf,  Marvin ia, ItemGiver eom) {
		super(gf, ia);
		this.eom = eom;
	}
	
	@Override
	public void start() {
		IntPoint palet = this.eom.getNearestpalet();
		
		System.out.println("palet = " + palet);
		
		if(palet != null){
			this.ia.pushGoal(this.gf.goalDrop());
			this.ia.pushGoal(this.gf.goalGrab(palet.toLejosPoint()));
		}
	}
	
	@Override
	public GoalType getName(){
		return this.NAME;
	}

	@Override
	protected boolean checkPreConditions() {
		if(!Main.HAND_OPEN){
			this.ia.open();
		}
		return true;
	}
}
