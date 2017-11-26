package ris.arch.util;

import ris.arch.domain.CacheLine;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;

/**
 * This is the utility class for the project
 **/
public class Utils {

    public static int getNumberOfCacheLine(int lineSize, int way, String cacheSizeString) {
        int numberOfCacheLine = 0;

        if (cacheSizeString.endsWith("K")) {
            cacheSizeString = cacheSizeString.replaceAll("\\D+", "");
            int cacheSize = Integer.parseInt(cacheSizeString) * 1024;
            numberOfCacheLine = cacheSize / (lineSize * way);

        }

        return numberOfCacheLine;
    }

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
