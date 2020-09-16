package engine;

import ai.Zobrist;
import engine.players.AIPlayer;
import engine.players.HumanPlayer;
import engine.players.MinimaxPlayer;
import engine.players.Player;

import java.util.ArrayList;
import java.util.List;

import static engine.Allegiance.BLACK;
import static engine.Allegiance.WHITE;
import static engine.Constants.*;

public class Board {
    private long[] bitboards;
    private long enPassantBitboard;
    private Allegiance nextToMove;
    private CastleRight castleRightWhite;
    private CastleRight castleRightBlack;
    private Player whitePlayer;
    private Player blackPlayer;
    private int moveCount;
    private boolean whiteCastled;
    private boolean blackCastled;
    private List<Move> moveLog; // TODO: only used in the main board, can't be accessed from elswhere
    private long hash;
    private Zobrist zobrist;
    private boolean endGamePhase;

    public Board() {
        createStandardBoard();
        whitePlayer = new HumanPlayer(this, WHITE);
//        whitePlayer = new AIPlayer(this, WHITE, false);
        blackPlayer = new AIPlayer(this, BLACK, true);
//        blackPlayer = new HumanPlayer(this, BLACK); // todo comp to other? constructors
        nextToMove = Allegiance.WHITE;
        castleRightWhite = CastleRight.ALL;
        castleRightBlack = CastleRight.ALL;
        enPassantBitboard = 0L;
        moveCount = 0;
        whiteCastled = false;
        blackCastled = false;
        moveLog = new ArrayList<>();
        zobrist = new Zobrist();
        hash = zobrist.hash(this); // fixme :? calling methods when not initialized
        endGamePhase = false;
    }

    // copying constructor
    public Board(Board other) {
        this.bitboards = new long[Pieces.NUM_PIECES];
        System.arraycopy(other.bitboards, 0, this.bitboards, 0, Pieces.NUM_PIECES);
        whitePlayer = new HumanPlayer(this, Allegiance.WHITE);
        blackPlayer = new HumanPlayer(this, Allegiance.BLACK); // todo can be safe-deleted?
        this.nextToMove = other.nextToMove;
        this.castleRightWhite = other.castleRightWhite;
        this.castleRightBlack = other.castleRightBlack;
        this.enPassantBitboard = other.enPassantBitboard;
        this.moveCount = other.moveCount;
        this.whiteCastled = other.whiteCastled;
        this.blackCastled = other.blackCastled;
        this.zobrist = other.zobrist;
        this.hash = other.hash;
        this.endGamePhase = other.endGamePhase;
    }

    public Board(boolean empty, Allegiance nextToMove, CastleRight crBlack, CastleRight crWhite,
                 long enPassantBitboard, int moveCount) {
        bitboards = new long[Pieces.NUM_PIECES];
        for (int i = 0; i < Pieces.NUM_PIECES; i++) {
            bitboards[i] = 0L;
        }
        whitePlayer = new HumanPlayer(this, Allegiance.WHITE);
        blackPlayer = new HumanPlayer(this, Allegiance.BLACK);
        this.nextToMove = nextToMove;
        this.castleRightBlack = crBlack;
        this.castleRightWhite = crWhite;
        this.enPassantBitboard = enPassantBitboard;
        this.moveCount = moveCount;
//        this.whiteCastled = false; // todo needed?
//        this.blackCastled = false;
        // todo no hash, no zobrist, no anything
    }

    public void createStandardBoard() {
        bitboards = new long[Pieces.NUM_PIECES];

        bitboards[Pieces.W_ROOK] = Constants.WHITE_ROOKS_INIT_POS;
        bitboards[Pieces.W_KNIGHT] = Constants.WHITE_KNIGHTS_INIT_POS;
        bitboards[Pieces.W_BISHOP] = Constants.WHITE_BISHOPS_INIT_POS;
        bitboards[Pieces.W_KING] = Constants.WHITE_KING_INIT_POS;
        bitboards[Pieces.W_QUEEN] = Constants.WHITE_QUEEN_INIT_POS;
        bitboards[Pieces.W_PAWN] = Constants.WHITE_PAWNS_INIT_POS;

        bitboards[Pieces.B_ROOK] = Constants.BLACK_ROOKS_INIT_POS;
        bitboards[Pieces.B_KNIGHT] = Constants.BLACK_KNIGHTS_INIT_POS;
        bitboards[Pieces.B_BISHOP] = Constants.BLACK_BISHOPS_INIT_POS;
        bitboards[Pieces.B_KING] = Constants.BLACK_KING_INIT_POS;
        bitboards[Pieces.B_QUEEN] = Constants.BLACK_QUEEN_INIT_POS;
        bitboards[Pieces.B_PAWN] = Constants.BLACK_PAWNS_INIT_POS;
    }

