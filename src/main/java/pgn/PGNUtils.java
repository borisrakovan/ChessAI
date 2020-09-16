package pgn;

import engine.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Class wrapping all of the static methods used for parsing PGN files,
 * as well as the methods used for exporting a game to PGN file
 * format and other useful things used across the application.
 */
public class PGNUtils {
    public static final Pattern PGNTagPattern = Pattern.compile("\\[.*?]");
    public static final Pattern pawnMovePattern = Pattern.compile("^([a-h][1-8])(\\+)?(#)?$");
    public static final Pattern pawnAttackingMovePattern = Pattern.compile("^([a-h])x([a-h][1-8])(\\+)?(#)?$");
    public static final Pattern basicMovePattern = Pattern.compile("^([KQRBN])([a-h])?([1-8])?([a-h][1-8])(\\+)?(#)?$");
    public static final Pattern attackingMovePattern = Pattern.compile("^([KQRBN])([a-h])?([1-8])?x([a-h][1-8])(\\+)?(#)?$");
    public static final Pattern kingSideCastlePattern = Pattern.compile("^O-O(\\+)?(#)?$");
    public static final Pattern queenSideCastlePattern = Pattern.compile("^O-O-O(\\+)?(#)?$");
    public static final Pattern pawnPromotionPattern = Pattern.compile("^([a-h][18])=([QRBN])(\\+)?(#)?$");
    public static final Pattern attackingPawnPromotionPattern = Pattern.compile("^([a-h])x([a-h][18])=([QRBN])(\\+)?(#)?$");

    private PGNUtils() {}

//    public static ArrayList<String> parseMovesFromText(String line) {
//        if (line.equals(""))
//            return new ArrayList<>();
//        ArrayList<String> moves = new ArrayList<>();
//        //String regex = "\\d+\\.\\s[a-zA-Z0-9]+";
//        String[] parts = line.split(" ");
//        String moveNumber = "\\d+\\.";
//        Pattern p = Pattern.compile(moveNumber);
//        Matcher m;
//
//        for (String string : parts) {
//            m = p.matcher(string);
//            if (!m.matches()) {
//                if (m.lookingAt())
//                    string = m.replaceAll("");
//                moves.add(string);
//            }
//        }
//        return moves;
//    }

    public static String pgnFromMove(Move move, Board board) {
        String pgn = null;
        int piece = move.getPiece();
        String notation = Constants.chessNotation(move.toSquare());
        String acronym = String.valueOf(Pieces.getAcronym(piece)).toUpperCase();
        switch (move.getType()) {
            case NORMAL:
                if (Pieces.isPawn(piece))
                    pgn = notation;
                else
                    pgn = acronym + checkForAmbiguity(move, board) + notation;
                break;
            case ATTACK:
                if (Pieces.isPawn(piece))
                    pgn = Constants.chessNotation(move.fromSquare()).charAt(0) +
                            "x" + notation;
                else
                    return acronym + checkForAmbiguity(move, board) + "x" + notation;
                break;
            case CASTLE_LEFT:
                pgn = "O-O";
                break;
            case CASTLE_RIGHT:
                pgn = "O-O-O";
                break;
            case PAWN_PROMOTION:
                pgn = notation + "=" + String.valueOf(Pieces.getAcronym(move.getPromotedPiece())).toUpperCase();
                break;
            case PROMOTION_ATTACK:
                pgn = Constants.chessNotation(move.fromSquare()).charAt(0) +
                        "x" + notation + String.valueOf(Pieces.getAcronym(move.getPromotedPiece())).toUpperCase();
                break;
            case ENPASSANT:
                pgn = Constants.chessNotation(move.fromSquare()).charAt(0) +
                        "x" + notation;
            break;
        }
        assert pgn != null;
        return pgn;
    }

    private static String checkForAmbiguity(Move move, Board board) {
        return ""; // TODO
    }

