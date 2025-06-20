package de.juniorjacki.game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class GameControl {


    // Start Checks
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
                .updateField(new GameField.FieldPosition((byte) 3, (byte) 4), (byte) 80);

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


        GameField kian = new GameField()
                .updateField(new GameField.FieldPosition((byte) 0, (byte) 5), (byte) 80) // Gelb
                .updateField(new GameField.FieldPosition((byte) 0, (byte) 4), (byte) 40) // Gelb
                .updateField(new GameField.FieldPosition((byte) 3, (byte) 5), (byte) 80) // Gelb
                .updateField(new GameField.FieldPosition((byte) 1, (byte) 5), (byte) 40) // Gelb
                .updateField(new GameField.FieldPosition((byte) 6, (byte) 5), (byte) 80) // Gelb
                .updateField(new GameField.FieldPosition((byte) 4, (byte) 5), (byte) 40) // Gelb
                .updateField(new GameField.FieldPosition((byte) 3, (byte) 4), (byte) 80) // Gelb
                .updateField(new GameField.FieldPosition((byte) 3, (byte) 3), (byte) 40) // Gelb
                .updateField(new GameField.FieldPosition((byte) 5, (byte) 5), (byte) 80) // Gelb
                .updateField(new GameField.FieldPosition((byte) 0, (byte) 3), (byte) 40) // Gelb
                .updateField(new GameField.FieldPosition((byte) 0, (byte) 2), (byte) 80) // Gelb
                .updateField(new GameField.FieldPosition((byte) 1, (byte) 4), (byte) 40) // Gelb
                .updateField(new GameField.FieldPosition((byte) 1, (byte) 3), (byte) 80) // Gelb
                //.updateField(new GameField.FieldPosition((byte) 2, (byte) 5), (byte) 40) // Gelb
                //.updateField(new GameField.FieldPosition((byte) 2, (byte) 4), (byte) 80) // Gelb

                ;



        /*

         */


        long time = System.currentTimeMillis();

    /*
        System.out.println(kian.generateBoardString());
        AlgorithmMove move = control.getPossibleSmartMove(kian, GameField.ICSValue.RED);
        if (move == null) System.out.println("is leer");
        System.out.println(move.toString());
    */

        //System.out.println(control.playBotAgainstBot().generateBoardString());
        control.playAgainstBot();
        System.out.println("Calculation Time:" + (System.currentTimeMillis() - time));
    }

    public GameField playBotAgainstBot() {
        GameField.ICSValue botCOL1 = GameField.ICSValue.RED;
        GameField.ICSValue botCOL2 = GameField.ICSValue.YELLOW;
        GameField gameField = new GameField();

        boolean currentBot = false;

        AlgorithmMove currentMove = getPossibleSmartMove(gameField, currentBot ? botCOL1 : botCOL2);
        while (currentMove != null && isWin(gameField) == null) {
            gameField.updateField(currentMove.position, (currentBot ? botCOL1 : botCOL2).getMin());
            System.out.println(currentMove.position + " " + currentMove.neededMovesToWinAfter);
            System.out.println(gameField.generateBoardString());
            currentBot = !currentBot;
            System.out.println("Player " + (currentBot ? botCOL1 : botCOL2));
            currentMove = getPossibleSmartMove(gameField, currentBot ? botCOL1 : botCOL2);
        }
        PossibleWinLine winLine = isWin(gameField);
        if (winLine != null) {
            System.out.println("WinLine " + winLine);
        } else {
            System.out.println("No winLine");
        }


        return gameField;
    }

    public void playAgainstBot() {
        // Enter data using BufferReader
        GameField gameField = new GameField();

        System.out.println("--------------------------------------");
        System.out.println("Du bist Gelb");
        System.out.println("--------------------------------------");
        System.out.println();
        System.out.println((gameField.generateBoardString()));
        BufferedReader r = new BufferedReader(
                new InputStreamReader(System.in));
        while (isWin(gameField) == null) {
            Map.Entry<Byte, Byte> input = getInput(r, gameField);
            gameField.updateField((byte) input.getKey(), (byte) input.getValue(), (byte) 80);
            if(isWin(gameField) != null) break;
            gameField.updateField(getPossibleSmartMove(gameField, GameField.ICSValue.RED).position, (byte) 40);
            System.out.println("--------------------------------------");
            System.out.println((gameField.generateBoardString()));
        }
        PossibleWinLine winLine = isWin(gameField);
        if (winLine != null) {
            System.out.println("WinLine " + winLine);
        }
        System.out.println((gameField.generateBoardString()));
    }

    public PossibleWinLine isWin(GameField gameField) {
        List<PossibleWinLine> possibleWinLines = getCurrentPossibleWinLines(gameField, (byte) 4, GameField.ICSValue.RED);
        possibleWinLines.addAll(getCurrentPossibleWinLines(gameField, (byte) 4, GameField.ICSValue.YELLOW));
        if (possibleWinLines.isEmpty()) return null;
        return possibleWinLines.get(0);
    }

    public Map.Entry<Byte, Byte> getInput(BufferedReader r, GameField gameField) {
        try {
            System.out.println("Bitte gib deinen Zug an: Row");
            String s = r.readLine();
            byte row = (byte) Integer.parseInt(s.split(",")[0]);
            while (!isMovePossible(gameField, row,getColumn(gameField, row))) {
                System.out.println("Move nicht möglich. Bitte gib einen anderen Zug an: Row");
                s = r.readLine();
                row = (byte) Integer.parseInt(s.split(",")[0]);
            }
            byte col = getColumn(gameField, row);
            return new AbstractMap.SimpleEntry<>(row, col);
        } catch (IOException e) {
            return getInput(r, gameField);
        }
    }

    private byte getColumn(GameField gameField,byte row) {
        return getPossibleNextMoves(gameField).stream().filter(position -> position.row() == row).findFirst().map(GameField.FieldPosition::column).orElse((byte) -1);
    }

    // Checks End


    private final boolean debugLogs = false;
    private GameField mainGameField = new GameField();
    private GameField.ICSValue playerColor = GameField.ICSValue.YELLOW;
    private GameField.ICSValue botColor = GameField.ICSValue.RED;



    /**
     * Represent a Move decided by the Algorithm to Win the Game
     *
     * @param position              Move that should be done
     * @param targetWinLine         WinLine that is targeted by the Algorithm
     * @param neededMovesToWinAfter Needed Moves to fill the WinLine and Win
     */
    public record AlgorithmMove(GameField.FieldPosition position, PossibleWinLine targetWinLine,
                                int neededMovesToWinAfter) {

        @Override
        public String toString() {
            return "Position: " + position.row() + " , " + position.column() + " TargetWinLine Direction: " + targetWinLine.direction() + " NeededMovesToWinAfter: " + neededMovesToWinAfter;
        }
    }

    /**
     * Represents a Line of Fields on the GameField that is ordered like a part of a Win Line (4 in a Line)
     *
     * @param associatedFields All Fields of the current Possible Win Line
     * @param direction
     */
    public record PossibleWinLine(GameField.FieldPosition originField, List<GameField.FieldPosition> associatedFields,
                                  Direction direction) {

        /**
         * @return If WinLine is already filled Completely
         */
        public boolean complete() {
            return associatedFields.size() > 3;
        }

        /**
         * @return How many fields of the WinLine already Filled with the Line Color
         */
        public int associatedFieldCount() {
            return associatedFields.size();
        }

        public int neededFieldsForLine() {
            return 4 - associatedFields.size();
        }

        /**
         * @return The remaining fields needed to be filled for the WinLine
         */
        public List<GameField.FieldPosition> getNextFields(boolean removeNotMoveable) {
            List<GameField.FieldPosition> fieldPositions = new ArrayList<>();

            // Except for Direction Up because there only one move is Possible to fill it at a time
            if (removeNotMoveable) {
                if (direction == Direction.UP) {
                    fieldPositions.add(getField(associatedFieldCount()));
                    return fieldPositions;
                }
            }

            IntStream.range(0, 4)
                    .filter(value -> !associatedFields.contains(getField(value)))
                    .mapToObj(this::getField)
                    .forEach(fieldPositions::add);
            return fieldPositions;
        }

        /**
         * @return The remaining fields needed to be filled for the WinLine
         */
        public List<GameField.FieldPosition> getNextFields() {
            return getNextFields(false);
        }


        /**
         * @param fieldPosition Position of field on GameField
         * @return The Index of the field in direction starting at, originField = 0
         */
        public int getFieldIndex(GameField.FieldPosition fieldPosition) {
            if (fieldPosition.equals(originField)) return 0;
            return switch (direction) {
                case UP -> originField.column() - fieldPosition.column();
                //case DOWN -> fieldPosition.column()-originField.column();
                case LEFT, DIAGONAL_DOWN_LEFT, DIAGONAL_UP_LEFT -> originField.row() - fieldPosition.row();
                case RIGHT, DIAGONAL_UP_RIGHT, DIAGONAL_DOWN_RIGHT -> fieldPosition.row() - originField.column();
            };
        }

        /**
         * @param index Origin = 0 : Index of Field started at Origin in WinLine Direction
         * @return The Line FieldPosition specified by Index
         */
        private GameField.FieldPosition getField(int index) {
            if (index == 0) return originField;
            return switch (direction) {
                case UP -> new GameField.FieldPosition(originField.row(), (byte) (originField().column() - index));
                //case DOWN -> new GameField.FieldPosition(originField.row(), (byte) (originField().column()+index));
                case LEFT -> new GameField.FieldPosition((byte) (originField.row() - index), originField().column());
                case RIGHT -> new GameField.FieldPosition((byte) (originField.row() + index), originField().column());
                case DIAGONAL_DOWN_LEFT ->
                        new GameField.FieldPosition((byte) (originField.row() - index), (byte) (originField().column() + index));
                case DIAGONAL_DOWN_RIGHT ->
                        new GameField.FieldPosition((byte) (originField.row() + index), (byte) (originField().column() + index));
                case DIAGONAL_UP_RIGHT ->
                        new GameField.FieldPosition((byte) (originField.row() + index), (byte) (originField().column() - index));
                case DIAGONAL_UP_LEFT ->
                        new GameField.FieldPosition((byte) (originField.row() - index), (byte) (originField().column() - index));
            };
        }

        /**
         * Checks if WinLine is possible
         *
         * @return if WinLine is in GameField and all Needed Fields are filled with AIR returns itself. If it goes over the Corner or is not anymore Possible returns null
         */
        public PossibleWinLine isPossible(GameField gameField, GameField.ICSValue lineColor) {
            return isLinePossible(gameField, lineColor) ? this : null;
        }

        /**
         * Checks if WinLine is possible
         *
         * @return if WinLine is in GameField and all Needed Fields are filled with AIR returns True. If it goes over the Corner or is not anymore Possible returns False
         */
        public boolean isLinePossible(GameField gameField, GameField.ICSValue lineColor) {
            if (isWinLineInField()) {
                for (GameField.FieldPosition neededFieldToWin : getNextFields()) {
                    if (!(neededFieldToWin.isFilledWith(gameField, lineColor) || neededFieldToWin.isEmpty(gameField)))
                        return false;
                }
                return true;
            }
            ;
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
         * @param gameField The GameField the Operation uses
         * @return A Sorted Queue of needed Moves to fill the Line
         */
        public Queue<GameField.FieldPosition> getNeededMovesToFillLine(GameField gameField) {
            return getNextFields().stream()
                    .flatMap(field -> field.getNeededMovesToFillField(gameField).stream().flatMap(Collection::stream))
                    .distinct()
                    .collect(Collectors.toCollection(LinkedList::new));
        }

        public String format(GameField gameField) {
            return "PossibleWinLine[" + originField + ", AssociatedFields: " + associatedFields + ", Direction: " + direction + ", NeededFields " + getNextFields() + ", MovesNeeded: " + getNeededMovesToFillLine(gameField).size() + "]";
        }
    }

    public enum Direction {
        LEFT,
        RIGHT,
        DIAGONAL_UP_RIGHT,
        DIAGONAL_UP_LEFT,
        DIAGONAL_DOWN_RIGHT,
        DIAGONAL_DOWN_LEFT,
        UP
        //,DOWN
    }


    public boolean isMovePossible(GameField gameField, byte col, byte row) {
        return getPossibleNextMoves(gameField).contains(new GameField.FieldPosition(col, row));
    }


    /**
     * Smart Moves from the Algorithm
     *
     * @param gameField The GameField the Algorithm uses
     * @return Null if no Move was Found or the Next AlgorithmMove that Should be done associated with The Line the Bot wants to Fill to Win
     */
    private AlgorithmMove getPossibleSmartMove(GameField gameField, GameField.ICSValue moveColor) {

        // MIN-MAX Algorithm
        List<PossibleWinLine> botCurrentWinLines = getCurrentPossibleWinLines(gameField, (byte) 1, moveColor).stream()
                .sorted(Comparator.comparingInt(winLine -> winLine.getNeededMovesToFillLine(gameField).size()))
                .collect(Collectors.toList()); // Sorted WinLine List by needed Moves to complete
        if (debugLogs) System.out.println("WinLines " + botCurrentWinLines);

        List<GameField.FieldPosition> badMoves = getBadMoves(gameField, moveColor == GameField.ICSValue.RED ? GameField.ICSValue.YELLOW : GameField.ICSValue.RED);
        if (debugLogs) System.out.println("BadMoves" + badMoves);


        AlgorithmMove currenWinLinesMove = filterMoves(gameField, botCurrentWinLines, badMoves);
        if (currenWinLinesMove != null) {
            if (currenWinLinesMove.neededMovesToWinAfter == 0) {
                return currenWinLinesMove;
            }
        }

        List<PossibleWinLine> winLinesToProhibit = getRequiredLineMoves(gameField, moveColor == GameField.ICSValue.RED ? GameField.ICSValue.YELLOW : GameField.ICSValue.RED);
        if (winLinesToProhibit != null) {
            List<PossibleWinLine> neededProhibitLines = winLinesToProhibit.stream().filter(possibleWinLine -> possibleWinLine.neededFieldsForLine() <= 2).sorted(Comparator.comparingInt((PossibleWinLine value) -> value.getNeededMovesToFillLine(gameField).size()).thenComparing(PossibleWinLine::direction)).collect(Collectors.toList()); // Sort After NeededFields and Direction ; Horizontal are easier for the player to create a 3,5win situation then vertical are so them are first prohibited
            if (debugLogs) System.out.println("Prohibit Lines " + neededProhibitLines);
            AlgorithmMove prohibitMove = filterProhibitMoves(gameField, neededProhibitLines, badMoves);
            if (currenWinLinesMove != null && prohibitMove != null) {
                if (prohibitMove.neededMovesToWinAfter > currenWinLinesMove.neededMovesToWinAfter) {
                    if (debugLogs) System.out.println("Win Move " + currenWinLinesMove);
                    return currenWinLinesMove;
                }
            }
            if (prohibitMove != null) {
                if (debugLogs) System.out.println("Prohibit: " + prohibitMove);
                return new AlgorithmMove(prohibitMove.position, prohibitMove.targetWinLine, -1);
            }
        }

        List<PossibleWinLine> botNewWinLines = getNewPossibleWinLines(gameField, moveColor).stream()
                .sorted(Comparator.comparingInt(winLine -> winLine.getNeededMovesToFillLine(gameField).size()))
                .collect(Collectors.toList()); // Sorted WinLine List by needed Moves to complete

        if (debugLogs) System.out.println("New WinLines " + botNewWinLines);
        AlgorithmMove newLineMove = filterNewMoves(gameField, botNewWinLines, badMoves);
        if (currenWinLinesMove != null) {
            if (newLineMove == null) return currenWinLinesMove;
            if (newLineMove.neededMovesToWinAfter >= currenWinLinesMove.neededMovesToWinAfter)
                return currenWinLinesMove;
        }

        // If GameField is full and no win is anymore Possible because of needed space
        if (newLineMove == null) {
            // If this return is Null none Move is Possible because of no space or Win of opponent
            AlgorithmMove lastPossiblemove = filterNoWinMoves(getPossibleNextMoves(gameField), getInstantEnemyWinMoves(gameField, moveColor == GameField.ICSValue.RED ? GameField.ICSValue.YELLOW : GameField.ICSValue.RED));
            if (lastPossiblemove == null)
                if (debugLogs) System.out.println("No Possible Move without make Win for Opponent or No Empty Fields");
            return lastPossiblemove;
        }
        return newLineMove;
    }

    public static PossibleWinLine pickRandomFromSmallestGroup(List<PossibleWinLine> winLines, GameField gameField) {
        // Group elements by getNeededMovesToFillLine value
        Map<Integer, List<PossibleWinLine>> groups = new HashMap<>();
        for (PossibleWinLine element : winLines) {
            int moves = element.getNeededMovesToFillLine(gameField).size();
            groups.computeIfAbsent(moves, k -> new ArrayList<>()).add(element);
        }

        if (groups.isEmpty()) {
            return null; // No elements
        }

        // Find the smallest moves value
        int minMoves = Collections.min(groups.keySet());

        // Get the group with the smallest moves value
        List<PossibleWinLine> smallestGroup = groups.get(minMoves);

        // If the smallest group has only one element, return it
        if (smallestGroup.size() == 1) {
            return smallestGroup.get(0);
        }

        // If the smallest group has multiple elements, pick a random one
        Random random = new Random();
        return smallestGroup.get(random.nextInt(smallestGroup.size()));
    }

    /**
     * @param gameField          GameField the Operation should be done on
     * @param botCurrentWinLines Sorted List of Current WinLines of the Bot on the GameField
     * @param badMoves           List of Bad Moves that should only be done as Last Move of a WinLine
     * @return Null if no Move was Found or the Next Move that Should be done associated with The Line the Bot wants to Fill
     */
    private static AlgorithmMove filterNewMoves(GameField gameField, List<PossibleWinLine> botCurrentWinLines, List<GameField.FieldPosition> badMoves) {
        if (!botCurrentWinLines.isEmpty()) {
            while (!botCurrentWinLines.isEmpty()) {
                GameControl.PossibleWinLine randomLine = pickRandomFromSmallestGroup(botCurrentWinLines, gameField);
                for (GameField.FieldPosition nextField : randomLine.getNextFields()) {
                    Optional<GameField.FieldPosition> nextNeededMoveForLine = nextField.getNeededMovesToFillField(gameField).map(fieldPositions -> {
                        if (!fieldPositions.isEmpty()) {
                            return fieldPositions.poll();
                        }
                        return null;
                    });
                    if (nextNeededMoveForLine.isPresent()) {
                        if (!badMoves.contains(nextNeededMoveForLine.get())) {
                            return new AlgorithmMove(nextNeededMoveForLine.get(), randomLine, randomLine.getNeededMovesToFillLine(gameField).size() - 1);
                        } else {
                            if (randomLine.getNeededMovesToFillLine(gameField).size() == 1) { // Checks if Move would be last Move and can be done despite it is a Bad Move
                                return new AlgorithmMove(nextNeededMoveForLine.get(), randomLine, 0);
                            }
                        }
                    }
                }
                botCurrentWinLines.remove(randomLine);
            }

        }
        return null;
    }

    /**
     * @param gameField          GameField the Operation should be done on
     * @param botCurrentWinLines Sorted List of Current WinLines of the Bot on the GameField
     * @param badMoves           List of Bad Moves that should only be done as Last Move of a WinLine
     * @return Null if no Move was Found or the Next Move that Should be done associated with The Line the Bot wants to Fill
     */
    private static AlgorithmMove filterMoves(GameField gameField, List<PossibleWinLine> botCurrentWinLines, List<GameField.FieldPosition> badMoves) {
        if (!botCurrentWinLines.isEmpty()) {
            for (PossibleWinLine botCurrentWinLine : botCurrentWinLines) {
                for (GameField.FieldPosition nextField : botCurrentWinLine.getNextFields()) {
                    Optional<GameField.FieldPosition> nextNeededMoveForLine = nextField.getNeededMovesToFillField(gameField).map(fieldPositions -> {
                        if (!fieldPositions.isEmpty()) {
                            return fieldPositions.poll();
                        }
                        return null;
                    });
                    if (nextNeededMoveForLine.isPresent()) {
                        if (!badMoves.contains(nextNeededMoveForLine.get())) {
                            return new AlgorithmMove(nextNeededMoveForLine.get(), botCurrentWinLine, botCurrentWinLine.getNeededMovesToFillLine(gameField).size() - 1);
                        } else {
                            if (botCurrentWinLine.getNeededMovesToFillLine(gameField).size() == 1) { // Checks if Move would be last Move and can be done despite it is a Bad Move
                                return new AlgorithmMove(nextNeededMoveForLine.get(), botCurrentWinLine, 0);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * @param botCurrentPossibleMoves Sorted List of Current PossibleMoves of the Bot on the GameField
     * @param badMoves                List of Bad Moves that should not be done
     * @return Null if no Move was Found or the Next Move that Should be done associated with The Line the Bot wants to Fill
     */
    private static AlgorithmMove filterNoWinMoves(List<GameField.FieldPosition> botCurrentPossibleMoves, List<GameField.FieldPosition> badMoves) {
        if (!botCurrentPossibleMoves.isEmpty()) {
            for (GameField.FieldPosition nextField : botCurrentPossibleMoves) {
                if (!badMoves.contains(nextField)) {
                    return new AlgorithmMove(nextField, null, -1);
                }
            }
        }
        return null;
    }

    /**
     * @param gameField          GameField the Operation should be done on
     * @param botCurrentWinLines Sorted List of Current WinLines of the Bot on the GameField
     * @param badMoves           List of Bad Moves that should only be done as Last Move of a WinLine
     * @return Null if no Move was Found or the Next Move that Should be done associated with The Line the Bot wants to Fill
     */
    private static AlgorithmMove filterProhibitMoves(GameField gameField, List<PossibleWinLine> botCurrentWinLines, List<GameField.FieldPosition> badMoves) {
        if (!botCurrentWinLines.isEmpty()) {
            for (PossibleWinLine botCurrentWinLine : botCurrentWinLines) {
                for (GameField.FieldPosition nextField : botCurrentWinLine.getNextFields()) {
                    Optional<Queue<GameField.FieldPosition>> nextNeededMovesForField = nextField.getNeededMovesToFillField(gameField);
                    if (nextNeededMovesForField.isPresent()) {
                        if (nextNeededMovesForField.get().size() == 1) {
                            if (!badMoves.contains(nextField)) {
                                return new AlgorithmMove(nextField, botCurrentWinLine, botCurrentWinLine.getNeededMovesToFillLine(gameField).size() - 1);
                            } else {
                                if (botCurrentWinLine.getNeededMovesToFillLine(gameField).size() <= 1) { // Checks if Move would be last Move and need to be done despite it is a Bad Move
                                    return new AlgorithmMove(nextField, botCurrentWinLine, 0);
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * @param gameField GameField the Operation should be done on
     * @return A List of Possible new WinLines on the specified GameField current Fields not considered
     */
    private List<PossibleWinLine> getNewPossibleWinLines(GameField gameField, GameField.ICSValue moveColor) {
        return getPossibleNextMoves(gameField)
                .stream()
                .parallel() // Multithreaded for each Field
                .map(position -> getNewPossibleWinLines(gameField, position, moveColor))
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * @param gameField GameField the Operation should be done on
     * @param origin    Field at which the WinLine starts
     * @return A List of Possible new WinLines on the specified GameField starting on the specified WinLine current Fields not considered
     */
    private List<PossibleWinLine> getNewPossibleWinLines(GameField gameField, GameField.FieldPosition origin, GameField.ICSValue moveColor) {
        return Arrays.stream(Direction.values())
                .parallel() // Multithreaded for each Direction
                .map(dir -> new PossibleWinLine(origin, List.of(), dir).isPossible(gameField, moveColor))
                .collect(Collectors.toList());
    }


    /**
     * @return Moves that are needed to prohibit the Win for the Player
     */
    private List<PossibleWinLine> getRequiredLineMoves(GameField gameField, GameField.ICSValue enemyColor) {
        return getCurrentPossibleWinLines(gameField, (byte) 2, enemyColor);
    }


    // win 1906 win with 4,2 write into paper TODO

    /**
     * @return Moves that would help the Player to Win and are Bad for the Bot
     */
    private List<GameField.FieldPosition> oldgetBadMoves(GameField gameField, GameField.ICSValue enemyColor) {
        return getCurrentPossibleWinLines(gameField, (byte) 2, enemyColor).stream()
                .flatMap(winLine -> winLine.getNextFields(true).stream())
                .map(field -> new GameField.FieldPosition(field.row(), (byte) (field.column() + 1))) // One field under it
                .collect(Collectors.toList());
    }

    /**
     * @return Moves that would instantly help the Player to Win and are Bad for the Bot
     */
    private List<GameField.FieldPosition> getInstantEnemyWinMoves(GameField gameField, GameField.ICSValue enemyColor) {
        return getCurrentPossibleWinLines(gameField, (byte) 3, enemyColor).stream()
                .flatMap(winLine -> winLine.getNextFields(true).stream())
                .map(field -> new GameField.FieldPosition(field.row(), (byte) (field.column() + 1))) // One field under it
                .collect(Collectors.toList());
    }

    /**
     * @return Moves that would help the Player to Win and are Bad for the Bot
     */
    private List<GameField.FieldPosition> getBadMoves(GameField gameField, GameField.ICSValue enemyColor) {
        return getCurrentPossibleWinLines(gameField, (byte) 2, enemyColor).stream()
                .flatMap(winLine -> {
                    /*

                        Filter Fields that are not Next to a 2 Block to prohibit Player (Middle Block 2x2 structure) Win
                        Fix for win 1906 with 4,2:
                            0: . . . . . . .
                            1: . . . . . . .
                            2: . R R R W . .
                            3: . R Y Y Y . .
                            4: . R Y Y R . .
                            5: R Y R Y Y R .
                               0 1 2 3 4 5 6

                        Example: X= Allowed To Place - Y = Bad Move -  R = Red Player Card
                        - X X R R X X
                        - X Y R R Y X

                        Old Algorithm would has acted like this: ( ref: oldgetBadMoves()) what makes it easy to win
                        - X X R R X X
                        - Y Y R R Y Y
                     */
                    List<GameField.FieldPosition> fieldPositions = winLine.getNextFields(true);
                    fieldPositions.removeIf(nextField -> !nextField.isAside(winLine.associatedFields));
                    return fieldPositions.stream();
                })
                .map(field -> new GameField.FieldPosition(field.row(), (byte) (field.column() + 1))) // One field under it
                .collect(Collectors.toList());


    }


    /**
     * @param gameField GameField the Operation should be done on
     * @param color     Color of Fields to consider
     * @return A List of Possible WinLines on the specified GameField starting on the specified WinLine considered current Fields
     */
    private List<PossibleWinLine> getCurrentPossibleWinLines(GameField gameField, byte minFieldsForLine, GameField.ICSValue color) {
        List<GameField.FieldPosition> colorFields = getFieldsByColor(gameField, color);
        List<GameField.FieldPosition> possibleOriginFields = new ArrayList<>(colorFields);
        possibleOriginFields.addAll(getFieldsByColor(gameField, GameField.ICSValue.AIR));
        return possibleOriginFields.stream().parallel().map(position -> getPossibleWinLinesForField(gameField, minFieldsForLine, colorFields, position, color)).filter(Objects::nonNull).flatMap(List::stream).collect(Collectors.toList());
    }

    /**
     * @param gameField     GameField the Operation should be done on
     * @param coloredFields Fields to Consider for WinLines
     * @param originField   The Field the WinLines start at
     * @return A List of Possible WinLines on the specified GameField starting on the specified WinLine considered current Fields
     */
    private List<PossibleWinLine> getPossibleWinLinesForField(GameField gameField, byte minFieldsForLine, List<GameField.FieldPosition> coloredFields, GameField.FieldPosition originField, GameField.ICSValue lineColor) {
        CompletableFuture<List<PossibleWinLine>> futureStraight = new CompletableFuture<>();
        CompletableFuture<List<PossibleWinLine>> futureDiagonal = new CompletableFuture<>();
        CompletableFuture<List<PossibleWinLine>> futureVertical = new CompletableFuture<>();


        AtomicReference<List<GameField.FieldPosition>> fieldMapRef = new AtomicReference<>(coloredFields);
        AtomicReference<GameField.FieldPosition> originFieldRef = new AtomicReference<>(originField);


        // Straight Thread
        new Thread(() -> {
            CompletableFuture<PossibleWinLine> futureStraightRight = new CompletableFuture<>();
            CompletableFuture<PossibleWinLine> futureStraightLeft = new CompletableFuture<>();
            AtomicReference<List<GameField.FieldPosition>> columnPositions = new AtomicReference<>(filterFieldsForColumnOfOrigin(fieldMapRef.get(), originFieldRef.get()));
            if (columnPositions.get().size() <= 1) { // IF Only origin is in Column directly return
                futureStraight.complete(null);
                return;
            }

            // Straight Right Thread
            new Thread(() -> {
                searchForStraightPossibleWinLine(gameField, minFieldsForLine, columnPositions, originFieldRef, futureStraightRight, Direction.RIGHT, lineColor);
            }).start();
            // Straight Left Thread
            new Thread(() -> {
                searchForStraightPossibleWinLine(gameField, minFieldsForLine, columnPositions, originFieldRef, futureStraightLeft, Direction.LEFT, lineColor);
            }).start();
            futureStraight.complete(getListOfFutures(futureStraightRight, futureStraightLeft));
        }).start();


        // Diagonal Thread
        new Thread(() -> {
            CompletableFuture<PossibleWinLine> futureDiagonalUpRight = new CompletableFuture<>();
            CompletableFuture<PossibleWinLine> futureDiagonalUpLeft = new CompletableFuture<>();
            CompletableFuture<PossibleWinLine> futureDiagonalDownRight = new CompletableFuture<>();
            CompletableFuture<PossibleWinLine> futureDiagonalDownLeft = new CompletableFuture<>();
            // Diagonal RIGHT UP THREAD

            new Thread(() -> {
                searchForDirectionalPossibleWinLine(gameField, minFieldsForLine, fieldMapRef, originFieldRef, futureDiagonalUpRight, Direction.DIAGONAL_UP_RIGHT, lineColor);
            }).start();
            // Diagonal LEFT UP THREAD
            new Thread(() -> {
                searchForDirectionalPossibleWinLine(gameField, minFieldsForLine, fieldMapRef, originFieldRef, futureDiagonalUpLeft, Direction.DIAGONAL_UP_LEFT, lineColor);
            }).start();
            // Diagonal RIGHT Down THREAD
            new Thread(() -> {
                searchForDirectionalPossibleWinLine(gameField, minFieldsForLine, fieldMapRef, originFieldRef, futureDiagonalDownRight, Direction.DIAGONAL_DOWN_RIGHT, lineColor);
            }).start();
            // Diagonal Left Down THREAD
            new Thread(() -> {
                searchForDirectionalPossibleWinLine(gameField, minFieldsForLine, fieldMapRef, originFieldRef, futureDiagonalDownLeft, Direction.DIAGONAL_DOWN_LEFT, lineColor);
            }).start();

            futureDiagonal.complete(getListOfFutures(futureDiagonalDownLeft, futureDiagonalUpLeft, futureDiagonalDownRight, futureDiagonalUpRight));
        }).start();

        // Vertical Thread
        new Thread(() -> {
            CompletableFuture<PossibleWinLine> futureUp = new CompletableFuture<>();
            CompletableFuture<PossibleWinLine> futureDown = new CompletableFuture<>();
            AtomicReference<List<GameField.FieldPosition>> rowPositions = new AtomicReference<>(filterFieldsForRowOfOrigin(fieldMapRef.get(), originFieldRef.get()));
            if (rowPositions.get().size() <= 1) { // IF Only origin is in Row directly return
                futureVertical.complete(null);
                return;
            }
            // Straight UP Thread
            new Thread(() -> {
                searchForVerticalPossibleWinLine(gameField, minFieldsForLine, rowPositions, originFieldRef, futureUp, Direction.UP, lineColor);
            }).start();
            /*
            // Straight Left Thread
            new Thread(() -> {
                searchForVerticalPossibleWinLine(gameField,minFieldsForLine,rowPositions, originFieldRef,futureDown,Direction.DOWN,lineColor);
            }).start();

             */
            futureVertical.complete(getListOfFutures(futureUp/*,futureDown*/));
        }).start();
        return getListOfFuturesLists(futureStraight, futureDiagonal, futureVertical);
    }

    @SafeVarargs
    private static List<PossibleWinLine> getListOfFutures(CompletableFuture<PossibleWinLine>... futures) {
        return Arrays.stream(futures).map(future -> {
            try {
                return future.get();
            } catch (Exception exception) {
                exception.printStackTrace();
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @SafeVarargs
    private static List<PossibleWinLine> getListOfFuturesLists(CompletableFuture<List<PossibleWinLine>>... futures) {
        return Arrays.stream(futures).map(future -> {
            try {
                return future.get();
            } catch (Exception exception) {
                exception.printStackTrace();
                return null;
            }
        }).filter(Objects::nonNull).flatMap(List::stream).collect(Collectors.toList());
    }


    private static void searchForVerticalPossibleWinLine(GameField gameField, byte minFieldsForLine, AtomicReference<List<GameField.FieldPosition>> rowPositionsRef, AtomicReference<GameField.FieldPosition> originFieldRef, CompletableFuture<PossibleWinLine> resultFuture, Direction direction, GameField.ICSValue lineColor) {
        if (direction != Direction.UP /*&& direction != Direction.DOWN*/) {
            throw new InputMismatchException("Wrong Input: Direction");
        }
        try {
            /* Only Accept WinLine if Current Field is right Color
            if (rowPositionsRef.get().stream().anyMatch(position -> position.column() == originFieldRef.get().column()+(direction == Direction.UP ? +1 : -1))) { // if originField is not the (for Direction Right: Left Field) (for Direction Left: Right Field)  of a Win Line
                resultFuture.complete(null);
                return;
            }
            List<GameField.FieldPosition> winLineFields = new ArrayList<>(List.of(originFieldRef.get()));

             */
            List<GameField.FieldPosition> winLineFields = new ArrayList<>();
            int count = 0;
            while (count <= 3) {
                int finalCount = count;
                if (rowPositionsRef.get().stream().anyMatch(position -> position.column() == originFieldRef.get().column() + (direction == Direction.UP ? -finalCount : +finalCount))) {
                    winLineFields.add(new GameField.FieldPosition(originFieldRef.get().row(), (byte) (originFieldRef.get().column() + (direction == Direction.UP ? -finalCount : +finalCount))));
                }
                count++;
            }

            if (winLineFields.size() < minFieldsForLine) {
                resultFuture.complete(null);
                return;
            }

            resultFuture.complete(new PossibleWinLine(originFieldRef.get(), winLineFields, direction).isPossible(gameField, lineColor));
        } catch (Exception ignored) {
            resultFuture.complete(null);
        }
    }

    private static void searchForDirectionalPossibleWinLine(GameField gameField, byte minFieldsForLine, AtomicReference<List<GameField.FieldPosition>> fieldMapRef, AtomicReference<GameField.FieldPosition> originFieldRef, CompletableFuture<PossibleWinLine> resultFuture, Direction direction, GameField.ICSValue lineColor) {

        if (direction != Direction.DIAGONAL_UP_RIGHT && direction != Direction.DIAGONAL_DOWN_LEFT && direction != Direction.DIAGONAL_DOWN_RIGHT && direction != Direction.DIAGONAL_UP_LEFT) {
            throw new InputMismatchException("Wrong Input: Direction");
        }
        try {
            /* Only Accept WinLine if Current Field is right Color
            if (fieldMapRef.get().stream().anyMatch(position -> position.row() == originFieldRef.get().row()+(direction == Direction.DIAGONAL_DOWN_LEFT || direction == Direction.DIAGONAL_UP_LEFT ? +1 : -1) &&  position.column() == originFieldRef.get().column()+(direction == Direction.DIAGONAL_DOWN_LEFT || direction == Direction.DIAGONAL_DOWN_RIGHT ? -1 : +1))) { // if originField is not the start of a Win Line
                resultFuture.complete(null);
                return;
            }
            List<GameField.FieldPosition> winLineFields = new ArrayList<>(List.of(originFieldRef.get()));
             */
            List<GameField.FieldPosition> winLineFields = new ArrayList<>();
            int count = 0;
            while (count <= 3) {
                int finalCount = count;
                if (fieldMapRef.get().stream().anyMatch(position -> position.row() == originFieldRef.get().row() + (direction == Direction.DIAGONAL_DOWN_LEFT || direction == Direction.DIAGONAL_UP_LEFT ? -finalCount : +finalCount) && position.column() == originFieldRef.get().column() + (direction == Direction.DIAGONAL_DOWN_LEFT || direction == Direction.DIAGONAL_DOWN_RIGHT ? +finalCount : -finalCount))) {
                    winLineFields.add(new GameField.FieldPosition((byte) (originFieldRef.get().row() + (direction == Direction.DIAGONAL_DOWN_LEFT || direction == Direction.DIAGONAL_UP_LEFT ? -finalCount : +finalCount)), (byte) (originFieldRef.get().column() + (direction == Direction.DIAGONAL_DOWN_LEFT || direction == Direction.DIAGONAL_DOWN_RIGHT ? +finalCount : -finalCount))));
                }
                count++;
            }

            if (winLineFields.size() < minFieldsForLine) {
                resultFuture.complete(null);
                return;
            }


            resultFuture.complete(new PossibleWinLine(originFieldRef.get(), winLineFields, direction).isPossible(gameField, lineColor));
        } catch (Exception ignored) {
            resultFuture.complete(null);
        }

    }

    private static void searchForStraightPossibleWinLine(GameField gameField, byte minFieldsForLine, AtomicReference<List<GameField.FieldPosition>> columnPositionsRef, AtomicReference<GameField.FieldPosition> originFieldRef, CompletableFuture<PossibleWinLine> resultFuture, Direction direction, GameField.ICSValue lineColor) {
        if (direction != Direction.RIGHT && direction != Direction.LEFT) {
            throw new InputMismatchException("Wrong Input: Direction");
        }
        try {
            /* Only Accept WinLine if Current Field is right Color
            if (columnPositionsRef.get().stream().anyMatch(position -> position.row() == originFieldRef.get().row()+(direction == Direction.RIGHT ? -1 : +1))) { // if originField is not the (for Direction Right: Left Field) (for Direction Left: Right Field)  of a Win Line
                resultFuture.complete(null);
                return;
            }
            List<GameField.FieldPosition> winLineFields = new ArrayList<>(List.of(originFieldRef.get()));
            */

            List<GameField.FieldPosition> winLineFields = new ArrayList<>();
            int count = 0;
            while (count <= 3) {
                int finalCount = count;
                if (columnPositionsRef.get().stream().anyMatch(position -> position.row() == originFieldRef.get().row() + (direction == Direction.RIGHT ? +finalCount : -finalCount))) {
                    winLineFields.add(new GameField.FieldPosition((byte) (originFieldRef.get().row() + (direction == Direction.RIGHT ? +finalCount : -finalCount)), originFieldRef.get().column()));
                }
                count++;
            }

            if (winLineFields.size() < minFieldsForLine) {
                resultFuture.complete(null);
                return;
            }


            resultFuture.complete(new PossibleWinLine(originFieldRef.get(), winLineFields, direction).isPossible(gameField, lineColor));
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
            gameField.getRow((byte) i).ifPresent(hashMap -> {
                map.put((byte) finalI, hashMap);
            });
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
