# Relatório do Projeto - Jogo de Xadrez em Java

## 1. Introdução

Este relatório documenta o desenvolvimento de um sistema de jogo de xadrez completo em Java, implementado seguindo o paradigma de Programação Orientada a Objetos (POO) e o padrão de arquitetura em camadas (Layered Pattern).

---

## 2. Decisões de Projeto

### 2.1 Arquitetura em Camadas

Optou-se por dividir o sistema em duas camadas principais:

| Camada | Pacote | Responsabilidade |
|--------|--------|------------------|
| **Board Layer** | `boardlayer` | Estrutura genérica de tabuleiro e peças |
| **Chess Layer** | `chesslayer` | Regras específicas do xadrez |
| **Application** | `application` | Interface com o usuário |

**Justificativa**: Esta separação permite reutilização do código. A camada de tabuleiro poderia ser utilizada para outros jogos de tabuleiro (damas, go, etc.), enquanto a camada de xadrez implementa apenas as regras específicas.

### 2.2 Uso de Classes Abstratas

- `Piece`: Classe abstrata na camada de tabuleiro
- `ChessPiece`: Classe abstrata na camada de xadrez

**Justificativa**: Permite definir comportamentos comuns e forçar implementação de métodos específicos (como `possibleMoves()`) nas subclasses.

### 2.3 Polimorfismo nos Movimentos

Cada peça implementa seu próprio `possibleMoves()`:

```java
// Na classe King
@Override
public boolean[][] possibleMoves() {
    // Movimento de 1 casa em todas as direções
}

// Na classe Rook
@Override
public boolean[][] possibleMoves() {
    // Movimento ilimitado horizontal/vertical
}
```

**Justificativa**: Permite que `ChessMatch` trate todas as peças de forma uniforme, sem precisar saber qual peça específica está manipulando.

### 2.4 Sistema de Coordenadas Duplo

O sistema mantém duas representações de posição:

| Classe | Formato | Uso |
|--------|---------|-----|
| `Position` | (0,0) a (7,7) | Interno - manipulação de matriz |
| `ChessPosition` | a1 a h8 | Externo - interação com usuário |

**Conversão**:
- `e2` → `Position(6, 4)`
- Fórmula: `row = 8 - chessRow`, `column = chessColumn - 'a'`

### 2.5 Defensive Programming

Validações em múltiplos níveis:

1. **Board Layer**: `positionExists()`, `thereIsAPiece()`
2. **Chess Layer**: `validateSourcePosition()`, `validateTargetPosition()`
3. **Application**: Try-catch para `ChessException` e `InputMismatchException`

---

## 3. Regras Implementadas

### 3.1 Movimentos Básicos

| Peça | Símbolo | Regra de Movimento |
|------|---------|-------------------|
| Rei (King) | K | 1 casa em qualquer direção |
| Rainha (Queen) | Q | Ilimitado em todas as 8 direções |
| Torre (Rook) | R | Ilimitado horizontal e vertical |
| Bispo (Bishop) | B | Ilimitado nas 4 diagonais |
| Cavalo (Knight) | N | Movimento em "L" (pode pular peças) |
| Peão (Pawn) | P | 1 casa à frente (2 no primeiro movimento) |

### 3.2 Movimentos Especiais

#### Roque (Castling)
- **Pequeno**: Rei move 2 casas para direita, Torre passa para o outro lado
- **Grande**: Rei move 2 casas para esquerda, Torre passa para o outro lado
- **Condições**: Rei e Torre sem movimentos anteriores, caminho livre, rei não em xeque

```java
// Verificação no King.possibleMoves()
if (getMoveCount() == 0 && !chessMatch.getCheck()) {
    // Roque pequeno
    Position posT1 = new Position(position.getRow(), position.getColumn() + 3);
    if (testRookCastling(posT1)) { ... }
}
```

#### En Passant
- Peão pode capturar peão adversário que acabou de mover 2 casas
- Válido apenas no turno imediatamente após o movimento do adversário

```java
// Propriedade em ChessMatch
private ChessPiece enPassantVulnerable;

// Marcação após movimento de 2 casas
if (movedPiece instanceof Pawn && Math.abs(target.getRow() - source.getRow()) == 2) {
    enPassantVulnerable = movedPiece;
}
```

#### Promoção
- Peão que atinge a última fileira é promovido
- Opções: Rainha (Q), Torre (R), Bispo (B) ou Cavalo (N)

### 3.3 Xeque e Xeque-Mate

