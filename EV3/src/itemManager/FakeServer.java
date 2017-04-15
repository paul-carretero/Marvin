package itemManager;

import java.util.ArrayList;
import java.util.List;

import aiPlanner.Main;
import interfaces.ServerListener;
import lejos.utility.Delay;
import shared.Item;
import shared.ItemType;

@SuppressWarnings("javadoc")
public class FakeServer extends Thread{
	
	private List<Item> lastPointsReceived;
	private int lastReceivedTimer = 0;
	private volatile boolean stop = false;
	private ServerListener eom;

	@Override
	public void run() {
		Main.printf("[FAKE SERVER]           : Started");
		int currentY = 2700;
		while(! isInterrupted() && !this.stop){
			String rawData = "0;1000;2700\n1;1000;2100\n";
			this.lastReceivedTimer = Main.TIMER.getElapsedMs();
			String[] items = rawData.split("\n");
			this.lastPointsReceived = new ArrayList<Item>();
			for (int i = 0; i < items.length; i++) 
	        {
				String[] coord = items[i].split(";");
				if(coord.length == 3){
		        	int x = Integer.parseInt(coord[1]);
		        	int y = Integer.parseInt(coord[2]);
		        	this.lastPointsReceived.add(new Item(x, y, this.lastReceivedTimer, ItemType.UNDEFINED));		        	
				}
	        }
			this.eom.receiveRawPoints(this.lastReceivedTimer, this.lastPointsReceived);
			Delay.msDelay(300);
			currentY = currentY-25;
		}
		Main.printf("[FAKE SERVER]           : Finished");
	}
	
	@Override
	public void interrupt(){
		this.stop = true;
	}
	
	public FakeServer(ServerListener sl){
		this.eom = sl;
		Main.printf("[FAKE SERVER]           : Initialized");
	}
}
      