package positionManager;

import aiPlanner.Main;
import interfaces.ItemGiver;
import interfaces.PoseGiver;
import lejos.robotics.geometry.Point;
import lejos.robotics.navigation.Pose;
import shared.Item;

/**
 * Classe permettant de calculer l'angle du robot (suivant les convention utilisées par LeJos)
 * Se base sur les données de la carte (générer par la caméra et le serveur) au début et à la fin d'un déplacement pour calculer les angles.
 */
public class DirectionCalculator {
	
	/**
	 * Point de départ pour un déplacement en ligne droite en avant
	 */
	private Point 			 startPoint;
	
	/**
	 * gestionnaire d'Item (gère la carte des Item)
	 */
	private final ItemGiver  eom;
	
	/**
	 * Le gestionnaire de position fournissant une interface pour la mise à jour de la Pose actuelle.
	 */
	private final PoseGiver	 pg;
	
	/**
	 * Symbolise le fait que l'on ai pas pu calculer l'angle
	 */
	private static final int NO_ANGLE_FOUND	= 9999;
	

	/**
	 * Créer une nouvelle instance du calculateur de Direction.
	 * @param pg Le gestionnaire de position fournissant une interface pour la mise à jour de la Pose actuelle.
	 * @param eom EyeOfMarvin un ItemGiver fournissant les données reçue par le serveur
	 */
	public DirectionCalculator(PoseGiver pg, ItemGiver eom){
		this.startPoint	= null;
		this.pg			= pg;
		this.eom 		= eom;
		
		Main.printf("[DIRECTION CALCULATOR]  : Initialized");
	}
	
	/**
	 * @param p un Point de la bibliothèque LeJos
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
	 * Met à jour l'angle de la pose passé en paramètre avec l'angle calculé durant la trajectoire.
	 * La mise à jour est plus importante si la distance parcouru est grande (plus fiable)
	 * @param p une pose obtenue par le calculateur de position.
	 * @return vrai si on a effectué une mise à jour sur l'angle
	 */
	private boolean updateAngle(final Pose p){
		Item me = this.eom.getMarvinPosition();
		if(p != null && me != null){
			float calcAngle = getAngle(me.toLejosPoint());
			if(calcAngle != NO_ANGLE_FOUND){
				p.setHeading((float) ((p.getHeading() * 0.5) + (calcAngle * 0.5)));
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Utilisé par l'ia notament afin d'informer le calculateur de direction que le robot à terminé une ligne droite en avant.
	 * calcul la direction durant ce trajet et met à jour le gestionnaire de position.
	 */
	public void reset(){
		
		// on tente de mettre à jour l'angle si possible avant de reset
		
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
	 * Utilisé par l'ia notament afin d'informer le calculateur de direction que le robot à commencé une ligne droite en avant.
	 * Enregistre la position initiale à ce moment.
	 */
	public void startLine(){
		if(this.eom != null){
			Item eomStart = this.eom.getMarvinPosition();
			if(eomStart != null){
				this.startPoint	= eomStart.toLejosPoint();
			}
			else{
				Main.printf("[ERREUR]                : Impossible d'initialiser le DirectionCalculator (getMarvinPosition null)");
			}
		}
		else{
			Main.printf("[ERREUR]                : Impossible d'initialiser le DirectionCalculator (eom null)");
		}
	}

}