    /** this has to be the only possible way to execute a move on a board **/
    public void makeMoveTransition(Move move) {

        assert Long.bitCount(enPassantBitboard) == 0 || Long.bitCount(enPassantBitboard) == 1;

        // reset epBB
        hash ^= zobrist.hashEnpassantPawn(enPassantBitboard);
        enPassantBitboard = 0L;

        Allegiance alg = Pieces.getAllegiance(move.getPiece());
        int piece = move.getPiece();

        /* castle rights handling */
        if (Pieces.isKing(piece)) {
            setCastleRight(CastleRight.NONE, alg);
        }

        /* enpassant pawn setting */
        else if (Pieces.isPawn(piece)) {
            if (move.isPawnJump()) {
                enPassantBitboard |= toBitboard(move.toSquare());
                hash ^= zobrist.hashEnpassantPawn(enPassantBitboard);
            }
        }
        /* BOARD TRANSITION HANDLING */
        if (move.isAttack()) {
            removePiece(at(move.toSquare()), move.toSquare());
        }

        if (move.getType() == MoveType.ENPASSANT) {
            int epPawnSq = alg == WHITE ? (move.toSquare() - 8) : (move.toSquare() + 8);
            removePiece(Pieces.getOppsEquivalent(piece), epPawnSq);
        }

        else if (move.isCastling()) {
            setCastleRight(CastleRight.NONE, alg);
            setCastled(alg);
            int rook = alg == WHITE ? Pieces.W_ROOK : Pieces.B_ROOK;

            if (move.getType() == MoveType.CASTLE_LEFT) {
                int pos = alg == WHITE ? 7 : 63;
                removePiece(rook, pos);
                putPiece(rook, pos - 3);
            }
            else {
                int pos = alg == WHITE ? 0 : 56;
                removePiece(rook, pos);
                putPiece(rook, pos + 2);
            }
        }

        removePiece(piece, move.fromSquare());
        int pieceToPut = move.isPromotion() ? move.getPromotedPiece() : move.getPiece();
        putPiece(pieceToPut, move.toSquare());

        /* castle rights update */
        updateCastleRights(WHITE);
        updateCastleRights(BLACK);

        if (moveLog != null)
            moveLog.add(move);

        if (!endGamePhase) {
            if (numOfPieces() < 10)
                endGamePhase = true;
        }
        moveCount++;
        nextTurn();
    }

    private void updateCastleRights(Allegiance alg) {
        int rook = alg == WHITE ? Pieces.W_ROOK : Pieces.B_ROOK;
        long rookBB = getBitboard(rook);
        CastleRight curCR = getCastleRight(alg);

        if ((rookBB & getLeftRookMask(alg)) == 0L) {
            CastleRight newCR = (curCR == CastleRight.LEFT || curCR == CastleRight.NONE) ? CastleRight.NONE : CastleRight.RIGHT;
            setCastleRight(newCR, alg);
        }
        curCR = getCastleRight(alg);
        if ((rookBB & getRightRookMask(alg)) == 0L) {
            CastleRight newCR = (curCR == CastleRight.RIGHT || curCR == CastleRight.NONE) ? CastleRight.NONE : CastleRight.LEFT;
            setCastleRight(newCR, alg);
        }
    }
    // todo should only be called when performance doesn't matter (GUI move executed), what about board eval tho?
    // maybe optimize moveGen for bitboards only? or for this particular purpose
    public Flag getGameState() {
        List<Move> moves = MoveGenerator.legalMoves(this, nextToMove);
        if (moves.isEmpty()) {
            if (MoveGenerator.isInCheck(this, nextToMove))
                return Flag.CHECKMATE;
            return Flag.STALEMATE;
        } // CHECK TOO?
        if (!hasEnoughPieces(WHITE) && !hasEnoughPieces(BLACK))
            return Flag.INSUFFICIENT_MATERIAL;
        return Flag.DONE;
    }

    private boolean hasEnoughPieces(Allegiance alg) {
        int pawn = Pieces.getPawn(alg);
        int queen = Pieces.getQueen(alg);
        int rook = Pieces.getRook(alg);

        if (getBitboard(pawn) != 0L || getBitboard(queen) != 0L || getBitboard(rook) != 0L)
            return true;
        int value = Long.bitCount(getBitboard(Pieces.getBishop(alg))) * 3 +
                Long.bitCount(getBitboard(Pieces.getKnight(alg))) * 3;
        return value > 3;
    }

    public boolean isOccupied(int square) {
        if (at(square) != -1)
            return true;

        return false;
    }

