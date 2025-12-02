package chesslayer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import boardlayer.Board;
import boardlayer.Piece;
import boardlayer.Position;
import chesslayer.pieces.Bishop;
import chesslayer.pieces.King;
import chesslayer.pieces.Knight;
import chesslayer.pieces.Pawn;
import chesslayer.pieces.Queen;
import chesslayer.pieces.Rook;

public class ChessMatch {
    
    private int turn;
    private Color currentPlayer;
    private Board board;
    private boolean check;
    private boolean checkMate;
    private ChessPiece enPassantVulnerable;
    private ChessPiece promoted;
    
    private List<Piece> piecesOnTheBoard = new ArrayList<>();
    private List<Piece> capturedPieces = new ArrayList<>();
    private List<String> moveHistory = new ArrayList<>();
    
    public ChessMatch() {
        board = new Board(8, 8);
        turn = 1;
        currentPlayer = Color.WHITE;
        initialSetup();
    }
    
    public int getTurn() {
        return turn;
    }
    
    public Color getCurrentPlayer() {
        return currentPlayer;
    }
    
    public boolean getCheck() {
        return check;
    }
    
    public boolean getCheckMate() {
        return checkMate;
    }
    
    public ChessPiece getEnPassantVulnerable() {
        return enPassantVulnerable;
    }
    
    public ChessPiece getPromoted() {
        return promoted;
    }
    
    public List<String> getMoveHistory() {
        return moveHistory;
    }
    
