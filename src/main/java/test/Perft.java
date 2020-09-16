package test;

import engine.*;
import fen.FenUtils;

import java.util.List;

public class Perft {
    /*
    * https://www.chessprogramming.org/Perft_Results
    * INIT POs: up to 6 OK, 7 LONG
    * POS 2: up to 5 OK, 6 LONG
    * POS 3: FIXME 2
    * POS 4: up to 6 OK
    *
    * */
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
//        Board b = FenUtils.fenToBoard("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -");
//        Board std = new Board();
//        System.out.println(perft(2, b, ""));
        runTests();

        long elapsedTime = System.currentTimeMillis() - start;
        System.out.println("Elapsed time: " + elapsedTime/1000 + " seconds.");

    }

    // TODO captures and other info
    private static long perft(int depth, Board board, String moveSeq) {

        if (depth == 0)
            return 1;

        long nodes = 0;
        List<Move> legals = MoveGenerator.legalMoves(board, board.whoseMove());

        for (Move move : legals) {
            Board newBoard = new Board(board);
            newBoard.makeMoveTransition(move);

            nodes += perft(depth - 1, newBoard, moveSeq + " " + move.toString());
        }

        return nodes;
    }

    private static void runTests() {
        System.out.println("Started perft testing");

        Board b1 = FenUtils.fenToBoard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        assert perft(6, b1, "") == 119060324;

        Board b2 = FenUtils.fenToBoard("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -");
        assert perft(5, b2, "") == 193690690;

        Board b4 = FenUtils.fenToBoard("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1");
//        assert perft(5, b4, "") == 15833292;
        assert perft(6, b4, "") == 706045033;

        Board b5 = FenUtils.fenToBoard("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8");
        assert perft(5, b5, "") == 89941194;

        System.out.println("Passed");
    }
}
