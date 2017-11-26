package ris.arch.service;

import ris.arch.domain.CacheConf;
import ris.arch.domain.CacheLine;
import ris.arch.domain.MainMemory;
import ris.arch.util.Utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class InstructionManager {
    public static final int MACHINE_BIT = 32;

    private BufferedReader bufferedReader = null;
    private FileReader fileReader = null;
    private Map<String, List<List<CacheLine>>> cacheLevelMap = null;
    private List<CacheConf> cacheConfList = null;
    private MainMemory mainMemory = null;

    public InstructionManager(Map<String, List<List<CacheLine>>> cacheLevelMap, MainMemory mainMemory, List<CacheConf> cacheConfList) {
        this.cacheLevelMap = cacheLevelMap;
        this.mainMemory = mainMemory;
        this.cacheConfList = cacheConfList;
    }

    private void initializeFile(String fileName) {

        try {
            fileReader = new FileReader(fileName);
            bufferedReader = new BufferedReader(fileReader);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void processInstructionFromFile(String inputSequenceFile) {
        String line;
        try {
            initializeFile(inputSequenceFile);

            while ((line = bufferedReader.readLine()) != null) {

                String instructionType = Utils.getInstructionTypeFromInstruction(line);
                String instructionValue = Utils.getValueFromInstruction(line);

                if (instructionType.equalsIgnoreCase("ld")) {
                    processReadOperations(instructionValue);
                }
                if (instructionType.equalsIgnoreCase("st")) {
                    processWriteOperations(instructionValue);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processReadOperations(String instructionValue) {
        String binaryValue = Utils.get32BitBinFromInt(Integer.parseInt(instructionValue)); //The binary representation of the input
            System.out.println("Instruction is [ld " + instructionValue + "]" + " binary value : " + binaryValue);
        boolean isMemoryAccessRequired = true;
        int accessTime = 0;

        for (CacheConf cacheConf : cacheConfList) {
            List<List<CacheLine>> cacheLevelX = cacheLevelMap.get(cacheConf.getLevel());
            isMemoryAccessRequired = true;
            accessTime += cacheConf.getHitTime();

            int numberOfSetIndex = Utils.getNumberOfCacheLine(cacheConf.getLine(), cacheConf.getWay(), cacheConf.getSize()); //Total number of set index counting from 0
            int numberOfBitForBlockOffset = Utils.getNumberOfBitForBlockOffset(cacheConf.getLine()); //Number of bit for block offset
            int numberOfBitsForSetIndex = Utils.getNumberOfBitsForSetIndex(numberOfSetIndex); //Number of bits for set index
            int numberOfBitsForTag = MACHINE_BIT - (numberOfBitForBlockOffset + numberOfBitsForSetIndex); //Considering 32 bit by default

            //Extract bits from binaryValue of the input
            String bitsForTag = binaryValue.substring(0, numberOfBitsForTag);
            String bitsForSetIndex = binaryValue.substring(numberOfBitsForTag, numberOfBitsForTag + numberOfBitsForSetIndex);
            String bitsForBlockOffset = binaryValue.substring(numberOfBitsForTag + numberOfBitsForSetIndex,
                    numberOfBitsForTag + numberOfBitsForSetIndex + numberOfBitForBlockOffset);


            //TODO: Debug Log in here. Remove in final version
//            System.out.println(binaryValue);
//            System.out.println("For Level " + cacheConf.getLevel() + " Number of bit for block offset : "
//                    + numberOfBitForBlockOffset + " ,Number of bit for Set Index : " + numberOfBitsForSetIndex + " ,Tag Bit: " + numberOfBitsForTag);
//
//            System.out.println("For Level " + cacheConf.getLevel() + " Bit for block offset : "
//                    + bitsForBlockOffset + " ,bit for Set Index : " + bitsForSetIndex + " ,Tag Bits: " + bitsForTag);

            int readHit = processCacheLevelRead(cacheLevelX, bitsForTag, bitsForSetIndex, bitsForBlockOffset);

            if (readHit > 0) {
                isMemoryAccessRequired = false; // Cache Hit so no need to access memory.
                System.out.println("Cache Hit at level : " + readHit);
                break; //Hit in earlier level no need to proceed further
            }
        }

        if (isMemoryAccessRequired) {
            //TODO: perform memory access requests
            accessTime += mainMemory.getHitTime();
            System.out.println("Main Memory accessed");
        }

        System.out.println("Total Access Time for the Read operation[" + instructionValue + "] is : " + accessTime);
        System.out.println("----------------------------------------------------------------------------------------------------");
    }

    /**
     * Default Read Policy is to check in level x if miss return false and if hit return true
     *
     * @param cacheLevelX       Level of the cache
     * @param bitForTags        the bits for Tag
     * @param bitForSetIndex    the bit for set index
     * @param bitForBlockOffset the bits for block offset.
     * @return true if tag matches other wise false
     */
    private int processCacheLevelRead(List<List<CacheLine>> cacheLevelX, String bitForTags, String bitForSetIndex,
                                      String bitForBlockOffset) {

        int setIndexIntegerValue = Integer.parseInt(bitForSetIndex, 2);
        int tagIntegerValue = Integer.parseInt(bitForTags, 2);
        List<CacheLine> cacheLine = cacheLevelX.get(setIndexIntegerValue);
        int hitFlag = 0;
        int memoryReference = 1;

        for (CacheLine block : cacheLine) {
            System.out.println(block);

            if (block.getValidBit() != 0 && block.getTag() == tagIntegerValue) { //Cache hit not need to check further
                hitFlag = 1;
                break;
            }

            if (block.getValidBit() == 0 || block.getTag() != tagIntegerValue) {
                hitFlag = 0;
            }
            memoryReference++;
        }

        if (hitFlag == 0) {
            CacheLine lruBlock = Utils.getLRUBlock(cacheLine); //Get the least recently used block.

            lruBlock.setTag(tagIntegerValue);
            lruBlock.setValidBit(1);
            lruBlock.setTime(System.nanoTime()); //No need to explicitly add back the block as we are using reference.
            memoryReference = -1; // No hit found set cache index to -1 for further access

            System.out.println("Changed Blocks :" + cacheLine);
            System.out.println();
        }

        return memoryReference;

    }

    private void processWriteOperations(String instructionValue) {

    }
}
