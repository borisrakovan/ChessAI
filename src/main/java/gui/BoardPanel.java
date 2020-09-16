package gui;


import engine.*;
import engine.players.ComputerPlayer;
import engine.players.HumanPlayer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static gui.Table.B_DIMENSION;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.SwingUtilities.isLeftMouseButton;


/**
 * The GUI component that displays the actual chessboard and its tiles.
 * Handles the interaction between GUI and user and sends proper instructions
 * to the rest of the program.
 */
public class BoardPanel extends JPanel {
    private static final Dimension boardDimensions = new Dimension(B_DIMENSION, B_DIMENSION);
    private static final String ICONS_PATH = "art/icons/";
    private static final Color darkTileColor = Color.decode("#3B7767");
    private static final Color lightTileColor = Color.decode("#ECECEC");
    private static final Color selectedDarkTileColor = Color.decode("#f7e27e");
    private static final Color selectedLightTileColor = Color.decode("#FFF9A8");
    private static Dimension tileDimensions = new Dimension(B_DIMENSION/8,B_DIMENSION/8);

    public Board board;
    private TilePanel[] boardTiles;
    private int sourceTile;
    private int destinationTile;
//    private int selectedPiece;
    private Table parent;
    private List<Move> highlightCache;
    private boolean isGameOver;

    public BoardPanel(Table parent) {
        super(new GridLayout(8, 8));
        this.parent = parent;
        this.isGameOver = false;
        this.board = parent.getBoard();
        this.boardTiles = new TilePanel[64];
        updateHighlightCache();
        for (int i = 63; i >= 0; i--) {
            TilePanel tilePanel = new TilePanel(this, i);
            boardTiles[i] = tilePanel;
            add(tilePanel);
        }
        setPreferredSize(boardDimensions);
        setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
        setBackground(Color.decode("#312e2B"));
        validate();

    }

    void redrawBoard() {
        removeAll();
        for (int i = 63; i >= 0; i--) {
            TilePanel tilePanel = new TilePanel(this, i);
            boardTiles[i] = tilePanel;
            add(boardTiles[i]);
        }
        validate();
        repaint();
    }

    public void processChanges(Flag flag) {
        switch(flag) {
            case INVALID:
                System.err.println("execution failed.");
                JOptionPane.showMessageDialog(this,"Not a valid move!", "Message", WARNING_MESSAGE);
                break;

            case DONE:
                redrawBoard();

                Flag gameState = board.getGameState();
                switch (gameState) {
                    case CHECKMATE:
                        setGameOver(true);
                        String msg1 = Allegiance.not(board.whoseMove()).fullString() + " has won the game!";
                        JOptionPane.showMessageDialog(this, msg1, "Game over", INFORMATION_MESSAGE);
                        break;

                    case STALEMATE:
                        setGameOver(true);
                        String msg2 = "The game is drawn due to a stalemate.";
                        JOptionPane.showMessageDialog(this, msg2, "Game over", INFORMATION_MESSAGE);
                        break;

                    case INSUFFICIENT_MATERIAL:
                        setGameOver(true);
                        String msg3 = "The game is drawn due to insufficient material.";
                        JOptionPane.showMessageDialog(this, msg3, "Game over", INFORMATION_MESSAGE);
                        break;
                }
                if (board.currentPlayer().isComputer() && !isGameOver())
                    executeComputerMove();

                updateHighlightCache(); // todo could cause issues with computer players and enpassant/promotion?
                break;
        }

        if(flag != Flag.INVALID) {
            sourceTile = -1;
            destinationTile = -1;
//            selectedPiece = null;
        }
    }

    public void executeComputerMove() {
//        if (board.isComputerGame()) {
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
        ComputerPlayer cp = (ComputerPlayer) board.currentPlayer();
        processChanges(cp.executeMove());

    }
    private void updateHighlightCache() {
        this.highlightCache = MoveGenerator.legalMoves(board, board.whoseMove());
    }


    /**
     * Class that represents all of the 64 tile panels in GUI
     */
    public class TilePanel extends JPanel {
        private BoardPanel boardPanel; //do we really need this

        private final int square;