    public int at(int square) {
        if (square < 0 || square >= 64)
            throw new RuntimeException();

        for (int i = 0; i < Pieces.NUM_PIECES; i++)
            if (((bitboards[i] >> square) & 1L) == 1)
                return i;

        return -1;
    }

    public void putPiece(int piece, int position) {
        if (piece < 0 || piece >= Pieces.NUM_PIECES || position < 0 || position >= 64)
            throw new RuntimeException("invalid arguments");

        long allPieces = allPieces();

        if (((allPieces >>> position) & 1) == 1L) {
            throw new RuntimeException("position " + position + " already occupied by " + at(position));
        }
        bitboards[piece] |= 1L << position;
        hash ^= zobrist.zPositions(position, piece);
    }

    public void removeKing(Allegiance alg) {
        int king = alg == Allegiance.WHITE ? Pieces.W_KING : Pieces.B_KING;
        long bitboard = getBitboard(king);
        assert Long.bitCount(bitboard) == 1;

        // numberOfTrailingZeros == position
        removePiece(king, Long.numberOfTrailingZeros(bitboard));
    }

    public void removePiece(int piece, int position) {
        if (position < 0 || position >= 64) {
            throw new RuntimeException("bad arguments: " + position);
        }
        assert at(position) == piece;
        bitboards[piece] &= ~(1L << position);
        hash ^= zobrist.zPositions(position, piece);
    }

    public long getBitboard(int piece) {
        return bitboards[piece];
    }

    public long getHash() {
        return hash;
    }

    public long allPieces() {
        return whitePieces() | blackPieces();
    }

    public long piecesOf(Allegiance alg) {
        return alg == Allegiance.WHITE ? whitePieces() : blackPieces();
    }

    public long whitePieces() {
        long whitePieces = 0L;
        for (int i = 0; i < Pieces.NUM_PIECES / 2 ; i++) {
            whitePieces |= bitboards[i];
        }
        return whitePieces;
    }

    public long blackPieces() {
        long blackPieces = 0L;
        for (int i = Pieces.NUM_PIECES / 2; i < Pieces.NUM_PIECES; i++) {
            blackPieces |= bitboards[i];
        }
        return blackPieces;
    }

    public boolean isCastled(Allegiance alg) {
        return alg == WHITE ? whiteCastled : blackCastled;
    }
    public void setCastled(Allegiance alg) {
        if (alg == WHITE)
            whiteCastled = true;
        else
            blackCastled = true;
    }

    public List<Move> getMoveLog() {
        return moveLog;
    }
    public Move getLastMove() {
        if (moveLog == null)
            throw new RuntimeException("Accessing movelog when uninitialized.");
        if (moveLog.size() == 0)
            return null;
        return moveLog.get(moveLog.size() - 1);
    }

    public Player currentPlayer() {
        return nextToMove == Allegiance.WHITE ? whitePlayer : blackPlayer;
    }
    public Player opponentPlayer() {
        return nextToMove == Allegiance.WHITE ? blackPlayer : whitePlayer;
    }

    private void nextTurn() {
        this.nextToMove = nextToMove == Allegiance.WHITE ? Allegiance.BLACK : Allegiance.WHITE;
        hash ^= zobrist.zSwitchMove();
    }

    public int numOfPieces() {
        return Long.bitCount(allPieces());
    }

    private void setCastleRight(CastleRight castleRight, Allegiance alg) {
        hash ^= zobrist.hashCastleRight(getCastleRight(alg), alg);
        if (alg == WHITE)
            castleRightWhite = castleRight;
        else
            castleRightBlack = castleRight;
        hash ^= zobrist.hashCastleRight(castleRight, alg);

    }

    public CastleRight getCastleRight(Allegiance alg) {
        return alg == Allegiance.WHITE ? castleRightWhite : castleRightBlack;
    }

    public boolean canCastleRight(Allegiance alg) {
        CastleRight cr = getCastleRight(alg);
        return cr == CastleRight.ALL || cr == CastleRight.RIGHT;
    }
    public boolean canCastleLeft(Allegiance alg) {
        CastleRight cr = getCastleRight(alg);
        return cr == CastleRight.ALL || cr == CastleRight.LEFT;
    }

    public static long toBitboard(int square) {
        if (square < 0 || square >= 64)
            throw new RuntimeException();
        return (1L << square);
    }

    public long getEnPassantBitboard() {
        return enPassantBitboard;
    }

    public int getMoveCount() {
        return moveCount;
    }

    public boolean isComputerGame() {
        return whitePlayer.isComputer() && blackPlayer.isComputer();
    }

    public Allegiance whoseMove() {
        return nextToMove;
    }

    public boolean endGamePhase() {
        return moveCount > 30;
    }
}
