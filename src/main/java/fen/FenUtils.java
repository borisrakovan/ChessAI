package fen;

import engine.*;

public class FenUtils {
    public static Board fenToBoard(String fen) {
        String[] parts = fen.split(" ");
        assert parts.length >= 4;
        Allegiance nextToMove = parts[1].equals("w") ? Allegiance.WHITE : Allegiance.BLACK;

        CastleRight crWhite = CastleRight.NONE;
        CastleRight crBlack = CastleRight.NONE;
        for (char c : parts[2].toCharArray()) {
            if (c == '-')
                break;
            if (Character.isLowerCase(c)) { // black
                if (c == 'k')
                    crBlack = CastleRight.RIGHT;
                else
                    crBlack = crBlack == CastleRight.NONE ? CastleRight.LEFT : CastleRight.ALL;
            } else {
                if (c == 'K')
                    crWhite = CastleRight.RIGHT;
                else
                    crWhite = crWhite == CastleRight.NONE ? CastleRight.LEFT : CastleRight.ALL;
            }
        }
        String epp = parts[3];
        long enPassantBitboard = 0L;
        if (!epp.equals("-")) {
            int sq = Constants.fromChessNotation(epp);

            enPassantBitboard = 1L << sq;
            if (nextToMove == Allegiance.WHITE)
                // enpassant pawn must be black
                enPassantBitboard >>>= 8;
            else
                enPassantBitboard <<= 8;
        }
        int moveCount = 0;
        if (parts.length > 4) {
            int fullMoveCount = Integer.parseInt(parts[5]);
            moveCount = (fullMoveCount - 1) * 2 + (nextToMove == Allegiance.BLACK ? 1 : 0);
        }

        Board board = new Board(true, nextToMove, crBlack, crWhite, enPassantBitboard, moveCount);
        int i = 0;

        for (char c : parts[0].toCharArray()) {
            if (c == '/')
                continue;
            if (Character.isDigit(c)) {
                int empty = Character.getNumericValue(c);
                assert empty >= 1 && empty <= 8;
                i += empty;
            } else {
                int piece = Pieces.fromAcronym(c);
                board.putPiece(piece, 63 - i);
                i++;
            }
        }

        return board;
    }

    public static String boardToFen(Board board) {
        StringBuilder builder = new StringBuilder();

        builder.append(piecePlacement(board)).append(" ").append(nextToMove(board)).append(" ")
                .append(casteRights(board)).append(" ").append(enpassantPawn(board)).append(" ")
                .append(halfMoves(board)).append(" ").append(fullMoves(board));

        return new String(builder);
    }

    private static String piecePlacement(Board board) {
        StringBuilder builder = new StringBuilder();
        int empty = 0;
        for (int i = 0; i < 64; i++) {
            int piece = board.at(63 - i);
            if (piece != -1){
                if (empty > 0) {
                    builder.append(empty);
                    empty = 0;
                }
                char acronym = Pieces.getAcronym(piece);

                builder.append(acronym);
            } else
                empty++;
            // if isOnEdge or !isOnBoard or
            if (i % 8 == 7) {
                if (empty > 0) {
                    builder.append(empty);
                    empty = 0;
                }
                if (i != 63)
                    builder.append("/");
            }
        }
        return new String(builder);
    }

    private static String enpassantPawn(Board board) {
        if (board.getEnPassantBitboard() == 0L)
            return "-";
        long enPassantBitboard = board.getEnPassantBitboard();
        if (board.whoseMove() == Allegiance.WHITE)
            // enpassant pawn must be black
            enPassantBitboard <<= 8;
        else
            enPassantBitboard >>>= 8;
        int sq = Long.numberOfTrailingZeros(enPassantBitboard);

        return Constants.chessNotation(sq);
    }

    private static String casteRights(Board board) {
        String castleRights = board.getCastleRight(Allegiance.WHITE).notation +
                board.getCastleRight(Allegiance.BLACK).notation.toLowerCase();
        if (castleRights.equals(""))
            return "-";
        return castleRights;
    }

    private static String nextToMove(Board board) {
        return board.whoseMove().toString().toLowerCase();
    }

    private static String fullMoves(Board board) {

        int fullMoveCount = board.getMoveCount() / 2 + 1;

        return String.valueOf(fullMoveCount);
    }
    private static String halfMoves(Board board) {
        return String.valueOf(0); // TODO
    }

}
