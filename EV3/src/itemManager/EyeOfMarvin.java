package itemManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import aiPlanner.Main;
import interfaces.ItemGiver;
import interfaces.PoseGiver;
import interfaces.ServerListener;
import interfaces.SignalListener;
import shared.Item;
import shared.ItemType;
import shared.IntPoint;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

public class EyeOfMarvin implements ServerListener, ItemGiver {
	private PoseGiver 				poseGiver 		= null;
	private Map<IntPoint,Item> 		masterMap 	= null;
	private SignalListener 			evtManager 		= null;
	private	final	static	int		MAP_PRECISION 	= 50; // on arrondi au multiple de MAP_PRECISION
	
	public EyeOfMarvin(PoseGiver pg, SignalListener evtManager) {
		this.poseGiver	= pg;
		this.evtManager	= evtManager;
		this.masterMap 	= new ConcurrentHashMap<IntPoint,Item>();
		
		putInHashTable(new Item(Main.X_INITIAL, Main.Y_INITIAL, Main.TIMER.getElapsedMs(), ItemType.ME));
		
		Main.printf("[EYE OF MARVIN]         : Initialized");
	}
	
	public static void averagize(Item i){
		i.silentUpdate((i.x()/MAP_PRECISION)*MAP_PRECISION,(i.y()/MAP_PRECISION)*MAP_PRECISION);
	}
	
	private void putInHashTable(Item i){
		averagize(i);
		IntPoint key = new IntPoint(i.x(), i.y());
		if(masterMap.containsKey(key)){
			masterMap.get(key).updateTimeStamp(i.getReferenceTime());
			if(i.getType() != ItemType.UNDEFINED){
				masterMap.get(key).setType(i.getType());
			}
		}
		else{
			masterMap.put(key,i);
		}
	}
	
	public Item getMarvinPosition(){
		for (Entry<IntPoint, Item> entry : masterMap.entrySet())
		{
			if(entry.getValue().getType() == ItemType.ME){
				return entry.getValue();
			}
		}
		return null;
	}
	
	public void receiveRawPoints(int timeout, List<Item> PointsList) {
		for(Item tp : PointsList){
			putInHashTable(tp);
		}
		cleanHashMap(timeout);
	}
	
	private void cleanHashMap(int timeout) {
		for(Iterator<Map.Entry<IntPoint, Item>> it = masterMap.entrySet().iterator(); it.hasNext(); ) {
			Entry<IntPoint, Item> entry = it.next();
			if(entry.getValue().getReferenceTime() < timeout){
				it.remove();
			}
			else if(entry.getValue().getLifeTime() > 1000 && entry.getValue().getType() == ItemType.UNDEFINED){
				entry.getValue().setType(ItemType.PALLET);
			}
		}
	}
	
	public Item getNearestPallet() {
		//IntPoint currentPosition = poseGiver.getPosition().toTimedPoint();
		int currentDistance = 9999;
		return null;
	}

	public Map<IntPoint,Item> getMasterMap() {
		return masterMap;
	}
	
	public static void printList(List<Item> list){
		Main.printf("--------------------------------------");
		for(Item i : list){
			Main.printf("[EYE OF MARVIN]         : " + i.toString());
		}
		Main.printf("======================================");
	}

	public static void printHashMap(Map<IntPoint,Item> map){
		Main.printf("--------------------------------" + map.size() + "Elements");
		for(Item item : map.values()) {
			Main.printf("[EYE OF MARVIN]         : " + item.toString());
		}
		Main.printf("------------------------------------------------------");
	}
}
