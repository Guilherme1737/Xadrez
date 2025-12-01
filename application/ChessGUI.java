package application;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import chesslayer.ChessException;
import chesslayer.ChessMatch;
import chesslayer.ChessPiece;
import chesslayer.ChessPosition;

public class ChessGUI extends JFrame {
    
    private static final long serialVersionUID = 1L;
    
    private ChessMatch chessMatch;
    private JButton[][] squares;
    private JLabel statusLabel;
    private JLabel capturedWhiteLabel;
    private JLabel capturedBlackLabel;
    
    private ChessPosition selectedPosition;
    private boolean[][] possibleMoves;
    private List<ChessPiece> capturedPieces;
    
    // Cores do tabuleiro
    private static final Color LIGHT_SQUARE = new Color(240, 217, 181);
    private static final Color DARK_SQUARE = new Color(181, 136, 99);
    private static final Color SELECTED_SQUARE = new Color(130, 151, 105);
    private static final Color POSSIBLE_MOVE = new Color(170, 162, 58);
    private static final Color WHITE_PIECE_COLOR = new Color(255, 255, 255);
    private static final Color BLACK_PIECE_COLOR = new Color(30, 30, 30);
    
    // Símbolos Unicode das peças
    private static final String[] WHITE_PIECES = {"♔", "♕", "♖", "♗", "♘", "♙"};
    private static final String[] BLACK_PIECES = {"♚", "♛", "♜", "♝", "♞", "♟"};
    
    public ChessGUI() {
        chessMatch = new ChessMatch();
        capturedPieces = new ArrayList<>();
        squares = new JButton[8][8];
        
        initializeGUI();
        updateBoard();
    }
    
