package shared;

/**
 * Classe utilitaire proposant une implémentation simple d'un timer pour mesurer le temps.
 * @author paul.carretero, florent.chastagner
 */
public class Timer {
	/**
	 * temps de référence utilisé par le timer
	 */
	private long start;
	
	/**
	 * @return le nombre de millisecondes écoulés depuis le temps de référence
	 */
	public int getElapsedMs(){
		return (int) (System.currentTimeMillis() - this.start);
	}
	
	/**
	 * @return le nombre de secondes écoulés depuis le temps de référence
	 */
	public int getElapsedSec(){
		return getElapsedMs()/1000;
	}
	
	/**
	 * @return le nombre de minutes écoulés depuis le temps de référence
	 */
	public int getElapsedMin(){
		return getElapsedSec()/60;
	}
	
	/**
	 * mise à 0 du timer, on redéfinie le temps de référence comme le temps courrant
	 */
	public void resetTimer(){
		this.start	= System.currentTimeMillis();
	}
	
	/**
	 * Instancie le timer avec le temps courrant comme référence
	 */
	public Timer(){
		this.start	= System.currentTimeMillis();
	}
}
