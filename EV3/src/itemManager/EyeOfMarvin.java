package itemManager;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import aiPlanner.Main;
import interfaces.ItemGiver;
import interfaces.PoseGiver;
import interfaces.ServerListener;
import lejos.robotics.navigation.Pose;
import shared.Item;
import shared.ItemType;
import shared.IntPoint;

import java.util.HashMap;
import java.util.Iterator;

public class EyeOfMarvin implements ServerListener, ItemGiver {
	
	private PoseGiver 				poseGiver 		= null;
	private Map<IntPoint,Item> 		masterMap 		= null;
	private	static final int		MAP_PRECISION 	= 50; // on arrondi au multiple de MAP_PRECISION
	private static final int		OUT_OF_RANGE	= 9999;
	
	public EyeOfMarvin(PoseGiver pg) {
		
		this.poseGiver	= pg;
		this.masterMap 	= new HashMap<IntPoint,Item>();
		
		putInHashMap(new Item(Main.X_INITIAL, Main.Y_INITIAL, Main.TIMER.getElapsedMs(), ItemType.ME));
		
		Main.printf("[EYE OF MARVIN]         : Initialized");
	}
	
	public static void averagize(Item i){
		i.silentUpdate((i.x()/MAP_PRECISION)*MAP_PRECISION,(i.y()/MAP_PRECISION)*MAP_PRECISION);
	}
	
	public static void averagize(IntPoint i){
		i.update((i.x()/MAP_PRECISION)*MAP_PRECISION,(i.y()/MAP_PRECISION)*MAP_PRECISION);
	}
	
	private void putInHashMap(Item i){
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
	
	private void defineMe(){
		Pose p = poseGiver.getPosition();
		Main.poseRealToSensor(p);
		IntPoint myPosition = new IntPoint(p);
		averagize(myPosition);
		
		if(masterMap.containsKey(myPosition)){
			masterMap.get(myPosition).setType(ItemType.ME);
		}
		else{
			int distance = OUT_OF_RANGE;
			IntPoint posOnMap = null;
			for (Entry<IntPoint, Item> entry : masterMap.entrySet()){
				if(entry.getValue().getDistance(myPosition) < distance){
					posOnMap = entry.getValue();
					distance = entry.getValue().getDistance(myPosition);
				}
			}
			
			if(posOnMap != null && masterMap.get(posOnMap) != null){
				masterMap.get(posOnMap).setType(ItemType.ME);
				masterMap.get(posOnMap).setProbability(100-(distance/2)); // 100 = 0cm, 0 = > 20cm
			}
		}
	}
	
	private void cleanHashMap(int timeout) {
		for(Iterator<Map.Entry<IntPoint, Item>> it = masterMap.entrySet().iterator(); it.hasNext(); ) {
			Entry<IntPoint, Item> entry = it.next();
			if(entry.getValue().getReferenceTime() < timeout){
				it.remove();
			}
			else if(entry.getValue().getLifeTime() > 1500){
				entry.getValue().setType(ItemType.PALLET);
			}
		}
	}
	
	synchronized public void receiveRawPoints(int timeout, List<Item> PointsList) {
			for(Item tp : PointsList){
				putInHashMap(tp);
			}
			cleanHashMap(timeout);
			defineMe();
		//printHashMap(masterMap);
	}
	
	synchronized public Item getNearestPallet() {
		IntPoint myIntPose = new IntPoint(poseGiver.getPosition().getLocation());
		int distance = OUT_OF_RANGE;
		IntPoint res = null;
		for (Entry<IntPoint, Item> entry : masterMap.entrySet()){
			if(myIntPose != null){
				if((entry.getValue().getDistance(myIntPose) < distance) && entry.getValue().getType() == ItemType.PALLET){
					res = entry.getKey();
					distance = entry.getValue().getDistance(myIntPose);
				}
			}
		}
		return masterMap.get(res);
	}
	
	synchronized public Item getNearestItem(IntPoint searchPoint) {
		int distance = OUT_OF_RANGE;
		IntPoint res = null;
		for (Entry<IntPoint, Item> entry : masterMap.entrySet()){
			if(searchPoint != null){
				if((entry.getValue().getDistance(searchPoint) < distance) && entry.getValue().getType() != ItemType.ME){
					res = entry.getKey();
					distance = entry.getValue().getDistance(searchPoint);
				}
			}
		}
		return masterMap.get(res);
	}
	
	synchronized public Item getMarvinPosition(){
		for (Entry<IntPoint, Item> entry : masterMap.entrySet())
		{
			if(entry.getValue().getType() == ItemType.ME){
				return entry.getValue();
			}
		}
		return null;
	}
	
	synchronized public int count(ItemType type){
		int res = 0;
		for (Item item : masterMap.values()){
			if(item.getType() == type){
				res++;
			}
		}
		return res;
	}
	
	/*
	 * @return null si plusieurs ou non trouvé
	 */
	synchronized public Item getPossibleEnnemy(){
		if( count(ItemType.UNDEFINED) == 1 ){
			for (Item item : masterMap.values()){
				if(item.getType() == ItemType.UNDEFINED){
					return item;
				}
			}
		}
		return null;
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
	
	synchronized public boolean checkPallet(IntPoint position){
		if(masterMap.containsKey(position)){
			return masterMap.get(position).getType() == ItemType.PALLET;
		}
		return false;
	}
}
