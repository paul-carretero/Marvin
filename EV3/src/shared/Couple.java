package shared;

public class Couple {

	private final IntPoint first;
	private final IntPoint second;

	public Couple(IntPoint first, IntPoint second) {
	    this.first	= first;
		this.second	= second;
	}

	public IntPoint getfirst() { 
		return this.first; 
	}
	  
	public IntPoint getsecond() { 
		return this.second;
	}
}
	