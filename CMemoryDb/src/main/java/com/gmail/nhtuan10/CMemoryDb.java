package com.gmail.nhtuan10;

import java.lang.foreign.*;
import java.lang.invoke.VarHandle;
import java.util.*;


public class CMemoryDb {

    public static final int LENGTH = 1_000_000_000;

    final static int INT_BITS = 32;
    final static int BYTE_BITS = 8;

    public static void useJavaObjOnHeap(String[] args) {
        Map<String, List<Object>> stockData = new HashMap<>();
        stockData.put("symbol", new ArrayList<>());
        stockData.put("user", new ArrayList<>());
        stockData.put("quantity", new ArrayList<>());
        stockData.put("price", new ArrayList<>());
        long xs = System.currentTimeMillis();
        for (long i = 0L; i < 1_000_000_000L; i++) {
            if (i % 2 == 0) {
                stockData.get("symbol").add("NFLX");
            } else {
                stockData.get("symbol").add("MSFT");
            }
//            stockData.get("user").add("A");
            stockData.get("quantity").add(15L);
//            stockData.get("price").add(400L);
        }
        System.out.println("pump time taken: " + (System.currentTimeMillis() - xs));

        xs = System.currentTimeMillis();
        long sum = 0L;
        for (int i = 0; i < stockData.get("quantity").size(); i++) {
            if (stockData.get("symbol").get(i).equals("NFLX")) {
                sum += (long) stockData.get("quantity").get(i);
            }
        }

        System.out.println("Total quantity: " + sum);
        System.out.println(sum + ", time taken: " + (System.currentTimeMillis() - xs));
    }


    public static void useJavaPrimitiveOnHeap(String[] args) {
//        Map<String, Object> stockData = new HashMap<>();
        Map<String, Integer> symbolData = Map.of("NTFLX", 0, "MSFT", 1);
        long xs = System.nanoTime();
        Map<String, int[]> stockData = new HashMap<>();
//        stockData.put("symbol", new String[1_000_000_000]);
        stockData.put("symbol", new int[LENGTH]);
        stockData.put("user", new int[LENGTH]);
        stockData.put("quantity", new int[LENGTH]);
        stockData.put("price", new int[LENGTH]);

        int[] symbols = stockData.get("symbol");
        int[] quantities = stockData.get("quantity");
        int[] users = stockData.get("user");
//        int[][] data = new int[2][LENGTH];
        for (int i = 0; i < LENGTH; i++) {
            symbols[i] = i % 5;
            users[i] = i % 7;
//            stockData.get("user").add("A");
            quantities[i] = 15;
            stockData.get("price")[i] = 400;
//            data[i] = new int[]{symbols[i], users[i]};
        }
        System.out.println("pump time taken: " + (System.nanoTime() - xs) / 1_000_000.0);
        // Create BitMap index
//        BitSet[] symbolIdx = createBitMapIndex(IntStream.range(0,5).toArray(), symbols);
//        BitSet[] userIdx = createBitMapIndex(IntStream.range(0,7).toArray(), users);
//        BitSet q = (BitSet) symbolIdx[0].clone();
//        q.and(userIdx[0]);

        xs = System.nanoTime();
//        List<Integer>[] mapIdx = createMapIndex(symbols, 5, users, 7);
        SimpleIntArrayList[] mapIdx = createMapIndex3(symbols, 5, users, 7);
        System.out.println("Index time taken (ms): " + (System.nanoTime() - xs) / 1_000_000.0);
        xs = System.nanoTime();
//        long sum = sumBitMapIndexQuery(quantities, q);
//        long sum = sumQuery(symbols, users, quantities);
//        long sum = sumQueryMapIndex(mapIdx, 0, quantities);
        long sum = queryFirstInMapIndex(mapIdx, 0, quantities);
        System.out.println("Total quantity: " + sum);
        System.out.println(sum + ", time taken (ms): " + (System.nanoTime() - xs) / 1_000_000.0);
    }

