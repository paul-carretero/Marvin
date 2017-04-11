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
import java.util.LinkedList;

public class EyeOfMarvin implements ServerListener, ItemGiver {
	
	private PoseGiver 				poseGiver;
	private Map<IntPoint,Item> 		masterMap;
	
	private	static final int		MAP_PRECISION 	= 40; // on arrondi au multiple de MAP_PRECISION
	private static final int		OUT_OF_RANGE	= 9999;
	private static final int		MIN_LIFE		= 1500;
	private static final int		MIN_PALET_MARGE	= 100;
	
	public EyeOfMarvin(PoseGiver pg) {
		
		this.poseGiver	= pg;
		this.masterMap 	= new HashMap<IntPoint,Item>();
		
		Main.printf("[EYE OF MARVIN]         : Initialized");
	}
	
	public void calibrateSensor(){
		int sensorMarge = 200;
		int xTotal = 0;
		int yTotal = 0;
		int total = 0;
		
		for(IntPoint pFix : Main.INITIAL_PALETS){
			IntPoint pSensor = getNearestItem(pFix);
			if(pSensor != null && pFix.getDistance(pSensor) < sensorMarge){
				xTotal += pFix.x() - pSensor.x();
				yTotal += pFix.y() - pSensor.y();
				total++;
			}
			else{
				Main.printf("[EYE OF MARVIN]         : Error, no item in the expected position : " + pFix);
			}
		}
		
		if(total > 0){
			Server.defineOffset(xTotal/total, yTotal/total);
		}
		else{
			Main.printf("[EYE OF MARVIN]         : Le serveur n'est probablement pas lanc�...");
		}
		
	}
	
	private static void averagize(Item i){
		i.silentUpdate((i.x()/MAP_PRECISION)*MAP_PRECISION,(i.y()/MAP_PRECISION)*MAP_PRECISION);
	}
	
	private static void averagize(IntPoint i){
		i.update((i.x()/MAP_PRECISION)*MAP_PRECISION,(i.y()/MAP_PRECISION)*MAP_PRECISION);
	}
	
	private void putInHashMap(Item i){
		averagize(i);
		IntPoint key = new IntPoint(i.x(), i.y());
		if(this.masterMap.containsKey(key)){
			this.masterMap.get(key).updateTimeStamp(i.getReferenceTime());
			if(i.getType() != ItemType.UNDEFINED){
				this.masterMap.get(key).setType(i.getType());
			}
		}
		else{
			this.masterMap.put(key,i);
		}
	}
	
	private void defineMe(){
		Pose p = this.poseGiver.getPosition();
		Main.poseRealToSensor(p);
		IntPoint myPosition = new IntPoint(p);
		averagize(myPosition);
		
		if(this.masterMap.containsKey(myPosition)){
			this.masterMap.get(myPosition).setType(ItemType.ME);
		}
		else{
			int distance = OUT_OF_RANGE;
			IntPoint posOnMap = null;
			for (Entry<IntPoint, Item> entry : this.masterMap.entrySet()){
				if(entry.getValue().getDistance(myPosition) < distance){
					posOnMap = entry.getValue();
					distance = entry.getValue().getDistance(myPosition);
				}
			}
			
			if(posOnMap != null && this.masterMap.get(posOnMap) != null){
				this.masterMap.get(posOnMap).setType(ItemType.ME);
			}
		}
	}
	
	private void cleanHashMap(int timeout) {
		for(Iterator<Map.Entry<IntPoint, Item>> it = this.masterMap.entrySet().iterator(); it.hasNext(); ) {
			Entry<IntPoint, Item> entry = it.next();
			if(entry.getValue().getReferenceTime() < timeout){
				it.remove();
			}
			else if(entry.getValue().getLifeTime() > MIN_LIFE){
				entry.getValue().setType(ItemType.PALET);
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
	
	synchronized public Item getNearestpalet() {
		IntPoint myIntPose = new IntPoint(this.poseGiver.getPosition().getLocation());
		int distance = OUT_OF_RANGE;
		IntPoint res = null;
		for (Entry<IntPoint, Item> entry : this.masterMap.entrySet()){
			if((entry.getValue().getDistance(myIntPose) < distance) && entry.getValue().getType() == ItemType.PALET
					&& entry.getValue().y() > Main.Y_BOTTOM_WHITE + MIN_PALET_MARGE && entry.getValue().y() < (Main.Y_TOP_WHITE - MIN_PALET_MARGE)){
				res = entry.getKey();
				distance = entry.getValue().getDistance(myIntPose);
			}
		}
		if(res != null){
			return this.masterMap.get(res);
		}
		return null;
	}
	
	synchronized public Item getNearestItem(IntPoint searchPoint) {
		int distance = OUT_OF_RANGE;
		IntPoint res = null;
		for (Entry<IntPoint, Item> entry : this.masterMap.entrySet()){
			if(searchPoint != null){
				if((entry.getValue().getDistance(searchPoint) < distance)){
					res = entry.getKey();
					distance = entry.getValue().getDistance(searchPoint);
				}
			}
		}
		return this.masterMap.get(res);
	}
	
	synchronized public Item getMarvinPosition(){
		for (Entry<IntPoint, Item> entry : this.masterMap.entrySet())
		{
			if(entry.getValue().getType() == ItemType.ME){
				return entry.getValue();
			}
		}
		return null;
	}
	
	private int count(ItemType type){
		int res = 0;
		for (Item item : this.masterMap.values()){
			if(item.getType() == type){
				res++;
			}
		}
		return res;
	}
	
	/*
	 * @return null si plusieurs ou non trouv�
	 */
	synchronized public Item getPossibleEnnemy(){
		if( count(ItemType.UNDEFINED) == 1 ){
			for (Item item : this.masterMap.values()){
				if(item.getType() == ItemType.UNDEFINED){
					return item;
				}
			}
		}
		return null;
	}
	
	synchronized public List<IntPoint> searchPosition(int range){
		List<IntPoint> resList = new LinkedList<IntPoint>();
		for (IntPoint key : this.masterMap.keySet()){
			if(key.x() < range || key.y() < range || key.x() > (2000 - range) || key.y() > (3000 - range)){
				if(this.masterMap.get(key).getType() != ItemType.PALET){
					resList.add(key);
				}
			}
		}
		return resList;
	}
	
	synchronized public List<IntPoint> searchPosition(IntPoint start, int minRange, int maxRange){
		List<IntPoint> resList = new LinkedList<IntPoint>();
		for (IntPoint key : this.masterMap.keySet()){
			if(start.getDistance(key) < maxRange && start.getDistance(key) > minRange && this.masterMap.get(key).getType() != ItemType.PALET ){
				resList.add(key);
			}
		}
		return resList;
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
	
	synchronized public boolean checkpalet(IntPoint position){
		if(this.masterMap.containsKey(position)){
			return this.masterMap.get(position).getType() == ItemType.PALET;
		}
		return false;
	}
}
