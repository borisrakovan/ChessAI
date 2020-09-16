//package ai;
//
//import engine.Allegiance;
//import engine.Board;
//import engine.Move;
//import engine.MoveGenerator;
//import java.util.List;
//
//public class Minimax implements MoveStrategy {
//    private BoardEvaluator boardEvaluator;
//    private int depth;
//
//    public Minimax(int depth) {
//        this.boardEvaluator = new BoardEvaluator();
//        this.depth = depth;
//    }
//    @Override
//    // TODO negamax, faster evaluation https://www.chessprogramming.org/Evaluation
//    public Move execute(Board board) {
//        long startTime = System.currentTimeMillis();
//        Allegiance alg = board.currentPlayer().getAllegiance();
//        List<Move> moves = MoveGenerator.legalMoves(board, alg);
//
//        Move bestMove = null;
//        if (alg == Allegiance.WHITE) {
//            int highest = Integer.MIN_VALUE;
//            for (Move move : moves) {
//                Board newBoard = new Board(board);
//                newBoard.makeMoveTransition(move);
//                int value = miniMax(newBoard, depth - 1, false);
//                if (highest < value) {
//                    highest = value;
//                    bestMove = move;
//                }
//            }
//        } else {
//            int lowest = Integer.MAX_VALUE;
//            for (Move move : moves) {
//                Board newBoard = new Board(board);
//                newBoard.makeMoveTransition(move);
//                int value = miniMax(newBoard, depth - 1, true);
//                if (lowest > value) {
//                    lowest = value;
//                    bestMove = move;
//                }
//            }
//        }
//        assert bestMove != null;
//        long executionTime = System.currentTimeMillis() - startTime;
//        System.out.println("Minimax returned in " + executionTime / 1000 + " seconds");
//        return bestMove;
//    }
//
//    private int miniMax(Board board, int depth, boolean max) {
//        if (depth == 0) /*TODO: or is GAMEOVER - urcite sa nevyhodnocuje ak je 1 splnena?*/
//            return boardEvaluator.evaluate(board); // todo depth too?
//
//        Allegiance alg = board.currentPlayer().getAllegiance();
//        List<Move> moves = MoveGenerator.legalMoves(board, alg);
//        int value;
//        if (max) { // Maximizing player
//            value = Integer.MIN_VALUE;
//            for (Move move : moves) {
//                Board newBoard = new Board(board); // todo undomove would speed things up?
//                newBoard.makeMoveTransition(move);
//                value = Math.max(value, miniMax(newBoard, depth - 1, false));
//            }
//        } else {
//            value = Integer.MAX_VALUE;
//            for (Move move : moves) {
//                Board newBoard = new Board(board);
//                newBoard.makeMoveTransition(move);
//                value = Math.min(value, miniMax(newBoard, depth - 1, true));
//            }
//        }
//
//        return value;
//    }
//
//}