    public static long sumBitMapIndexQuery(int[] quantities, BitSet q) {
        long sum = 0L;
        for (int i = 0; i < LENGTH; i++) {
            if (q.get(i)) {
                sum += quantities[i];
            }
        }
        return sum;
    }

    public static long sumQuery(int[] symbols, int[] users, int[] quantities) {
        long sum = 0L;
        for (int i = 0; i < LENGTH; i++) {
            if (symbols[i] == 0 && users[i] == 0) {
                sum += quantities[i];
            }
        }
        return sum;
    }

    public static BitSet[] createBitMapIndex(int[] columnValues, int[] data) {
        BitSet[] bitSets = new BitSet[columnValues.length];
        for (int i = 0; i < columnValues.length; i++) {
            bitSets[i] = new BitSet(data.length);
        }

        for (int j = 0; j < data.length; j++) {
            bitSets[data[j]].set(j);
        }

        return bitSets;
    }

    public static TreeSet<Integer> createTreeSetIndex(int[] data) {
        TreeSet<Integer> treeSet = new TreeSet<>();
        for (int datum : data) {
            treeSet.add(datum);
        }
        return treeSet;
    }

    public static Map<int[], List<Integer>> createMapIndex2(int[] data1, int[] data2, int mapInitialSize) {
//        int[][] map = new int[data.length/columnValues.length][columnValues.length];
        Map<int[], List<Integer>> map = new HashMap<>(mapInitialSize);
        int estListLength = data1.length / mapInitialSize;
        for (int i = 0; i < data1.length; i++) {
            int[] key = new int[]{data1[i], data2[i]};
            map.putIfAbsent(key, new ArrayList<>(estListLength));
            map.get(key).add(i);
        }
        return map;
    }

    public static long sumQueryMapIndex(List<Integer>[] mapIndex, int pos, int[] quantities) {
        long sum = 0L;
        List<Integer> list = mapIndex[pos];
        for (int i = 0; i < list.size(); i++) {
            sum += quantities[list.get(i)];
        }
        return sum;
    }

    public static long sumQueryMapIndex(SimpleIntArrayList[] mapIndex, int pos, int[] quantities) {
        long sum = 0L;
        SimpleIntArrayList list = mapIndex[pos];
        for (int i = 0; i < list.size(); i++) {
            sum += quantities[list.get(i)];
        }
        return sum;
    }

    public static long queryFirstInMapIndex(SimpleIntArrayList[] mapIndex, int pos, int[] quantities) {
        SimpleIntArrayList list = mapIndex[pos];
        return quantities[list.get(0)];
    }

    public static List<Integer>[] createMapIndex(int[] data1, int data1MaxSize, int[] data2, int data2MaxSize) {
//        int[][] map = new int[data.length/columnValues.length][columnValues.length];
        int mapInitialSize = data1MaxSize * data2MaxSize;
        List<Integer>[] map = new List[mapInitialSize];
        int estListLength = data1.length / mapInitialSize + 1;
//        int[] mapEleLstPointer = new int[mapInitialSize];
        for (int i = 0; i < mapInitialSize; i++) {
//            map[i] = new int[estListLength];
//            mapEleLstPointer[i] = 0;
            map[i] = new ArrayList<>(estListLength);
        }

        for (int i = 0; i < data1.length; i++) {
            int key = data1[i] * data2MaxSize + data2[i];
            //            map[key][mapEleLstPointer[key]] = i;
            //            mapEleLstPointer[key]++;
            map[key].add(i);
        }
        return map;
    }

    public static SimpleIntArrayList[] createMapIndex3(int[] data1, int data1MaxSize, int[] data2, int data2MaxSize) {
        int mapInitialSize = data1MaxSize * data2MaxSize;
        SimpleIntArrayList[] map = new SimpleIntArrayList[mapInitialSize];
        int estListLength = data1.length / mapInitialSize + 1;
        for (int i = 0; i < mapInitialSize; i++) {
            map[i] = new SimpleIntArrayList(estListLength);
        }

        for (int i = 0; i < data1.length; i++) {
            int key = data1[i] * data2MaxSize + data2[i];
            map[key].add(i);
        }
        return map;
    }

