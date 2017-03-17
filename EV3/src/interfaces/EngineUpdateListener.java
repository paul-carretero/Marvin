package interfaces;

public interface EngineUpdateListener {
	public void backwardUpdateCoef(boolean add);
	public void forwardUpdateCoef(boolean add);
	public void turnHereUpdateCoef(boolean add);
	public void turnSmoothForwardUpdateCoef(boolean add);
	public void turnSmoothBackwardUpdateCoef(boolean add);
}