#### Detecção de Xeque
```java
private boolean testCheck(Color color) {
    Position kingPosition = king(color).getChessPosition().toPosition();
    List<Piece> opponentPieces = // peças do oponente
    for (Piece p : opponentPieces) {
        if (p.possibleMoves()[kingPosition.getRow()][kingPosition.getColumn()]) {
            return true; // Rei está ameaçado
        }
    }
    return false;
}
```

#### Prevenção de Auto-Xeque
Todo movimento é simulado e desfeito se resultar em xeque próprio:

```java
Piece capturedPiece = makeMove(source, target);
if (testCheck(currentPlayer)) {
    undoMove(source, target, capturedPiece);
    throw new ChessException("Você não pode se colocar em xeque!");
}
```

#### Detecção de Xeque-Mate
```java
private boolean testCheckMate(Color color) {
    if (!testCheck(color)) return false;
    
    // Para cada peça do jogador
    for (Piece p : playerPieces) {
        // Para cada movimento possível
        for (cada posição possível) {
            // Simula movimento
            // Se sair do xeque, não é mate
            if (!testCheck(color)) return false;
            // Desfaz movimento
        }
    }
    return true; // Nenhum movimento tira do xeque
}
```

---

## 4. Interface do Usuário

### 4.1 Visualização do Tabuleiro
```
8 R N B Q K B N R 
7 P P P P P P P P 
6 - - - - - - - - 
5 - - - - - - - - 
4 - - - - - - - - 
3 - - - - - - - - 
2 P P P P P P P P 
1 R N B Q K B N R 
  a b c d e f g h
```

### 4.2 Cores ANSI
| Elemento | Cor |
|----------|-----|
| Peças brancas | Branco (`\u001B[37m`) |
| Peças pretas | Amarelo (`\u001B[33m`) |
| Movimentos possíveis | Fundo azul (`\u001B[44m`) |
| Xeque | Vermelho (`\u001B[31m`) |
| Xeque-mate | Verde (`\u001B[32m`) |

### 4.3 Fluxo de Jogo
1. Exibir tabuleiro e informações
2. Solicitar posição de origem
3. Destacar movimentos possíveis
4. Solicitar posição de destino
5. Executar movimento
6. Verificar promoção (se aplicável)
7. Alternar turno
8. Repetir até xeque-mate

---

## 5. Conceitos de POO Aplicados

| Conceito | Aplicação |
|----------|-----------|
| **Encapsulamento** | Atributos privados com getters/setters |
| **Herança** | ChessPiece extends Piece, King/Queen/etc extends ChessPiece |
| **Polimorfismo** | Cada peça implementa possibleMoves() diferente |
| **Abstração** | Classes abstratas Piece e ChessPiece |
| **Composição** | Board contém matriz de Piece |
| **Agregação** | ChessMatch conhece listas de peças |

---

## 6. Tratamento de Exceções

| Exceção | Camada | Situações |
|---------|--------|-----------|
| `BoardException` | Board | Posição inválida, peça já existente |
| `ChessException` | Chess | Movimento ilegal, peça do oponente, auto-xeque |
| `InputMismatchException` | Application | Entrada inválida do usuário |

---

## 7. Estrutura de Arquivos

```
Xadrez2/
├── application/
│   ├── Program.java          # Main
│   ├── UI.java               # Interface console
├── boardlayer/
│   ├── Board.java
│   ├── BoardException.java
│   ├── Piece.java
│   └── Position.java
├── chesslayer/
│   ├── ChessException.java
│   ├── ChessMatch.java
│   ├── ChessPiece.java
│   ├── ChessPosition.java
│   ├── Color.java
│   └── pieces/
│       ├── Bishop.java
│       ├── King.java
│       ├── Knight.java
│       ├── Pawn.java
│       ├── Queen.java
│       └── Rook.java
├── docs/
│   ├── DOCUMENTACAO.md
│   ├── UML.md
│   └── RELATORIO.md
├── .gitignore
└── README.md
```

---

## 8. Conclusão

O sistema implementa com sucesso todas as regras oficiais do xadrez, incluindo movimentos especiais (roque, en passant, promoção), detecção de xeque e xeque-mate, seguindo boas práticas de programação orientada a objetos e arquitetura em camadas.

A separação em camadas facilita manutenção e possíveis extensões futuras, como implementação de IA, histórico de partidas ou interface gráfica avançada.

---

**Autor**: Guilherme1737  
**Data**: Dezembro 2025  
**Linguagem**: Java  
**Repositório**: github.com/Guilherme1737/Xadrez
