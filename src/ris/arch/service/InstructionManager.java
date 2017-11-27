package ris.arch.service;

import ris.arch.domain.CacheConf;
import ris.arch.domain.CacheLine;
import ris.arch.domain.MainMemory;
import ris.arch.util.MemoryRefReq;
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

    /**
     * Initialization function
     *
     * @param fileName file name
     */
    private void initializeFile(String fileName) {

        try {
            fileReader = new FileReader(fileName);
            bufferedReader = new BufferedReader(fileReader);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * This function process the input sequence from the file.
     *
     * @param inputSequenceFile this is the input sequence file.
     */
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

    /**
     * This function process read operation and manage the hit or miss in cache line
     *
     * @param instructionValue this is the input from the instruction set
     */
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

            int memoryRefReq = processCacheLevelRead(cacheLevelX, bitsForTag, bitsForSetIndex, bitsForBlockOffset);

            if (memoryRefReq == MemoryRefReq.NO_REQ) {
                isMemoryAccessRequired = false; // Cache Hit so no need to access memory.
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
     * This function manages hit or miss in the cache level.
     *
     * @param cacheLevelX       Level of the cache
     * @param bitForTags        the bits for Tag
     * @param bitForSetIndex    the bit for set index
     * @param bitForBlockOffset the bits for block offset.
     * @return the memory reference to the cache level. If miss return -1.
     */
    private int processCacheLevelRead(List<List<CacheLine>> cacheLevelX, String bitForTags, String bitForSetIndex,
                                      String bitForBlockOffset) {

        int setIndexIntegerValue = Integer.parseInt(bitForSetIndex, 2);
        int tagIntegerValue = Integer.parseInt(bitForTags, 2);
        List<CacheLine> cacheLine = cacheLevelX.get(setIndexIntegerValue);
        int hitFlag = 0;
        int memoryRefeReq = MemoryRefReq.NO_REQ;

        for (CacheLine block : cacheLine) {
            System.out.println(block);
            block.setTime(System.nanoTime()); //Accessing block so need to change the access time.
            if (block.getValidBit() != 0 && block.getTag() == tagIntegerValue) { //Cache hit not need to check further
                hitFlag = 1;
                memoryRefeReq = MemoryRefReq.NO_REQ; //Hit so no memory ref request to lower.
                break;
            }
            if (block.getValidBit() == 0 || block.getTag() != tagIntegerValue) {
                hitFlag = 0;
            }
        }

        if (hitFlag == 0) {
            CacheLine lruBlock = Utils.getLRUBlock(cacheLine); //Get the least recently used block.
            lruBlock.setTag(tagIntegerValue);
            lruBlock.setValidBit(1);
            lruBlock.setTime(System.nanoTime()); //No need to explicitly add back the block as we are using reference.
            memoryRefeReq = MemoryRefReq.READ_REQ;

            System.out.println("Changed Blocks :" + cacheLine);
            System.out.println();
        }

        return memoryRefeReq;
    }

    private void processWriteOperations(String instructionValue) {
        String binaryValue = Utils.get32BitBinFromInt(Integer.parseInt(instructionValue)); //The binary representation of the input
        System.out.println("Instruction is [st " + instructionValue + "]" + " binary value : " + binaryValue);
        boolean isMemoryAccessRequired = true;
        int accessTime = 0;
        int memoryRefReq = MemoryRefReq.WRITE_REQ;
        int cacheSize = cacheConfList.size();

        for (int i = 0; i < cacheConfList.size(); i++) {
            CacheConf cacheConf = cacheConfList.get(i);
            List<List<CacheLine>> cacheLevelX = cacheLevelMap.get(cacheConf.getLevel());
            isMemoryAccessRequired = true;

            int numberOfSetIndex = Utils.getNumberOfCacheLine(cacheConf.getLine(), cacheConf.getWay(), cacheConf.getSize()); //Total number of set index counting from 0
            int numberOfBitForBlockOffset = Utils.getNumberOfBitForBlockOffset(cacheConf.getLine()); //Number of bit for block offset
            int numberOfBitsForSetIndex = Utils.getNumberOfBitsForSetIndex(numberOfSetIndex); //Number of bits for set index
            int numberOfBitsForTag = MACHINE_BIT - (numberOfBitForBlockOffset + numberOfBitsForSetIndex); //Considering 32 bit by default

            //Extract bits from binaryValue of the input
            String bitsForTag = binaryValue.substring(0, numberOfBitsForTag);
            String bitsForSetIndex = binaryValue.substring(numberOfBitsForTag, numberOfBitsForTag + numberOfBitsForSetIndex);
            String bitsForBlockOffset = binaryValue.substring(numberOfBitsForTag + numberOfBitsForSetIndex,
                    numberOfBitsForTag + numberOfBitsForSetIndex + numberOfBitForBlockOffset);

            if (memoryRefReq == MemoryRefReq.NO_REQ) {
                isMemoryAccessRequired = false;
                break;
            }

            if (memoryRefReq == MemoryRefReq.READ_REQ) {
                memoryRefReq = processCacheLevelRead(cacheLevelX, bitsForTag, bitsForSetIndex, bitsForBlockOffset);

                if (memoryRefReq == MemoryRefReq.NO_REQ) {
                    isMemoryAccessRequired = false; // Cache Hit so no need to access memory.
                    break; //Hit in earlier level no need to proceed further
                }
            }

            if (memoryRefReq == MemoryRefReq.WRITE_REQ) {
                memoryRefReq = processWriteLevelCache(cacheLevelX, bitsForTag, bitsForSetIndex, bitsForBlockOffset,
                        cacheConf.getWritePolicy(), cacheConf.getAllocationPolicy());
            }

            accessTime += cacheConf.getHitTime();
        }

        if (isMemoryAccessRequired) {
            //TODO: perform memory access requests
            accessTime += mainMemory.getHitTime();
            System.out.println("Main Memory accessed");
        }

        System.out.println("Total Access Time for the Write operation[" + instructionValue + "] is : " + accessTime);
        System.out.println("----------------------------------------------------------------------------------------------------");
    }

    private int processWriteLevelCache(List<List<CacheLine>> cacheLevelX, String bitForTags, String bitForSetIndex,
                                       String bitForBlockOffset, String writePolicy, String allocatePolicy) {

        //TODO: perform write operations
        int setIndexIntegerValue = Integer.parseInt(bitForSetIndex, 2);
        int tagIntegerValue = Integer.parseInt(bitForTags, 2);
        List<CacheLine> cacheLine = cacheLevelX.get(setIndexIntegerValue);
        int hitFlag = 0;
        int memoryReq = MemoryRefReq.NO_REQ;

        for (CacheLine block : cacheLine) {
            System.out.println(block);

            block.setTime(System.nanoTime());
            if (block.getValidBit() != 0 && block.getTag() == tagIntegerValue) {
                hitFlag = 1;
                if (writePolicy.equalsIgnoreCase("WriteThrough")) {
                    memoryReq = MemoryRefReq.WRITE_REQ;
                }
                if (writePolicy.equalsIgnoreCase("WriteBack")) {
                    if (block.getDirtyBit() == 1) {
                        memoryReq = MemoryRefReq.WRITE_REQ;
                        block.setDirtyBit(0);
                        System.out.println("Write Hit: " + block);
                    } else {
                        memoryReq = MemoryRefReq.NO_REQ;
                        block.setDirtyBit(1);
                        System.out.println("Write Hit: " + block);
                    }
                }

                break;
            }

            if (block.getValidBit() == 0 || block.getTag() != tagIntegerValue) {
                hitFlag = 0;
            }
        }
        //Write Miss, So need to use allocate policies here.
        if (hitFlag == 0) {

            if (allocatePolicy.equalsIgnoreCase("WriteAllocate")) {
                memoryReq = MemoryRefReq.READ_REQ; // As policy is Write Allocate, so bring the block to the cache from lower level. Send a READ REQ.
                CacheLine lruBlock = Utils.getLRUBlock(cacheLine); //Get the least recently used block.
                lruBlock.setTag(tagIntegerValue);
                lruBlock.setValidBit(1);
                lruBlock.setDirtyBit(1);
                lruBlock.setTime(System.nanoTime());
            }

            if (allocatePolicy.equalsIgnoreCase("NoWriteAllocate")) {
                memoryReq = MemoryRefReq.WRITE_REQ;
            }

            System.out.println("Changed Blocks :" + cacheLine);
            System.out.println();
        }

        return memoryReq;
    }
}
