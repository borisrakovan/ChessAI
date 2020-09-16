package engine.players;

import engine.*;

import java.util.List;
import java.util.Random;

public class RandomComputerPlayer extends ComputerPlayer {
    public RandomComputerPlayer(Board board, Allegiance alg) {
        super(board, alg);
    }

    @Override
    public Flag executeMove() {
        List<Move> legals = MoveGenerator.legalMoves(board, alg);

        Random r = new Random();
        int idx = r.nextInt(legals.size());
        board.makeMoveTransition(legals.get(idx));
        return Flag.DONE;
    }
}
