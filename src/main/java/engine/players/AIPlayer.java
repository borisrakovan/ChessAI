package engine.players;

import ai.AlphaBeta;
import ai.OpeningBook;
import engine.Allegiance;
import engine.Board;
import engine.Flag;
import engine.Move;

import java.io.IOException;

public class AIPlayer extends ComputerPlayer {
    private static final int DEPTH = 7;

    private AlphaBeta alphaBeta;
    private OpeningBook openingBook;
    private boolean usingBook;

    public AIPlayer(Board board, Allegiance alg, boolean useOpeningBook) {
        super(board, alg);
        alphaBeta = new AlphaBeta(DEPTH);
        usingBook = useOpeningBook;
        if (usingBook) {
            try {
                openingBook = new OpeningBook();
            } catch (IOException e) {
                throw new RuntimeException("Error while loading the opening book for AIPlayer");
            }
        }
    }

    @Override
    public Flag executeMove() {
        Move move;
        if (usingBook && openingBook.active()) {
            move = openingBook.execute(board);
            if (move != null) {
                System.out.println("Using opening book to play: " + move);
                board.makeMoveTransition(move);
                return Flag.DONE;
            }
        }

        move = alphaBeta.execute(board);
        board.makeMoveTransition(move);

        return Flag.DONE;
    }
}