    // Retorna matriz de peças para a interface
    public ChessPiece[][] getPieces() {
        ChessPiece[][] mat = new ChessPiece[board.getRows()][board.getColumns()];
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getColumns(); j++) {
                mat[i][j] = (ChessPiece) board.piece(i, j);
            }
        }
        return mat;
    }
    
    // Retorna movimentos possíveis para uma posição (para colorir no UI)
    public boolean[][] possibleMoves(ChessPosition sourcePosition) {
        Position position = sourcePosition.toPosition();
        validateSourcePosition(position);
        return board.piece(position).possibleMoves();
    }
    
    public ChessPiece performChessMove(ChessPosition sourcePosition, ChessPosition targetPosition) {
        Position source = sourcePosition.toPosition();
        Position target = targetPosition.toPosition();
        validateSourcePosition(source);
        validateTargetPosition(source, target);
        
        // Guarda informações para notação SAN antes do movimento
        ChessPiece movingPiece = (ChessPiece) board.piece(source);
        boolean isCapture = board.thereIsAPiece(target) || 
                           (movingPiece instanceof Pawn && source.getColumn() != target.getColumn());
        boolean isCastlingShort = movingPiece instanceof King && target.getColumn() == source.getColumn() + 2;
        boolean isCastlingLong = movingPiece instanceof King && target.getColumn() == source.getColumn() - 2;
        String disambiguation = getDisambiguation(movingPiece, source, target);
        
        Piece capturedPiece = makeMove(source, target);
        
        // Verifica se o jogador se colocou em xeque
        if (testCheck(currentPlayer)) {
            undoMove(source, target, capturedPiece);
            throw new ChessException("Você não pode se colocar em xeque!");
        }
        
        ChessPiece movedPiece = (ChessPiece) board.piece(target);
        
        // Promoção
        promoted = null;
        String promotionPiece = "";
        if (movedPiece instanceof Pawn) {
            if ((movedPiece.getColor() == Color.WHITE && target.getRow() == 0) || 
                (movedPiece.getColor() == Color.BLACK && target.getRow() == 7)) {
                promoted = (ChessPiece) board.piece(target);
                promoted = replacePromotedPiece("Q"); // Promoção padrão para Rainha
                promotionPiece = "=Q";
            }
        }
        
        check = testCheck(opponent(currentPlayer));
        boolean isCheckMateMove = testCheckMate(opponent(currentPlayer));
        
        // Gera notação SAN
        String notation = generateSANNotation(movingPiece, sourcePosition, targetPosition, 
                                              isCapture, isCastlingShort, isCastlingLong, 
                                              disambiguation, promotionPiece, check, isCheckMateMove);
        moveHistory.add(notation);
        
        if (isCheckMateMove) {
            checkMate = true;
        } else {
            nextTurn();
        }
        
        // En Passant
        if (movedPiece instanceof Pawn && (target.getRow() == source.getRow() - 2 || target.getRow() == source.getRow() + 2)) {
            enPassantVulnerable = movedPiece;
        } else {
            enPassantVulnerable = null;
        }
        
        return (ChessPiece) capturedPiece;
    }
    
    // Gera notação algébrica padrão (SAN)
    private String generateSANNotation(ChessPiece piece, ChessPosition source, ChessPosition target,
                                       boolean isCapture, boolean isCastlingShort, boolean isCastlingLong,
                                       String disambiguation, String promotion, boolean isCheck, boolean isCheckMate) {
        StringBuilder notation = new StringBuilder();
        
        // Roque
        if (isCastlingShort) {
            notation.append("O-O");
        } else if (isCastlingLong) {
            notation.append("O-O-O");
        } else if (piece instanceof Pawn) {
            // Peão
            if (isCapture) {
                notation.append(source.getColumn()); // coluna de origem
                notation.append("x");
            }
            notation.append(target.getColumn());
            notation.append(target.getRow());
            notation.append(promotion);
        } else {
            // Outras peças
            notation.append(piece.toString()); // K, Q, R, B, N
            notation.append(disambiguation);
            if (isCapture) {
                notation.append("x");
            }
            notation.append(target.getColumn());
            notation.append(target.getRow());
        }
        
        // Xeque ou xeque-mate
        if (isCheckMate) {
            notation.append("#");
        } else if (isCheck) {
            notation.append("+");
        }
        
        return notation.toString();
    }
    
    // Determina se precisa desambiguar o movimento (quando duas peças iguais podem ir para o mesmo destino)
    private String getDisambiguation(ChessPiece piece, Position source, Position target) {
        if (piece instanceof Pawn || piece instanceof King) {
            return "";
        }
        
        List<ChessPiece> samePieces = new ArrayList<>();
        for (Piece p : piecesOnTheBoard) {
            ChessPiece cp = (ChessPiece) p;
            if (cp != piece && cp.getClass() == piece.getClass() && cp.getColor() == piece.getColor()) {
                boolean[][] moves = cp.possibleMoves();
                if (moves[target.getRow()][target.getColumn()]) {
                    samePieces.add(cp);
                }
            }
        }
        
        if (samePieces.isEmpty()) {
            return "";
        }
        
        ChessPosition sourceChess = ChessPosition.fromPosition(source);
        boolean sameColumn = false;
        boolean sameRow = false;
        
        for (ChessPiece sp : samePieces) {
            ChessPosition spPos = sp.getChessPosition();
            if (spPos.getColumn() == sourceChess.getColumn()) {
                sameColumn = true;
            }
            if (spPos.getRow() == sourceChess.getRow()) {
                sameRow = true;
            }
        }
        
        if (sameColumn && sameRow) {
            return "" + sourceChess.getColumn() + sourceChess.getRow();
        } else if (sameColumn) {
            return "" + sourceChess.getRow();
        } else {
            return "" + sourceChess.getColumn();
        }
    }
    
    // Substitui peça promovida
    public ChessPiece replacePromotedPiece(String type) {
        if (promoted == null) {
            throw new IllegalStateException("Não há peça para ser promovida");
        }
        if (!type.equals("B") && !type.equals("N") && !type.equals("R") && !type.equals("Q")) {
            return promoted;
        }
        
        Position pos = promoted.getChessPosition().toPosition();
        Piece p = board.removePiece(pos);
        piecesOnTheBoard.remove(p);
        
        ChessPiece newPiece = newPiece(type, promoted.getColor());
        board.placePiece(newPiece, pos);
        piecesOnTheBoard.add(newPiece);
        
        return newPiece;
    }
    
    private ChessPiece newPiece(String type, Color color) {
        if (type.equals("B")) return new Bishop(board, color);
        if (type.equals("N")) return new Knight(board, color);
        if (type.equals("Q")) return new Queen(board, color);
        return new Rook(board, color);
    }
    
    private Piece makeMove(Position source, Position target) {
        ChessPiece p = (ChessPiece) board.removePiece(source);
        p.increaseMoveCount();
        Piece capturedPiece = board.removePiece(target);
        board.placePiece(p, target);
        
        if (capturedPiece != null) {
            piecesOnTheBoard.remove(capturedPiece);
            capturedPieces.add(capturedPiece);
        }
        
        // Roque pequeno
        if (p instanceof King && target.getColumn() == source.getColumn() + 2) {
            Position sourceT = new Position(source.getRow(), source.getColumn() + 3);
            Position targetT = new Position(source.getRow(), source.getColumn() + 1);
            ChessPiece rook = (ChessPiece) board.removePiece(sourceT);
            board.placePiece(rook, targetT);
            rook.increaseMoveCount();
        }
        
        // Roque grande
        if (p instanceof King && target.getColumn() == source.getColumn() - 2) {
            Position sourceT = new Position(source.getRow(), source.getColumn() - 4);
            Position targetT = new Position(source.getRow(), source.getColumn() - 1);
            ChessPiece rook = (ChessPiece) board.removePiece(sourceT);
            board.placePiece(rook, targetT);
            rook.increaseMoveCount();
        }
        
        // En Passant
        if (p instanceof Pawn) {
            if (source.getColumn() != target.getColumn() && capturedPiece == null) {
                Position pawnPosition;
                if (p.getColor() == Color.WHITE) {
                    pawnPosition = new Position(target.getRow() + 1, target.getColumn());
                } else {
                    pawnPosition = new Position(target.getRow() - 1, target.getColumn());
                }
                capturedPiece = board.removePiece(pawnPosition);
                capturedPieces.add(capturedPiece);
                piecesOnTheBoard.remove(capturedPiece);
            }
        }
        
        return capturedPiece;
    }
    
    private void undoMove(Position source, Position target, Piece capturedPiece) {
        ChessPiece p = (ChessPiece) board.removePiece(target);
        p.decreaseMoveCount();
        board.placePiece(p, source);
        
        if (capturedPiece != null) {
            board.placePiece(capturedPiece, target);
            capturedPieces.remove(capturedPiece);
            piecesOnTheBoard.add(capturedPiece);
        }
        
        // Desfaz roque pequeno
        if (p instanceof King && target.getColumn() == source.getColumn() + 2) {
            Position sourceT = new Position(source.getRow(), source.getColumn() + 3);
            Position targetT = new Position(source.getRow(), source.getColumn() + 1);
            ChessPiece rook = (ChessPiece) board.removePiece(targetT);
            board.placePiece(rook, sourceT);
            rook.decreaseMoveCount();
        }
        
        // Desfaz roque grande
        if (p instanceof King && target.getColumn() == source.getColumn() - 2) {
            Position sourceT = new Position(source.getRow(), source.getColumn() - 4);
            Position targetT = new Position(source.getRow(), source.getColumn() - 1);
            ChessPiece rook = (ChessPiece) board.removePiece(targetT);
            board.placePiece(rook, sourceT);
            rook.decreaseMoveCount();
        }
        
        // Desfaz En Passant
        if (p instanceof Pawn) {
            if (source.getColumn() != target.getColumn() && capturedPiece == enPassantVulnerable) {
                ChessPiece pawn = (ChessPiece) board.removePiece(target);
                Position pawnPosition;
                if (p.getColor() == Color.WHITE) {
                    pawnPosition = new Position(3, target.getColumn());
                } else {
                    pawnPosition = new Position(4, target.getColumn());
                }
                board.placePiece(pawn, pawnPosition);
            }
        }
    }
    
    private void validateSourcePosition(Position position) {
        if (!board.thereIsAPiece(position)) {
            throw new ChessException("Não existe peça na posição de origem");
        }
        if (currentPlayer != ((ChessPiece) board.piece(position)).getColor()) {
            throw new ChessException("A peça escolhida não é sua");
        }
        if (!board.piece(position).isThereAnyPossibleMove()) {
            throw new ChessException("Não existe movimentos possíveis para a peça escolhida");
        }
    }
    
    private void validateTargetPosition(Position source, Position target) {
        if (!board.piece(source).possibleMove(target)) {
            throw new ChessException("A peça escolhida não pode se mover para a posição de destino");
        }
    }
    
    private void nextTurn() {
        turn++;
        currentPlayer = (currentPlayer == Color.WHITE) ? Color.BLACK : Color.WHITE;
    }
    
    private Color opponent(Color color) {
        return (color == Color.WHITE) ? Color.BLACK : Color.WHITE;
    }
    
    private ChessPiece king(Color color) {
        List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece) x).getColor() == color).collect(Collectors.toList());
        for (Piece p : list) {
            if (p instanceof King) {
                return (ChessPiece) p;
            }
        }
        throw new IllegalStateException("Não existe rei " + color + " no tabuleiro");
    }
    
    private boolean testCheck(Color color) {
        Position kingPosition = king(color).getChessPosition().toPosition();
        List<Piece> opponentPieces = piecesOnTheBoard.stream().filter(x -> ((ChessPiece) x).getColor() == opponent(color)).collect(Collectors.toList());
        for (Piece p : opponentPieces) {
            boolean[][] mat = p.possibleMoves();
            if (mat[kingPosition.getRow()][kingPosition.getColumn()]) {
                return true;
            }
        }
        return false;
    }
    
    private boolean testCheckMate(Color color) {
        if (!testCheck(color)) {
            return false;
        }
        List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece) x).getColor() == color).collect(Collectors.toList());
        for (Piece p : list) {
            boolean[][] mat = p.possibleMoves();
            for (int i = 0; i < board.getRows(); i++) {
                for (int j = 0; j < board.getColumns(); j++) {
                    if (mat[i][j]) {
                        Position source = ((ChessPiece) p).getChessPosition().toPosition();
                        Position target = new Position(i, j);
                        Piece capturedPiece = makeMove(source, target);
                        boolean testCheck = testCheck(color);
                        undoMove(source, target, capturedPiece);
                        if (!testCheck) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
    
    private void placeNewPiece(char column, int row, ChessPiece piece) {
        board.placePiece(piece, new ChessPosition(column, row).toPosition());
        piecesOnTheBoard.add(piece);
    }
    
    // Configuração inicial das peças
    private void initialSetup() {
        // Peças brancas
        placeNewPiece('a', 1, new Rook(board, Color.WHITE));
        placeNewPiece('b', 1, new Knight(board, Color.WHITE));
        placeNewPiece('c', 1, new Bishop(board, Color.WHITE));
        placeNewPiece('d', 1, new Queen(board, Color.WHITE));
        placeNewPiece('e', 1, new King(board, Color.WHITE, this));
        placeNewPiece('f', 1, new Bishop(board, Color.WHITE));
        placeNewPiece('g', 1, new Knight(board, Color.WHITE));
        placeNewPiece('h', 1, new Rook(board, Color.WHITE));
        placeNewPiece('a', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('b', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('c', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('d', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('e', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('f', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('g', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('h', 2, new Pawn(board, Color.WHITE, this));
        
        // Peças pretas
        placeNewPiece('a', 8, new Rook(board, Color.BLACK));
        placeNewPiece('b', 8, new Knight(board, Color.BLACK));
        placeNewPiece('c', 8, new Bishop(board, Color.BLACK));
        placeNewPiece('d', 8, new Queen(board, Color.BLACK));
        placeNewPiece('e', 8, new King(board, Color.BLACK, this));
        placeNewPiece('f', 8, new Bishop(board, Color.BLACK));
        placeNewPiece('g', 8, new Knight(board, Color.BLACK));
        placeNewPiece('h', 8, new Rook(board, Color.BLACK));
        placeNewPiece('a', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('b', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('c', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('d', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('e', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('f', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('g', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('h', 7, new Pawn(board, Color.BLACK, this));
    }
}
