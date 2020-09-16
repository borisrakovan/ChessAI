package ai;

import engine.*;
import ai.PieceSquareTables.*;
import java.util.List;

import static engine.Constants.*;

public class BoardEvaluator {
    /* TODO: moves queen too much possibly due to big mobility bonus; introduce development bonuses
     TODO: if i am left with a king only, i can get the ai to repeat moves unreasonably and not push pawns -> endgame handling
        TODO: ENPASSANT is not evaluated as attack -> isAttack || is Eps
        todo: INUFFICIENTMATERIAL
     */
    private final static int PIECE_VALUE_MUL = 100;
    private final static int PAWN_STRUCTURE_MUL = -50;
    private final static int MOBILITY_MUL = 2;
    private final static int CASTLED_BONUS = 50;
    private final static int CASTLE_RIGHT_PENALTY = -20;
    private final static int CHECKMATE_BONUS = Integer.MAX_VALUE;

    public BoardEvaluator() { }

    public int evaluateTerminalNode(Board board, int inverseDepth) {
        Allegiance alg = board.whoseMove();
        // calling this function means that the player to move has no moves left
        if (MoveGenerator.isInCheck(board, alg)) {
            int mul = alg == Allegiance.WHITE ? -1 : 1;
            return (CHECKMATE_BONUS - inverseDepth) * mul;
        }
        // else stalemate => draw:
        return 0;
    }

    public int evaluate(Board board, int inverseDepth) {
        Allegiance alg = board.whoseMove();
        List<Move> moves = MoveGenerator.legalMoves(board, alg);
        if (moves.isEmpty()) {
            return evaluateTerminalNode(board, inverseDepth);
        }
        int score = scorePlayer(board, Allegiance.WHITE) - scorePlayer(board, Allegiance.BLACK);
        return score;
    }
    // todo : move to 1 big method
    private int scorePlayer(Board board, Allegiance alg) {
//      should return value in range of +- three or four queens (270 - 360)
        return evaluatePieces(board, alg) +
                mobility(board, alg) +
                castling(board, alg) +
                check(board, alg) +
                attacks(board, alg) +
                pawnStructure(board, alg) +
                whoseMove(board, alg);
    }

    private int evaluatePieces(Board board, Allegiance alg) {
        int score = 0;
        int[] pieces = Pieces.getPieces(alg);

        for (int piece : pieces) {
            long bb = board.getBitboard(piece);
            while (bb != 0L) {
                int sq = Long.numberOfTrailingZeros(bb);
                score += pieceScore(piece, sq, board, alg);

                bb &= bb - 1;
            }
        }
        return score;
    }
    // todo: defended & attacked values
    // pawn walls
    // todo insuff materail
    private int pieceScore(int piece, int sq, Board board, Allegiance alg) {
        int score = 0;
        boolean endGame = board.endGamePhase();
        int idx = alg == Allegiance.WHITE ? 63 - sq : sq;
        score += Pieces.getValue(piece) * PIECE_VALUE_MUL;
        if (Pieces.isPawn(piece)) {
            if (sq % 8 == 0 || sq % 8 == 7) {
                if (endGame)
                    score -= 20; // rook pawns are worth less
                else
                    score -= 15;

            }
            score += PieceSquareTables.PAWN_TABLE[idx];
            // todo: are defending any piece?

        } else if (Pieces.isRook(piece)) {
            if (!endGame)
                score -= PIECE_VALUE_MUL;
        } else if (Pieces.isQueen(piece)) {
            int homeSq = alg == Allegiance.WHITE ? 4 : 60;
            if (sq == homeSq && !endGame)
                score -= 15;
        } else if (Pieces.isBishop(piece)) {
            if (Long.bitCount(board.getBitboard(piece)) > 1)
                score += 5;
            if (endGame)
                score += 10;
            score += PieceSquareTables.BISHOP_TABLE[idx];

        } else if (Pieces.isKnight(piece)) {
            if (endGame)
                score -= 10;
            score += PieceSquareTables.KNIGHT_TABLE[idx];

        } else if (Pieces.isKing(piece)) {
            // if few (<2) valid king moves, penalize
            if (endGame)
                score += PieceSquareTables.KING_TABLE_ENDGAME[idx];
            else
                score += PieceSquareTables.KING_TABLE[idx];
        }

        return score;
    }

    private int whoseMove(Board board, Allegiance alg) {
        if (board.whoseMove() == alg) {
            return 10;
        }
        return 0;
    }
    private int pawnStructure(Board board, Allegiance alg) {
        // todo passed pawns
        //- 0.5(D-D' + S-S' + I-I')
        // doubled pawns
        long pawns = board.getBitboard(Pieces.getPawn(alg));
        if (Long.bitCount(pawns) <= 1)
            return 0;
        int doubled, blocked = 0, isolated = 0;
        if (alg == Allegiance.WHITE) {
            doubled = Long.bitCount((pawns & (pawns << 8)));

        } else {
            doubled = Long.bitCount((pawns & (pawns >>> 8)));
        }
        long p = pawns;
        while (p != 0L) {
            int f = 7 - (Long.numberOfTrailingZeros(p) % 8);
            long left = f == 0 ? 0L : (FILES[f - 1] & pawns);
            long right = f == 7 ? 0L : (FILES[f + 1] & pawns);
            if (left == 0L &&  right == 0L) {
                isolated++;
            }
            p &= p - 1;
        }
        return PAWN_STRUCTURE_MUL*(doubled + blocked + isolated);
    }

    private int attacks(Board board, Allegiance alg) {

        return 0;
    }

    private int check(Board board, Allegiance alg) {
        int score = 0;

        if (MoveGenerator.isInCheck(board, alg)) {
            score = -70;
            if (board.endGamePhase()) {
                score -= 10;
            }
        }

        return score;
    }

    private int castling(Board board, Allegiance alg) {
        if (board.endGamePhase()) {
            return 0;
        }
        if (board.isCastled(alg))
            return CASTLED_BONUS;

        CastleRight cr = board.getCastleRight(alg);
        if (cr == CastleRight.NONE)
            return CASTLE_RIGHT_PENALTY * 2;
        else if (cr != CastleRight.ALL)
            return CASTLE_RIGHT_PENALTY;

        return 0;
    }

    private int mobility(Board board, Allegiance alg) {
        int pseudoLegals = 0;
        for (int piece : Pieces.getPieces(alg)) {
            long moves = MoveGenerator.pseudoLegalMoves(board, piece);
            pseudoLegals += Long.bitCount(moves);
        }

        return MOBILITY_MUL*pseudoLegals;
    }

}
