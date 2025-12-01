package boardlayer;

public abstract class Piece {
    
    protected Position position;
    private Board board;
    
    public Piece(Board board) {
        this.board = board;
        position = null; // Peça ainda não está no tabuleiro
    }
    
    protected Board getBoard() {
        return board;
    }
    
    // Retorna matriz de movimentos possíveis
    public abstract boolean[][] possibleMoves();
    
    // Verifica se existe pelo menos um movimento possível para a posição
    public boolean possibleMove(Position position) {
        return possibleMoves()[position.getRow()][position.getColumn()];
    }
    
    // Verifica se a peça está bloqueada (sem movimentos possíveis)
    public boolean isThereAnyPossibleMove() {
        boolean[][] mat = possibleMoves();
        for (int i = 0; i < mat.length; i++) {
            for (int j = 0; j < mat.length; j++) {
                if (mat[i][j]) {
                    return true;
                }
            }
        }
        return false;
    }
}
