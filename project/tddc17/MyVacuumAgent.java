package tddc17;

import aima.core.environment.liuvacuum.*;
import aima.core.agent.Action;
import aima.core.agent.AgentProgram;
import aima.core.agent.Percept;
import aima.core.agent.impl.*;

import java.util.Random;

class MyAgentState {
	public int[][] world = new int[30][30];
	public int initialized = 0;
	final int UNKNOWN = 0;
	final int WALL = 1;
	final int CLEAR = 2;
	final int DIRT = 3;
	final int HOME = 4;
	final int ACTION_NONE = 0;
	final int ACTION_MOVE_FORWARD = 1;
	final int ACTION_TURN_RIGHT = 2;
	final int ACTION_TURN_LEFT = 3;
	final int ACTION_SUCK = 4;

	public int agent_x_position = 1;
	public int agent_y_position = 1;
	public int agent_last_action = ACTION_NONE;

	public static final int NORTH = 0;
	public static final int EAST = 1;
	public static final int SOUTH = 2;
	public static final int WEST = 3;
	public int agent_direction = EAST;

	MyAgentState() {
		for (int i = 0; i < world.length; i++)
			for (int j = 0; j < world[i].length; j++)
				world[i][j] = UNKNOWN;
		world[1][1] = HOME;
		agent_last_action = ACTION_NONE;
	}

	// Based on the last action and the received percept updates the x & y agent
	// position
	public void updatePosition(DynamicPercept p) {
		Boolean bump = (Boolean) p.getAttribute("bump");

		if (agent_last_action == ACTION_MOVE_FORWARD && !bump) {
			switch (agent_direction) {
			case MyAgentState.NORTH:
				agent_y_position--;
				break;
			case MyAgentState.EAST:
				agent_x_position++;
				break;
			case MyAgentState.SOUTH:
				agent_y_position++;
				break;
			case MyAgentState.WEST:
				agent_x_position--;
				break;
			}
		}

	}

	public void updateWorld(int x_position, int y_position, int info) {
		world[x_position][y_position] = info;
	}

	public void printWorldDebug() {
		for (int i = 0; i < world.length; i++) {
			for (int j = 0; j < world[i].length; j++) {
				if (world[j][i] == UNKNOWN)
					System.out.print(" ? ");
				if (world[j][i] == WALL)
					System.out.print(" # ");
				if (world[j][i] == CLEAR)
					System.out.print(" . ");
				if (world[j][i] == DIRT)
					System.out.print(" D ");
				if (world[j][i] == HOME)
					System.out.print(" H ");
			}
			System.out.println("");
		}
	}
}

class MyAgentProgram implements AgentProgram {

	private int initnialRandomActions = 10;
	private Random random_generator = new Random();

	// Here you can define your variables!
	public MyAgentState state = new MyAgentState();
	
	boolean reached_bottom = false; //did agent reach bottom (i.e south) wall?
	boolean bottom_right_corner = false; //did agent reach the bottom corner?

	boolean leftTurn = true; //in zigzag move, turn left or right? (first in turns left)
	boolean isTurning = false; //is agent performing turning in zigzag (true only when agent reaches wall)
	
	int trapped_count = 0; //needed to keep track whether agent got stuck
	boolean moved_random = false; //after getting stuck and moving random, set this to true

	// moves the Agent to a random start position
	// uses percepts to update the Agent position - only the position, other
	// percepts are ignored
	// returns a random action
	private Action moveToRandomStartPosition(DynamicPercept percept) {
		int action = random_generator.nextInt(6);
		initnialRandomActions--;
		state.updatePosition(percept);
		if (action == 0) {
			state.agent_direction = ((state.agent_direction - 1) % 4);
			if (state.agent_direction < 0)
				state.agent_direction += 4;
			state.agent_last_action = state.ACTION_TURN_LEFT;
			return LIUVacuumEnvironment.ACTION_TURN_LEFT;
		} else if (action == 1) {
			state.agent_direction = ((state.agent_direction + 1) % 4);
			state.agent_last_action = state.ACTION_TURN_RIGHT;
			return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
		}
		state.agent_last_action = state.ACTION_MOVE_FORWARD;
		return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
	}

