package engine;

public class Move {
    private int from;
    private int to;
    private int piece;
    private MoveType type;
    private int promotedPiece;
    private int score;

    public Move(int from, int to, int piece, MoveType type) {
        assert type != MoveType.PROMOTION_ATTACK && type != MoveType.PAWN_PROMOTION;
        this.from = from;
        this.to = to;
        this.piece = piece;
        this.type = type;
        this.score = -1;
    }

    public Move(int from, int to, int piece, MoveType type, int promotedPiece) {
        assert type == MoveType.PROMOTION_ATTACK || type == MoveType.PAWN_PROMOTION;
        this.from = from;
        this.to = to;
        this.piece = piece;
        this.type = type;
        this.promotedPiece = promotedPiece;
        this.score = -1;
    }

    @Override
    public String toString() {
//        return Constants.CHESS_NOTATION.get(63 - from) + " -> " + Constants.CHESS_NOTATION.get(63 - to);
        return Constants.chessNotation(from) + " -> " + Constants.chessNotation(to);
    }

    public boolean isPawnJump() {
        if (!Pieces.isPawn(piece))
            return false;

        Allegiance alg = Pieces.getAllegiance(piece);
        return (Board.toBitboard(from) & Constants.getPawnStartRank(alg)) != 0L
                && (Board.toBitboard(to) & Constants.getPawnJumpRank(alg)) != 0L;
    }

    public int fromSquare() {
        return from;
    }

    public int toSquare() {
        return to;
    }

    public void setPiece(int piece) {
        this.piece = piece;
    }

    public int getPiece() {
        return piece;
    }

    public boolean isAttack() { // ENPASSANT IS NOT CLASSIFIED AS ATTACK
        return type == MoveType.ATTACK || type == MoveType.PROMOTION_ATTACK;
    }

    public boolean isCastling() {
        return type == MoveType.CASTLE_LEFT || type == MoveType.CASTLE_RIGHT;
    }

    public boolean isPromotion() {
        return type == MoveType.PAWN_PROMOTION || type == MoveType.PROMOTION_ATTACK;
    }

    public int getPromotedPiece() {
        assert isPromotion();
        return promotedPiece;
    }

    public MoveType getType() {
        return type;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }
}
