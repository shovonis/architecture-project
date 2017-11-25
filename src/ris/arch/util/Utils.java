package ris.arch.util;

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
}
