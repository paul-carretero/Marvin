package aiPlanner;

public class GoalRecalibrate extends Goal {
	
	protected final String NAME = "GoalRecalibrate";

	public GoalRecalibrate(Marvin ia) {
		super(ia);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void defineDefault() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start() {
		Main.printf("Sounds awful.");
		
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
