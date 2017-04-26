package positionManager;

import aiPlanner.Main;
import interfaces.PoseGiver;
import lejos.robotics.geometry.Point;
import lejos.robotics.navigation.Pose;

/**
 * Classe permettant de calculer l'angle du robot (suivant les convention utilis�es par LeJos)
 * Se base sur les donn�es de la carte (g�n�rer par la cam�ra et le serveur) au d�but et � la fin d'un d�placement pour calculer les angles.
 */
public class DirectionCalculator {
	
	/**
	 * Point de d�part pour un d�placement en ligne droite en avant
	 */
	private Point 			 startPoint;
	
	/**
	 * Le gestionnaire de position fournissant une interface pour la mise � jour de la Pose actuelle.
	 */
	private final PoseGiver	 pg;
	
	/**
	 * Symbolise le fait que l'on ai pas pu calculer l'angle
	 */
	private static final int NO_ANGLE_FOUND	= 9999;
	

	/**
	 * Cr�er une nouvelle instance du calculateur de Direction.
	 * @param pg Le gestionnaire de position fournissant une interface pour la mise � jour de la Pose actuelle.
	 */
	public DirectionCalculator(PoseGiver pg){
		this.startPoint	= null;
		this.pg			= pg;
		
		Main.printf("[DIRECTION CALCULATOR]  : Initialized");
	}
	
	/**
	 * @param p un Point de la biblioth�que LeJos
	 * @return l'angle entre le point initial et le point p.
	 */
	private float getAngle(Point p){
		if(p != null && p.distance(this.startPoint) > Main.FIABLE_DIST){
				Main.printf("[DIRECTION CALCULATOR]  : angle calcule = " + this.startPoint.angleTo(p));
				return this.startPoint.angleTo(p);
		}
		return NO_ANGLE_FOUND;
	}
	
	/**
	 * Met � jour l'angle de la pose pass� en param�tre avec l'angle calcul� durant la trajectoire.
	 * La mise � jour est plus importante si la distance parcouru est grande (plus fiable)
	 * @param p une pose obtenue par le calculateur de position.
	 * @return vrai si on a effectu� une mise � jour sur l'angle
	 */
	private boolean updateAngle(final Pose p){
		if(p != null){
			float calcAngle = getAngle(p.getLocation());
			float distance = p.distanceTo(this.startPoint);
			float newCoeff = 0.5f;
			if(distance > 2 * Main.FIABLE_DIST){
				newCoeff = 0.7f;
			}
			if(calcAngle != NO_ANGLE_FOUND){
				p.setHeading((p.getHeading() * (1f - newCoeff)) + (calcAngle * newCoeff));
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Utilis� par l'ia notament afin d'informer le calculateur de direction que le robot � termin� une ligne droite en avant.
	 * calcul la direction durant ce trajet et met � jour le gestionnaire de position.
	 */
	public void reset(){
		
		// on tente de mettre � jour l'angle si possible avant de reset
		
		if(this.startPoint != null){
			Pose myPose = this.pg.getPosition();
			if(updateAngle(myPose)){
				this.pg.setPose(myPose);
			}
			Main.printf("[DIRECTION CALCULATOR]  : nouveau angle pose = " + myPose);
		}
		
		// reset
		
		this.startPoint = null;
	}
	
	
	/**
	 * Utilis� par l'ia notament afin d'informer le calculateur de direction que le robot � commenc� une ligne droite en avant.
	 * Enregistre la position initiale � ce moment.
	 */
	public void startLine(){
		this.startPoint = this.pg.getPosition().getLocation();
	}

}
