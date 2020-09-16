package engine.players;

import engine.Board;
import engine.Allegiance;

public abstract class Player {
    protected Board board;
    protected Allegiance alg;
//    private boolean isCastled;
//    private boolean isInCheck;
//    private boolean isInCheckMate; fixme not supported by board copy constructor yet

    public Player(Board board, Allegiance alg) {
        this.alg = alg;
        this.board = board;
    }

    public Allegiance getAllegiance() {
        return alg;
    }

    public abstract boolean isComputer();
}
