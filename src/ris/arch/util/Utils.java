package ris.arch.util;

import ris.arch.domain.CacheLine;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;

/**
 * This is the utility class for the project
 **/
public class Utils {

    /**
     * Get the number of cache line or number of set in the cache
     *
     * @param lineSize        the size of each block
     * @param way             associativity of the cache
     * @param cacheSizeString cache total size
     * @return the number number of cache line in the cache
     */
    public static int getNumberOfCacheLine(int lineSize, int way, String cacheSizeString) {
        int numberOfCacheLine = 0;

        if (cacheSizeString.endsWith("K")) {
            cacheSizeString = cacheSizeString.replaceAll("\\D+", "");
            int cacheSize = Integer.parseInt(cacheSizeString) * 1024;
            numberOfCacheLine = cacheSize / (lineSize * way);
        }

        return numberOfCacheLine;
    }

    public static int getWay(String way, int lineSize, String cacheSizeString) {
        int intWay = 0;
        if (way.equalsIgnoreCase("Full")) {
            if (cacheSizeString.endsWith("K")) {
                cacheSizeString = cacheSizeString.replaceAll("\\D+", "");
                int cacheSize = Integer.parseInt(cacheSizeString) * 1024;
                intWay = cacheSize / lineSize;
            }

        } else {
            intWay = Integer.parseInt(way);
        }

        return intWay;
    }

    /**
     * Log 2 base operation
     *
     * @param number the operand
     * @return the log 2 base return
     */
    private static int log2(int number) {
        return (int) (Math.log(number) / Math.log(2));
    }

    public static String getInstructionTypeFromInstruction(String instruction) {
        if (instruction != null) {
            return instruction.split(" ")[0];
        }
        return null;
    }

    public static String getValueFromInstruction(String instruction) {
        if (instruction != null) {
            return instruction.split(" ")[1];
        }
        return null;
    }

    public static String get32BitBinFromInt(int number) {
        String binAddr = Integer.toBinaryString(number);
        return String.format("%032d", new BigInteger(binAddr));
    }

    public static int getNumberOfBitForBlockOffset(int lineSize) {
        return log2(lineSize);
    }

    public static int getNumberOfBitsForSetIndex(int numberOfSetIndex) {
        return log2(numberOfSetIndex);
    }

    public static CacheLine getLRUBlock(List<CacheLine> cacheLine) {
        CacheLine lruBlock = null;
        lruBlock = cacheLine.stream().min(Comparator.comparingLong(CacheLine::getTime)).get();
        return lruBlock;
    }
}
