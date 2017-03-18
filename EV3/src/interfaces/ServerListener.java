package interfaces;

import java.util.List;

import shared.Item;

public interface ServerListener {
	public void receiveRawPoints(int timer, List<Item> lastPointsReceived);
}
