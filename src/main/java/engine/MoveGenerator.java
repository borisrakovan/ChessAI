package engine;

import java.util.ArrayList;
import java.util.List;

import static engine.Allegiance.BLACK;
import static engine.Allegiance.WHITE;
import static engine.Constants.*;


public class MoveGenerator {
    private static final long[] KNIGHT_ATTACKS = initializeKnightAttacks();
    private static final long[] KING_ATTACKS = initializeKingAttacks();
    public static int i = 0;

    private MoveGenerator() { }


    public static List<Move> legalMoves(Board board, Allegiance alg) {
        i++;
        List<Move> legalMoves = new ArrayList<>();

        /* 1) is player in check evaluation */
        long checkers = getCheckingPiecesBB(board, alg);
        int numCheckers = Long.bitCount(checkers);
        int king = alg == WHITE ? Pieces.W_KING : Pieces.B_KING;
        int queen = alg == WHITE ? Pieces.W_QUEEN : Pieces.B_QUEEN;
        int kingSq = Long.numberOfTrailingZeros(board.getBitboard(king));

        assert numCheckers <= 2;

        /* 2) check position handling */
        if (numCheckers == 2) {
            return getLegalMovesRestricted(board, king, kingSq, ~kingDangerSquares(board, alg), FULL_BOARD, FULL_BOARD);
        }

        long pushMask = -1L;
        long captureMask = -1L;

        if (numCheckers == 1) {
            captureMask = checkers;
            assert Long.bitCount(captureMask) == 1;

            int sq = Long.numberOfTrailingZeros(checkers);
            int checker = board.at(sq);

            if (Pieces.isSlider(checker)) {
                pushMask = maskRayBetween(sq, kingSq, queen, false);
            } else {
                pushMask = 0L;
            }
        }

        long captureAndPushTemp = captureMask | pushMask;

        long allPinned = 0L;
        long kingSqSlidingFFs = attackedSquares(board, queen, true) & board.piecesOf(alg);
        if (kingSqSlidingFFs != 0) {
            for (int oppPiece : Pieces.getOppsPieces(alg)) {
                if (Pieces.isSlider(oppPiece)) {
                    long bitboard = board.getBitboard(oppPiece);
                    while (bitboard != 0L) {
                        long ray = maskRayBetween(kingSq, Long.numberOfTrailingZeros(bitboard), oppPiece, true);
                        if (Long.bitCount(ray & board.allPieces()) == 1){
//                            assert Pieces.getColor(board.at(Long.numberOfTrailingZeros(ray & board.allPieces()))) == alg;
                            allPinned |= ray & kingSqSlidingFFs;
                        }

                        bitboard &= bitboard - 1;
                    }
                }
            }
        }

        /* 4) all legal moves generation */

        for (int piece : Pieces.getPieces(alg)) {
            if (Pieces.isKing(piece))
                continue;

            long bitboard = board.getBitboard(piece);
            long pinned = bitboard & allPinned;

            if (pinned != 0L) {
//                System.out.println("pinned:");
//                Game.printBitboard(allPinned);
                long notPinned = bitboard & ~pinned;
//                legalMoves.addAll(getLegalMovesRestricted(board, piece, notPinned, FULL_BOARD));
                legalMoves.addAll(getLegalMovesRestricted(board, piece, notPinned, captureAndPushTemp, pushMask, captureMask));

                while (pinned != 0L) {
                    int sq = Long.numberOfTrailingZeros(pinned);

                    long pinRay = maskWholeRay(kingSq, sq); // FIXME urcite whole ray?

                    legalMoves.addAll(getLegalMovesRestricted(board, piece, sq, pinRay & captureAndPushTemp, pushMask, captureMask));

                    pinned &= pinned - 1;
                }
            }
            else {
                legalMoves.addAll(getLegalMovesRestricted(board, piece, bitboard, captureAndPushTemp, pushMask, captureMask));
            }
        }
        // we include king moves in either case
        long kingDangerSquares = kingDangerSquares(board, alg);
        legalMoves.addAll(getLegalMovesRestricted(board, king, kingSq, ~kingDangerSquares, FULL_BOARD, FULL_BOARD));

        /* castle move generation */
        if (numCheckers == 0) {
            long kingBB = board.getBitboard(king); //todo to delete
            long kingRank = alg == WHITE ? RANK_1 : RANK_8;
            if (board.canCastleRight(alg)) {
                assert kingBB == Constants.getKingInitPos(alg);
                long safePath = kingRank & (FILE_F | FILE_G);
                if ((safePath & board.allPieces()) == 0L && (safePath & kingDangerSquares) == 0L)
                    legalMoves.add(new Move(kingSq, kingSq - 2, king, MoveType.CASTLE_RIGHT));
            }
            if (board.canCastleLeft(alg)) {
                assert kingBB == Constants.getKingInitPos(alg);
                long safePath = kingRank & (FILE_C | FILE_D);

                long freePath = kingRank & (FILE_B | FILE_C | FILE_D);
                if ((freePath & board.allPieces()) == 0L && (safePath & kingDangerSquares) == 0L)
                    legalMoves.add(new Move(kingSq, kingSq + 2, king, MoveType.CASTLE_LEFT));
            }
        }
        return legalMoves;
    }

