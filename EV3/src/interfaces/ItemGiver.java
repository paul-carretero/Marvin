package interfaces;

import java.util.Hashtable;

import shared.Item;
import shared.Point;

public interface ItemGiver {
	public Item getNearestPallet();
	public Hashtable<Point,Item> getmasterTable();
}
