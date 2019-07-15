package controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

import model.Board;
import model.Board.State;
import model.Game;
import model.Location;
import model.NotImplementedException;
import model.Player;

/**
 * A MinMaxAI is a controller that uses the minimax algorithm to select the next
 * move. The minimax algorithm searches for the best possible next move, under
 * the assumption that your opponent will also always select the best possible
 * move.
 *
 * <p>
 * The minimax algorithm assigns a score to each possible game configuration g.
 * The score is assigned recursively as follows: if the game g is over and the
 * player has won, then the score is infinity. If the game g is over and the
 * player has lost, then the score is negative infinity. If the game is a draw,
 * then the score is 0.
 * 
 * <p>
 * If the game is not over, then there are many possible moves that could be
 * made; each of these leads to a new game configuration g'. We can recursively
 * find the score for each of these configurations.
 * 
 * <p>
 * If it is the player's turn, then they will choose the action that maximizes
 * their score, so the score for g is the maximum of all the scores of the g's.
 * However, if it is the opponent's turn, then the opponent will try to minimize
 * the score for the player, so the score for g is the <em>minimum</em> of all
 * of the scores of the g'.
 * 
 * <p>
 * You can think of the game as defining a tree, where each node in the tree
 * represents a game configuration, and the children of g are all of the g'
 * reachable from g by taking a turn. The minimax algorithm is then a particular
 * traversal of this tree.
 * 
 * <p>
 * In practice, game trees can become very large, so we apply a few strategies
 * to narrow the set of paths that we search. First, we can decide to only
 * consider certain kinds of moves. For five-in-a-row, there are typically at
 * least 70 moves available at each step; but it's (usually) not sensible to go
 * on the opposite side of the board from where all of the other pieces are; by
 * restricting our search to only part of the board, we can reduce the space
 * considerably.
 * 
 * <p>
 * A second strategy is that we can look only a few moves ahead instead of
 * planning all the way to the end of the game. This requires us to be able to
 * estimate how "good" a given board looks for a player.
 * 
 * <p>
 * This class implements the minimax algorithm with support for these two
 * strategies for reducing the search space. The abstract method
 * {@link #moves(Board)} is used to list all of the moves that the AI is willing
 * to consider, while the abstract method {@link #estimate(Board)} returns the
 * estimation of how good the board is for the given player.
 */
public abstract class MinMaxAI extends Controller {
	/**
	 * Return an estimate of how good the given board is for me. A result of
	 * infinity means I have won. A result of negative infinity means that I have
	 * lost.
	 */
	protected abstract int estimate(Board b);

	/**
	 * Return the set of moves that the AI will consider when planning ahead. Must
	 * contain at least one move if there are any valid moves to make.
	 */
	protected abstract Iterable<Location> moves(Board b);

	private int depth;

	/**
	 * Create an AI that will recursively search for the next move using the minimax
	 * algorithm. When searching for a move, the algorithm will look depth moves
	 * into the future.
	 *
	 * <p>
	 * choosing a higher value for depth makes the AI smarter, but requires more
	 * time to select moves.
	 */
	protected MinMaxAI(Player me, int depth) {
		super(me);
		this.depth = depth;
	}

	/**
	 * Return the move that maximizes the score according to the minimax algorithm
	 * described above.
	 */
	protected @Override Location nextMove(Game g) {
		Iterator<Location> available = moves(g.getBoard()).iterator();
		List<Node> nodes = new ArrayList<>();

		while (available.hasNext()) {
			Board b = copyBoard(g);
			Location next = available.next();
			b = b.update(me, next);
			b = nextLayer(depth - 1, b);
			Node n = new Node(estimate(b), next);
			nodes.add(n);
		}
		
		for(Node node: nodes) {
			
		}
		return findMax(nodes).spot;
	}

	/**
	 * Node that stores the location of the move and the maximum possible score
	 * achievable by making that move.
	 */
	private class Node {
		/**
		 * int score holds the maximum possible score achievable by making this move.
		 */
		private int score;

