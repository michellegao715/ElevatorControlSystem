import java.util.ArrayList;

public class ElevatorControlSystem {
	public static final int max_elevators = 16;
	private int numOfElevators = 0;
	private int numOfFloors = 0;
	//maintain a pickuplocations(saving eleID) when people from different floors ask for pickup 
	private ArrayList<Elevator> elevators;

	public ElevatorControlSystem(Integer numOfElevators, Integer numOfFloors) {
		this.numOfElevators = numOfElevators;
		this.numOfFloors = numOfFloors; 
		initializeElevators(numOfElevators);
	}
	
	public void initializeElevators(int numOfElevators){
		elevators = new ArrayList<Elevator>();
		for(int id =1;id<=numOfElevators;id++){
			elevators.add(new Elevator());
		}
	}

	/*Invoked when a client press the up/down button. The system finds an elevator and add the
	 * client's current floor to the elevator's destination floors list.*/
	public void pickup(int currentFloor, int direction){
		//find an elevator has smallest distance to currentFloor(same direction preferred)
		Elevator e = getElevator(currentFloor,direction);
		e.addDestination(currentFloor);
	}
	/*Find smallest distance(take direction into consideration) elevator to pick up a client: 4
	 * situations: same/reversed direction + 
	 * pickup floor is higher/lower than elevator's current floor (# --> client, * --> elevator) 
	 * 4 situations: 
	 *  # (up)    *(up)    #(up)       *(down)
	 *   
	 *	* (up)	  #(up)    *(down)     #(up)
	 *
	 * */ 
	public Elevator getElevator(int currentFloor, int direction){
		int distance = Integer.MAX_VALUE;
		int id = 0;
		Elevator res = null;
		for(Elevator e : elevators){
			int tempDistance = 0;
			//calculate distances for 4 different situations and pick the elevator which has smallest distance
			if(e.getDirection() == direction){
				if(e.getCurFloor() < currentFloor) tempDistance = currentFloor-e.getCurFloor();
				else tempDistance= ((topOfDestinationFloor(e.getGoalFloors())) - currentFloor) * 2 - (e.getCurFloor()-currentFloor);
			}
			else if (e.getDirection() != direction){
				tempDistance = (e.getCurFloor() - bottomOfDestinationFloors(e.getGoalFloors()))*2 - (e.getCurFloor()-currentFloor);
			}
			if(distance > tempDistance) {
				res = new Elevator(e.getID(),e.getCurFloor(),e.getGoalFloors());
				distance = tempDistance;
			}	
		}
		return res;
	}
	/* An update alters these numbers for one elevator.Invoked after pickup a people: remove currentFloor from pickupLocations and add destinationFloor to elevators(status).  TODO this is the simplest implementation which assumes a client press a destination floor button right after being picked up */  
	public void update(int eleID, int currentFloor, int destinationFloor){
		Elevator elevator = elevators.get(eleID);
		elevator.addDestination(destinationFloor);
		elevator.removeDestination(currentFloor);	
}
	/* Return all elevators' status.*/
	public ArrayList<Status> status(){
		ArrayList<Status> stats = new ArrayList<Status>();
		for(Elevator e : elevators){
			Status s = new Status();
			s.id = e.getID();
			s.currentFloor = e.getCurFloor();
			s.destinationFloors = e.getGoalFloors();
			stats.add(s);
		}
		return stats;
	}
	/*time-stepping the simulation by calling each elevator's step() function */
	public void step(){
		for(Elevator elevator:elevators){
			elevator.step();
		}
	}
}

class Status {
	int id;
	int currentFloor;
	ArrayList<Integer> destinationFloors;
}
/*Each Elevator has unique ID, which floor it is at currently and a list of destination floors.
 elevator has functions addDestination() and removeDestination() to update list of destination floors when picking up a client and arrives at
 destination floor. Direction() returns which way the elevator is going and step is the simulation of time-stepping.*/
class Elevator{
	private static int count = 0;
	private int id;
	private int curFloor;
	private int direction; //stop:0. up:1. down:-1. modified when an elevator get to a destination(step()), or pick up a client(pickup()) 
	private ArrayList<Integer> goalFloors;
	public Elevator(){
		this.id = count++;
		this.curFloor = 1;
		this.goalFloors = new ArrayList<Integer>();
		this.direction = 0;  
	}
	public Elevator(int id, int curFloor, ArrayList<Integer> goalFloors){
		this.id = id;
		this.curFloor = curFloor;
		this.goalFloors = goalFloors;
	}
	public int getID(){
		return id;
	}
	public int getDirection(){
		return direction;
	}
	public int getCurFloor(){
		return curFloor;
	}
	public ArrayList<Integer> getGoalFloors(){
		return goalFloors;
	}
	// insert destinationFloor if it is not in the goalFloors list. 	
	public void addDestination(int destinationFloor){
		goalFloors.add(destinationFloor);
	}
	//remove by value: new Integer(destinationFloor)
	public void removeDestination(int destinationFloor){
		goalFloors.remove(new Integer(destinationFloor));
	}
	
	// modify curFloor, goalFloors and update direction of the elevator.
	public void step(){
		//modify curFloor:
		curFloor += direction; //if direction = 0(elevator's current floor doesn't change, if direction is up(1), then curFloor++. 	
		//modify goalFloors:
		goalFloors.remove(new Integer(curFloor));
		//update direction of the elevator
		if(goalFloors.size() == 0) direction = 0;  //when no more destination floor, pause the elevator. 
		//elevator moves up, and  arrive at the top of the destination floors: if there is any lower floor to go now: direction = -1.
		boolean directionChangeFlag = false;
		if(direction == 1 && curFloor == topOfDestinationFloors(goalFloors)){
			for(int i : goalFloors){
				if(i < curFloor){
					directionChangeFlag = true;
					break;
				}
			}
		}
		//elevator moves down and arrive at the lowest floor of the destination floors:if there is any higher floor to go in the goalFloors: direction = 1
		if(direction == -1 && curFloor == bottomOfDestinationFloors(goalFloors)){
			for(int i : goalFloors){
				if(i > curFloor){
					directionChangeFlag = true;
					break;
				}
			}
		}
		if(directionChangeFlag == true) direction = -direction;
		else direction = 0; //no more lower/higher floor to go when the elevator hit the top/bottom of the destination floor
	}
	public int topOfDestinationFloors(ArrayList<Integer> goalFloors){
		int top = Integer.MIN_VALUE;
		for(int i : goalFloors){
			if(top < i) top = i;
		}
		return top;
	}
	public int bottomOfDestinationFloors(ArrayList<Integer> goalFloors){
		int bottom = Integer.MAX_VALUE;
		for(int i : goalFloors){
			if(bottom > i) bottom = i;
		}
		return bottom;
	}
}

