package ai;

import engine.Allegiance;
import engine.Board;
import engine.Move;
import engine.MoveGenerator;
import pgn.OpeningParser;
import pgn.PGNUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OpeningBook implements MoveStrategy {
    private List<String[]> openings;
    private boolean active;

    public OpeningBook() throws IOException {
        // TODO : not tested from black's perspective
        active = true;
        openings = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(OpeningParser.OPENING_FILE_PATH));
        String line;
        while ((line = br.readLine()) != null) {
            String[] pgnMoves = line.split(" ");
            assert pgnMoves.length == OpeningParser.BOOK_ENTRY_SIZE;
            openings.add(pgnMoves);
        }
    }

    @Override
    public Move execute(Board board) {
        Random random = new Random();
        Allegiance alg = board.whoseMove();
        List<Move> moves = MoveGenerator.legalMoves(board, alg);

        Move lastMove = board.getLastMove();

        int moveCount = board.getMoveCount();
        if (moveCount >= OpeningParser.BOOK_ENTRY_SIZE - 2) {
            active = false;
        }
        if (lastMove != null) { // filter out lines
            String lastMovePgn = PGNUtils.pgnFromMove(lastMove, board);
            applyFiltering(lastMovePgn, moveCount-1);
        }
        if (openings.size() == 0)
            return null;
//        } else { // first call
//            int r = random.nextInt(openings.size());
//            String pgnMove = openings.get(r)[0];
//            // TODO
//        }

        int r = random.nextInt(openings.size());
        String pgnMove = openings.get(r)[moveCount];
        applyFiltering(pgnMove, moveCount);

        return PGNUtils.moveFromPgn(pgnMove, board, alg);
    }

    private void applyFiltering(String pgnMove, int idx) {
        List<String[]> filteredOpenings = new ArrayList<>();
        for (String[] opening : openings) {
            if (opening[idx].equals(pgnMove)) {
                filteredOpenings.add(opening);
            }
        }
        openings = filteredOpenings;

    }

    public boolean active() {
        return active;
    }
}

