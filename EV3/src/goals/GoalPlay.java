package goals;

import aiPlanner.Main;
import aiPlanner.Marvin;

public class GoalPlay extends Goal {
	
	protected final GoalType NAME = GoalType.PLAY;

	public GoalPlay(GoalFactory gf, Marvin ia) {
		super(gf, ia);
	}

	@Override
	protected void defineDefault() {
		this.preConditions.add(Main.HAND_OPEN);
	}

	@Override
	public void start() {
		Main.printf("This is the sort of thing you lifeforms enjoy, is it?");
	}

}
