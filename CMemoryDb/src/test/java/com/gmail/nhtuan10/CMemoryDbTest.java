package com.gmail.nhtuan10;

import org.junit.jupiter.api.Test;

import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.*;

class CMemoryDbTest {

    @Test
    void createBitMapIndex() {
        BitSet[] bitSets = CMemoryDb.createBitMapIndex(new int[]{0,1,2,3},new int[]{0,3,1,2,2,3,0,1});
        assertEquals( BitSet.valueOf(new long[]{(long) 0b01000001}), bitSets[0]);
        assertEquals( BitSet.valueOf(new long[]{(long) 0b10000100}), bitSets[1]);
        assertEquals( BitSet.valueOf(new long[]{(long) 0b00011000}), bitSets[2]);
        assertEquals( BitSet.valueOf(new long[]{(long) 0b00100010}), bitSets[3]);
    }
}