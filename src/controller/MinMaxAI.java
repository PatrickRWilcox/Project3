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
 * move.  The minimax algorithm searches for the best possible next move, under
 * the assumption that your opponent will also always select the best possible
 * move.
 *
 * <p>The minimax algorithm assigns a score to each possible game configuration
 * g.  The score is assigned recursively as follows: if the game g is over and
 * the player has won, then the score is infinity.  If the game g is over and
 * the player has lost, then the score is negative infinity.  If the game is a
 * draw, then the score is 0.
 * 
 * <p>If the game is not over, then there are many possible moves that could be
 * made; each of these leads to a new game configuration g'.  We can
 * recursively find the score for each of these configurations.
 * 
 * <p>If it is the player's turn, then they will choose the action that
 * maximizes their score, so the score for g is the maximum of all the scores
 * of the g's.  However, if it is the opponent's turn, then the opponent will
 * try to minimize the score for the player, so the score for g is the
 * <em>minimum</em> of all of the scores of the g'.
 * 
 * <p>You can think of the game as defining a tree, where each node in the tree
 * represents a game configuration, and the children of g are all of the g'
 * reachable from g by taking a turn.  The minimax algorithm is then a
 * particular traversal of this tree.
 * 
 * <p>In practice, game trees can become very large, so we apply a few
 * strategies to narrow the set of paths that we search.  First, we can decide
 * to only consider certain kinds of moves.  For five-in-a-row, there are
 * typically at least 70 moves available at each step; but it's (usually) not
 * sensible to go on the opposite side of the board from where all of the other
 * pieces are; by restricting our search to only part of the board, we can
 * reduce the space considerably.
 * 
 * <p>A second strategy is that we can look only a few moves ahead instead of
 * planning all the way to the end of the game.  This requires us to be able to
 * estimate how "good" a given board looks for a player.
 * 
 * <p>This class implements the minimax algorithm with support for these two
 * strategies for reducing the search space.  The abstract method {@link
 * #moves(Board)} is used to list all of the moves that the AI is willing to
 * consider, while the abstract method {@link #estimate(Board)} returns
 * the estimation of how good the board is for the given player.
 */
public abstract class MinMaxAI extends Controller {
	/**
	 * Return an estimate of how good the given board is for me.
	 * A result of infinity means I have won.  A result of negative infinity
	 * means that I have lost.
	 */
	protected abstract int estimate(Board b);
	
	/**
	 * Return the set of moves that the AI will consider when planning ahead.
	 * Must contain at least one move if there are any valid moves to make.
	 */
	protected abstract Iterable<Location> moves(Board b);
	
	private Player player;
	
	private int depth;
	/**
	 * Create an AI that will recursively search for the next move using the
	 * minimax algorithm.  When searching for a move, the algorithm will look
	 * depth moves into the future.
	 *
	 * <p>choosing a higher value for depth makes the AI smarter, but requires
	 * more time to select moves.
	 */
	protected MinMaxAI(Player me, int depth) {
		super(me);
		player = me;
		this.depth = depth;
	}

	/**
	 * Return the move that maximizes the score according to the minimax
	 * algorithm described above.
	 */
	protected @Override Location nextMove(Game g) {
		Iterator<Location> available = moves(g.getBoard()).iterator();
		List<Node> scores = new ArrayList<>();
		
		while(available.hasNext()) {
			Game g2 = new Game(player);
			copyBoard(g, g2);
			Location next = available.next();
			g2.submitMove(me, next);
			int tree_depth = depth;
			while(tree_depth > 1) {
				List<Node> opp_nodes = new ArrayList<>();
				List<Node> your_nodes = new ArrayList<>();
				Iterator<Location> opp_available = moves(g2.getBoard()).iterator();
				while(opp_available.hasNext()) {
					Game g3 = new Game(me.opponent());
					copyBoard(g2, g3);
					Location opp_next = opp_available.next();
					g3.submitMove(me.opponent(), opp_next);
					Node opp_n = new Node(estimate(g3.getBoard()), opp_next);
					opp_nodes.add(opp_n);
				}
				g2.submitMove(me.opponent(), findMin(opp_nodes).spot);
				Iterator<Location> your_available = moves(g2.getBoard()).iterator();
				while(your_available.hasNext()) {
					Game g4 = new Game(me);
					copyBoard(g2, g4);
					Location your_next = your_available.next();
					g4.submitMove(me, your_next);
					Node your_n = new Node(estimate(g4.getBoard()), your_next);
					your_nodes.add(your_n);
				}
				//g2.submitMove(me, findMax(opp_nodes).spot);//
			}
			Node n = new Node(estimate(g2.getBoard()), next);
			scores.add(n);
		}
		
		return findMax(scores).spot;
	}
	
	private class Node<E> {
		 private int score;
		 private Location spot;
		 
		 private Node(int score, Location spot) {
			 this.score = score;
			 this.spot = spot;
		 }
	}
	
	private Node findMax(List<Node> nodes) {
		Node max = nodes.get(0);
		for(Node node: nodes) {
			if(node.score > max.score)
				max = node;
		}
		return max;
	}
	
	private Node findMin(List<Node> nodes) {
		Node min = nodes.get(0);
		for(Node node: nodes) {
			if(node.score < min.score)
				min = node;
		}
		return min;
	}
	
	private boolean greaterThan(int value, List<Node> nodes) {
		for(Node node: nodes)
			if (node.score > value)
				return true;
		return false;
	}
	
	private void copyBoard(Game g, Game g2) {
		for(int x = 0; x < g2.getBoard().NUM_COLS; x++) {
			for(int y = 0; y < g2.getBoard().NUM_ROWS; y++) {
				Location l = new Location(x,y);
				g2.getBoard().update(g.getBoard().get(l), l);
			}
		}
	}
	
}