    private void initializeGUI() {
        setTitle("Xadrez - Java");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // Painel principal do tabuleiro
        JPanel boardPanel = new JPanel(new GridLayout(8, 8));
        boardPanel.setPreferredSize(new Dimension(560, 560));
        
        // Criar quadrados do tabuleiro
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                squares[i][j] = createSquare(i, j);
                boardPanel.add(squares[i][j]);
            }
        }
        
        // Painel com letras (a-h)
        JPanel lettersPanel = new JPanel(new GridLayout(1, 8));
        lettersPanel.setBorder(new EmptyBorder(0, 25, 0, 0));
        for (char c = 'a'; c <= 'h'; c++) {
            JLabel label = new JLabel(String.valueOf(c), SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 14));
            lettersPanel.add(label);
        }
        
        // Painel com números (8-1)
        JPanel numbersPanel = new JPanel(new GridLayout(8, 1));
        for (int i = 8; i >= 1; i--) {
            JLabel label = new JLabel(" " + i + " ", SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 14));
            numbersPanel.add(label);
        }
        
        // Painel de status
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        statusLabel = new JLabel("Turno 1 - Jogador: WHITE", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        capturedWhiteLabel = new JLabel("Capturadas (Brancas): ", SwingConstants.LEFT);
        capturedBlackLabel = new JLabel("Capturadas (Pretas): ", SwingConstants.LEFT);
        
        JPanel capturedPanel = new JPanel(new GridLayout(2, 1));
        capturedPanel.add(capturedWhiteLabel);
        capturedPanel.add(capturedBlackLabel);
        
        statusPanel.add(statusLabel, BorderLayout.NORTH);
        statusPanel.add(capturedPanel, BorderLayout.CENTER);
        
        // Botão de novo jogo
        JButton newGameBtn = new JButton("Novo Jogo");
        newGameBtn.addActionListener(e -> {
            chessMatch = new ChessMatch();
            capturedPieces.clear();
            selectedPosition = null;
            possibleMoves = null;
            updateBoard();
        });
        statusPanel.add(newGameBtn, BorderLayout.SOUTH);
        
        // Adicionar componentes
        add(boardPanel, BorderLayout.CENTER);
        add(lettersPanel, BorderLayout.SOUTH);
        add(numbersPanel, BorderLayout.WEST);
        add(statusPanel, BorderLayout.NORTH);
        
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }
    
    private JButton createSquare(int row, int col) {
        JButton button = new JButton();
        button.setFont(new Font("Serif", Font.PLAIN, 50));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        
        final int r = row;
        final int c = col;
        
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSquareClick(r, c);
            }
        });
        
        return button;
    }
    
    private void handleSquareClick(int row, int col) {
        if (chessMatch.getCheckMate()) {
            return;
        }
        
        try {
            if (selectedPosition == null) {
                // Primeira seleção - escolher peça
                ChessPosition pos = new ChessPosition((char)('a' + col), 8 - row);
                ChessPiece[][] pieces = chessMatch.getPieces();
                
                if (pieces[row][col] != null && pieces[row][col].getColor() == chessMatch.getCurrentPlayer()) {
                    selectedPosition = pos;
                    possibleMoves = chessMatch.possibleMoves(pos);
                    updateBoard();
                }
            } else {
                // Segunda seleção - mover peça
                ChessPosition target = new ChessPosition((char)('a' + col), 8 - row);
                
                try {
                    ChessPiece captured = chessMatch.performChessMove(selectedPosition, target);
                    if (captured != null) {
                        capturedPieces.add(captured);
                    }
                    
                    // Promoção de peão
                    if (chessMatch.getPromoted() != null) {
                        String[] options = {"Rainha", "Torre", "Bispo", "Cavalo"};
                        int choice = JOptionPane.showOptionDialog(this,
                            "Escolha a peça para promoção:",
                            "Promoção de Peão",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null, options, options[0]);
                        
                        String type = "Q";
                        switch (choice) {
                            case 1: type = "R"; break;
                            case 2: type = "B"; break;
                            case 3: type = "N"; break;
                        }
                        chessMatch.replacePromotedPiece(type);
                    }
                    
                } catch (ChessException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Movimento Inválido", JOptionPane.WARNING_MESSAGE);
                }
                
                selectedPosition = null;
                possibleMoves = null;
                updateBoard();
            }
        } catch (ChessException ex) {
            selectedPosition = null;
            possibleMoves = null;
            updateBoard();
        }
    }
    
    private void updateBoard() {
        ChessPiece[][] pieces = chessMatch.getPieces();
        
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                // Cor de fundo do quadrado
                Color bgColor = ((i + j) % 2 == 0) ? LIGHT_SQUARE : DARK_SQUARE;
                
                // Destacar movimentos possíveis
                if (possibleMoves != null && possibleMoves[i][j]) {
                    bgColor = POSSIBLE_MOVE;
                }
                
                // Destacar peça selecionada
                if (selectedPosition != null) {
                    int selRow = 8 - selectedPosition.getRow();
                    int selCol = selectedPosition.getColumn() - 'a';
                    if (i == selRow && j == selCol) {
                        bgColor = SELECTED_SQUARE;
                    }
                }
                
                squares[i][j].setBackground(bgColor);
                
                // Colocar peça
                if (pieces[i][j] != null) {
                    String symbol = getPieceSymbol(pieces[i][j]);
                    squares[i][j].setText(symbol);
                    squares[i][j].setForeground(
                        pieces[i][j].getColor() == chesslayer.Color.WHITE ? WHITE_PIECE_COLOR : BLACK_PIECE_COLOR
                    );
                } else {
                    squares[i][j].setText("");
                }
            }
        }
        
        // Atualizar status
        if (chessMatch.getCheckMate()) {
            statusLabel.setText("XEQUE-MATE! Vencedor: " + chessMatch.getCurrentPlayer());
            statusLabel.setForeground(Color.RED);
        } else {
            String status = "Turno " + chessMatch.getTurn() + " - Jogador: " + chessMatch.getCurrentPlayer();
            if (chessMatch.getCheck()) {
                status += " - XEQUE!";
                statusLabel.setForeground(Color.RED);
            } else {
                statusLabel.setForeground(Color.BLACK);
            }
            statusLabel.setText(status);
        }
        
        // Atualizar peças capturadas
        updateCapturedPieces();
    }
    
    private void updateCapturedPieces() {
        StringBuilder white = new StringBuilder("Capturadas (Brancas): ");
        StringBuilder black = new StringBuilder("Capturadas (Pretas): ");
        
        for (ChessPiece p : capturedPieces) {
            String symbol = getPieceSymbol(p);
            if (p.getColor() == chesslayer.Color.WHITE) {
                white.append(symbol).append(" ");
            } else {
                black.append(symbol).append(" ");
            }
        }
        
        capturedWhiteLabel.setText(white.toString());
        capturedBlackLabel.setText(black.toString());
    }
    
    private String getPieceSymbol(ChessPiece piece) {
        String pieceStr = piece.toString();
        boolean isWhite = piece.getColor() == chesslayer.Color.WHITE;
        
        switch (pieceStr) {
            case "K": return isWhite ? "♔" : "♚";
            case "Q": return isWhite ? "♕" : "♛";
            case "R": return isWhite ? "♖" : "♜";
            case "B": return isWhite ? "♗" : "♝";
            case "N": return isWhite ? "♘" : "♞";
            case "P": return isWhite ? "♙" : "♟";
            default: return "?";
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ChessGUI().setVisible(true);
        });
    }
}
