package aiPlanner;

public class GoalGrab extends Goal {
	
	protected final String NAME = "GoalGrab";

	public GoalGrab(Marvin ia, int timeout) {
		super(ia, timeout);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void defineDefault() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start() {
		Main.printf("Here I am, brain the size of a planet, and they ask me to pick up a piece of paper.");
		
	}

	@Override
	public void preConditionsFailHandler() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected String getName() {
		return NAME;
	}
}
