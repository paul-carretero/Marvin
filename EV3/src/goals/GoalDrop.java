package goals;

import aiPlanner.Marvin;

public class GoalDrop extends Goal{
	
	protected	final	String	NAME	= "GoalDrop";

	public GoalDrop(Marvin ia, int timeout) {
		super(ia, timeout);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void defineDefault() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void preConditionsFailHandler() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		return NAME;
	}
}
