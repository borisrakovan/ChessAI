package ai;

import engine.Allegiance;
import engine.Board;
import engine.CastleRight;
import engine.Move;

import java.util.Arrays;
import java.util.Random;

public class Zobrist {
    private long[][] positions;
    private long black;
    private long[] castleRights; // WL, WR, BL, BR
    private long[] enpassant;

    public Zobrist() {
        init();
    }

    // todo: consider implementing own pseudo num generator
    public long hash(Board board) {
        long hash = 0L;
        Allegiance alg = board.whoseMove();
        for (int i = 0; i < 64; i++) {
            int piece = board.at(i);
            if (piece != -1) {
                hash ^= positions[i][piece];
            }
        }
        if (alg == Allegiance.BLACK)
            hash ^= black;
        hash ^= hashCastleRight(board.getCastleRight(Allegiance.WHITE), Allegiance.WHITE);
        hash ^= hashCastleRight(board.getCastleRight(Allegiance.BLACK), Allegiance.BLACK);
//        if (board.canCastleLeft(Allegiance.WHITE))
//            hash ^= castleRights[0];
//        if (board.canCastleRight(Allegiance.WHITE))
//            hash ^= castleRights[1];
//        if (board.canCastleLeft(Allegiance.BLACK))
//            hash ^= castleRights[2];
//        if (board.canCastleRight(Allegiance.BLACK))
//            hash ^= castleRights[3];

        long enpassantBB = board.getEnPassantBitboard();
        hash ^= hashEnpassantPawn(enpassantBB);

        return hash;
    }

    public long hashEnpassantPawn(long enpassantBB) {
        if (enpassantBB != 0L) {
            // works in opposite order but it shouldn't matter and it's more clear
            int file = Long.numberOfTrailingZeros(enpassantBB) % 8;
            return enpassant[file];
        }
        return 0L;
    }

    public long hashCastleRight(CastleRight cr, Allegiance alg) {
        int offset = alg == Allegiance.BLACK ? 2 : 0;

        switch (cr) {
            case ALL:
                return castleRights[offset] ^ castleRights[1 + offset];
            case LEFT:
                return castleRights[offset];
            case RIGHT:
                return castleRights[1 + offset];
            default:
                return 0L;
        }
    }

//    public long updateHash(long hash, Board board, Move move) {
//        int piece = move.getPiece();
//        hash ^= zobrist[move.fromSquare()][piece];
//        hash ^= zobrist[move.toSquare()][piece];
//        if (move.isAttack()) {
//            hash ^= zobrist[move.toSquare()][board.at(move.toSquare())]; //TODO: IMP: HAS TO BE HASHED BEFORE MOVETRANSITION
//        } // TODO: xoring nothing at source sq?!
//
//        return hash;
//    }

    public long zPositions(int sq, int piece) {
        return positions[sq][piece];
    }
    public long zSwitchMove() {
        return black;
    }
    public long zCastle(int idx) {
        return castleRights[idx];
    }
    public long zEnpassant(int file) {
        return enpassant[file];
    }

    private void init() {
        Random r = new Random();

        positions = new long[64][12];
        for (int i = 0; i < 64; i++) {
            for (int j = 0; j < 12; j++) {
                positions[i][j] = r.nextLong();
            }
        }
        System.out.println(Arrays.toString(positions[0]));
        black = r.nextLong();
        castleRights = new long[4];
        for (int i = 0; i < 4; i++) {
            castleRights[i] = r.nextLong();
        }
        enpassant = new long[8];
        for (int i = 0; i < 8; i++) {
            enpassant[i] = r.nextLong();
        }
    }

}
