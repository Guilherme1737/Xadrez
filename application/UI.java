package application;

import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import chesslayer.ChessMatch;
import chesslayer.ChessPiece;
import chesslayer.ChessPosition;
import chesslayer.Color;

public class UI {
    
    // Códigos ANSI para cores
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    
    public static final String ANSI_BLACK_BACKGROUND = "\u001B[40m";
    public static final String ANSI_RED_BACKGROUND = "\u001B[41m";
    public static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
    public static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
    public static final String ANSI_BLUE_BACKGROUND = "\u001B[44m";
    public static final String ANSI_PURPLE_BACKGROUND = "\u001B[45m";
    public static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";
    public static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";
    
    // Limpa a tela do console
    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
    
    // Lê posição de xadrez digitada pelo usuário
    public static ChessPosition readChessPosition(Scanner sc) {
        try {
            String s = sc.nextLine();
            char column = s.charAt(0);
            int row = Integer.parseInt(s.substring(1));
            return new ChessPosition(column, row);
        } catch (RuntimeException e) {
            throw new InputMismatchException("Erro ao ler a posição de xadrez. Valores válidos são de a1 a h8.");
        }
    }
    
    // Imprime a partida atual
    public static void printMatch(ChessMatch chessMatch, List<ChessPiece> captured) {
        printBoard(chessMatch.getPieces());
        System.out.println();
        printMoveHistory(chessMatch.getMoveHistory());
        System.out.println();
        printCapturedPieces(captured);
        System.out.println();
        System.out.println("Turno: " + chessMatch.getTurn());
        if (!chessMatch.getCheckMate()) {
            System.out.println("Aguardando jogador: " + chessMatch.getCurrentPlayer());
            if (chessMatch.getCheck()) {
                System.out.println(ANSI_RED + "XEQUE!" + ANSI_RESET);
            }
        } else {
            System.out.println(ANSI_GREEN + "XEQUE-MATE!" + ANSI_RESET);
            System.out.println("Vencedor: " + chessMatch.getCurrentPlayer());
        }
    }
    
    // Imprime histórico de jogadas em notação SAN
    private static void printMoveHistory(List<String> moveHistory) {
        if (moveHistory.isEmpty()) {
            return;
        }
        System.out.print(ANSI_CYAN + "Jogadas: " + ANSI_RESET);
        for (int i = 0; i < moveHistory.size(); i++) {
            if (i % 2 == 0) {
                System.out.print((i / 2 + 1) + ".");
            }
            System.out.print(moveHistory.get(i) + " ");
        }
        System.out.println();
    }
    
    public static void printBoard(ChessPiece[][] pieces) {
        System.out.println();
        printHorizontalLine();
        for (int i = 0; i < pieces.length; i++) {
            System.out.print(ANSI_GREEN + (8 - i) + ANSI_RESET + " ");
            for (int j = 0; j < pieces.length; j++) {
                System.out.print("| ");
                printPiece(pieces[i][j], false);
            }
            System.out.println("|");
            printHorizontalLine();
        }
        System.out.println(ANSI_GREEN + "    a   b   c   d   e   f   g   h" + ANSI_RESET);
        System.out.println();
    }
    
    // Imprime tabuleiro com movimentos possíveis destacados
    public static void printBoard(ChessPiece[][] pieces, boolean[][] possibleMoves) {
        System.out.println();
        printHorizontalLine();
        for (int i = 0; i < pieces.length; i++) {
            System.out.print(ANSI_GREEN + (8 - i) + ANSI_RESET + " ");
            for (int j = 0; j < pieces.length; j++) {
                System.out.print("| ");
                printPiece(pieces[i][j], possibleMoves[i][j]);
            }
            System.out.println("|");
            printHorizontalLine();
        }
        System.out.println(ANSI_GREEN + "    a   b   c   d   e   f   g   h" + ANSI_RESET);
        System.out.println();
    }
    
    // Imprime linha horizontal do tabuleiro
    private static void printHorizontalLine() {
        System.out.print("  ");
        for (int i = 0; i < 8; i++) {
            System.out.print("+---");
        }
        System.out.println("+");
    }
    
    private static void printPiece(ChessPiece piece, boolean background) {
        if (background) {
            System.out.print(ANSI_BLUE_BACKGROUND);
        }
        if (piece == null) {
            System.out.print(" " + ANSI_RESET);
        } else {
            if (piece.getColor() == Color.WHITE) {
                System.out.print(ANSI_WHITE + piece + ANSI_RESET);
            } else {
                System.out.print(ANSI_YELLOW + piece + ANSI_RESET);
            }
        }
        System.out.print(" ");
    }
    
    private static void printCapturedPieces(List<ChessPiece> captured) {
        List<ChessPiece> white = captured.stream().filter(x -> x.getColor() == Color.WHITE).collect(Collectors.toList());
        List<ChessPiece> black = captured.stream().filter(x -> x.getColor() == Color.BLACK).collect(Collectors.toList());
        System.out.println("Peças capturadas:");
        System.out.print("Brancas: ");
        System.out.print(ANSI_WHITE);
        System.out.println(Arrays.toString(white.toArray()));
        System.out.print(ANSI_RESET);
        System.out.print("Pretas: ");
        System.out.print(ANSI_YELLOW);
        System.out.println(Arrays.toString(black.toArray()));
        System.out.print(ANSI_RESET);
    }
}
