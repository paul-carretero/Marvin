package itemManager;

import java.util.ArrayList;
import java.util.List;

import aiPlanner.Main;
import interfaces.ServerListener;
import lejos.utility.Delay;
import shared.Item;
import shared.ItemType;

public class FakeServer extends Thread{
	
	private List<Item> lastPointsReceived;
	private int lastReceivedTimer = 0;
	private volatile boolean stop = false;
	private ServerListener eom;

	@Override
	public void run() {
		Main.printf("[FAKE SERVER]           : Started");
		while(! isInterrupted() && !stop){
			String rawData = "0;1000;1000\n1;700;1300\n2;300;1000\n";
			lastReceivedTimer = Main.TIMER.getElapsedMs();
			String[] items = rawData.split("\n");
			lastPointsReceived = new ArrayList<Item>();
			for (int i = 0; i < items.length; i++) 
	        {
				String[] coord = items[i].split(";");
				if(coord.length == 3){
		        	int x = Integer.parseInt(coord[1]);
		        	int y = Integer.parseInt(coord[2]);
		        	lastPointsReceived.add(new Item(x, y, lastReceivedTimer, ItemType.UNDEFINED));		        	
				}
	        }
			eom.receiveRawPoints(lastReceivedTimer, lastPointsReceived);
			Delay.msDelay(300);
		}
		Main.printf("[FAKE SERVER]           : Finished");
	}
	
	@Override
	public void interrupt(){
		stop = true;
	}
	
	public FakeServer(ServerListener sl){
		this.eom = sl;
		Main.printf("[FAKE SERVER]           : Initialized");
	}
}
      