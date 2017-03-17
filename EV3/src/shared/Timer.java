package shared;

public class Timer {
	private long start;
	
	public int getElapsedMs(){
		return (int) (System.currentTimeMillis() - start);
	}
	
	public int getElapsedSec(){
		return getElapsedMs()/1000;
	}
	
	public int getElapsedMin(){
		return getElapsedSec()/60;
	}
	
	public void resetTimer(){
		this.start = System.currentTimeMillis();
	}
	
	public Timer(){
		this.start = System.currentTimeMillis();
	}
	
	public Timer(long start){
		this.start = start;
	}
}
