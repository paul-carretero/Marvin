package area;

import aiPlanner.Main;
import lejos.robotics.navigation.Pose;
import shared.Color;

/**
 * Regroupe les différentes informations sur les "Area" du terrain (définies en annexes)
 * Permet en autre de vérifier le cohérence des données de navigation
 */
public abstract class Area {

	/**
	 * ID de l'area
	 */
	protected final int 		id;
	
	/**
	 * Angle par rapport à une ligne de couleur en dessous duquel on ne peut pas considérer être fiable pour un changement d'Area
	 */
	protected final static int 	AMBIGUOUS_ANGLE	= 10;
	
	/**
	 * Marge d'erreur authorisé pour le calcul de cohérence, en mm
	 */
	protected static final int 	MARGE_ERREUR	= 30;
	
	/**
	 * @param id ID de l'Area
	 */
	public Area(final int id){
		this.id = id;
	}
	
	/**
	 * @param p la position actuelle du robot.
	 * @return l'area associé a cette position.
	 */
	public static Area getAreaWithPosition(final Pose p){
		if(p.getY() > (Main.Y_TOP_WHITE)){
			return Main.getArea(0);
		}
		else if(p.getY() < (Main.Y_BOTTOM_WHITE)){
			return Main.getArea(14);
		}
		else if(p.getY() < (Main.Y_GREEN_LINE) && p.getY() > (Main.Y_BOTTOM_WHITE)){
			if(p.getX() < (Main.X_YELLOW_LINE)){
				return Main.getArea(10);
			}
			else if(p.getX() > (Main.X_YELLOW_LINE) && p.getX() < (Main.X_BLACK_LINE)){
				return Main.getArea(11);
			}
			else if(p.getX() > (Main.X_BLACK_LINE) && p.getX() < (Main.X_RED_LINE)){
				return Main.getArea(12);
			}
			else if(p.getX() > (Main.X_RED_LINE)){
				return Main.getArea(13);
			}
		}
		else if(p.getY() < (Main.Y_BLACK_LINE) && p.getY() > (Main.Y_GREEN_LINE)){
			if(p.getX() < (Main.X_YELLOW_LINE)){
				return Main.getArea(8);
			}
			else if(p.getX() > (Main.X_YELLOW_LINE) && p.getX() < (Main.X_BLACK_LINE)){
				return Main.getArea(6);
			}
			else if(p.getX() > (Main.X_BLACK_LINE) && p.getX() < (Main.X_RED_LINE)){
				return Main.getArea(6);
			}
			else if(p.getX() > (Main.X_RED_LINE)){
				return Main.getArea(9);
			}
		}
		else if(p.getY() < (Main.Y_BLUE_LINE) && p.getY() > (Main.Y_BLACK_LINE)){
			if(p.getX() < (Main.X_YELLOW_LINE)){
				return Main.getArea(5);
			}
			else if(p.getX() > (Main.X_YELLOW_LINE) && p.getX() < (Main.X_BLACK_LINE)){
				return Main.getArea(6);
			}
			else if(p.getX() > (Main.X_BLACK_LINE) && p.getX() < (Main.X_RED_LINE)){
				return Main.getArea(6);
			}
			else if(p.getX() > (Main.X_RED_LINE)){
				return Main.getArea(7);
			}
		}
		else if(p.getY() < (Main.Y_TOP_WHITE) && p.getY() > (Main.Y_BLUE_LINE)){
			if(p.getX() < (Main.X_YELLOW_LINE)){
				return Main.getArea(1);
			}
			else if(p.getX() > (Main.X_YELLOW_LINE) && p.getX() < (Main.X_BLACK_LINE)){
				return Main.getArea(2);
			}
			else if(p.getX() > (Main.X_BLACK_LINE) && p.getX() < (Main.X_RED_LINE)){
				return Main.getArea(3);
			}
			else if(p.getX() > (Main.X_RED_LINE)){
				return Main.getArea(4);
			}
		}
		return Main.getArea(15);
	}
	
	@Override
	public String toString(){
		return "A"+this.id;
	}
	
	/**
	 * @return l'ID de l'area
	 */
	public int getId(){
		return this.id;
	}
	
	/**
	 * @param p une position du robot
	 * @return vrai si il est possible que la pose soit comprise dans cette area, faux sinon
	 */
	public abstract boolean getConsistency(Pose p);
	
	/**
	 * @return un tableau contenant les borne minimal et maximal de l'area
	 */
	public abstract float[] getBorder();
	
	/**
	 * @param currentColor la couleur que l'on vient de détecter
	 * @param p la pose du robot
	 * @return l'area associée ce changement de couleur
	 */
	public abstract Area colorChange(Color currentColor, Pose p);
	
	/**
	 * Vérifie si l'angle est succeptible d'entrainer un doute lorsque l'on rencontre une ligne Verticale
	 * @param p la pose du robot
	 * @return vrai si l'angle ne perttra pas de définir avec précision la nouvelle area en fonction d'une couleur, faux sinon
	 */
	public static boolean checkAmbiguousAngleVertical(final Pose p){
		return Math.abs(p.getHeading()) > (90 + AMBIGUOUS_ANGLE) || 
				Math.abs(p.getHeading()) < (90 - AMBIGUOUS_ANGLE);
	}
	
	/**
	 * Vérifie si l'angle est succeptible d'entrainer un doute lorsque l'on rencontre une ligne horizontale
	 * @param p la pose du robot
	 * @return vrai si l'angle ne perttra pas de définir avec précision la nouvelle area en fonction d'une couleur, faux sinon
	 */
	public static boolean checkAmbiguousAngleHorizontal(final Pose p){
		return Math.abs(p.getHeading()) > (0 + AMBIGUOUS_ANGLE) || 
				Math.abs(p.getHeading()) < (180 - AMBIGUOUS_ANGLE);
	}
}
