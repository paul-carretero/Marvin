package area;

import lejos.robotics.navigation.Pose;
import shared.Color;

public class DefaultArea extends Area {

	public DefaultArea(int id) {
		super(id);
	}

	@Override
	public int getConsistency(Pose p) {
		return -1;
	}

	@Override
	public Area colorChange(Color color, Pose p) {
		return this;
	}

}