    public static Move moveFromPgn(String pgnMove, Board board, Allegiance alg) {
        System.out.println("Trying to find move for png: " + pgnMove);
        List<Move> legals = MoveGenerator.legalMoves(board, alg);
        Move move = null;
        int from, to;
        int i = 0;
        Matcher pawnMoveMatcher = pawnMovePattern.matcher(pgnMove);
        Matcher pawnAttackingMoveMatcher = pawnAttackingMovePattern.matcher(pgnMove);
        Matcher basicMoveMatcher = basicMovePattern.matcher(pgnMove);
        Matcher attackingMoveMatcher = attackingMovePattern.matcher(pgnMove);
        Matcher queenSideCastleMatcher = queenSideCastlePattern.matcher(pgnMove);
        Matcher kingSideCastleMatcher = kingSideCastlePattern.matcher(pgnMove);
        Matcher pawnPromotionMatcher = pawnPromotionPattern.matcher(pgnMove);
        Matcher attackingPawnPromotionMatcher = attackingPawnPromotionPattern.matcher(pgnMove);

        if (pawnMoveMatcher.matches()) {
            i++;
            to = Constants.fromChessNotation(pawnMoveMatcher.group(1));
            move = retrieveMove(to, Pieces.getPawn(alg), null, legals);
        }
        if (pawnAttackingMoveMatcher.matches()) {
            i++;
            to = Constants.fromChessNotation(pawnAttackingMoveMatcher.group(2));
            // FIXME: is null at all?
            String ambiguous = pawnAttackingMoveMatcher.group(1);
            move = retrieveMove(to, Pieces.getPawn(alg), ambiguous, legals);
        }
        if (basicMoveMatcher.matches()) {
            i++;

            String ambiguous = basicMoveMatcher.group(2);
            to = Constants.fromChessNotation(basicMoveMatcher.group(4));
            int piece = Pieces.getByAcronym(basicMoveMatcher.group(1), alg);
            move = retrieveMove(to, piece, ambiguous, legals);
        }
        if(attackingMoveMatcher.matches()) {
            i++;
            String ambiguous = attackingMoveMatcher.group(2);
            to = Constants.fromChessNotation(attackingMoveMatcher.group(4));
            int piece = Pieces.getByAcronym(attackingMoveMatcher.group(1), alg);
            move = retrieveMove(to, piece, ambiguous, legals);
        }
        if(queenSideCastleMatcher.matches()) {
            i++;
            int y = alg == Allegiance.WHITE ? 0 : 7;
            int x = 5;
            move = retrieveMove(y * 8 + x, Pieces.getKing(alg), null, legals);
        }
        if(kingSideCastleMatcher.matches()) {
            i++;
            int y = alg == Allegiance.WHITE ? 0 : 7;
            int x = 1;
            move = retrieveMove(y * 8 + x, Pieces.getKing(alg), null, legals);
        }
        if(pawnPromotionMatcher.matches()) {
            i++;
            to = Constants.fromChessNotation(pawnPromotionMatcher.group(1));
            move = retrieveMove(to, Pieces.getPawn(alg), null, legals);
        }
        if(attackingPawnPromotionMatcher.matches()) {
            i++;
            to = Constants.fromChessNotation(attackingPawnPromotionMatcher.group(1));

            String ambiguous = attackingPawnPromotionMatcher.group(1);
            move = retrieveMove(to, Pieces.getPawn(alg), null, legals);
        }
        if(i != 1)
            throw new RuntimeException("Most likely not a PGN format.");

        assert move != null;
        return move;
    }

    private static Move retrieveMove(int destinationSq, int piece, String ambiguous, List<Move> legals) {
        Allegiance alg = Pieces.getAllegiance(piece);
        for (Move move : legals) {
            if (move.getPiece() == piece && move.toSquare() == destinationSq) {
                if (ambiguous == null) {
                    return move;
                } else {
                    char amb = ambiguous.charAt(0);
                    if (Character.isDigit(amb)) {
                        int fromFile = Constants.chessNotation(move.fromSquare()).charAt(1);
                        if (fromFile == amb) {
                            return move;
                        }
                    } else if (Character.isLetter(amb)) {
                        int fromRank = Constants.chessNotation(move.fromSquare()).charAt(0);
                        if (fromRank == amb) {
                            return move;
                        }
                    }
                    throw new RuntimeException("invalid ambiguity char passed");
                }
            }
        }
        throw new RuntimeException("unable to find legal move corresponding to given PGN.");
    }


}