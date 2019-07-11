package controller;

import java.util.ArrayList;
import java.util.List;

import model.Board;
import model.Game;
import model.Location;
import model.NotImplementedException;
import model.Player;

/**
 * A DumbAI is a Controller that always chooses the blank space with the
 * smallest column number from the row with the smallest row number.
 */
public class DumbAI extends Controller {

	public DumbAI(Player me) {
		super(me);
	}

	protected @Override Location nextMove(Game g) {
		Location l = null;
		for(int x = 0; x < g.getBoard().NUM_ROWS; x++) {
			for(int y = 0; y < g.getBoard().NUM_COLS; y++) {
				if(l == null) {
					if(g.getBoard().get(x, y) == null)
						l = new Location(x, y);
				}
			}
		}
		return l;
	}
}
