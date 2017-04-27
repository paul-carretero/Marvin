package goals;

import aiPlanner.Main;
import aiPlanner.Marvin;

/**
 * Représente un objectif et ses fonctions communes.
 * @author paul.carretero
 */
public abstract class Goal {
	
	/**
	 * instance de Marvin, gestionnaire de l'ia et des moteurs
	 */
	protected final Marvin		ia;
	/**
	 * le GoalFactory
	 */
	protected final	GoalFactory	gf;
	
	/**
	 * @param gf le GoalFactory
	 * @param ia instance de Marvin, gestionnaire de l'ia et des moteurs
	 */
	protected Goal(final GoalFactory gf, final Marvin ia) {
		this.ia	= ia;
		this.gf	= gf;
	}
	
	/**
	 * @return un GoalType correspondant au nom de l'objectif
	 */
	public abstract GoalType getName();

	/**
	 * @return vrai si les préconditions sont vérifiées, faux sinon
	 */
	protected boolean checkPreConditions(){
		return true;
	}
	
	/**
	 * Méthode encapsulant le fonctionnement de l'objectif, test les préconditions et le lance notament.
	 */
	public void startWrapper(){
		if(this.checkPreConditions()){
			Main.printf("[GOAL]                  : EXECUTE GOAL : " + getName());
			this.start();
		}
		else{
			Main.printf("[GOAL]                  : FAIL EXECUTE GOAL : " + getName());
		}
	}
	
	/**
	 * Démarre l'objectif en assumant que ses préconditions sont vérifiées
	 */
	protected abstract void start();
}