    public static boolean isInCheck(Board board, Allegiance kingColor) {
        int king = kingColor == WHITE ? Pieces.W_KING : Pieces.B_KING;

        for (int oppPiece : Pieces.getOppsPieces(kingColor)) {
            if (oppPiece != Pieces.getOppsEquivalent(king)) {
                long kingSqAttacks = attackedSquares(board, Pieces.getOppsEquivalent(oppPiece), true);
                if ((kingSqAttacks & board.getBitboard(oppPiece)) != 0L) {
                    return true;
                }
            }
        }
        return false;
    }

    public static long getCheckingPiecesBB(Board board, Allegiance kingColor) {
        long checkers = 0L;
        int king = kingColor == WHITE ? Pieces.W_KING : Pieces.B_KING;
//        int kingSq = Long.numberOfTrailingZeros(kingBB);

        for (int oppPiece : Pieces.getOppsPieces(kingColor)) {
            if (oppPiece != Pieces.getOppsEquivalent(king)) {
                long kingSqAttacks = attackedSquares(board, Pieces.getOppsEquivalent(oppPiece), true);
                checkers |= kingSqAttacks & board.getBitboard(oppPiece);
            }
        }
        return checkers;
    }

    /**
     * only looks at push and capture mask in case of enpassant
     * **/
    private static List<Move> getLegalMovesRestricted(Board board, int piece, long bitboard, long restriction, long pushMask, long captureMask) {
        List<Move> legalMoves = new ArrayList<>();

        while (bitboard != 0L) {
            int sq = Long.numberOfTrailingZeros(bitboard);
            legalMoves.addAll(getLegalMovesRestricted(board, piece, sq, restriction, pushMask, captureMask));

            bitboard &= bitboard - 1;
        }

        return legalMoves;
    }

    private static List<Move> getLegalMovesRestricted(Board board, int piece, int sq, long restriction, long pushMask, long captureMask) {
        List<Move> legalMoves = new ArrayList<>();

        long legals = pseudoLegalMoves(board, piece, sq) & restriction;

        /* ENPASSANT & PROMOTIONS moves */
        if (Pieces.isPawn(piece)) {
            Move eppMove = generateEnpassantMove(board, piece, sq, restriction, pushMask, captureMask);
            if (eppMove != null)
                legalMoves.add(eppMove);

            else if ((legals & Constants.getPawnPromotionRank(Pieces.getAllegiance(piece))) != 0L) {
                while (legals != 0L) {
                    Move move;
                    int toSq = Long.numberOfTrailingZeros(legals);
                    if (board.isOccupied(toSq))
                        legalMoves.addAll(generatePromotionMoves(sq, toSq, piece, MoveType.PROMOTION_ATTACK));
                    else
                        legalMoves.addAll(generatePromotionMoves(sq, toSq, piece, MoveType.PAWN_PROMOTION));

                    legals &= legals - 1;
                }
                return legalMoves;
            }
        }
        /* BASIC MOVES */
        while (legals != 0L) {
            int toSq = Long.numberOfTrailingZeros(legals);

            Move move;
            if (board.isOccupied(toSq))
                move = new Move(sq, toSq, piece, MoveType.ATTACK);
            else
                move = new Move(sq, toSq, piece, MoveType.NORMAL);

            legalMoves.add(move);
            legals &= legals - 1;
        }

        return legalMoves;
    }

    private static List<Move> generatePromotionMoves(int sq, int toSq, int piece, MoveType promotionType) {
        List<Move> promotions = new ArrayList<>();
        int black = Pieces.getAllegiance(piece) == BLACK ? 6 : 0;
        promotions.add(new Move(sq, toSq, piece, promotionType, Pieces.W_QUEEN + black));
        promotions.add(new Move(sq, toSq, piece, promotionType, Pieces.W_ROOK + black));
        promotions.add(new Move(sq, toSq, piece, promotionType, Pieces.W_KNIGHT + black));
        promotions.add(new Move(sq, toSq, piece, promotionType, Pieces.W_BISHOP + black));
        return promotions;
    }

