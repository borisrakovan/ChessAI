package ai;

import engine.Board;
import engine.Move;

public interface MoveStrategy {

    Move execute(Board board);
}

