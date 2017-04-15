package goals;

import aiPlanner.Main;
import aiPlanner.Marvin;

/**
 * Représente un objectif et ses fonctions communes
 */
public abstract class Goal {
	
	/**
	 * instance de Marvin, gestionnaire de l'ia et des moteurs
	 */
	protected	Marvin 				ia;
	/**
	 * le GoalFactory
	 */
	protected	GoalFactory			gf;
	
	/**
	 * Type d'ordre pour la marche arrière
	 */
	public enum OrderType {
		/**
		 * autorise l'action
		 */
		ALLOWED,
		/**
		 * Oblige l'action
		 */
		MANDATORY,
		/**
		 * Interdit l'action
		 */
		FORBIDEN;
	}
	
	/**
	 * @param gf le GoalFactory
	 * @param ia instance de Marvin, gestionnaire de l'ia et des moteurs
	 */
	protected Goal(GoalFactory gf, Marvin ia) {
		this.ia				= ia;
		this.gf				= gf;
	}
	
	/**
	 * @return un GoalType correspondant au nom de l'objectif
	 */
	public abstract GoalType getName();

	/**
	 * @return vrai si les précondition sont vérifié, faux sinon
	 */
	@SuppressWarnings("static-method")
	protected boolean checkPreConditions(){
		return true;
	}
	
	/**
	 * Méthode encapsulant le fonctionnement de l'objectif, test les préconditions et le lance notament.
	 */
	public void startWrapper(){
		if(this.checkPreConditions()){
			Main.printf("EXECUTE GOAL : " + getName());
			this.start();
		}
		else{
			Main.printf("FAIL EXECUTE GOAL : " + getName());
		}
	}
	
	/**
	 * Démarre l'objectif en assumant que ses préconditions sont vérifiées
	 */
	protected abstract void start();
}
