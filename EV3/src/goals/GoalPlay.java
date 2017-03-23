package goals;

import aiPlanner.Main;
import aiPlanner.Marvin;

public class GoalPlay extends Goal {
	
	protected final String NAME = "GoalPlay";

	public GoalPlay(GoalFactory gf, Marvin ia, int timeout) {
		super(gf, ia, timeout);
	}

	@Override
	protected void defineDefault() {
		preConditions.add(Main.HAND_OPEN);
	}

	@Override
	public void start() {
		Main.printf("This is the sort of thing you lifeforms enjoy, is it?");
	}

	@Override
	public String getName() {
		return NAME;
	}

}
