package goals;

import aiPlanner.Main;
import aiPlanner.Marvin;

public class GoalPlay extends Goal {
	
	protected final GoalType NAME = GoalType.PLAY;

	public GoalPlay(GoalFactory gf, Marvin ia) {
		super(gf, ia);
	}

	@Override
	public void start() {
		
		this.ia.pushGoal(this.gf.play());
		this.ia.pushGoal(this.gf.goalGrabAndDropPalet());

		Main.printf("This is the sort of thing you lifeforms enjoy, is it?");
	}
	
	@Override
	public GoalType getName(){
		return this.NAME;
	}
}
