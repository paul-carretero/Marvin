package interfaces;

import shared.Item;

import java.util.List;

import shared.Color;
import shared.IntPoint;

/**
 * Interface proposant la gestion des item (recherche) en se basant sur les données de la caméra
 */
public interface ItemGiver {
	public Item getNearestpalet();
	public Item getMarvinPosition();
	public Item getPossibleEnnemy();
	public Item getNearestItem(IntPoint searchPoint);
	public boolean checkpalet(IntPoint position);
	public List<IntPoint> searchPosition(Color color);
	public List<IntPoint> searchPosition(IntPoint start, int minRange, int maxRange);
}
