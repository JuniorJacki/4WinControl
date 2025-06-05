package de.juniorjacki.game;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class GameField {

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


    /**
     * Gets Value of Field of the GameField
     * @param rowNum Row Number Counted from Driving Side Left to Right (Starting from Scanner reflektor)
     * @param columnNum Column Counted Downwards (0 is the Top) , 5 is the Column at the Bottom
     * @return Optional of Field Value or Optional.empty()
     */
    public Optional<Byte> getFieldValue(byte rowNum, byte columnNum) {
        AtomicReference<HashMap<Byte, Byte>> row = switch (rowNum) {
            case 0 -> row0;
            case 1 -> row1;
            case 2 -> row2;
            case 3 -> row3;
            case 4 -> row4;
            case 5 -> row5;
            case 6 -> row6;
            default -> null;
        };
        if (row == null) return Optional.empty();
        return Optional.of(row.get().get(columnNum));
    }

    /**
     * Gets Interpreted Color Value of Field of the GameField
     * @param rowNum Row Number Counted from Driving Side Left to Right (Starting from Scanner reflektor)
     * @param columnNum Column Counted Downwards (0 is the Top) , 5 is the Column at the Bottom
     * @return Optional of Color Value or Air for Empty Fields
     */
    public Optional<ICSValue> getFieldICSValue(byte rowNum, byte columnNum) {
        return getFieldValue(rowNum, columnNum).map(ICSValue::getColorByValue).or(() -> Optional.of(ICSValue.AIR));
    }

    /**
     * InterpretedColorScannerValue
     */
    public enum ICSValue {
        RED_DROP(40,65 ),
        YELLOW_DROP(70,100 ),
        BLUE(20,35 ),
        AIR(0,  0);

        private final byte min;
        private final byte max;

        ICSValue(int min, int max) {
            this.min = (byte)min;
            this.max = (byte)max;
        }

        public static ICSValue getColorByValue(Byte colorValue) {
            return Arrays.stream(values()).filter(iCSValue -> iCSValue.min >= colorValue && iCSValue.max <= colorValue).findFirst().orElse(AIR);
        }

    }
}