    public static class SimpleIntArrayList {
        int size = 0;
        int increment = 100;
        public int size() {
            return size;
        }

        int[] elements;

        public SimpleIntArrayList(int initialSize) {
            this.elements = new int[initialSize];
        }

        public SimpleIntArrayList add(int e) {
            if (size == elements.length) {
//                elements = Arrays.copyOf(elements, elements.length + increment);
                System.arraycopy(elements, 0, elements, 0,  size+ increment);
            }
            elements[size++] = e;
            return this;
        }

        public SimpleIntArrayList add(int index, int e) {
            if (size == elements.length) {
//                elements = Arrays.copyOf(elements, elements.length + increment);
                System.arraycopy(elements, index , elements, index + 1, size + increment);
            }
//            for (int i = size -1; i >= index; i++) {
//                elements[i++] = elements[i];
//            }
            elements[index] = e;
            return this;
        }

        public int get(int index) {
            return elements[index];
        }
    }

    public static void useJavaPrimitiveOffHeap(String[] args) {
        Map<String, Integer> symbolData = Map.of("NTFLX", 0, "MSFT", 1);
        long xs = System.currentTimeMillis();
        try (Arena arena = Arena.ofConfined()) {

            SequenceLayout dataLayout
                    = MemoryLayout.sequenceLayout(LENGTH,
                    MemoryLayout.structLayout(
                            ValueLayout.JAVA_INT.withName("symbol"),
                            ValueLayout.JAVA_INT.withName("quantity"),
                            ValueLayout.JAVA_INT.withName("price")));

            VarHandle symbolHandle
                    = dataLayout.varHandle(MemoryLayout.PathElement.sequenceElement(),
                    MemoryLayout.PathElement.groupElement("symbol"));
            VarHandle quantityHandle
                    = dataLayout.varHandle(MemoryLayout.PathElement.sequenceElement(),
                    MemoryLayout.PathElement.groupElement("quantity"));

            VarHandle priceHandle
                    = dataLayout.varHandle(MemoryLayout.PathElement.sequenceElement(),
                    MemoryLayout.PathElement.groupElement("price"));

            MemorySegment segment = arena.allocate(dataLayout);

            for (int i = 0; i < dataLayout.elementCount(); i++) {
                symbolHandle.set(segment, 0L, (long) i, i % 2);
                quantityHandle.set(segment, 0L, (long) i, 15);
                priceHandle.set(segment, 0L, (long) i, 400);
            }
            System.out.println("pump time taken: " + (System.currentTimeMillis() - xs));
            xs = System.currentTimeMillis();

            long sum = 0L;
            for (int i = 0; i < dataLayout.elementCount(); i++) {
                if ((int) symbolHandle.get(segment, 0L, (long) i) % 2 == 0) {
                    sum += (int) quantityHandle.get(segment, 0L, (long) i);
                }
            }
            System.out.println("Total quantity: " + sum);
            System.out.println(sum + ", time taken: " + (System.currentTimeMillis() - xs));
        }
    }



    /**
     * Left rotate integer n by k bit
     * @param n
     * @param k
     * @return
     */
    public static int leftRotateBit(int n, int k ) {
        return ( n << k) | ( n >> (INT_BITS - k));
    }

    /**
     * Right rotate integer n by k bit
     * @param n
     * @param k
     * @return
     */
    public static int rightRotateBit(int n, int k ) {
        return ( n >> k) | ( n << (INT_BITS - k));
    }

    /**
     * Right rotate integer n by k bit
     * @param n
     * @param k
     * @return
     */
    public static int rightRotateBit(byte n, int k ) {
        return ( n >> k) | (byte)( n << (BYTE_BITS - k));
    }

    public static void main(String[] args) {
        System.out.println(rightRotateBit((byte)21, (byte)2));
        useJavaPrimitiveOnHeap(args);
//        useJavaPrimitiveOffHeap(args);
    }
}
