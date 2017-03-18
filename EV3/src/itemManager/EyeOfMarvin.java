package itemManager;

import java.util.ArrayList;
import java.util.List;

import aiPlanner.Main;
import interfaces.ItemGiver;
import interfaces.PoseGiver;
import interfaces.ServerListener;
import interfaces.SignalListener;
import shared.Item;
import shared.ItemType;
import shared.Point;
import java.util.Enumeration;
import java.util.Hashtable;

public class EyeOfMarvin implements ServerListener, ItemGiver {
	private PoseGiver 		poseGiver;
	private volatile 		Hashtable<Point,Item> masterTable;
	private SignalListener 	evtManager;
	private final static int		MAP_PRECISION = 5; // on arrondi au multiple de MAP_PRECISION
	
	public EyeOfMarvin(PoseGiver pg, SignalListener evtManager) {
		this.poseGiver	= pg;
		this.evtManager 	= evtManager;
		masterTable 			= new Hashtable<Point,Item>();
		putInHashTable(new Item(Main.X_INITIAL, Main.Y_INITIAL, Main.TIMER.getElapsedMs(), ItemType.ME));
		Main.printf("[EYE OF MARVIN]         : Initialized");
	}
	
	public static void averagize(Item i){
		i.silentUpdate((i.x()/MAP_PRECISION)*MAP_PRECISION,(i.y()/MAP_PRECISION)*MAP_PRECISION);
	}
	
	private void putInHashTable(Item i){
		averagize(i);
		Point key = new Point(i.x(), i.y());
		if(masterTable.containsKey(key)){
			masterTable.get(key).updateTimeStamp(i.getReferenceTime());
			if(i.getType() != ItemType.UNDEFINED){
				masterTable.get(key).setType(i.getType());
			}
		}
		else{
			masterTable.put(key,i);
		}
	}
	
	public Item getMarvinPosition(){
		Enumeration<Item> items = masterTable.elements();
		while(items.hasMoreElements()){
			Item item = items.nextElement();
			if(item.getType() == ItemType.ME){
				return item;
			}
		}
		return null;
	}
	
	public void receiveRawPoints(int timeout, List<Item> PointsList) {
		// pour tout les timedPoint présent dans la liste recue on verifie la masterliste et on met a jour leur timestamp
		//printList(PointsList);
		for(Item tp : PointsList){
			putInHashTable(tp);
		}
		Main.printf("---->" + timeout + "<----");
		cleanHashTable(getObsoleteKey(timeout));
		printHashtable(masterTable);
	}
	
	private void cleanHashTable(List<Point> obsoleteKey) {
		for(Point p : obsoleteKey){
			masterTable.remove(p);
		}
	}

	// side effect : flag comme item les objects qui n'ont pas bougé de puis un bout de temps...
	private List<Point> getObsoleteKey(int timeout) {
		List<Point> toDelete = new ArrayList<Point>();
		Enumeration<Point> keys = masterTable.keys();
		
		while(keys.hasMoreElements()){
			Point key = keys.nextElement();
			Item i = masterTable.get(key);
			if(i.getReferenceTime() < timeout && i.getType() != ItemType.ME ){
				toDelete.add(key);
			}
			else if(i.getLifeTime() > 1000 && i.getType() == ItemType.UNDEFINED){
				i.setType(ItemType.PALLET);
			}
		}
		return toDelete;
	}
	
	public static void printList(List<Item> list){
		Main.printf("--------------------------------------");
		for(Item i : list){
			Main.printf("[EYE OF MARVIN]         : " + i.toString());
		}
		Main.printf("======================================");
	}

	public static void printHashtable(Hashtable<Point,Item> ht){
		Enumeration<Item> items = ht.elements();
		Main.printf("--------------------------------" + ht.size() + "Elements");
		while(items.hasMoreElements()){
			Item item = items.nextElement();
			Main.printf("[EYE OF MARVIN]         : " + item.toString());
		}
		Main.printf("------------------------------------------------------");
	}
	
	public Item getNearestPallet() {
		Point currentPosition = poseGiver.getPosition().toTimedPoint();
		int currentDistance = 9999;
		return null;
	}

	public Hashtable<Point,Item> getmasterTable() {
		return masterTable;
	}
}
