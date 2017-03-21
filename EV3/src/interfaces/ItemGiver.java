package interfaces;

import java.util.Map;

import shared.Item;
import shared.IntPoint;

public interface ItemGiver {
	public Item getNearestPallet();
	public Map<IntPoint, Item> getMasterMap();
}
