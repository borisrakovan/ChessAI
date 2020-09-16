package engine;

import fen.FenUtils;
import gui.Table;

import java.util.List;
import java.util.Random;

public class Game {

    private Game() {
//        Board b = FenUtils.fenToBoard("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -");
//        Table table = new Table(b); // TODO FIX ENP
        Table table = new Table(null);

    }


    public static void main(String[] args) {
        Game game = new Game();
    }

    private static long movesToBitboard(List<Move> moves) {
        long bitboard = 0L;

        for (Move move : moves) {
            bitboard |= Board.toBitboard(move.toSquare());
        }

        return bitboard;
    }

    static public void printBitboard(long bitboard) {
        String str = Long.toBinaryString(bitboard);
        StringBuilder builder = new StringBuilder();
        int numZeros = Long.numberOfLeadingZeros(bitboard);
        for (int i = 0; i < numZeros; i++) {
            str = '0' + str;
        }
        for (int i = 0; i < str.length(); i++) {
            builder.append(str.charAt(i)).append(' ');
            if (i % 8 == 7)
                builder.append('\n');

        }

        System.out.println(builder.toString());
    }

    static public void printBoard(Board board) {
        char[] out = new char[64];

        for (int i = 0; i < 64; i++) {
            for (int j = 0; j < Pieces.NUM_PIECES; j++) {
                if (((board.getBitboard(j) >>> i) & 1) == 1L) {
                    char p = Pieces.getAcronym(j);
                    out[i] = p;
                    break;
                }
                else {
                    out[i] = '-';
                }
            }
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 63; i >= 0; i--) {
            builder.append(out[i]);
            builder.append(' ');
            if (i % 8 == 0)
                builder.append('\n');
        }
        System.out.println(builder.toString());
    }

}