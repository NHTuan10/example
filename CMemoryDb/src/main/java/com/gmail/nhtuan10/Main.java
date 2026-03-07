package com.gmail.nhtuan10;

import java.math.BigInteger;

public class Main {
    public static void main(String[] args) {
        long xs = System.currentTimeMillis();
        System.out.println("Hello, World!");
        long sum = 0L;
        for (long i=0L; i < 10_000_000_000L; i++){
            sum += i;
        }
        System.out.println( sum);
//
//        BigInteger sum = BigInteger.ZERO;
//        for (long i=0L; i < 10_000_000_000L; i++){
//            sum = sum.add( BigInteger.valueOf(i));
//        }
        System.out.println( sum + ", time taken: " + (System.currentTimeMillis() - xs));

    }
}