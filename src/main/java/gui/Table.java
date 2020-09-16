package gui;


import engine.Board;
import engine.players.ComputerPlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;
import java.util.Stack;

/**
 * The main GUI class. Initializes, displays and manages all of the GUI objects,
 * as well as the interaction between GUI and user.
 */
public class Table {
    static int B_DIMENSION = 560;
    static int E_WIDTH = 120;

    private static Dimension windowDimension = new Dimension(600, 600);
    final JFrame gameWindow;
    private BoardPanel boardPanel; // FIXME : changed this from jpanel, might have severe consequences
    private Board board;


    public Table(Board board) {
        gameWindow = new JFrame("Chess");
        gameWindow.setLayout(new BorderLayout());
        gameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.board = Objects.requireNonNullElseGet(board, Board::new);

        final JMenuBar menuBar = createMenu();
        this.gameWindow.setJMenuBar(menuBar);

        gameWindow.setSize(windowDimension);

        boardPanel = new BoardPanel(this);
        gameWindow.add(boardPanel, BorderLayout.CENTER);

        gameWindow.pack();
        gameWindow.setVisible(true);

        // prompt the computer to play for the first time
        if (this.board.currentPlayer().isComputer()) {
            boardPanel.executeComputerMove();
        }
    }

    private JMenuBar createMenu() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createFileMenu());
//        menuBar.add(createPreferencesMenu());

        return menuBar;
    }

    private JMenu createFileMenu() {
        JMenu fileMenu = new JMenu("File");
        JMenuItem exportToPGN = new JMenuItem("Export as PGN");
        JMenuItem saveGame = new JMenuItem("Save game");
//        exportToPGN.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                exportToPGN();
//            }
//        });
//        saveGame.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                saveGame();
//            }
//        });
        fileMenu.add(exportToPGN);
        fileMenu.add(saveGame);
        return fileMenu;
    }

    public Board getBoard() {
        return board;
    }
}