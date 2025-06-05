package de.juniorjacki.game;

import java.util.HashMap;
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

}