		/** Location spot stores the location that is played. */
		private Location spot;

		/**
		 * Constructor to create a Node.
		 * 
		 * @param score int to set the score to.
		 * @param spot  location of the move that was made.
		 */
		Node(int score, Location spot) {
			this.score = score;
			this.spot = spot;
		}
	}

	/**
	 * Finds the highest score out of all nodes in a list, then returns that node.
	 * 
	 * @param nodes List of Nodes to search.
	 * @return The Node with the highest score, or null if the list is empty.
	 */
	private Node findMax(List<Node> nodes) {
		if (nodes.size() > 0) {
			Node max = nodes.get(0);
			for (Node node : nodes) {
				if (node.score > max.score)
					max = node;
			}
			return max;
		}
		return null;
	}

	/**
	 * Finds the highest score out of all nodes in a list, then returns that node.
	 * 
	 * @param nodes List of Nodes to search.
	 * @return The Node with the highest score, or null if the list is empty.
	 */
	private Node findMin(List<Node> nodes) {
		if (nodes.size() > 0) {
			Node min = nodes.get(0);
			for (Node node : nodes) {
				if (node.score < min.score)
					min = node;
			}
			return min;
		}
		return null;
	}

	/**
	 * Copies board from Game g to a new empty Board and then returns it.
	 * 
	 * @param g Game to have its board copied.
	 * @return A new Board with the same configuration as Game g's board.
	 */
	private Board copyBoard(Game g) {
		Board b = Board.EMPTY;
		for (int x = 0; x < g.getBoard().NUM_COLS; x++) {
			for (int y = 0; y < g.getBoard().NUM_ROWS; y++) {
				Location l = new Location(y, x);
				if (g.getBoard().get(l) != null && b.getState() == State.NOT_OVER)
					b = b.update(g.getBoard().get(l), l);
			}
		}
		return b;
	}

	/**
	 * Copies board from Board b to a new empty Board and then returns it.
	 * 
	 * @param b Board to have its configuration copied.
	 * @return A new Board with the same configuration as Board b.
	 */
	private Board copyBoard(Board b) {
		Board b2 = Board.EMPTY;
		for (int x = 0; x < b.NUM_COLS; x++) {
			for (int y = 0; y < b.NUM_ROWS; y++) {
				Location l = new Location(y, x);
				if (b.get(l) != null && b2.getState() == State.NOT_OVER)
					b2 = b2.update(b.get(l), l);
			}
		}
		return b2;
	}

	/**
	 * Recursively called to find the board configuration d moves ahead starting
	 * from board b that gives Player me the highest score.
	 * 
	 * @param d int representing how many times to recur.
	 * @param b Board initial configuration of the board.
	 * @return A new Board object where each player has played d more moves.
	 */
	private Board nextLayer(int d, Board b) {
		if (d > 0 && b.getState() == State.NOT_OVER) {
			b = nextLayer(d - 1, b);
			List<Node> opp_nodes = new ArrayList<>();
			List<Node> your_nodes = new ArrayList<>();
			Iterator<Location> opp_available = moves(b).iterator();
			while (opp_available.hasNext()) {
				Board b2 = copyBoard(b);
				Location opp_next = opp_available.next();
				if (b2.getState() == State.NOT_OVER)
					b2 = b2.update(me.opponent(), opp_next);
				Node opp_n = new Node(estimate(b2), opp_next);
				opp_nodes.add(opp_n);
			}
			if (b.getState() == State.NOT_OVER)
				b = b.update(me.opponent(), findMin(opp_nodes).spot);
			Iterator<Location> your_available = moves(b).iterator();
			while (your_available.hasNext()) {
				Board b3 = copyBoard(b);
				Location your_next = your_available.next();
				if (b3.getState() == State.NOT_OVER)
					b3 = b3.update(me, your_next);
				Node your_n = new Node(estimate(b3), your_next);
				your_nodes.add(your_n);
			}
			if (b.getState() == State.NOT_OVER)
				b = b.update(me, findMax(your_nodes).spot);
		}
		return b;
	}

}
