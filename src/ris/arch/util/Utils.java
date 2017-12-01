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

    /**
     * The the integer associativity from the params
     * @param way way in string
     * @param lineSize size of the line
     * @param cacheSizeString cache size
     * @return the integer value of the way
     */
    public static int getWay(String way, int lineSize, String cacheSizeString) {
        int intWay = 0;
        if (way.equalsIgnoreCase("Full")) {
            if (cacheSizeString.endsWith("K")) { //Only implemented for K. Not compatible with others.
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

    /**
     * Return the value from the instruction
     * @param instruction the instruction
     * @return the value from the instruction
     */
    public static String getValueFromInstruction(String instruction) {
        if (instruction != null) {
            return instruction.split(" ")[1];
        }
        return null;
    }

    /**
     * Return value in 32 bit format
     * @param number the number
     * @return value in 32 bit
     */
    public static String get32BitBinFromInt(int number) {
        String binAddr = Integer.toBinaryString(number);
        return String.format("%032d", new BigInteger(binAddr));
    }

    /**
     * Return the block offset
     * @param lineSize
     * @return
     */
    public static int getNumberOfBitForBlockOffset(int lineSize) {
        return log2(lineSize);
    }

    /**
     * Return the set index
     * @param numberOfSetIndex
     * @return
     */
    public static int getNumberOfBitsForSetIndex(int numberOfSetIndex) {
        return log2(numberOfSetIndex);
    }

    /**
     * Return the Least recently used block based on time
     * @param cacheLine List of Cache Line
     * @return Cache Line
     */
    public static CacheLine getLRUBlock(List<CacheLine> cacheLine) {
        CacheLine lruBlock = null;
        lruBlock = cacheLine.stream().min(Comparator.comparingLong(CacheLine::getTime)).get();
        return lruBlock;
    }
}
