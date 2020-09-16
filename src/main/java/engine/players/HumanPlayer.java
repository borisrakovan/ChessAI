package engine.players;

import engine.*;

import java.util.List;

public class HumanPlayer extends Player {

    public HumanPlayer(Board board, Allegiance alg) {
        super(board, alg);
    }

    public Flag executeMove(int from, int to) {
        List<Move> legals = MoveGenerator.legalMoves(board, alg);

        Move move = null;
        for (Move legal : legals) {
            if (legal.fromSquare() == from && legal.toSquare() == to) {
                // TODO: pri promotion obojstranna vazba na board a get
//                System.out.println("Found match: " + legal.toString());
                move = legal;
                break;
            }
        }
        if (move == null) {
            return Flag.INVALID;
        }
        board.makeMoveTransition(move);
        return Flag.DONE;
    }

    @Override
    public boolean isComputer() {
        return false;
    }

}
