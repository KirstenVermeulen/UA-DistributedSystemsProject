package ua.util;

import static java.lang.Math.abs;

public class Functions {
    public static int hashFunction(String toHash) {
        long max = 2147483647;
        long min = -2147483648;
        long A = toHash.hashCode();
        A += (max + 1);
        long B = max + abs(min) + 1;
        return (int) (A * 32768 / B);
    }
}
