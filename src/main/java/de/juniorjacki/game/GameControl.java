package de.juniorjacki.game;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class GameControl {

    private GameField mainGameField = new GameField();
    private GameField.ICSValue playerColor = GameField.ICSValue.YELLOW;
    private GameField.ICSValue botColor = GameField.ICSValue.RED;


    /**
     * Represent a Move decided by the Algorithm to Win the Game
     * @param position Move that should be done
     * @param targetWinLine WinLine that is targeted by the Algorithm
     * @param neededMovesToWinAfter Needed Moves to fill the WinLine and Win
     */
    public record AlgorithmMove(GameField.FieldPosition position,PossibleWinLine targetWinLine, int neededMovesToWinAfter){

        @Override
        public String toString() {
            return "Position: " + position.row()+" , "+position.column() + " TargetWinLine Direction: " + targetWinLine.direction() + " NeededMovesToWinAfter: " + neededMovesToWinAfter;
        }
    }

    /**
     * Represents a Line of Fields on the GameField that is ordered like a part of a Win Line (4 in a Line)
     * @param associatedFields All Fields of the current Possible Win Line
     * @param direction
     */
    public record PossibleWinLine(GameField.FieldPosition originField,List<GameField.FieldPosition> associatedFields, Direction direction){

        /**
         * @return If WinLine is already filled Completely
         */
        public boolean complete() {return associatedFields.size()>3;}

        /**
         * @return How many fields of the WinLine already Filled with the Line Color
         */
        public int associatedFieldCount() {return associatedFields.size();}

        public int neededFieldsForLine() {return 4-associatedFields.size();}

        /**
         * @return The remaining fields needed to be filled for the WinLine
         */
        public List<GameField.FieldPosition> getNextFields() {
            List<GameField.FieldPosition> fieldPositions = new ArrayList<>();
            IntStream.range(associatedFields.size(), 4)
                    .mapToObj(this::getField)
                    .forEach(fieldPositions::add);
            return fieldPositions;
        }

        /**
         * @param index Origin = 0 : Index of Field started at Origin in WinLine Direction
         * @return The Line FieldPosition specified by Index
         */
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

        /**
         * Checks if WinLine is possible
         * @return if WinLine is in GameField and all Needed Fields are filled with AIR returns itself. If it goes over the Corner or is not anymore Possible returns null
         */
        public PossibleWinLine isPossible(GameField gameField,GameField.ICSValue lineColor) {
            return isLinePossible(gameField,lineColor ) ? this : null;
        }

        /**
         * Checks if WinLine is possible
         * @return if WinLine is in GameField and all Needed Fields are filled with AIR returns True. If it goes over the Corner or is not anymore Possible returns False
         */
        public boolean isLinePossible(GameField gameField, GameField.ICSValue lineColor){
            if (isWinLineInField())  {
                for (GameField.FieldPosition neededFieldToWin : getNextFields()) {
                    if (!(neededFieldToWin.isFilledWith(gameField,lineColor) || neededFieldToWin.isEmpty(gameField))) return false;
                }
                return true;
            };
            return false;
        }

        /**
         * Checks if all Fields of the WinLine are in the GameField
         */
        private boolean isWinLineInField() {
            return Stream.concat(associatedFields.stream(), getNextFields().stream())
                    .allMatch(field -> field.column() >= 0 && field.column() <= 5 &&
                            field.row() >= 0 && field.row() <= 6);
        }

        /**
         *
         * @param gameField The GameField the Operation uses
         * @return A Sorted Queue of needed Moves to fill the Line
         */
        public Queue<GameField.FieldPosition> getNeededMovesToFillLine(GameField gameField) {
            return getNextFields().stream()
                    .flatMap(field -> field.getNeededMovesToFillField(gameField).stream().flatMap(Collection::stream))
                    .distinct()
                    .collect(Collectors.toCollection(LinkedList::new));
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
        DIAGONAL_DOWN_LEFT
    }




    public static void main(String[] args) {
        GameControl control = new GameControl();
        GameField gameField = new GameField()
                .updateField(new GameField.FieldPosition((byte) 0, (byte) 5), (byte) 42)
                .updateField(new GameField.FieldPosition((byte) 1, (byte) 4), (byte) 42)
                .updateField(new GameField.FieldPosition((byte) 0, (byte) 4), (byte) 42)
                .updateField(new GameField.FieldPosition((byte) 4, (byte) 5), (byte) 42)

                .updateField(new GameField.FieldPosition((byte) 1, (byte) 5), (byte) 80)
                .updateField(new GameField.FieldPosition((byte) 2, (byte) 5), (byte) 80)
                .updateField(new GameField.FieldPosition((byte) 2, (byte) 4), (byte) 80)
                .updateField(new GameField.FieldPosition((byte) 3, (byte) 5), (byte) 80)
                .updateField(new GameField.FieldPosition((byte) 3, (byte) 4), (byte) 80)
                ;

        GameField gameField2 = new GameField()
                .updateField(new GameField.FieldPosition((byte) 0, (byte) 5), (byte) 42) // Rot
                .updateField(new GameField.FieldPosition((byte) 0, (byte) 4), (byte) 80) // Gelb
                .updateField(new GameField.FieldPosition((byte) 1, (byte) 5), (byte) 80) // Gelb
                .updateField(new GameField.FieldPosition((byte) 2, (byte) 5), (byte) 42) // Rot
                .updateField(new GameField.FieldPosition((byte) 2, (byte) 4), (byte) 80) // Gelb
                .updateField(new GameField.FieldPosition((byte) 3, (byte) 5), (byte) 42) // Rot
                .updateField(new GameField.FieldPosition((byte) 4, (byte) 5), (byte) 80) // Gelb
                .updateField(new GameField.FieldPosition((byte) 4, (byte) 4), (byte) 42); // Rot

        /*
                    0: . . . . . . .
                    1: . . . . . . .
                    2: . . . . . . .
                    3: . . . . . . .
                    4: Y . Y . R . .
                    5: R Y R R Y Y .
                       0 1 2 3 4 5 6
         */


        GameField gameField3 = new GameField()
                .updateField(new GameField.FieldPosition((byte) 2, (byte) 5), (byte) 80) // Gelb
                .updateField(new GameField.FieldPosition((byte) 2, (byte) 4), (byte) 80) // Gelb
                .updateField(new GameField.FieldPosition((byte) 2, (byte) 3), (byte) 80) // Gelb
                .updateField(new GameField.FieldPosition((byte) 3, (byte) 5), (byte) 42) // Rot
                .updateField(new GameField.FieldPosition((byte) 3, (byte) 4), (byte) 42) // Rot
                .updateField(new GameField.FieldPosition((byte) 4, (byte) 5), (byte) 80) // Gelb
                .updateField(new GameField.FieldPosition((byte) 4, (byte) 4), (byte) 42) // Rot
                .updateField(new GameField.FieldPosition((byte) 5, (byte) 5), (byte) 42); // Rot

        /*
                    0: . . . . . . .
                    1: . . . . . . .
                    2: . . . . . . .
                    3: . . Y . . . .
                    4: . . Y . R . .
                    5: . . Y R Y R .
                       0 1 2 3 4 5 6
         */


        GameField gameField4 = new GameField()
                .updateField(new GameField.FieldPosition((byte) 1, (byte) 5), (byte) 42) // Rot
                .updateField(new GameField.FieldPosition((byte) 2, (byte) 5), (byte) 80) // Gelb
                .updateField(new GameField.FieldPosition((byte) 2, (byte) 4), (byte) 42) // Rot
                .updateField(new GameField.FieldPosition((byte) 3, (byte) 5), (byte) 80) // Gelb
                .updateField(new GameField.FieldPosition((byte) 3, (byte) 4), (byte) 80) // Gelb
                .updateField(new GameField.FieldPosition((byte) 3, (byte) 3), (byte) 42) // Rot
                .updateField(new GameField.FieldPosition((byte) 4, (byte) 5), (byte) 42) // Rot
                .updateField(new GameField.FieldPosition((byte) 4, (byte) 4), (byte) 80) // Gelb
                .updateField(new GameField.FieldPosition((byte) 4, (byte) 3), (byte) 80); // Gelb

        /*
                    0: . . . . . . .
                    1: . . . . . . .
                    2: . . . . . . .
                    3: . . . R Y . .
                    4: . . R Y Y . .
                    5: . R Y Y R Y .
                       0 1 2 3 4 5 6
         */

        System.out.println("Color Fields Player " + control.getFieldsByColor(gameField,control.playerColor).size());
        System.out.println("Color Fields Bot " + control.getFieldsByColor(gameField,control.botColor).size());


        GameField kian = new GameField()
                .updateField(new GameField.FieldPosition((byte) 3, (byte) 5), (byte) 80)
                .updateField(new GameField.FieldPosition((byte) 0, (byte) 5), (byte) 40)


                .updateField(new GameField.FieldPosition((byte) 0, (byte) 4), (byte) 80)
                .updateField(new GameField.FieldPosition((byte) 0, (byte) 3), (byte) 40)


                .updateField(new GameField.FieldPosition((byte) 4, (byte) 5), (byte) 80)

                ;

         /*
                    0: . . . . . . .
                    1: . . . . . . .
                    2: . . . . . . .
                    3: R . . . . . .
                    4: Y . . . . . .
                    5: R . . Y Y . .
                       0 1 2 3 4 5 6
         */

        //System.out.println(control.getCurrentPossibleWinLines(gameField,control.botColor));
        long time = System.currentTimeMillis();
        AlgorithmMove move = control.getPossibleSmartMove(kian);
        if (move == null) System.out.println("is leer");
        System.out.println(move.toString());
        System.out.println("Calculation Time:" + (System.currentTimeMillis() - time));
    }


    /**
     * Smart Moves from the Algorithm
     * @param gameField The GameField the Algorithm uses
     * @return  Null if no Move was Found or the Next AlgorithmMove that Should be done associated with The Line the Bot wants to Fill to Win
     */
    private AlgorithmMove getPossibleSmartMove(GameField gameField) {

        // MIN-MAX Algorithm
        List<PossibleWinLine> botCurrentWinLines = getCurrentPossibleWinLines(gameField,botColor).stream()
                .sorted(Comparator.comparingInt(winLine -> winLine.getNeededMovesToFillLine(gameField).size()))
                .collect(Collectors.toList()); // Sorted WinLine List by needed Moves to complete
        List<GameField.FieldPosition> badMoves = getBadMoves(gameField);

        AlgorithmMove currenWinLinesMove = filterMoves(gameField, botCurrentWinLines, badMoves);
        if (currenWinLinesMove != null) {
            if (currenWinLinesMove.neededMovesToWinAfter == 0) {
                return currenWinLinesMove;
            }
        }

        List<GameField.FieldPosition> requiredMoves = getRequiredMoves(gameField);
        if (!requiredMoves.isEmpty()) {
            GameField.FieldPosition neededMoveToProhibitPlayerWin = requiredMoves.stream()
                    .filter(field -> field.getNeededMovesToFillField(gameField)
                            .map(Collection::size)
                            .orElse(0) == 1)
                    .findFirst()
                    .orElse(null); // Gets the needed move to prohibit WinLine of Player if one move is needed to complete the WinLine
            if (neededMoveToProhibitPlayerWin != null) {
                return new AlgorithmMove(neededMoveToProhibitPlayerWin,new PossibleWinLine(neededMoveToProhibitPlayerWin,new ArrayList<>(List.of(neededMoveToProhibitPlayerWin)),null),-1);
            }
        }

        if (currenWinLinesMove != null) {
            return currenWinLinesMove;
        }

        List<PossibleWinLine> botNewWinLines = getNewPossibleWinLines(gameField).stream()
                .sorted(Comparator.comparingInt(winLine -> winLine.getNeededMovesToFillLine(gameField).size()))
                .collect(Collectors.toList()); // Sorted WinLine List by needed Moves to complete
        System.out.println("ente");
        return filterMoves(gameField, botNewWinLines, badMoves);
    }

    /**
     * @param gameField GameField the Operation should be done on
     * @param botCurrentWinLines Sorted List of Current WinLines of the Bot on the GameField
     * @param badMoves List of Bad Moves that should only be done as Last Move of a WinLine
     * @return Null if no Move was Found or the Next Move that Should be done associated with The Line the Bot wants to Fill
     */
    private static AlgorithmMove filterMoves(GameField gameField, List<PossibleWinLine> botCurrentWinLines, List<GameField.FieldPosition> badMoves) {
        if (!botCurrentWinLines.isEmpty()) {
            for (PossibleWinLine botCurrentWinLine : botCurrentWinLines) {
                System.out.println(botCurrentWinLine.toString());
                for (GameField.FieldPosition nextField : botCurrentWinLine.getNextFields()) {
                    Optional<GameField.FieldPosition> nextNeededMoveForLine = nextField.getNeededMovesToFillField(gameField).map(fieldPositions -> {
                        if (!fieldPositions.isEmpty()) {return fieldPositions.poll();}
                        return null;
                    });
                    if (nextNeededMoveForLine.isPresent()) {
                        if (!badMoves.contains(nextField)) {
                            System.out.println("next");
                            return new AlgorithmMove(nextNeededMoveForLine.get(), botCurrentWinLine, botCurrentWinLine.neededFieldsForLine() - 1);
                        } else {
                            if (botCurrentWinLine.neededFieldsForLine() == 1) { // Checks if Move would be last Move and can be done despite it is a Bad Move
                                System.out.println("other");
                                return new AlgorithmMove(nextNeededMoveForLine.get(), botCurrentWinLine, 0);
                            }
                        }
                    }
                }
            }
        }
        System.out.println("bad");
        return null;
    }

    /**
     * @param gameField GameField the Operation should be done on
     * @return A List of Possible new WinLines on the specified GameField current Fields not considered
     */
    private List<PossibleWinLine> getNewPossibleWinLines(GameField gameField) {
        return getPossibleNextMoves(gameField)
                .stream()
                .parallel()
                .map(position -> getNewPossibleWinLines(gameField,position))
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * @param gameField GameField the Operation should be done on
     * @param origin Field at which the WinLine starts
     * @return A List of Possible new WinLines on the specified GameField starting on the specified WinLine current Fields not considered
     */
    private List<PossibleWinLine> getNewPossibleWinLines(GameField gameField, GameField.FieldPosition origin) {
        return Arrays.stream(Direction.values())
                .parallel() // Multithreaded for each Direction
                .map(dir -> new PossibleWinLine(origin, List.of(origin), dir).isPossible(gameField,botColor))
                .collect(Collectors.toList());
    }


    /**
     * @return Moves that are needed to prohibit the Win for the Player
     */
    private List<GameField.FieldPosition> getRequiredMoves(GameField gameField) {
        return getCurrentPossibleWinLines(gameField, playerColor).stream()
                .filter(possibleWinLine -> {

                            if (possibleWinLine.associatedFieldCount() > 2) {
                                System.out.println("User WinLine" +possibleWinLine.toString());
                                return true;
                            }
                            return false;
                }) // Only if 3 / 4 are already on GameField
                .flatMap(winLine -> winLine.getNextFields().stream())
                .collect(Collectors.toList());
    }

    /**
     * @return Moves that would help the Player to Win and are Bad for the Bot
     */
    private List<GameField.FieldPosition> getBadMoves(GameField gameField) {
        return getCurrentPossibleWinLines(gameField, playerColor).stream()
                .filter(possibleWinLine -> {
                    if (possibleWinLine.associatedFieldCount() > 2) {
                        System.out.println("User Bad WinLine" +possibleWinLine.toString());
                        return true;
                    }
                    return false;
                })
                .flatMap(winLine -> winLine.getNextFields().stream())
                .map(field -> new GameField.FieldPosition(field.row(), (byte) (field.column() + 1))) // One field under it
                .collect(Collectors.toList());
    }


    /**
     * @param gameField GameField the Operation should be done on
     * @param color Color of Fields to consider
     * @return A List of Possible WinLines on the specified GameField starting on the specified WinLine considered current Fields
     */
    private List<PossibleWinLine> getCurrentPossibleWinLines(GameField gameField, GameField.ICSValue color) {
        List<GameField.FieldPosition> colorFields = getFieldsByColor(gameField,color);
        return colorFields.stream().parallel().map(position -> getPossibleWinLinesForField(gameField,colorFields,position)).filter(Objects::nonNull).flatMap(List::stream).collect(Collectors.toList());
    }

    /**
     * @param gameField GameField the Operation should be done on
     * @param consideredFields Fields to Consider for WinLines
     * @param originField The Field the WinLines start at
     * @return A List of Possible WinLines on the specified GameField starting on the specified WinLine considered current Fields
     */
    private List<PossibleWinLine> getPossibleWinLinesForField(GameField gameField,List<GameField.FieldPosition> consideredFields,GameField.FieldPosition originField) {
        CompletableFuture<List<PossibleWinLine>> futureStraight = new CompletableFuture<>();
        CompletableFuture<List<PossibleWinLine>> futureDiagonal = new CompletableFuture<>();
        CompletableFuture<List<PossibleWinLine>> futureVertical = new CompletableFuture<>();


        AtomicReference<List<GameField.FieldPosition>> fieldMapRef = new AtomicReference<>(consideredFields);
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
                searchForStraightPossibleWinLine(gameField,columnPositions, originFieldRef, futureStraightRight,Direction.RIGHT);
            }).start();
            // Straight Left Thread
            new Thread(() -> {
                searchForStraightPossibleWinLine(gameField,columnPositions, originFieldRef, futureStraightLeft,Direction.LEFT);
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
                searchForDirectionalPossibleWinLine(gameField,fieldMapRef, originFieldRef, futureDiagonalUpRight,Direction.DIAGONAL_UP_RIGHT);
            }).start();
            // Diagonal LEFT UP THREAD
            new Thread(() -> {
                searchForDirectionalPossibleWinLine(gameField,fieldMapRef, originFieldRef, futureDiagonalUpLeft,Direction.DIAGONAL_UP_LEFT);
            }).start();
            // Diagonal RIGHT Down THREAD
            new Thread(() -> {
                searchForDirectionalPossibleWinLine(gameField,fieldMapRef, originFieldRef, futureDiagonalDownRight,Direction.DIAGONAL_DOWN_RIGHT);
            }).start();
            // Diagonal Left Down THREAD
            new Thread(() -> {
                searchForDirectionalPossibleWinLine(gameField,fieldMapRef, originFieldRef, futureDiagonalDownLeft,Direction.DIAGONAL_DOWN_LEFT);
            }).start();

            futureDiagonal.complete(getListOfFutures(futureDiagonalDownLeft,futureDiagonalUpLeft,futureDiagonalDownRight,futureDiagonalUpRight));
        }).start();

        // Vertical Thread
        new Thread(() -> {
            CompletableFuture<PossibleWinLine> futureUp = new CompletableFuture<>();
            CompletableFuture<PossibleWinLine> futureDown = new CompletableFuture<>();
            AtomicReference<List<GameField.FieldPosition>> rowPositions = new AtomicReference<>(filterFieldsForRowOfOrigin(fieldMapRef.get(), originFieldRef.get()));
            if (rowPositions.get().size()<=1) { // IF Only origin is in Row directly return
                futureVertical.complete(null);
                return;
            }
            // Straight Right Thread
            new Thread(() -> {
                searchForVerticalPossibleWinLine(gameField,rowPositions, originFieldRef, futureUp,Direction.UP);
            }).start();
            // Straight Left Thread
            new Thread(() -> {
                searchForVerticalPossibleWinLine(gameField,rowPositions, originFieldRef,futureDown,Direction.DOWN);
            }).start();
            futureVertical.complete(getListOfFutures(futureUp,futureDown));
        }).start();
        return getListOfFuturesLists(futureStraight,futureDiagonal,futureVertical);
    }

    @SafeVarargs
    private static List<PossibleWinLine> getListOfFutures(CompletableFuture<PossibleWinLine>... futures) {
        return Arrays.stream(futures).map(future -> {try {return future.get();} catch (Exception exception) {
            exception.printStackTrace();
            return null;}}).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @SafeVarargs
    private static List<PossibleWinLine> getListOfFuturesLists(CompletableFuture<List<PossibleWinLine>>... futures) {
        return Arrays.stream(futures).map(future -> {try {return future.get();} catch (Exception exception) {
            exception.printStackTrace();
            return null;}}).filter(Objects::nonNull).flatMap(List::stream).collect(Collectors.toList());
    }

    private static void searchForVerticalPossibleWinLine(GameField gameField,AtomicReference<List<GameField.FieldPosition>> rowPositionsRef, AtomicReference<GameField.FieldPosition> originFieldRef, CompletableFuture<PossibleWinLine> resultFuture,Direction direction) {
        if (direction != Direction.UP && direction != Direction.DOWN) { throw new InputMismatchException("Wrong Input: Direction");}
        try {
            if (rowPositionsRef.get().stream().anyMatch(position -> position.column() == originFieldRef.get().column()+(direction == Direction.UP ? +1 : -1))) { // if originField is not the (for Direction Right: Left Field) (for Direction Left: Right Field)  of a Win Line
                resultFuture.complete(null);
                return;
            }
            List<GameField.FieldPosition> winLineFields = new ArrayList<>(List.of(originFieldRef.get()));
            int count = 0;
            while (count <= 2) {
                count++;
                int finalCount = count;
                if (rowPositionsRef.get().stream().anyMatch(position -> position.column() == originFieldRef.get().column()+(direction == Direction.UP ? -finalCount : +finalCount))) {
                    winLineFields.add(new GameField.FieldPosition(originFieldRef.get().row(), (byte) (originFieldRef.get().column()+(direction == Direction.UP ? -finalCount : +finalCount))));
                } else break;
            }
            if (winLineFields.size() < 2) {
                resultFuture.complete(null);
                return;
            }

            resultFuture.complete(new PossibleWinLine(originFieldRef.get(),winLineFields,direction).isPossible(gameField,originFieldRef.get().getFieldICSValue(gameField)));
        } catch (Exception ignored) {
            resultFuture.complete(null);
        }
    }

    private static void searchForDirectionalPossibleWinLine(GameField gameField,AtomicReference<List<GameField.FieldPosition>> fieldMapRef, AtomicReference<GameField.FieldPosition> originFieldRef, CompletableFuture<PossibleWinLine> resultFuture,Direction direction) {

        if (direction != Direction.DIAGONAL_UP_RIGHT && direction != Direction.DIAGONAL_DOWN_LEFT && direction != Direction.DIAGONAL_DOWN_RIGHT && direction != Direction.DIAGONAL_UP_LEFT) { throw new InputMismatchException("Wrong Input: Direction");}
        try {
            ;
            if (fieldMapRef.get().stream().anyMatch(position -> position.row() == originFieldRef.get().row()+(direction == Direction.DIAGONAL_DOWN_LEFT || direction == Direction.DIAGONAL_UP_LEFT ? +1 : -1) &&  position.column() == originFieldRef.get().column()+(direction == Direction.DIAGONAL_DOWN_LEFT || direction == Direction.DIAGONAL_DOWN_RIGHT ? -1 : +1))) { // if originField is not the start of a Win Line
                resultFuture.complete(null);
                return;
            }
            List<GameField.FieldPosition> winLineFields = new ArrayList<>(List.of(originFieldRef.get()));
            int count = 0;
            while (count <= 2) {
                count++;
                int finalCount = count;
                if (fieldMapRef.get().stream().anyMatch(position -> position.row() == originFieldRef.get().row()+(direction == Direction.DIAGONAL_DOWN_LEFT || direction == Direction.DIAGONAL_UP_LEFT ? -finalCount : +finalCount) &&  position.column() == originFieldRef.get().column()+(direction == Direction.DIAGONAL_DOWN_LEFT || direction == Direction.DIAGONAL_DOWN_RIGHT ? +finalCount : -finalCount))) {
                    winLineFields.add(new GameField.FieldPosition((byte) (originFieldRef.get().row()+(direction == Direction.DIAGONAL_DOWN_LEFT || direction == Direction.DIAGONAL_UP_LEFT ? -finalCount : +finalCount)), (byte) (originFieldRef.get().column()+(direction == Direction.DIAGONAL_DOWN_LEFT || direction == Direction.DIAGONAL_DOWN_RIGHT ? +finalCount : -finalCount))));
                } else break;
            }
            if (winLineFields.size() < 2) {
                resultFuture.complete(null);
                return;
            }
            resultFuture.complete(new PossibleWinLine(originFieldRef.get(),winLineFields,direction).isPossible(gameField,originFieldRef.get().getFieldICSValue(gameField)));
        } catch (Exception ignored) {
            resultFuture.complete(null);
        }

    }

    private static void searchForStraightPossibleWinLine(GameField gameField,AtomicReference<List<GameField.FieldPosition>> columnPositionsRef, AtomicReference<GameField.FieldPosition> originFieldRef, CompletableFuture<PossibleWinLine> resultFuture,Direction direction) {
        if (direction != Direction.RIGHT && direction != Direction.LEFT) { throw new InputMismatchException("Wrong Input: Direction");}
        try {
            if (columnPositionsRef.get().stream().anyMatch(position -> position.row() == originFieldRef.get().row()+(direction == Direction.RIGHT ? -1 : +1))) { // if originField is not the (for Direction Right: Left Field) (for Direction Left: Right Field)  of a Win Line
                resultFuture.complete(null);
                return;
            }

            List<GameField.FieldPosition> winLineFields = new ArrayList<>(List.of(originFieldRef.get()));
            int count = 0;
            while (count <= 2) {
                count++;
                int finalCount = count;
                if (columnPositionsRef.get().stream().anyMatch(position -> position.row() == originFieldRef.get().row()+(direction == Direction.RIGHT ? +finalCount : -finalCount))) {
                    winLineFields.add(new GameField.FieldPosition((byte) (originFieldRef.get().row()+(direction == Direction.RIGHT ? +finalCount : -finalCount)),originFieldRef.get().column()));
                } else break;
            }
            if (winLineFields.size() < 2) {
                resultFuture.complete(null);
                return;
            }
            resultFuture.complete(new PossibleWinLine(originFieldRef.get(),winLineFields,direction).isPossible(gameField,originFieldRef.get().getFieldICSValue(gameField)));
        } catch (Exception exc) {
            exc.printStackTrace();
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
     * @return Maximum 7 Positions. Possible Moves at current State of the GameField
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

    public void overrideGameField(GameField newGameField) {
        this.mainGameField = newGameField;
    }


    public GameField getMainGameField() {
        return mainGameField;
    }
}