    private static Move generateEnpassantMove(Board board, int piece, int sq, long restriction, long pushMask, long captureMask) {
        Move eppMove = null;
        long pawnBB = 1L << sq;
        long oppPawnJumpRank = Constants.getPawnJumpRank(Allegiance.not(Pieces.getAllegiance(piece)));
        long enpassantBB = board.getEnPassantBitboard();
        if (enpassantBB != 0L && (pawnBB & oppPawnJumpRank) != 0L) {
            assert (oppPawnJumpRank & enpassantBB) != 0L;
            if (pawnBB == (enpassantBB << 1) || pawnBB == (enpassantBB >>> 1)) {
//                System.out.println("hurray!");
//                Game.printBitboard(pushMask);
//                Game.printBitboard(captureMask);
                int fromSq = Long.numberOfTrailingZeros(pawnBB);
                long toSqBB = Pieces.getAllegiance(piece) == WHITE ? (enpassantBB << 8) : (enpassantBB >>> 8);
                int toSq = Long.numberOfTrailingZeros(toSqBB);
                // TODO:
                // FIXME: BUG HERE, NOT WORKING FOR CAPTURE MASK
                if ((pushMask & toSqBB & restriction) != 0L && (captureMask & enpassantBB) != 0L)
                    eppMove = new Move(fromSq, toSq, piece, MoveType.ENPASSANT);
//                else
//                    System.err.println("ENPASSANT BUSTED!");
            }
        }
        return eppMove;
    }

    private static long kingDangerSquares(Board board, Allegiance kingColor) { // todo dbcheck the slider gotcha
        long kingDangerSq = 0L;
        Board boardCopy = new Board(board);
        // we have to remove the king when looking for king danger squares, as the king can't move outwards a sliding piece
        boardCopy.removeKing(kingColor);

        int[] opponentPieces = kingColor == WHITE ? Pieces.BLACK_PIECES : Pieces.WHITE_PIECES;

        for (int pieceType : opponentPieces) {
            kingDangerSq |= attackedSquares(boardCopy, pieceType, false);
        }

        return kingDangerSq;
    }

    // TODO SHOULD ALSO BE TRANSFORMED INTO INDIVIDUAL PIECE  MOVE GENERATION
    private static long attackedSquares(Board board, int pieceType, boolean fromKingSq) {
        long attacks = 0L;
        long squares;
        long bitboard;
        if (fromKingSq) {
            int king = Pieces.isWhite(pieceType) ? Pieces.W_KING : Pieces.B_KING;
            bitboard = board.getBitboard(king);
            squares = board.getBitboard(king);
            assert Long.bitCount(squares) == 1;
        }
        else {
            squares = board.getBitboard(pieceType);
            bitboard = board.getBitboard(pieceType);
        }
        long myPieces, oppPieces;
        if (Pieces.isWhite(pieceType)) {
            myPieces = board.whitePieces();
            oppPieces = board.blackPieces();
        } else {
            myPieces = board.blackPieces();
            oppPieces = board.whitePieces();
        }

        switch (pieceType) {
            case Pieces.W_PAWN :
                attacks |= ((bitboard & getMaskForFiles(1, 7)) << 9);
                attacks |= ((bitboard & getMaskForFiles(0, 6)) << 7);
                break;

            case Pieces.B_PAWN :
                attacks |= ((bitboard & getMaskForFiles(1, 7)) >>> 7);
                attacks |= ((bitboard & getMaskForFiles(0, 6)) >>> 9);
                break;

            case Pieces.W_KING : // maximum of 1 king on the board
            case Pieces.B_KING :
                assert Long.bitCount(bitboard) == 1;
                int sq = Long.numberOfTrailingZeros(bitboard);
                attacks |= MoveGenerator.KING_ATTACKS[sq];
                break;

            case Pieces.W_KNIGHT :
            case Pieces.B_KNIGHT :
                while (squares != 0L) {
                    sq = Long.numberOfTrailingZeros(squares);
                    attacks |= MoveGenerator.KNIGHT_ATTACKS[sq];
                    squares &= squares - 1;
                }
                break;

            case Pieces.W_ROOK :
            case Pieces.B_ROOK :
                while (squares != 0L) {
                    sq = Long.numberOfTrailingZeros(squares);
                    attacks |= maskRookRays(sq, myPieces, oppPieces, true);
                    squares &= squares - 1;
                }
                break;

            case Pieces.W_BISHOP :
            case Pieces.B_BISHOP :
                while (squares != 0L) {
                    sq = Long.numberOfTrailingZeros(squares);
                    attacks |= maskBishopRays(sq, myPieces, oppPieces, true);
                    squares &= squares - 1;
                }
                break;

            case Pieces.W_QUEEN :
            case Pieces.B_QUEEN :
                while (squares != 0L) {
                    sq = Long.numberOfTrailingZeros(squares);
                    attacks |= maskBishopRays(sq, myPieces, oppPieces, true) |
                            maskRookRays(sq, myPieces, oppPieces, true);
                    squares &= squares - 1;
                }
                break;

            default:
                throw new RuntimeException("Operation not supported yet.");
        }

        return attacks;
    }