        public TilePanel(final BoardPanel boardPanel, int square) {
            super(new GridBagLayout());
            this.boardPanel = boardPanel;
            this.square = square;

            setPreferredSize(tileDimensions);
            assignColor();
            assignIcon();

            addMouseListener(new MouseListener() {
                public void mouseClicked(MouseEvent e) {
                    if(board.currentPlayer().isComputer() || isGameOver()) return;

                    if (isLeftMouseButton(e)) {
                        if (sourceTile == -1) { //first click
                            sourceTile = square;
                            if (board.isOccupied(sourceTile) &&
                                    Pieces.getAllegiance(board.at(sourceTile)) == board.whoseMove()) {
//                                selectedPiece = sourceTile.getPiece();
                                //highlightLegalMoves();
                                highlightTile();
                            }
                            else {
                                sourceTile = -1;
                            }
                        }
                        else { //second click
//                            assert selectedPiece != null;
                            if (board.isOccupied(square)
                                    && Pieces.getAllegiance(board.at(square)) == board.whoseMove()) {
                                //redraw();
                                unselectTile(sourceTile);
                                sourceTile = square;
                                int selectedPiece = board.at(sourceTile);
                                assert selectedPiece != -1;
                                assert Pieces.getAllegiance(selectedPiece) == board.whoseMove();
                                highlightTile();
                            }
                            else { //DESTINATION TILE CHOSEN
                                destinationTile = square;
                                HumanPlayer hp = (HumanPlayer) board.currentPlayer();

                                Flag flag = hp.executeMove(sourceTile, destinationTile);
                                processChanges(flag);
                            }
                        }
                    }
                }

                public void mousePressed(MouseEvent e) { }

                public void mouseReleased(MouseEvent e) { }

                public void mouseEntered(MouseEvent e) {
                    // so we don't have to call it twice todo might think about doing this in all places
                    int pieceOnSquare = board.at(square);
                    if (pieceOnSquare != -1) {
                        setCursor(new Cursor(Cursor.HAND_CURSOR));
                        if (Pieces.getAllegiance(pieceOnSquare) == board.whoseMove())
                            highlightLegalMoves();
                    }
                }

                public void mouseExited(MouseEvent e) {
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    if (board.isOccupied(square)) {
                        removeHighlighters();
                    }
                }
            });
            validate();
        }

        private void unselectTile(int square) {
            assert square != -1;
//            assert selectedPiece != null;
            boardPanel.boardTiles[square].assignColor();
            boardPanel.boardTiles[square].validate();
        }

        private void assignIcon() {
            this.removeAll();
            if(board.isOccupied(square)){
                try {
                    int piece = board.at(square);
                    BufferedImage image = ImageIO.read(new File(ICONS_PATH +
                            Pieces.getAllegiance(piece).toString() +
                            Pieces.getAcronym(piece) + ".png"));
                    Image newImage = image.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                    add(new JLabel(new ImageIcon(newImage)));
                } catch (IOException e) {
                    System.err.println("failed to load img");
                    System.exit(-1);
                }
            }
        }

        private void assignColor() {
            if (square % 16 < 8) {
                if(square % 2 == 0 )
                    setBackground(lightTileColor);
                else
                    setBackground(darkTileColor);
            }
            else {
                if(square % 2 == 0 )
                    setBackground(darkTileColor);
                else
                    setBackground(lightTileColor);
            }
        }

        private void highlightTile(){
            if (square % 2 == 0)
                setBackground(selectedLightTileColor);

            else
                setBackground(selectedDarkTileColor);
        }


        private void highlightLegalMoves() {
            int piece = board.at(square);
            boolean promotionHighlighted = false;
            for (Move move : highlightCache) {
                if (!move.isAttack() && move.fromSquare() == square) {
                    if (move.isPromotion()) {
                        if (promotionHighlighted)
                            continue;
                        promotionHighlighted = true;
                    }
                    assert move.getPiece() == piece;
                    try {
                        JLabel highlighter = new JLabel(new ImageIcon(ImageIO.read(new File("art/icons/highlighter4.png"))));
                        boardPanel.boardTiles[move.toSquare()].add(highlighter);
                        boardPanel.boardTiles[move.toSquare()].validate();
                        boardPanel.validate();
                        boardPanel.repaint();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }

        private void removeHighlighters() {
//            int piece = board.at(square);
            for (Move move : highlightCache) {
                if (!move.isAttack() && move.fromSquare() == square) {
                    boardPanel.boardTiles[move.toSquare()].removeAll();
//                    if (chessBoard.getTile(tilePanel).getPiece() != null){
//                        tilePanel.assignIcon();
//                    } todo should never happen?
                    boardPanel.boardTiles[move.toSquare()].validate();
                }
            }
            boardPanel.validate();
            boardPanel.repaint();
        }

//        private int convert(int y, int x) {
//            return (7 - y)*8 + x;
//        }

    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public void setGameOver(boolean gameOver) {
        isGameOver = gameOver;
    }
}

