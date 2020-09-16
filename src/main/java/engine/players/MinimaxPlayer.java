package engine.players;

import ai.AlphaBeta;
import engine.Allegiance;
import engine.Board;
import engine.Flag;
import engine.Move;

import java.util.function.DoublePredicate;

public class MinimaxPlayer extends ComputerPlayer {
    private static final int DEPTH = 3;
    private AlphaBeta alphaBeta;

    public MinimaxPlayer(Board board, Allegiance alg) {
        super(board, alg);
        alphaBeta = new AlphaBeta(DEPTH);
    }

    @Override
    public Flag executeMove() {
//        Move move = minimax.execute(board);
        Move move = alphaBeta.execute(board);
        board.makeMoveTransition(move);
        return Flag.DONE;
    }
}
