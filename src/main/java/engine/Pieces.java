package engine;

public class Pieces {
    private Pieces() { }

    public static final int NUM_PIECES = 12;

    public static final int W_ROOK = 0;
    public static final int W_KNIGHT = 1;
    public static final int W_BISHOP = 2;
    public static final int W_KING = 3;
    public static final int W_QUEEN = 4;
    public static final int W_PAWN = 5;

    public static final int B_ROOK = 6;
    public static final int B_KNIGHT = 7;
    public static final int B_BISHOP = 8;
    public static final int B_KING = 9;
    public static final int B_QUEEN = 10;
    public static final int B_PAWN = 11;

    private static final int[] PIECE_VALUES = {5, 3, 3, 0, 9, 1};
    public static final int[] WHITE_PIECES = {0, 1, 2, 3, 4, 5};
    public static final int[] BLACK_PIECES = {6, 7, 8, 9, 10, 11};
    private static final char[] ACRONYMS = {'R', 'N', 'B', 'K', 'Q', 'P'};
    private static final int[] SLIDERS = {0, 2, 4, 6, 8, 10};


    public static int getValue(int piece) {
        return PIECE_VALUES[piece % 6];
    }
    public static Allegiance getAllegiance(int piece) {
        if (isWhite(piece))
            return Allegiance.WHITE;

        return Allegiance.BLACK;
    }

    public static char getAcronym(int piece) {
        if (Pieces.isWhite(piece))
            return ACRONYMS[piece];
        else
            return Character.toLowerCase(ACRONYMS[piece - 6]);
    }

    public static int fromAcronym(char c) {
        for (int i = 0; i < ACRONYMS.length; i++) {
            if (ACRONYMS[i] == c)
                return i;
            else if (Character.toLowerCase(ACRONYMS[i]) == c)
                return i + 6;
        }
        throw new RuntimeException("Invalid acronym passed: " + c);
    }

    public static boolean isWhite(int piece) {
        return piece < 6;
    }

    public static int[] getPieces(Allegiance alg) {
        if (alg == Allegiance.WHITE)
            return WHITE_PIECES;
        else
            return BLACK_PIECES;
    }

    public static int[] getOppsPieces(Allegiance alg) {
        if (alg == Allegiance.WHITE)
            return BLACK_PIECES;
        else
            return WHITE_PIECES;
    }

    public static int getOppsEquivalent(int piece) {
        if (piece <= 5)
            return piece + 6;
        else return piece - 6;
    }

    public static boolean isSlider(int piece) {
        for (int slider : SLIDERS) {
            if (slider == piece) {
                return true;
            }
        }
        return false;
    }

    public static int getRook(Allegiance alg) {
        return alg == Allegiance.WHITE ? W_ROOK : B_ROOK;
    }
    public static int getKnight(Allegiance alg) {
        return alg == Allegiance.WHITE ? W_KNIGHT : B_KNIGHT;
    }
    public static int getBishop(Allegiance alg) {
        return alg == Allegiance.WHITE ? W_BISHOP : B_BISHOP;
    }
    public static int getKing(Allegiance alg) {
        return alg == Allegiance.WHITE ? W_KING : B_KING;
    }
    public static int getQueen(Allegiance alg) {
        return alg == Allegiance.WHITE ? W_QUEEN : B_QUEEN;
    }
    public static int getPawn(Allegiance alg) {
        return alg == Allegiance.WHITE ? W_PAWN : B_PAWN;
    }

    public static boolean isKing(int piece) {
        return piece == W_KING || piece == B_KING;
    }
    public static boolean isKnight(int piece) {
        return piece == W_KNIGHT || piece == B_KNIGHT;
    }
    public static boolean isRook(int piece) {
        return piece == W_ROOK || piece == B_ROOK;
    }

    public static boolean isPawn(int piece) {
        return piece == W_PAWN || piece == B_PAWN;
    }

    public static boolean isBishop(int piece) {
        return piece == W_BISHOP || piece == B_BISHOP;
    }
    public static boolean isQueen(int piece) {
        return piece == W_QUEEN || piece == B_QUEEN;
    }

    public static int getByAcronym(String acronym, Allegiance alg) {
        if (alg == Allegiance.BLACK)
            acronym = acronym.toLowerCase();
        return fromAcronym(acronym.charAt(0));
    }
}
