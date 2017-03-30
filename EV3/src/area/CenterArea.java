package area;

import aiPlanner.Main;
import lejos.robotics.navigation.Pose;
import shared.Color;

public class CenterArea extends Area {

	public CenterArea(int id) {
		super(id);
	}

	@Override
	public boolean getConsistency(Pose p) {
		return (
				p.getX() < (Main.X_RED_LINE + MARGE_ERREUR) && 
				p.getX() > (Main.X_YELLOW_LINE - MARGE_ERREUR) &&
				p.getY() > (Main.Y_GREEN_LINE - MARGE_ERREUR) &&
				p.getY() < (Main.Y_BLUE_LINE + MARGE_ERREUR)
		);
	}

	@Override
	public Area colorChange(Color color, Pose p) {
		switch (color) {
		case GREY:
			return this;
		case BLACK:
			// ici on soustrait bien la marge d'erreur au lieu de la rajout�, afin d'�viter les cas limites
			if(p.getX() < (Main.X_RED_LINE - MARGE_ERREUR) && p.getX() > (Main.X_YELLOW_LINE + MARGE_ERREUR) && 
				p.getY() < (Main.Y_BLUE_LINE - MARGE_ERREUR) && p.getY() > (Main.Y_GREEN_LINE + MARGE_ERREUR)){
				return this;
			}
			else{
				return Main.getArea(15);
			}
		case RED:
			if( checkAmbiguousAngleVertical(p) ){
				if(p.getY() > (Main.Y_GREEN_LINE) && p.getY() < (Main.Y_BLACK_LINE)){
					return Main.getArea(9);
				}
				if(p.getY() > (Main.Y_BLACK_LINE) && p.getY() < (Main.Y_BLUE_LINE)){
					return Main.getArea(7);
				}
			}
		case GREEN:
			if( checkAmbiguousAngleHorizontal(p) ){
				if(p.getX() < (Main.X_RED_LINE) && p.getX() > (Main.X_BLACK_LINE)){
					return Main.getArea(12);
				}
				if(p.getX() < (Main.X_BLACK_LINE) && p.getX() > (Main.X_YELLOW_LINE)){
					return Main.getArea(11);
				}
			}
		case BLUE:
			if( checkAmbiguousAngleHorizontal(p) ){
				if(p.getX() < (Main.X_RED_LINE) && p.getX() > (Main.X_BLACK_LINE)){
					return Main.getArea(3);
				}
				if(p.getX() < (Main.X_BLACK_LINE) && p.getX() > (Main.X_YELLOW_LINE)){
					return Main.getArea(2);
				}
			}
		case YELLOW:
			if( checkAmbiguousAngleVertical(p) ){
				if(p.getY() > (Main.Y_GREEN_LINE) && p.getY() < (Main.Y_BLACK_LINE)){
					return Main.getArea(8);
				}
				if(p.getY() > (Main.Y_BLACK_LINE) && p.getY() < (Main.Y_BLUE_LINE)){
					return Main.getArea(5);
				}
			}
		default:
			return Main.getArea(15);
		}
		
	}
	
	@Override
	public float[] getBorder() {
		return new float[]{
			500,
			1500,
			900,
			2100,
		};
	}

}
