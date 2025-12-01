# Documentação das Classes - Jogo de Xadrez

## Visão Geral da Arquitetura

O sistema segue o padrão de **arquitetura em camadas (Layered Pattern)**, dividido em:

- **Board Layer** (`boardlayer/`): Camada genérica de tabuleiro
- **Chess Layer** (`chesslayer/`): Camada de regras do xadrez
- **Application** (`application/`): Interface com o usuário

---

## Board Layer (Camada de Tabuleiro)

### `Position`
Representa uma posição na matriz do tabuleiro.

| Atributo | Tipo | Descrição |
|----------|------|-----------|
| `row` | int | Linha (0-7) |
| `column` | int | Coluna (0-7) |

| Método | Retorno | Descrição |
|--------|---------|-----------|
| `getRow()` | int | Retorna a linha |
| `getColumn()` | int | Retorna a coluna |
| `setValues(row, column)` | void | Define linha e coluna |
| `toString()` | String | Representação textual |

---

### `Board`
Gerencia o tabuleiro 8x8 e as peças.

| Atributo | Tipo | Descrição |
|----------|------|-----------|
| `rows` | int | Número de linhas |
| `columns` | int | Número de colunas |
| `pieces` | Piece[][] | Matriz de peças |

| Método | Retorno | Descrição |
|--------|---------|-----------|
| `piece(row, column)` | Piece | Retorna peça na posição |
| `piece(position)` | Piece | Retorna peça na posição |
| `placePiece(piece, position)` | void | Coloca peça no tabuleiro |
| `removePiece(position)` | Piece | Remove e retorna peça |
| `positionExists(position)` | boolean | Valida se posição existe |
| `thereIsAPiece(position)` | boolean | Verifica se há peça |

---

### `Piece` (Abstrata)
Classe base para todas as peças.

| Atributo | Tipo | Descrição |
|----------|------|-----------|
| `position` | Position | Posição atual (protected) |
| `board` | Board | Referência ao tabuleiro |

| Método | Retorno | Descrição |
|--------|---------|-----------|
| `possibleMoves()` | boolean[][] | **Abstrato** - Movimentos possíveis |
| `possibleMove(position)` | boolean | Verifica movimento específico |
| `isThereAnyPossibleMove()` | boolean | Verifica se há movimentos |

---

### `BoardException`
Exceção para erros de tabuleiro (posição inválida, peça existente).

---

## Chess Layer (Camada de Xadrez)

### `Color` (Enum)
```java
enum Color { WHITE, BLACK }
```

---

### `ChessPosition`
Converte coordenadas de xadrez (a1-h8) para matriz (0,0-7,7).

| Método | Retorno | Descrição |
|--------|---------|-----------|
| `toPosition()` | Position | Converte para posição de matriz |
| `fromPosition(position)` | ChessPosition | Converte de matriz para xadrez |

**Exemplo de conversão:**
- `a1` → `Position(7, 0)`
- `e4` → `Position(4, 4)`
- `h8` → `Position(0, 7)`

---

### `ChessPiece` (Abstrata)
Classe base para peças de xadrez.

| Atributo | Tipo | Descrição |
|----------|------|-----------|
| `color` | Color | Cor da peça |
| `moveCount` | int | Contador de movimentos |

| Método | Retorno | Descrição |
|--------|---------|-----------|
| `getColor()` | Color | Retorna cor |
| `getMoveCount()` | int | Retorna nº de movimentos |
| `increaseMoveCount()` | void | Incrementa contador |
| `decreaseMoveCount()` | void | Decrementa contador |
| `getChessPosition()` | ChessPosition | Posição no formato xadrez |
| `isThereOpponentPiece(pos)` | boolean | Verifica peça adversária |

---

### `ChessMatch`
**Classe principal** - Gerencia toda a partida.

| Atributo | Tipo | Descrição |
|----------|------|-----------|
| `turn` | int | Turno atual |
| `currentPlayer` | Color | Jogador da vez |
| `board` | Board | Tabuleiro |
| `check` | boolean | Estado de xeque |
| `checkMate` | boolean | Estado de xeque-mate |
| `enPassantVulnerable` | ChessPiece | Peão vulnerável a en passant |
| `promoted` | ChessPiece | Peão promovido |
| `piecesOnTheBoard` | List<Piece> | Peças em jogo |
| `capturedPieces` | List<Piece> | Peças capturadas |

| Método | Retorno | Descrição |
|--------|---------|-----------|
| `getPieces()` | ChessPiece[][] | Matriz de peças para UI |
| `possibleMoves(source)` | boolean[][] | Movimentos possíveis |
| `performChessMove(source, target)` | ChessPiece | Executa movimento |
| `replacePromotedPiece(type)` | ChessPiece | Substitui peão promovido |
| `validateSourcePosition(pos)` | void | Valida origem |
| `validateTargetPosition(src, tgt)` | void | Valida destino |
| `testCheck(color)` | boolean | Testa xeque |
| `testCheckMate(color)` | boolean | Testa xeque-mate |

---

### Peças (`chesslayer/pieces/`)

Todas herdam de `ChessPiece` e implementam `possibleMoves()`.

| Classe | Símbolo | Movimento |
|--------|---------|-----------|
| `King` | K | 1 casa em qualquer direção + Roque |
| `Queen` | Q | Ilimitado em todas as direções |
| `Rook` | R | Ilimitado horizontal/vertical |
| `Bishop` | B | Ilimitado diagonal |
| `Knight` | N | Movimento em "L" |
| `Pawn` | P | 1 casa (2 no início) + captura diagonal + En Passant |

---

### `ChessException`
Exceção para erros de regra do xadrez.

---

## Application Layer

### `UI`
Interface de usuário no console.

| Método | Descrição |
|--------|-----------|
| `clearScreen()` | Limpa o terminal |
| `readChessPosition(sc)` | Lê posição do usuário |
| `printMatch(match, captured)` | Imprime estado do jogo |
| `printBoard(pieces)` | Imprime tabuleiro |
| `printBoard(pieces, possibleMoves)` | Imprime com destaques |
| `printCapturedPieces(captured)` | Lista peças capturadas |

### `Program`
Ponto de entrada da aplicação (`main`).

---

## Códigos ANSI (Cores)

| Código | Uso |
|--------|-----|
| `ANSI_WHITE` | Peças brancas |
| `ANSI_YELLOW` | Peças pretas |
| `ANSI_BLUE_BACKGROUND` | Movimentos possíveis |
| `ANSI_RED` | Xeque |
| `ANSI_GREEN` | Xeque-mate |