    public static long pseudoLegalMoves(Board board, int piece) {
        long moves = 0L;
        long bitboard = board.getBitboard(piece);
        while (bitboard != 0L) {
            int sq = Long.numberOfTrailingZeros(bitboard);
            moves |= pseudoLegalMoves(board, piece, sq);
            bitboard &= bitboard - 1;
        }
        return moves;
    }

    private static long pseudoLegalMoves(Board board, int piece, int sq) {
        long legals = 0L;
        long bitboard = 1L << sq;

        long allPieces = board.allPieces();
        long whitePieces = board.whitePieces();
        long blackPieces = board.blackPieces();
        Allegiance alg = Pieces.getAllegiance(piece);
        long myPieces = alg == WHITE ? whitePieces : blackPieces;
        long oppPieces = alg == WHITE ? blackPieces : whitePieces;

        switch (piece) {
            case Pieces.W_PAWN:
                // normal moves
                legals |= bitboard << 8 & ~allPieces;

//                if ((bitboard & ((RANK_3 & ~allPieces) >>> 8)) != 0L)
                legals |= ((bitboard & ((RANK_3 & ~allPieces) >>> 8)) << 16) & ~allPieces;

                //attacks
                legals |= ((bitboard & getMaskForFiles(1, 7)) << 9) & blackPieces;
                legals |= ((bitboard & getMaskForFiles(0, 6)) << 7) & blackPieces;
                break;

            case Pieces.B_PAWN:
                // normal moves
                legals |= bitboard >>> 8 & ~allPieces;
//                if ((bitboard & ((RANK_6 & ~allPieces) << 8)) != 0L)
                legals |=  ((bitboard & ((RANK_6 & ~allPieces) << 8)) >>> 16) & ~allPieces;

                //attacks
                legals |= ((bitboard & getMaskForFiles(1, 7)) >>> 7) & whitePieces;
                legals |= ((bitboard & getMaskForFiles(0, 6)) >>> 9) & whitePieces;
                break;

            case Pieces.W_KING: // maximum of 1 king on the board
            case Pieces.B_KING:
                legals |= KING_ATTACKS[sq] & ~myPieces;
                break;

            case Pieces.W_KNIGHT:
            case Pieces.B_KNIGHT:
                legals |= MoveGenerator.KNIGHT_ATTACKS[sq] & ~myPieces;
                break;

            case Pieces.W_ROOK:
            case Pieces.B_ROOK:
                legals |= maskRookRays(sq, myPieces,  oppPieces, false);
                break;

            case Pieces.W_BISHOP:
            case Pieces.B_BISHOP:
                legals |= maskBishopRays(sq, myPieces,  oppPieces, false);
                break;

            case Pieces.W_QUEEN:
            case Pieces.B_QUEEN:
                legals |= maskBishopRays(sq, myPieces,  oppPieces, false) |
                        maskRookRays(sq, myPieces, oppPieces, false);
                break;

            default:
                throw new RuntimeException("Operation not supported yet.");
        }

        return legals;
    }



    private static long maskRookRays(int sq, long myPieces, long enemyPieces, boolean friendlyFire) {

        long mask = 0L;
        mask |= maskRay(sq, 0, 1, myPieces, enemyPieces, friendlyFire);
        mask |= maskRay(sq, 0, -1, myPieces, enemyPieces, friendlyFire);
        mask |= maskRay(sq, 1, 0, myPieces, enemyPieces, friendlyFire);
        mask |= maskRay(sq, -1, 0, myPieces, enemyPieces, friendlyFire);
        return mask;
    }
    private static long maskBishopRays(int sq, long myPieces, long enemyPieces, boolean friendlyFire) {

        long mask = 0L;
        mask |= maskRay(sq, 1, 1, myPieces, enemyPieces, friendlyFire);
        mask |= maskRay(sq, 1, -1, myPieces, enemyPieces, friendlyFire);
        mask |= maskRay(sq, -1, 1, myPieces, enemyPieces, friendlyFire);
        mask |= maskRay(sq, -1, -1, myPieces, enemyPieces, friendlyFire);
        return mask;
    }

