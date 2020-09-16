package ai;

import engine.*;

import java.util.Comparator;
import java.util.List;

public class AlphaBeta implements MoveStrategy {
    private static final int NUM_KILLERS = 2;
    private BoardEvaluator boardEvaluator;
//    private TranspositionTable tt;
    private int depth;
    private int i;
    Move[][] killers;

    public AlphaBeta(int depth) {
        this.boardEvaluator = new BoardEvaluator();
        this.depth = depth;
        this.i = 0;
        this.killers = new Move[depth][NUM_KILLERS];
//        this.tt = new TranspositionTable();
    }

    public Move execute(Board board) {
        long startTime = System.currentTimeMillis();
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        Allegiance alg = board.whoseMove();
        List<Move> moves = MoveGenerator.legalMoves(board, alg);
        sortMoves(moves, board, depth);

        Move bestMove = null;
        if (alg == Allegiance.WHITE) {
            for (Move move : moves) {
                Board newBoard = new Board(board);
                newBoard.makeMoveTransition(move);
                int score = alphaBeta(newBoard, 1, false, alpha, beta);
                if (score > alpha) {
                    alpha = score;
                    bestMove = move;
                }
            }
        } else {
            for (Move move : moves) {
                Board newBoard = new Board(board);
                newBoard.makeMoveTransition(move);
                int score = alphaBeta(newBoard, 1, true, alpha, beta);
                if (score < beta) {
                    beta = score;
                    bestMove = move;
                }
            }
        }
        assert bestMove != null;
        long executionTime = System.currentTimeMillis() - startTime;
        long s = (long)Math.ceil((double)executionTime/1000d);
        System.out.println("Alphabeta returned in " + s + " seconds");
        System.out.println(i + " nodes evaluated.");
        System.out.println("Speed: " + (i/(1000*s)) + "K nodes per second");
//        System.out.println(tt.getHits() + " transposition table hits.");
//        System.out.println(tt.getMisses() + " transposition table misses.");
//        System.out.println(tt.getCollisions() + " transposition table collisions.\n");
        i = 0;
//        tt.clear();
        killers = new Move[depth][NUM_KILLERS];
        return bestMove;
    }
    // TODO HERE:::: reset tt after each move?!!! asnwer: empirical data suggest that it is better.
    // todo make use of other fields in tt entry, finish implementing tt use totally
    // todo RESTORE ALPHABETA W/O TT AND MAKE SURE IT PLAYS THE SAME MOVES
    // todo there is way too many collisions, ponder about birthday rule
    // todo might consider using stack to store moves

    private int alphaBeta(Board board, int depth, boolean max, int alpha, int beta) {
        i++;
        if (depth == this.depth)
            return boardEvaluator.evaluate(board, depth);

        Allegiance alg = board.whoseMove();
        List<Move> moves = MoveGenerator.legalMoves(board, alg);
        if (moves.isEmpty()) { // either checkmate or stalemate
            return boardEvaluator.evaluateTerminalNode(board, depth);
        }
        if (depth < 5)
            sortMoves(moves, board, depth);

        if (max) { // Maximizing player
            for (Move move : moves) {
                Board newBoard = new Board(board);
                newBoard.makeMoveTransition(move);

                int score = alphaBeta(newBoard, depth + 1, false, alpha, beta);
                if (score >= beta) {
                    killers[depth][1] = killers[depth][0];
                    killers[depth][0] = move;
                    return beta; // cut-off
                }

                alpha = Math.max(score, alpha);
            }
            return alpha;
        } else { // Minimizing player
            for (Move move : moves) {
                Board newBoard = new Board(board);
                newBoard.makeMoveTransition(move);

                int score = alphaBeta(newBoard, depth + 1, true, alpha, beta);

                if (score <= alpha) {
                    killers[depth][1] = killers[depth][0];
                    killers[depth][0] = move;
                    return alpha; // cut-off
                }

                beta = Math.min(score, beta);
            }
            return beta;
        }
    }

    private void sortMoves(List<Move> moves, Board board, int depth) {
        moves.sort((m1, m2) -> moveScore(m2, board, depth) - moveScore(m1, board, depth));
    }

    private int moveScore(Move move, Board board, int depth) {
        if (move.getScore() != -1)
            return move.getScore();
        int promotionBonus = 100, attackBonus = 25, castleBonus = 10;

        int score = 0;
        if (depth != this.depth) {
            for (int i = 0; i < NUM_KILLERS; i++) {
                Move killer = killers[depth][i];
                if (killer != null) {
                    if (move.fromSquare() == killer.fromSquare() && move.toSquare() == killer.toSquare()) {
                        score += 50;
                        break;
                    }
                }

            }
        }
        // TODO: treating all normal moves the same
        switch (move.getType()) {
            case PROMOTION_ATTACK:
                score = promotionBonus + 3 * attackBonus;
                break;
            case PAWN_PROMOTION:
                score = promotionBonus;
                break;
            case ATTACK:
                score = captureGain(move.getPiece(), board.at(move.toSquare())) * attackBonus;
                break;
            case CASTLE_LEFT:
            case CASTLE_RIGHT:
                score = castleBonus;
                break;
            case ENPASSANT:
                score = attackBonus;
                break;
//            case NORMAL:
//                break;
        }
        move.setScore(score);
        return score;
    }

    private static int captureGain(int attacker, int victim) {
        if (Pieces.isKing(attacker))
            return 1;
        int diff = Pieces.getValue(victim) - Pieces.getValue(attacker);
        if (diff < 0)
            return 0;
        else if (diff == 0)
            return 1;

        return diff;
    }

}

// TT check
//        TableEntry te = tt.probe(board.getHash(), (byte) depth);
//        if (te != null) {
//            return te.getScore(); // cut-off
//        }
//                    te = new TableEntry(newBoard.getHash(), alpha, (byte) depth, TableEntry.HASH_ALPHA);
//                    tt.save(te);

//                te = new TableEntry(newBoard.getHash(), score, (byte) depth, TableEntry.HASH_EXACT);
//                tt.save(te);
