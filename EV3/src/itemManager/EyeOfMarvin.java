package itemManager;

import java.util.ArrayList;
import java.util.List;

import aiPlanner.Main;
import interfaces.ItemGiver;
import interfaces.ModeListener;
import interfaces.PositionGiver;
import interfaces.ServerListener;
import interfaces.SignalListener;
import shared.Item;
import shared.ItemType;
import shared.Mode;
import shared.Point;
import shared.SignalType;
import shared.TimedPoint;

public class EyeOfMarvin implements ServerListener, ItemGiver {
	private PositionGiver 	positionGiver;
	private volatile 		List<Item> masterList;
	private final int 		maxDistanceBias = 5; // imprécision du serveur, en cm
	private SignalListener 	evtManager;
	
	public EyeOfMarvin(PositionGiver pg, SignalListener evtManager) {
		this.positionGiver	= pg;
		this.evtManager 	= evtManager;
		masterList 			= new ArrayList<Item>();
		masterList.add(new Item(Main.X_INITIAL, Main.Y_INITIAL, Main.TIMER.getElapsedMs(), ItemType.ME));
		Main.printf("[EYE OF MARVIN]         : Initialized");
	}
	
	//on check les element de la master liste qui n'ont pas bougé avec un nouveau timeout
	private void updateMasterList(TimedPoint tp){
		boolean isupdated = false;
		for(Item item : masterList){
			if(item.getDistance(tp) < maxDistanceBias && !isupdated){
				item.update(tp.x(), tp.y(), tp.getReferenceTime());
				isupdated = true;
			}
		}
		if(!isupdated){
			masterList.add(new Item(tp.x(), tp.y(), tp.getReferenceTime(), ItemType.UNDEFINED));
		}
	}
	
	// retourne l'item flagué comme me
	private Item findMe(){
		Item marvin = null;
		for(Item item : masterList){
			if(item.getType() == ItemType.ME){
				marvin = item;
			}
		}
		return marvin;
	}
	
	private void updateMe(){
		Point currentPosition = positionGiver.getPosition().toTimedPoint();
		Item marvin = findMe();
		Item marvinNewPos = marvin;
		int currentDistance = 9999;
		// on choisit l'item qui est le plus proche de nous
		for(Item item : masterList){
			if(item.getDistance(currentPosition) < currentDistance){
				marvinNewPos = item;
				currentDistance = item.getDistance(currentPosition);
			}
		}
		marvin.update(marvinNewPos.x(), marvinNewPos.y(), marvinNewPos.getReferenceTime());
		masterList.remove(marvinNewPos); // une fois qu'on a mid à jour marvin (l'original) alors on peut supprime son clone.
	}
	
	// on défini comme pallet les items qui sont la et qui n'ont pas bougé depuis plus de 1 seconde
	// a refaire pour ne pas supprimer les ancien point des ennemy et me
	private void updatePalletMasterList(){
		for(Item item : masterList){
			// tout les item qui n'ont pas bougé et qui ont juste été mid à jour
			if(item.getMovedDistance() < maxDistanceBias && item.getType() == ItemType.UNDEFINED){
				item.setType(ItemType.PALLET);
			}			
		}
	}
	
	private void cleanMasterList(int timeout){
		List<Item> deleteList = new ArrayList<Item>();
		for(Item item : masterList){
			// tout les item qui n'ont pas bougé et qui ont juste été mid à jour
			if(item.getMovedDistance() < maxDistanceBias && item.getType() == ItemType.UNDEFINED){
				item.setType(ItemType.PALLET);
			}			
			if(item.getReferenceTime() < timeout && item.getType() != ItemType.ME){
				deleteList.add(item);
			}
		}
		masterList.removeAll(deleteList);
		deleteList.clear();
	}

	public void receiveRawPoints(List<Item> PointsList) {
		int timeout = 0;
		// pour tout les timedPoint présent dans la liste recue on verifie la masterliste et on met a jour leur timestamp
		for(TimedPoint tp : PointsList){
			updateMasterList(tp);
			timeout = tp.getReferenceTime();
		}
		printList(masterList);
		/*updateMe();
		updatePalletMasterList();
		cleanMasterList(timeout);*/
	}

	public static void printList(List<Item> list){
		for(Item i : list){
			Main.printf("[EYE OF MARVIN]         : " + i.toString());
		}
		Main.printf("------------------------------------------------------");
	}
	
	public Item getNearestPallet() {
		Point currentPosition = positionGiver.getPosition().toTimedPoint();
		int currentDistance = 9999;
		Item nearestPallet = null;
		// on choisit l'item qui est le plus proche de nous
		for(Item item : masterList){
			if(item.getDistance(currentPosition) < currentDistance && item.getType() == ItemType.PALLET){
				nearestPallet = item;
				currentDistance = item.getDistance(currentPosition);
			}
		}
		return nearestPallet;
	}

	public List<Item> getItemsList() {
		return masterList;
	}
}