    /**
     * somewhat hard-to-understand method, masks existing ray between any 2 squares if axisPiece is queen
     * if axis piece is rook or bishop it only masks existing straight of diagonal rays respectively
     * **/
    private static long maskRayBetween(int sq1, int sq2, int axisPiece, boolean robust) {
        int x1 = sq1 % 8;
        int y1 = sq1 / 8;
        int x2 = sq2 % 8;
        int y2 = sq2 / 8;

        int dx = signum(x2 - x1);
        int dy = signum(y2 - y1);

        long mask = 0L;

        if (!Pieces.isSlider(axisPiece))
            throw new RuntimeException();

        if ((Pieces.isRook(axisPiece) && !isRookAxis(dx, dy)) ||
            (Pieces.isBishop(axisPiece) && !isBishopAxis(dx, dy))) {
            return 0L;
        }

        while (true) {
            x1 += dx;
            y1 += dy;

            if (x1 == x2 && y1 == y2)
                break;
            if (!isOnBoard(x1, y1)) {
                if (robust)
                    return 0L;
                throw new RuntimeException("function called with unreasonable args, bad implementation of calling function");
            }

            mask |= 1L << y1 * 8 + x1;
        }

        return mask;
     }

    private static boolean isBishopAxis(int dx, int dy) {
        return (dx + dy) % 2 == 0;
    }

    private static boolean isRookAxis(int dx, int dy) {
        return (dx + dy) % 2 != 0;
    }

    private static int signum(int x) {
        if (x < 0)
            return -1;
        else if (x > 0)
            return 1;
        return 0;
    }

    public static long maskWholeRay(int sq1, int sq2) {
        int x1 = sq1 % 8;
        int y1 = sq1 / 8;
        int x2 = sq2 % 8;
        int y2 = sq2 / 8;

        int dx = signum(x2 - x1);
        int dy = signum(y2 - y1);

        long mask = 0L;

        int x = x1;
        int y = y1;
        boolean passed = false;
        while (isOnBoard(x, y)) {
            mask |= 1L << y * 8 + x;

            if (x == x2 && y == y2)
                passed = true;

            x += dx;
            y += dy;
        }
        dx *= -1;
        dy *= -1;
        x = x1 + dx;
        y = y1 + dy;
        while (isOnBoard(x, y)) {
            mask |= 1L << y * 8 + x;

            if (x == x2 && y == y2)
                passed = true;

            x += dx;
            y += dy;
        }
        if (!passed) {
            throw new RuntimeException("function called with unreasonable args, bad implementation of calling function");
        }

        return mask;
    }
    private static long maskRay(int sq, int dx, int dy, long myPieces, long enemyPieces, boolean friendlyFire) {
        int x = sq % 8;
        int y = sq / 8;

        long mask = 0L;
        int newSq;
        while (true) {
            x += dx;
            y += dy;
            if (isOnBoard(x, y)) {
                newSq = y * 8 + x;
                if (!friendlyFire && ((myPieces >>> newSq) & 1) == 1) {
                    break;
                }
                mask |= 1L << newSq;

                if (((enemyPieces >>> newSq) & 1) == 1 || ((myPieces >>> newSq) & 1) == 1) {
                    break;
                }
            }
            else break;
        }
        return mask;
    }

    private static boolean isOnBoard(int x, int y) {
        return x >= 0 && x < 8 && y >= 0 && y < 8;
    }


    private static long[] initializeKingAttacks() {
        long[] kingAttacks = new long[64];

        for (int sq = 0; sq < 64; sq++) {
            long m = 1L << sq;
            long mask = (((m >>> 1) | (m << 7) | (m >>> 9)) & getMaskForFiles(1, 7)) |
                    (((m <<  1) | (m << 9) | (m >>> 7)) & getMaskForFiles(0, 6)) |
                    (m << 8) | (m >>> 8);
            kingAttacks[sq] = mask;
        }

        return kingAttacks;
    }


    private static long[] initializeKnightAttacks() {
        long[] knightAttacks = new long[64];
        for (int sq = 0; sq < 64; sq++) {
            long m = 1L << sq;
            long mask = (((m << 6) | (m >>> 10)) & getMaskForFiles(2, 7) |
                    (((m << 15) | (m >>> 17)) & getMaskForFiles(1, 7))  |
                    (((m << 17) | (m >>> 15)) & getMaskForFiles(0, 6))|
                    (((m << 10) | (m >>>  6)) &  getMaskForFiles(0, 5)));
            knightAttacks[sq] = mask;
        }
        return knightAttacks;
    }

}

