package shared;

/**
 * Classe utilitaire proposant une impl�mentation simple d'un timer pour mesurer le temps.
 * @author paul.carretero, florent.chastagner
 */
public class Timer {
	/**
	 * temps de r�f�rence utilis� par le timer
	 */
	private long start;
	
	/**
	 * @return le nombre de millisecondes �coul�s depuis le temps de r�f�rence
	 */
	public int getElapsedMs(){
		return (int) (System.currentTimeMillis() - this.start);
	}
	
	/**
	 * @return le nombre de secondes �coul�s depuis le temps de r�f�rence
	 */
	public int getElapsedSec(){
		return getElapsedMs()/1000;
	}
	
	/**
	 * @return le nombre de minutes �coul�s depuis le temps de r�f�rence
	 */
	public int getElapsedMin(){
		return getElapsedSec()/60;
	}
	
	/**
	 * mise � 0 du timer, on red�finie le temps de r�f�rence comme le temps courrant
	 */
	public void resetTimer(){
		this.start	= System.currentTimeMillis();
	}
	
	/**
	 * Instancie le timer avec le temps courrant comme r�f�rence
	 */
	public Timer(){
		this.start	= System.currentTimeMillis();
	}
}
