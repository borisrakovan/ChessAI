package engine;

public enum MoveType {
    NORMAL,
    ATTACK,
    ENPASSANT,
    CASTLE_LEFT,
    CASTLE_RIGHT,
    // todo what about promotion to attack? possible problems w/ move-highlighting
    PAWN_PROMOTION,
    PROMOTION_ATTACK
}
