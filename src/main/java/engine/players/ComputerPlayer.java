package engine.players;

import engine.*;

import java.util.List;
import java.util.Random;

public abstract class ComputerPlayer extends Player {
    public ComputerPlayer(Board board, Allegiance alg) {
        super(board, alg);
    }


    public abstract Flag executeMove();
    @Override
    public boolean isComputer() {
        return true;
    }
}
