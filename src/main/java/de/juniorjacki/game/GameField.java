package de.juniorjacki.game;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class GameField implements Cloneable{

    // Rows are Counted from Driving Side Left to Right (Starting from Scanner reflektor)
    // Each Row hat its own reference because of optimization for Multithreading
    // First Byte is the Column Counted Downwards (0 is the Highest) , 5 is the Column at the Bottom
    // Associated Byte is its Color Value measured by the Color Sensor
    // Example: Entry in row0 <4,45> -> The 4th Column of Row 4 has a HSV().v value of 45
    AtomicReference<HashMap<Byte, Byte>> row0 = new AtomicReference<>(new HashMap<Byte, Byte>());
    AtomicReference<HashMap<Byte, Byte>> row1 = new AtomicReference<>(new HashMap<Byte, Byte>());
    AtomicReference<HashMap<Byte, Byte>> row2 = new AtomicReference<>(new HashMap<Byte, Byte>());
    AtomicReference<HashMap<Byte, Byte>> row3 = new AtomicReference<>(new HashMap<Byte, Byte>());
    AtomicReference<HashMap<Byte, Byte>> row4 = new AtomicReference<>(new HashMap<Byte, Byte>());
    AtomicReference<HashMap<Byte, Byte>> row5 = new AtomicReference<>(new HashMap<Byte, Byte>());
    AtomicReference<HashMap<Byte, Byte>> row6 = new AtomicReference<>(new HashMap<Byte, Byte>());

    @Override
    public GameField clone() {
        try {
            GameField clone = (GameField) super.clone();
            clone.row0 = new AtomicReference<>(new HashMap<>(row0.get()));
            clone.row1 = new AtomicReference<>(new HashMap<>(row1.get()));
            clone.row2 = new AtomicReference<>(new HashMap<>(row2.get()));
            clone.row3 = new AtomicReference<>(new HashMap<>(row3.get()));
            clone.row4 = new AtomicReference<>(new HashMap<>(row4.get()));
            clone.row5 = new AtomicReference<>(new HashMap<>(row5.get()));
            clone.row6 = new AtomicReference<>(new HashMap<>(row6.get()));
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }


    /**
     *
     * @param gameField Gamefield at Timestamp
     * @param currentMove Current Move that was done
     * @param badMoves Bad Moves for current Move
     * @param prohibitWinLines Prohibit Moves that that should be done to prohibit Opponent Win
     * @param currentWinLines Current Possible WinLine
     * @param neededMovesToWinAfterCurrent Needed Moves to Win After for Current
     * @param neededMovesToWinAfterOpponent Needed Moves to Win After for Opponent
     * @param timestamp Timestamp of Move
     */
    public record HistoryMove(GameField gameField, FieldPosition currentMove, List<FieldPosition> badMoves, List<Algorithm.PossibleWinLine> prohibitWinLines, List<Algorithm.PossibleWinLine> currentWinLines, int neededMovesToWinAfterCurrent, int neededMovesToWinAfterOpponent, long timestamp, ICSValue lineColor) {}

    /**
     * Object for selecting Field on the GameField
     * @param row Row Counted from Driving Side Left to Right (Starting from Scanner reflektor)
     * @param column Column Counted Downwards (0 is the Top) , 5 is the Column at the Bottom
     */
    public record FieldPosition(byte row,byte column) {
        public FieldPosition getFieldBelow() {
            return new FieldPosition(row, (byte) (column+1));
        }
        public boolean isFilled(GameField gameField) {
            return !gameField.getFieldICSValue(this).equals(ICSValue.AIR);
        }

        public ICSValue getFieldICSValue(GameField gameField) {
            return gameField.getFieldICSValue(this);
        }

        public boolean isFilledWith(GameField gameField,ICSValue color) {
            return gameField.getFieldICSValue(this).equals(color);
        }

        /**
         * @return If Position is neighbour to given Position
         */
        public boolean isAside(FieldPosition position) {
            if (position.column == this.column) {
                if (position.row-1 == this.row || position.row+1 == this.row) return true ;
            }
            return false;
        }

        /**
         * @return If Position is neighbour to any of the given Positions. But only if Position in not in the middle of the Positions
         */
        public boolean isAside(List<FieldPosition> positions) {
            if (positions.isEmpty()) return false;
            if (positions.size() >= 3) return true;

            if (positions.size() == 2) {
                if (column == positions.get(0).column || column == positions.get(1).column) {
                    if ((positions.get(0).row > this.row && positions.get(1).row > this.row) ||  (positions.get(0).row < this.row && positions.get(1).row < this.row) && ((positions.get(1).row+1) != positions.get(0).row || (positions.get(1).row-1) != positions.get(0).row)) return false;
                }
            } else return isAside(positions.get(0));
            return true;
        }

        public boolean isEmpty(GameField gameField) {
            return gameField.getFieldICSValue(this).equals(ICSValue.AIR);
        }

        public Optional<Queue<FieldPosition>> getNeededMovesToFillField(GameField gameField) {
            Optional<HashMap<Byte, Byte>> fieldRow = gameField.getRow(this.row());
            if (fieldRow.isPresent()) {
                if (this.isEmpty(gameField)) {
                    Queue<GameField.FieldPosition> queue = new LinkedList<>();
                    for (int i = 5; i >= this.column(); i--) {
                        GameField.FieldPosition rowField = new GameField.FieldPosition(this.row(), (byte) i);
                        if (rowField.isEmpty(gameField)) {
                            queue.add(rowField);
                        }
                    }
                    return Optional.of(queue);
                }
            }
            return Optional.empty();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof FieldPosition position) {
                return position.row == this.row && position.column == this.column;
            }
            return false;
        }
    }

    public boolean isEmpty() {
        for (Map.Entry<Byte, HashMap<Byte, Byte>> entry : getAllFields().entrySet()) {
            for (Byte value : entry.getValue().values()) {
                if (value != null) {
                    return false; // Early exit, sobald ein nicht-null Wert gefunden wird
                }
            }
        }
        return true; // Alle Felder sind null, also leer
    }

    public String generateBoardString() {
        StringBuilder board = new StringBuilder();
        for (byte column = 0; column < 6; column++) {
            board.append(String.format("%d: ", column));
            for (byte row = 0; row < 7; row++) {
                ICSValue value = getFieldICSValue(row, column);
                if (value == ICSValue.RED) {
                    board.append("R ");
                } else if (value == ICSValue.YELLOW) {
                    board.append("Y ");
                } else {
                    board.append(". ");
                }
            }
            board.setLength(board.length() - 1);
            board.append("\n");
        }
        board.append("   0 1 2 3 4 5 6\n");
        return board.toString();
    }

    /**
     * @return Map of All Fields on GameField
     */
    public HashMap<Byte, HashMap<Byte, Byte>> getAllFields() {
        HashMap<Byte, HashMap<Byte, Byte>> map = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            int finalI = i;
            getRow((byte) i).ifPresent(hashMap -> {
                map.put((byte) finalI, hashMap);
            });
        }
        return map;
    }

    /**
     * @param rowindex Row Index
     * @return The Reference of the Row
     */
    private AtomicReference<HashMap<Byte, Byte>> getRowReference(byte rowindex) {
        return switch (rowindex) {
            case 0 -> row0;
            case 1 -> row1;
            case 2 -> row2;
            case 3 -> row3;
            case 4 -> row4;
            case 5 -> row5;
            case 6 -> row6;
            default -> null;
        };
    }

    /**
     * Gets all Values saved for specified Row
     * @param rowNum Row Number Counted from Driving Side Left to Right (Starting from Scanner reflektor)
     * @return The Row or Empty Optional
     */
    public Optional<HashMap<Byte, Byte>> getRow(byte rowNum) {
        return Optional.ofNullable(getRowReference(rowNum)).map(AtomicReference::get);
    }

    /**
     * Sets the new Color Value of the specified gameField
     * @param newValue New Color Value
     */
    public GameField updateField(FieldPosition position, byte newValue ){
        if (newValue == -1) return this; // Filter Bad Input Values
        return updateField(position.row(),position.column(),newValue);
    }

    /**
     * Sets the new Color Value of the specified gameField
     * @param rowNum Row Number Counted from Driving Side Left to Right (Starting from Scanner reflektor)
     * @param columnNum Column Counted Downwards (0 is the Top) , 5 is the Column at the Bottom
     * @param newValue New Color Value
     */
    public GameField updateField(byte rowNum, byte columnNum, byte newValue ){
        Optional.ofNullable(getRowReference(rowNum)).ifPresent(rowReference -> {
            rowReference.get().put(columnNum,newValue);
        });
        return this;
    }

    /**
     * Gets Value of Field of the GameField
     * @return Optional of Field Value or Optional.empty()
     */
    public Optional<Byte> getFieldValue(FieldPosition position) {
        return getFieldValue(position.row(),position.column());
    }

    /**
     * Gets Value of Field of the GameField
     * @param rowNum Row Number Counted from Driving Side Left to Right (Starting from Scanner reflektor)
     * @param columnNum Column Counted Downwards (0 is the Top) , 5 is the Column at the Bottom
     * @return Optional of Field Value or Optional.empty()
     */
    public Optional<Byte> getFieldValue(byte rowNum, byte columnNum) {
        return Optional.ofNullable(getRowReference(rowNum)).map(rowReference -> rowReference.get().get(columnNum));
    }

    /**
     * Gets Interpreted Color Value of Field of the GameField
     * @return Optional of Color Value or Air for Empty Fields
     */
    public ICSValue getFieldICSValue(FieldPosition position) {
        return getFieldICSValue(position.row(),position.column());
    }

    /**
     * Gets Interpreted Color Value of Field of the GameField
     * @param rowNum Row Number Counted from Driving Side Left to Right (Starting from Scanner reflektor)
     * @param columnNum Column Counted Downwards (0 is the Top) , 5 is the Column at the Bottom
     * @return Optional of Color Value or Air for Empty Fields
     */
    public ICSValue getFieldICSValue(byte rowNum, byte columnNum) {
        return getFieldValue(rowNum, columnNum).map(ICSValue::getColorByValue).or(() -> Optional.of(ICSValue.AIR)).get();
    }

    /**
     * InterpretedColorScannerValue
     */
    public enum ICSValue {
        RED(40,65 ,new Color(220,37,37)),
        YELLOW(66,100 ,new Color(255,193,7)),
        BLUE(5,39, Color.BLUE),
        AIR(0,  0,null),


        // GUI
        RED_WIN(101,101,new Color(0, 255, 234)),
        YELLOW_WIN(102,102,new Color(200, 255, 0)),
        BAD_MOVE(104,104,new Color(47, 0, 255)),
        CURRENT_MOVE(105,105,new Color(48, 131, 255))
        ;


        public byte getMin() {
            return min;
        }

        private final byte min;
        private final byte max;
        public final Color displayColor;

        ICSValue(int min, int max,Color displayColor) {
            this.min = (byte)min;
            this.max = (byte)max;
            this.displayColor = displayColor;
        }

        public static ICSValue getColorByValue(Byte colorValue) {
            return Arrays.stream(values()).filter(iCSValue -> iCSValue.min <= colorValue && iCSValue.max >= colorValue).findFirst().orElse(AIR);
        }

    }
}
