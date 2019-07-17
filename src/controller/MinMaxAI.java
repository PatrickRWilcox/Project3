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

	/**
	 * Holds the value depth that represents the number of moves the AI will look 
	 * ahead when NextMove is called.
	 */
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
		Node n = nextLayer(depth, g.getBoard(), me);
		return n.spot;
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

		private Board board;

		/**
		 * Constructor to create a Node.
		 * 
		 * @param score int to set the score to.
		 * @param spot  location of the move that was made.
		 */
		Node(int score, Location spot, Board b) {
			this.score = score;
			this.spot = spot;
			this.board = b;
		}
	}

	 /** Copies board from Game g to a new empty Board and then returns it.
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
	 * This method finds the best move for player to make d moves into the future
	 * starting from board b.
	 * @param d number of moves to look forward as an int.
	 * @param b the current configuration of the board to start looking 
	 * into the future from.
	 * @param player the player who's turn it is to make a move.
	 * @return Node with the best possible score, or null if there are no nodes.
	 */
	private Node nextLayer(int d, Board b, Player player) {
		Node node = new Node(estimate(b), null, b);
		
		//handler for base case
		if (d == 1 && b.getState() == State.NOT_OVER) {
			Iterator<Location> available = moves(b).iterator();
			while (available.hasNext()) {
				Location next = available.next();
				Board b2 = b.update(player, next);
				if (player == me) {
					if(node.spot == null || estimate(b2) > node.score) {
						node = new Node(estimate(b2), next, copyBoard(b2));
					}
				}
				else {
					if(node.spot == null || estimate(b2) < node.score) {
						node = new Node(estimate(b2), next, copyBoard(b2));
					}
				}
			}
		} 
		
		//handler for when the game is not over and the execution has not 
		//reached the base case.
		else if (d > 1 && b.getState() == State.NOT_OVER) {
			Iterator<Location> available = moves(b).iterator();
			while (available.hasNext()) {
				Location next = available.next(); 
				Board b2 = b.update(player, next);
				Node n2 = nextLayer(d - 1, copyBoard(b2), player.opponent());
				Node n3 = new Node(estimate(copyBoard(n2.board)), next, b2);
				if (player == me) {
					if(node.spot == null || n3.score > node.score) {
						node = n3;
					}
				}
				else {
					if(node.spot == null || n3.score < node.score) {
						node = n3;
					}
				}
			}	
		} 
		
		//handles the states when the game is over
		else if (b.getState() != State.NOT_OVER) {
			if(b.getState() == State.DRAW) {
				node = new Node(0, null, b);
			}
			else if(b.getState() == State.HAS_WINNER && 
					b.getWinner().winner == me) {
				node = new Node(11150, null, b);
			}
			else {
				node = new Node(-11150, null, b);
			}
		}
		return node;
	}
}