	@Override
	public Action execute(Percept percept) {

		// DO NOT REMOVE this if condition!!!
		if (initnialRandomActions > 0) {
			return moveToRandomStartPosition((DynamicPercept) percept);
		} else if (initnialRandomActions == 0) {
			// process percept for the last step of the initial random actions
			initnialRandomActions--;
			state.updatePosition((DynamicPercept) percept);
			System.out.println("Processing percepts after the last execution of moveToRandomStartPosition()");
			state.agent_last_action = state.ACTION_SUCK;
			return LIUVacuumEnvironment.ACTION_SUCK;
		}

		// This example agent program will update the internal agent state while only
		// moving forward.
		// START HERE - code below should be modified!

		System.out.println("x=" + state.agent_x_position);
		System.out.println("y=" + state.agent_y_position);
		System.out.println("dir=" + state.agent_direction);

		DynamicPercept p = (DynamicPercept) percept;
		Boolean bump = (Boolean) p.getAttribute("bump");
		Boolean dirt = (Boolean) p.getAttribute("dirt");
		Boolean home = (Boolean) p.getAttribute("home");
		System.out.println("percept: " + p);

		// State update based on the percept value and the last action
		state.updatePosition((DynamicPercept) percept);
		if (bump) {
			switch (state.agent_direction) {
			case MyAgentState.NORTH:
				state.updateWorld(state.agent_x_position, state.agent_y_position - 1, state.WALL);
				break;
			case MyAgentState.EAST:
				state.updateWorld(state.agent_x_position + 1, state.agent_y_position, state.WALL);
				break;
			case MyAgentState.SOUTH:
				state.updateWorld(state.agent_x_position, state.agent_y_position + 1, state.WALL);
				break;
			case MyAgentState.WEST:
				state.updateWorld(state.agent_x_position - 1, state.agent_y_position, state.WALL);
				break;
			}
		}
		if (dirt)
			state.updateWorld(state.agent_x_position, state.agent_y_position, state.DIRT);
		else {
			state.updateWorld(state.agent_x_position, state.agent_y_position, state.CLEAR);
		}
		state.printWorldDebug();

		// Next action selection based on the percept value
		//whenever agent sees dirt, before performing any other action, it cleans the dirt
		if (dirt) {
			System.out.println("DIRT -> choosing SUCK action!");
			state.agent_last_action = state.ACTION_SUCK;
			return LIUVacuumEnvironment.ACTION_SUCK;
		} else {
			
			//Part 1. Reaching the bottom right corner and facing correct (NORTH) direction when finished
			
			// Code to lead the agent to the bottom right corner
			// The agent's direction must be NORTH to start the zigzag method
			if (!bottom_right_corner) {
				//if not facing SOUTH, turn
				if (!reached_bottom && state.agent_direction != MyAgentState.SOUTH) {
					turnRight();
					return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
				}
				//if facing south and bumbed, the agent reached the bottom wall
				if (state.agent_direction == MyAgentState.SOUTH && bump)
					reached_bottom = true;
				//after reaching bottom wall, the agent needs to face EAST
				if (reached_bottom && state.agent_direction != MyAgentState.EAST) {
					turnLeft();
					return LIUVacuumEnvironment.ACTION_TURN_LEFT;
				}
				if (reached_bottom && state.agent_direction == MyAgentState.EAST && bump) {
					turnLeft();
					bottom_right_corner = true;
					return LIUVacuumEnvironment.ACTION_TURN_LEFT;
				}

				state.agent_last_action = state.ACTION_MOVE_FORWARD;
				return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
			}
			
			//Part 2. Exploring the world via zigzag moves

			//code for agent to not end up not at home for even-sized board
			if(isTurning && home && bump) {
				state.agent_last_action = state.ACTION_NONE;
				return NoOpAction.NO_OP;
			} else if (isTurning && bump && !home) {
				isTurning = false;
				turnRight();
				leftTurn = true;
				trapped_count++;
				return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
			}
			
			//if got unstuck after moving random, reset the zigzag movement
			if (moved_random) {
				isTurning = false;
				if (!leftTurn && state.agent_direction != MyAgentState.NORTH) {
					turnRight();
					return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
				} else if (leftTurn && state.agent_direction != MyAgentState.SOUTH) {
					turnRight();
					return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
				}
				leftTurn = !leftTurn;
				trapped_count = 0;
				moved_random = false;
			}
			
			//if stuck in one place for more than 2 moves, make random move
			if (trapped_count>2) {
				//after 3 random moves it probably got unstuck
				if (trapped_count > 5) {
					moved_random = true; 
				}
				trapped_count++;
				int action = random_generator.nextInt(6);
				if (action == 0) {
					turnLeft();
					return LIUVacuumEnvironment.ACTION_TURN_LEFT;
				} else if (action == 1) {
					turnRight();
					return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
				}
				state.agent_last_action = state.ACTION_MOVE_FORWARD;
				return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
			}
			
			
			//Turn (consists of 3 actions) - turn left/right, move forward, turn left/right
			if (isTurning) {
				//move_forward
				if (state.agent_last_action != state.ACTION_MOVE_FORWARD && state.agent_last_action != state.ACTION_SUCK) {
					state.agent_last_action = state.ACTION_MOVE_FORWARD;
					return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
				} else { //second turn left/right action
					isTurning = false;
					if (leftTurn) {
						turnLeft();
						leftTurn = false;
						return LIUVacuumEnvironment.ACTION_TURN_LEFT;
					} else {
						turnRight();
						leftTurn = true;
						return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
					}
				}
			}
			
			if (bump) {
				isTurning = true;
				//first turn left/right action
				if (leftTurn) {
					turnLeft();
					return LIUVacuumEnvironment.ACTION_TURN_LEFT;
				} else {
					turnRight();
					return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
				}
			} else { //move forward until the agent reaches the wall
				state.agent_last_action = state.ACTION_MOVE_FORWARD;
				return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
			}
		}
	}

	//function for updating the state of the agent, before making a right turn
	//needs to be called before every turn right action
	private void turnRight() {
		state.agent_last_action = state.ACTION_TURN_RIGHT;
		switch (state.agent_direction) {
		case MyAgentState.NORTH:
			state.agent_direction = MyAgentState.EAST;
			break;
		case MyAgentState.EAST:
			state.agent_direction = MyAgentState.SOUTH;
			break;
		case MyAgentState.SOUTH:
			state.agent_direction = MyAgentState.WEST;
			break;
		case MyAgentState.WEST:
			state.agent_direction = MyAgentState.NORTH;
			break;

		}
	}

	//function for updating the state of the agent, before making a left turn
	//needs to be called before every turn left action
	private void turnLeft() {
		state.agent_last_action = state.ACTION_TURN_LEFT;
		switch (state.agent_direction) {
		case MyAgentState.NORTH:
			state.agent_direction = MyAgentState.WEST;
			break;
		case MyAgentState.EAST:
			state.agent_direction = MyAgentState.NORTH;
			break;
		case MyAgentState.SOUTH:
			state.agent_direction = MyAgentState.EAST;
			break;
		case MyAgentState.WEST:
			state.agent_direction = MyAgentState.SOUTH;
			break;

		}
	}

}

public class MyVacuumAgent extends AbstractAgent {
	public MyVacuumAgent() {
		super(new MyAgentProgram());
	}
}
