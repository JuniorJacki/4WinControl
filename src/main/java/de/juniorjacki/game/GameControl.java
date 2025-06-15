package de.juniorjacki.game;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GameControl {

    private GameField mainGameField = new GameField();
    private GameField.ICSValue playerColor = GameField.ICSValue.YELLOW_DROP;


    public record possibleMove(GameField.FieldPosition position,long successRate){}

    /**
     * Represents a Line of Fields on the GameField that is ordered like a part of a Win Line
     * @param associatedFields All Fields of the current Possible Win Line
     * @param direction
     */
    public record PossibleWinLine(GameField.FieldPosition originField,List<GameField.FieldPosition> associatedFields, Direction direction){
        public boolean complete() {return associatedFields.size()>3;}
        public int associatedFieldCount() {return associatedFields.size();}
        public List<GameField.FieldPosition> getNextFields() {
            List<GameField.FieldPosition> fieldPositions = new ArrayList<>(associatedFields);
            IntStream.range(associatedFields.size()-1, 3)
                    .mapToObj(this::getField)
                    .forEach(fieldPositions::add);
            return fieldPositions;
        }
        private GameField.FieldPosition getField(int index){
            if (index == 0) return originField;
            return switch (direction) {
                case UP -> new GameField.FieldPosition(originField.row(), (byte) (originField().column()-index));
                case DOWN -> new GameField.FieldPosition(originField.row(), (byte) (originField().column()+index));
                case LEFT -> new GameField.FieldPosition((byte) (originField.row()-index), originField().column());
                case RIGHT-> new GameField.FieldPosition((byte) (originField.row()+index), originField().column());
                case DIAGONAL_DOWN_LEFT -> new GameField.FieldPosition((byte) (originField.row()-index), (byte) (originField().column()+index));
                case DIAGONAL_DOWN_RIGHT -> new GameField.FieldPosition((byte) (originField.row()+index), (byte) (originField().column()+index));
                case DIAGONAL_UP_RIGHT -> new GameField.FieldPosition((byte) (originField.row()+index), (byte) (originField().column()-index));
                case DIAGONAL_UP_LEFT -> new GameField.FieldPosition((byte) (originField.row()-index), (byte) (originField().column()-index));
            };
        }
    }

    public enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT,
        DIAGONAL_UP_RIGHT,
        DIAGONAL_UP_LEFT,
        DIAGONAL_DOWN_RIGHT,
        DIAGONAL_DOWN_LEFT,
    }

    public List<possibleMove> getNextRuns(GameField gameField) {

    }


    /**
     * Smart Moves from the Algorithm
     * @param gameField
     * @return
     */
    private List<GameField.FieldPosition> getPossibleSmartMoves(GameField gameField) {

    }

    /**
     * @return Moves that are needed to prohibit the Win for the Player
     */
    private List<GameField.FieldPosition> getRequiredMoves(GameField gameField) {
        return getPossibleWinLines(gameField, playerColor).stream()
                .filter(possibleWinLine -> possibleWinLine.associatedFieldCount() > 2) // Only if 3 / 4 are already on GameField
                .flatMap(winLine -> winLine.getNextFields().stream())
                .collect(Collectors.toList());
    }

    /**
     * @return Moves that would help the Player to Win and are Bad for the Bot
     */
    private List<GameField.FieldPosition> getBadMoves(GameField gameField) {
        return getPossibleWinLines(gameField, playerColor).stream()
                .flatMap(winLine -> winLine.getNextFields().stream())
                .map(field -> new GameField.FieldPosition(field.row(), (byte) (field.column() + 1))) // One field under it
                .collect(Collectors.toList());
    }


    private List<PossibleWinLine> getPossibleWinLines(GameField gameField,GameField.ICSValue color) {
        List<GameField.FieldPosition> colorFields = getFieldsByColor(gameField,color);
        return colorFields.stream().map(position -> {return getPossibleWinLinesForField(colorFields,position);}).filter(Objects::nonNull).flatMap(List::stream).collect(Collectors.toList());
    }

    private List<PossibleWinLine> getPossibleWinLinesForField(List<GameField.FieldPosition> colorFields,GameField.FieldPosition originField) {
        CompletableFuture<List<PossibleWinLine>> futureStraight = new CompletableFuture<>();
        CompletableFuture<List<PossibleWinLine>> futureDiagonal = new CompletableFuture<>();
        CompletableFuture<List<PossibleWinLine>> futureVertical = new CompletableFuture<>();

        AtomicReference<List<GameField.FieldPosition>> fieldMapRef = new AtomicReference<>(colorFields);
        AtomicReference<GameField.FieldPosition> originFieldRef = new AtomicReference<>(originField);
        // Straight Thread
        new Thread(() -> {
            CompletableFuture<PossibleWinLine> futureStraightRight = new CompletableFuture<>();
            CompletableFuture<PossibleWinLine> futureStraightLeft = new CompletableFuture<>();
            AtomicReference<List<GameField.FieldPosition>> columnPositions = new AtomicReference<>(filterFieldsForColumnOfOrigin(fieldMapRef.get(), originFieldRef.get()));
            if (columnPositions.get().size()<=1) { // IF Only origin is in Column directly return
                futureStraight.complete(null);
                return;
            }
            // Straight Right Thread
            new Thread(() -> {
                searchForStraightPossibleWinLine(columnPositions, originFieldRef, futureStraightRight,Direction.RIGHT);
            }).start();
            // Straight Left Thread
            new Thread(() -> {
                searchForStraightPossibleWinLine(columnPositions, originFieldRef, futureStraightLeft,Direction.LEFT);
            }).start();
            futureStraight.complete(getListOfFutures(futureStraightRight,futureStraightLeft));
        }).start();


        // Diagonal Thread
        new Thread(() -> {
            CompletableFuture<PossibleWinLine> futureDiagonalUpRight = new CompletableFuture<>();
            CompletableFuture<PossibleWinLine> futureDiagonalUpLeft = new CompletableFuture<>();
            CompletableFuture<PossibleWinLine> futureDiagonalDownRight = new CompletableFuture<>();
            CompletableFuture<PossibleWinLine> futureDiagonalDownLeft = new CompletableFuture<>();
            // Diagonal RIGHT UP THREAD
            new Thread(() -> {
                searchForDirectionalPossibleWinLine(fieldMapRef, originFieldRef, futureDiagonalUpRight,Direction.DIAGONAL_UP_RIGHT);
            }).start();
            // Diagonal LEFT UP THREAD
            new Thread(() -> {
                searchForDirectionalPossibleWinLine(fieldMapRef, originFieldRef, futureDiagonalUpLeft,Direction.DIAGONAL_UP_LEFT);
            }).start();
            // Diagonal RIGHT Down THREAD
            new Thread(() -> {
                searchForDirectionalPossibleWinLine(fieldMapRef, originFieldRef, futureDiagonalDownRight,Direction.DIAGONAL_DOWN_RIGHT);
            }).start();
            // Diagonal Left Down THREAD
            new Thread(() -> {
                searchForDirectionalPossibleWinLine(fieldMapRef, originFieldRef, futureDiagonalDownLeft,Direction.DIAGONAL_DOWN_LEFT);
            }).start();
            futureDiagonal.complete(getListOfFutures(futureDiagonalDownLeft,futureDiagonalUpLeft,futureDiagonalDownRight,futureDiagonalUpRight));
        }).start();

        // Vertical Thread
        new Thread(() -> {
            CompletableFuture<PossibleWinLine> futureUp = new CompletableFuture<>();
            CompletableFuture<PossibleWinLine> futureDown = new CompletableFuture<>();
            AtomicReference<List<GameField.FieldPosition>> rowPositions = new AtomicReference<>(filterFieldsForColumnOfOrigin(fieldMapRef.get(), originFieldRef.get()));
            if (rowPositions.get().size()<=1) { // IF Only origin is in Row directly return
                futureVertical.complete(null);
                return;
            }
            // Straight Right Thread
            new Thread(() -> {
                searchForVerticalPossibleWinLine(rowPositions, originFieldRef, futureUp,Direction.UP);
            }).start();
            // Straight Left Thread
            new Thread(() -> {
                searchForVerticalPossibleWinLine(rowPositions, originFieldRef,futureDown,Direction.DOWN);
            }).start();
            futureVertical.complete(getListOfFutures(futureUp,futureDown));
        }).start();
        return getListOfFuturesLists(futureStraight,futureDiagonal,futureVertical);
    }

    @SafeVarargs
    private static List<PossibleWinLine> getListOfFutures(CompletableFuture<PossibleWinLine>... futures) {
        return Arrays.stream(futures).map(future -> {try {return future.get();} catch (Exception ignored) {return null;}}).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @SafeVarargs
    private static List<PossibleWinLine> getListOfFuturesLists(CompletableFuture<List<PossibleWinLine>>... futures) {
        return Arrays.stream(futures).map(future -> {try {return future.get();} catch (Exception ignored) {return null;}}).filter(Objects::nonNull).flatMap(List::stream).collect(Collectors.toList());
    }

    private static void searchForVerticalPossibleWinLine(AtomicReference<List<GameField.FieldPosition>> rowPositionsRef, AtomicReference<GameField.FieldPosition> originFieldRef, CompletableFuture<PossibleWinLine> resultFuture,Direction direction) {
        if (direction != Direction.UP && direction != Direction.DOWN) { throw new InputMismatchException("Wrong Input: Direction");}
        try {
            if (rowPositionsRef.get().stream().anyMatch(position -> position.column() == originFieldRef.get().column()+(direction == Direction.UP ? +1 : -1))) { // if originField is not the (for Direction Right: Left Field) (for Direction Left: Right Field)  of a Win Line
                resultFuture.complete(null);
            }
            List<GameField.FieldPosition> winLineFields = new ArrayList<>(List.of(originFieldRef.get()));
            int count = 0;
            while (count <= 2) {
                count++;
                int finalCount = count;
                if (rowPositionsRef.get().stream().anyMatch(position -> position.column() == originFieldRef.get().column()+(direction == Direction.UP ? -finalCount : +finalCount))) {
                    winLineFields.add(originFieldRef.get());
                } else break;
            }
            if (count < 2) {
                resultFuture.complete(null);
            }
            resultFuture.complete(new PossibleWinLine(originFieldRef.get(),winLineFields,direction));
        } catch (Exception ignored) {
            resultFuture.complete(null);
        }
    }

    private static void searchForDirectionalPossibleWinLine(AtomicReference<List<GameField.FieldPosition>> fieldMapRef, AtomicReference<GameField.FieldPosition> originFieldRef, CompletableFuture<PossibleWinLine> resultFuture,Direction direction) {
        if (direction != Direction.DIAGONAL_UP_RIGHT && direction != Direction.DIAGONAL_DOWN_LEFT && direction != Direction.DIAGONAL_DOWN_RIGHT && direction != Direction.DIAGONAL_UP_LEFT) { throw new InputMismatchException("Wrong Input: Direction");}
        try {
            if (fieldMapRef.get().stream().anyMatch(position -> position.row() == originFieldRef.get().row()+(direction == Direction.DIAGONAL_DOWN_LEFT || direction == Direction.DIAGONAL_UP_LEFT ? +1 : -1) &&  position.column() == originFieldRef.get().column()+(direction == Direction.DIAGONAL_DOWN_LEFT || direction == Direction.DIAGONAL_DOWN_RIGHT ? +1 : -1))) { // if originField is not the start of a Win Line
                resultFuture.complete(null);
            }
            List<GameField.FieldPosition> winLineFields = new ArrayList<>(List.of(originFieldRef.get()));
            int count = 0;
            while (count <= 2) {
                count++;
                int finalCount = count;
                if (fieldMapRef.get().stream().anyMatch(position -> position.row() == originFieldRef.get().row()+(direction == Direction.DIAGONAL_DOWN_LEFT || direction == Direction.DIAGONAL_UP_LEFT ? -finalCount : +finalCount) &&  position.column() == originFieldRef.get().column()+(direction == Direction.DIAGONAL_DOWN_LEFT || direction == Direction.DIAGONAL_DOWN_RIGHT ? -finalCount : +finalCount))) {
                    winLineFields.add(originFieldRef.get());
                } else break;
            }
            if (count < 2) {
                resultFuture.complete(null);
            }
            resultFuture.complete(new PossibleWinLine(originFieldRef.get(),winLineFields,direction));
        } catch (Exception ignored) {
            resultFuture.complete(null);
        }

    }

    private static void searchForStraightPossibleWinLine(AtomicReference<List<GameField.FieldPosition>> columnPositionsRef, AtomicReference<GameField.FieldPosition> originFieldRef, CompletableFuture<PossibleWinLine> resultFuture,Direction direction) {
        if (direction != Direction.RIGHT && direction != Direction.LEFT) { throw new InputMismatchException("Wrong Input: Direction");}
        try {
            if (columnPositionsRef.get().stream().anyMatch(position -> position.row() == originFieldRef.get().row()+(direction == Direction.RIGHT ? -1 : +1))) { // if originField is not the (for Direction Right: Left Field) (for Direction Left: Right Field)  of a Win Line
                resultFuture.complete(null);
            }
            List<GameField.FieldPosition> winLineFields = new ArrayList<>(List.of(originFieldRef.get()));
            int count = 0;
            while (count <= 2) {
                count++;
                int finalCount = count;
                if (columnPositionsRef.get().stream().anyMatch(position -> position.row() == originFieldRef.get().row()+(direction == Direction.RIGHT ? +finalCount : -finalCount))) {
                    winLineFields.add(originFieldRef.get());
                } else break;
            }
            if (count < 2) {
                resultFuture.complete(null);
            }
            resultFuture.complete(new PossibleWinLine(originFieldRef.get(),winLineFields,direction));
        } catch (Exception ignored) {
            resultFuture.complete(null);
        }
    }

    private static List<GameField.FieldPosition> filterFieldsForColumnOfOrigin(List<GameField.FieldPosition> fieldPositions, GameField.FieldPosition origin) {
        return fieldPositions.stream().filter(fieldPosition -> fieldPosition.column() == origin.column()).toList();
    }

    private static List<GameField.FieldPosition> filterFieldsForRowOfOrigin(List<GameField.FieldPosition> fieldPositions, GameField.FieldPosition origin) {
        return fieldPositions.stream().filter(fieldPosition -> fieldPosition.row() == origin.row()).toList();
    }

    private List<GameField.FieldPosition> getFieldsByColor(GameField gameField, GameField.ICSValue color) {
        List<GameField.FieldPosition> coloredFields = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            for (int j = 5; j >= 0; j--) {
                GameField.FieldPosition position = new GameField.FieldPosition((byte) i, (byte) j);
                if (gameField.getFieldICSValue(position).equals(color)) coloredFields.add(position);
            }
        }
        return coloredFields;
    }



    /**
     * @param gameField GameField for Operation
     * @return Maximum 7 Positions of Possible Moves at current State of the GameField
     */
    private List<GameField.FieldPosition> getPossibleNextMoves(GameField gameField) {
        List<GameField.FieldPosition> possibleNextMove = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            for (int j = 5; j >= 0; j--) {
                GameField.FieldPosition position = new GameField.FieldPosition((byte) i, (byte) j);
                if (gameField.getFieldICSValue(position).equals(GameField.ICSValue.AIR)) {
                    possibleNextMove.add(position);
                    break;
                }
            }
        }
        return possibleNextMove;
    }

    public HashMap<Byte, HashMap<Byte, Byte>> getGameFieldAsSingleMap(GameField gameField) {
        HashMap<Byte, HashMap<Byte, Byte>> map = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            int finalI = i;
            gameField.getRow((byte) i).ifPresent(hashMap -> {map.put((byte) finalI,hashMap);});
        }
        return map;
    }

    public void newFieldData(GameField.FieldPosition position, byte value) {
        mainGameField.updateField(position, value);
    }


    public GameField getMainGameField() {
        return mainGameField;
    }
}
