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
	public Area(int id){
		this.id = id;
	}
	
	public static Area getAreaWithPosition(Pose p){
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
	
	public int getId(){
		return this.id;
	}
	
	public abstract boolean getConsistency(Pose p);
	
	public abstract float[] getBorder();
	
	public abstract Area colorChange(Color currentColor, Pose p);
	
	public static boolean checkAmbiguousAngleVertical(Pose p){
		return Math.abs(p.getHeading()) > (90 + AMBIGUOUS_ANGLE) || 
				Math.abs(p.getHeading()) < (90 - AMBIGUOUS_ANGLE);
	}
	
	public static boolean checkAmbiguousAngleHorizontal(Pose p){
		return Math.abs(p.getHeading()) > (0 + AMBIGUOUS_ANGLE) || 
				Math.abs(p.getHeading()) < (180 - AMBIGUOUS_ANGLE);
	}
}
