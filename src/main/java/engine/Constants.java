package engine;

import java.util.ArrayList;
import java.util.Arrays;

public class Constants {
    private static final ArrayList<String> CHESS_NOTATION = initializeChessNotation();

    public static final long FULL_BOARD = -1L;

    public static final long FILE_A = 0x8080808080808080L;
    public static final long FILE_B = 0x4040404040404040L;
    public static final long FILE_C = 0x2020202020202020L;
    public static final long FILE_D = 0x1010101010101010L;
    public static final long FILE_E = 0x0808080808080808L;
    public static final long FILE_F = 0x0404040404040404L;
    public static final long FILE_G = 0x0202020202020202L;
    public static final long FILE_H = 0x0101010101010101L;
    public static final long[] FILES = { FILE_A, FILE_B, FILE_C, FILE_D, FILE_E, FILE_F, FILE_G, FILE_H };

    public static final long RANK_1 = 0x00000000000000FFL;
    public static final long RANK_2 = 0x000000000000FF00L;
    public static final long RANK_3 = 0x0000000000FF0000L;
    public static final long RANK_4 = 0x00000000FF000000L;
    public static final long RANK_5 = 0x000000FF00000000L;
    public static final long RANK_6 = 0x0000FF0000000000L;
    public static final long RANK_7 = 0x00FF000000000000L;
    public static final long RANK_8 = 0xFF00000000000000L;
    public static final long[] RANKS = { RANK_1, RANK_2, RANK_3, RANK_4, RANK_5, RANK_6, RANK_7, RANK_8 };
    public static long LIGHT_SQUARES = 0x55AA55AA55AA55AAL;
    public static long DARK_SQUARES = 0xAA55AA55AA55AA55L;
    public static final long CORNERS = 0x8100000000000081L;

    public static final long WHITE_PAWNS_INIT_POS = 0x000000000000FF00L;
    public static final long WHITE_KNIGHTS_INIT_POS = 0x0000000000000042L;
    public static final long WHITE_BISHOPS_INIT_POS = 0x0000000000000024L;
    public static final long WHITE_ROOKS_INIT_POS = 0x0000000000000081L;
    public static final long WHITE_QUEEN_INIT_POS = 0x0000000000000010L;
    public static final long WHITE_KING_INIT_POS = 0x0000000000000008L;
    public static final long BLACK_PAWNS_INIT_POS = 0x00FF000000000000L;
    public static final long BLACK_KNIGHTS_INIT_POS = 0x4200000000000000L;
    public static final long BLACK_BISHOPS_INIT_POS = 0x2400000000000000L;
    public static final long BLACK_ROOKS_INIT_POS = 0x8100000000000000L;
    public static final long BLACK_QUEEN_INIT_POS = 0x1000000000000000L;
    public static final long BLACK_KING_INIT_POS = 0x0800000000000000L;

    private static final long LEFT_BLACK_ROOK_INIT_POS = RANK_8 & FILE_A;
    private static final long RIGHT_BLACK_ROOK_INIT_POS = RANK_8 & FILE_H;
    private static final long LEFT_WHITE_ROOK_INIT_POS = RANK_1 & FILE_A;
    private static final long RIGHT_WHITE_ROOK_INIT_POS = RANK_1 & FILE_H;

    public static long getMaskForFiles(int from, int to) {
        if (from < 0 || to < 0 || to > 7 || from >= to) {
            throw new RuntimeException("invalid arguments");
        }

        long mask = 0L;

        for (int i = from; i <= to; i++) {
            mask |= FILES[i];
        }

        return mask;
    }
    public static int fromChessNotation(String notation) {
        int sq = CHESS_NOTATION.indexOf(notation);
        if (sq == -1)
            throw new RuntimeException("Invalid notation passed : " + notation + ".");
        return 63 - sq;
    }
    public static String chessNotation(int sq) {
        return CHESS_NOTATION.get(63 - sq);
    }

    public static long getPawnPromotionRank(Allegiance alg) {
        return alg == Allegiance.WHITE ? RANK_8 : RANK_1;
    }
    public static long getKingInitPos(Allegiance alg) {
        return alg == Allegiance.WHITE ? WHITE_KING_INIT_POS : BLACK_KING_INIT_POS;
    }

    public static long getPawnStartRank(Allegiance alg) {
        return alg == Allegiance.WHITE ? RANK_2 : RANK_7;
    }

    public static long getPawnJumpRank(Allegiance alg) {
        return alg == Allegiance.WHITE ? RANK_4 : RANK_5;
    }

    public static long getLeftRookMask(Allegiance alg) {
        return alg == Allegiance.WHITE ? LEFT_WHITE_ROOK_INIT_POS : LEFT_BLACK_ROOK_INIT_POS;
    }
    public static long getRightRookMask(Allegiance alg) {
        return alg == Allegiance.WHITE ? RIGHT_WHITE_ROOK_INIT_POS : RIGHT_BLACK_ROOK_INIT_POS;
    }

    private static ArrayList<String> initializeChessNotation() {
        String[] chessNotation = {
                "a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8",
                "a7", "b7", "c7", "d7", "e7", "f7", "g7", "h7",
                "a6", "b6", "c6", "d6", "e6", "f6", "g6", "h6",
                "a5", "b5", "c5", "d5", "e5", "f5", "g5", "h5",
                "a4", "b4", "c4", "d4", "e4", "f4", "g4", "h4",
                "a3", "b3", "c3", "d3", "e3", "f3", "g3", "h3",
                "a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2",
                "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1"};

        return new ArrayList<>(Arrays.asList(chessNotation));
    }
}
