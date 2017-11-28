package ris.arch.service;

import ris.arch.domain.CacheConf;
import ris.arch.domain.CacheLine;
import ris.arch.domain.MainMemory;
import ris.arch.domain.ResultSummary;
import ris.arch.util.MemoryRefReq;
import ris.arch.util.Utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
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
    public Map<String, ResultSummary> processInstructionFromFile(String inputSequenceFile) {
        String line;
        Map<String, ResultSummary> resultSummaryHashMap = new HashMap<>();

        try {
            initializeFile(inputSequenceFile);

            while ((line = bufferedReader.readLine()) != null) {

                String instructionType = Utils.getInstructionTypeFromInstruction(line);
                String instructionValue = Utils.getValueFromInstruction(line);

                if (instructionType.equalsIgnoreCase("ld")) {
                    processReadOperations(instructionValue, resultSummaryHashMap);
                }
                if (instructionType.equalsIgnoreCase("st")) {
                    processWriteOperations(instructionValue, resultSummaryHashMap);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return resultSummaryHashMap;
    }

    /**
     * This function process read operation and manage the hit or miss in cache line
     *
     * @param instructionValue this is the input from the instruction set
     */
    private void processReadOperations(String instructionValue, Map<String, ResultSummary> resultSummaries) {
        String binaryValue = Utils.get32BitBinFromInt(Integer.parseInt(instructionValue)); //The binary representation of the input
        System.out.println("Instruction is [ld " + instructionValue + "]" + " binary value : " + binaryValue);
        boolean isMemoryAccessRequired = true;
        int accessTime = 0;
        int memoryRefReq = MemoryRefReq.READ_REQ;
        ResultSummary resultSummary;

        for (CacheConf cacheConf : cacheConfList) {
            List<List<CacheLine>> cacheLevelX = cacheLevelMap.get(cacheConf.getLevel());
            isMemoryAccessRequired = true;
            resultSummary = resultSummaries.get(cacheConf.getLevel());
            if (resultSummary == null) {
                resultSummary = new ResultSummary();
                resultSummary.setLevel(cacheConf.getLevel());
            }

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

            if (memoryRefReq == MemoryRefReq.READ_REQ + MemoryRefReq.WRITE_REQ) {
                //TODO: Implement this one.
                memoryRefReq = processWriteLevelCache(cacheLevelX, bitsForTag, bitsForSetIndex, bitsForBlockOffset,
                        cacheConf.getWritePolicy(), cacheConf.getAllocationPolicy(), resultSummary);

                memoryRefReq = memoryRefReq - MemoryRefReq.WRITE_REQ; //Write to the lower level done.
            }
            if (memoryRefReq == MemoryRefReq.READ_REQ) {
                memoryRefReq = processCacheLevelRead(cacheLevelX, bitsForTag, bitsForSetIndex, bitsForBlockOffset, resultSummary);
            }
            if (memoryRefReq == MemoryRefReq.NO_REQ) {
                isMemoryAccessRequired = false; // Cache Hit so no need to access memory.
                break; //Hit in earlier level no need to proceed further
            }

            accessTime += cacheConf.getHitTime();
            resultSummary.setTotalTime((int) (resultSummary.getTotalTime() + cacheConf.getHitTime()));
            resultSummaries.put(cacheConf.getLevel(), resultSummary);
        }

        if (isMemoryAccessRequired) {
            resultSummary = resultSummaries.get("Memory");
            if(resultSummary == null){
                resultSummary = new ResultSummary();
                resultSummary.setLevel(mainMemory.getLevel());
            }
            accessTime += mainMemory.getHitTime();
            resultSummary.setHit(resultSummary.getHit() + 1);
            resultSummary.setAccess(resultSummary.getAccess() + 1);
            resultSummary.setTotalTime((int) (resultSummary.getTotalTime() +  mainMemory.getHitTime()));
            resultSummaries.put("Memory", resultSummary);
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
                                      String bitForBlockOffset, ResultSummary resultSummary) {

        int setIndexIntegerValue = Integer.parseInt(bitForSetIndex, 2);
        int tagIntegerValue = Integer.parseInt(bitForTags, 2);
        List<CacheLine> cacheLine = cacheLevelX.get(setIndexIntegerValue);
        int hitFlag = 0;
        int memoryRefeReq = MemoryRefReq.NO_REQ;
        resultSummary.setAccess(resultSummary.getAccess() + 1);

        for (CacheLine block : cacheLine) {
            System.out.println(block);
            block.setTime(System.nanoTime()); //Accessing block so need to change the access time.
            if (block.getValidBit() != 0 && block.getTag() == tagIntegerValue) { //Cache hit not need to check further
                hitFlag = 1;
                resultSummary.setHit(resultSummary.getHit() + 1);
                memoryRefeReq = MemoryRefReq.NO_REQ; //Hit so no memory ref request to lower.
                break;
            }
            if (block.getValidBit() == 0 || block.getTag() != tagIntegerValue) {
                hitFlag = 0;
            }
        }

        if (hitFlag == 0) {
            resultSummary.setMiss(resultSummary.getMiss() + 1);
            CacheLine lruBlock = Utils.getLRUBlock(cacheLine); //Get the least recently used block.
            lruBlock.setTag(tagIntegerValue);
            lruBlock.setValidBit(1);
            lruBlock.setTime(System.nanoTime()); //No need to explicitly add back the block as we are using reference.

            if (lruBlock.getDirtyBit() == 1) {
                memoryRefeReq = MemoryRefReq.READ_REQ + MemoryRefReq.WRITE_REQ;
            } else {
                memoryRefeReq = MemoryRefReq.READ_REQ;
            }

            System.out.println("Changed Blocks :" + cacheLine);
            System.out.println();
        }

        return memoryRefeReq;
    }

    private void processWriteOperations(String instructionValue, Map<String, ResultSummary> resultSummaries) {
        String binaryValue = Utils.get32BitBinFromInt(Integer.parseInt(instructionValue)); //The binary representation of the input
        System.out.println("Instruction is [st " + instructionValue + "]" + " binary value : " + binaryValue);
        boolean isMemoryAccessRequired = true;
        int accessTime = 0;
        int memoryRefReq = MemoryRefReq.WRITE_REQ;
        ResultSummary resultSummary;

        for (CacheConf cacheConf : cacheConfList) {
            List<List<CacheLine>> cacheLevelX = cacheLevelMap.get(cacheConf.getLevel());
            isMemoryAccessRequired = true;

            resultSummary = resultSummaries.get(cacheConf.getLevel());
            if (resultSummary == null) {
                resultSummary = new ResultSummary();
                resultSummary.setLevel(cacheConf.getLevel());
            }

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
                memoryRefReq = processCacheLevelRead(cacheLevelX, bitsForTag, bitsForSetIndex, bitsForBlockOffset, resultSummary);

                if (memoryRefReq == MemoryRefReq.NO_REQ) {
                    isMemoryAccessRequired = false; // Cache Hit so no need to access memory.
                    break; //Hit in earlier level no need to proceed further
                }
            }

            if (memoryRefReq == MemoryRefReq.WRITE_REQ) {
                memoryRefReq = processWriteLevelCache(cacheLevelX, bitsForTag, bitsForSetIndex, bitsForBlockOffset,
                        cacheConf.getWritePolicy(), cacheConf.getAllocationPolicy(), resultSummary);
            }

            accessTime += cacheConf.getHitTime();
            resultSummary.setTotalTime((int) (resultSummary.getTotalTime() + cacheConf.getHitTime()));
            resultSummaries.put(cacheConf.getLevel(), resultSummary);
        }

        if (isMemoryAccessRequired) {
            resultSummary = resultSummaries.get("Memory");
            if(resultSummary == null){
                resultSummary = new ResultSummary();
                resultSummary.setLevel(mainMemory.getLevel());
            }
            accessTime += mainMemory.getHitTime();
            resultSummary.setHit(resultSummary.getHit() + 1);
            resultSummary.setAccess(resultSummary.getAccess() + 1);
            resultSummary.setTotalTime((int) (resultSummary.getTotalTime() + mainMemory.getHitTime()));
            resultSummaries.put("Memory", resultSummary);
            System.out.println("Main Memory accessed");
        }

        System.out.println("Total Access Time for the Write operation[" + instructionValue + "] is : " + accessTime);
        System.out.println("----------------------------------------------------------------------------------------------------");
    }

    private int processWriteLevelCache(List<List<CacheLine>> cacheLevelX, String bitForTags, String bitForSetIndex,
                                       String bitForBlockOffset, String writePolicy, String allocatePolicy, ResultSummary resultSummary) {

        int setIndexIntegerValue = Integer.parseInt(bitForSetIndex, 2);
        int tagIntegerValue = Integer.parseInt(bitForTags, 2);
        List<CacheLine> cacheLine = cacheLevelX.get(setIndexIntegerValue);
        int hitFlag = 0;
        int memoryReq = MemoryRefReq.NO_REQ;

        resultSummary.setAccess(resultSummary.getAccess() + 1);
        for (CacheLine block : cacheLine) {
            System.out.println(block);

            block.setTime(System.nanoTime());
            if (block.getValidBit() != 0 && block.getTag() == tagIntegerValue) {
                hitFlag = 1;
                resultSummary.setHit(resultSummary.getHit() + 1);
                if (writePolicy.equalsIgnoreCase("WriteThrough")) {
                    memoryReq = MemoryRefReq.WRITE_REQ;
                    System.out.println("Write Hit. Policy is: " + writePolicy + "Block is: " + block + " WRITE REQ Sent to lower level");
                }
                if (writePolicy.equalsIgnoreCase("WriteBack")) {
                    if (block.getDirtyBit() == 1) {
                        memoryReq = MemoryRefReq.WRITE_REQ;
                        block.setDirtyBit(0);
                        block.setTime(System.nanoTime());
                        System.out.println("Write Hit. Policy is: " + writePolicy + "Block is: " + block + " WRITE REQ Sent to lower level");

                    } else {
                        memoryReq = MemoryRefReq.NO_REQ;
                        block.setDirtyBit(1);
                        block.setTime(System.nanoTime());
                        System.out.println("Write Hit. Policy is: " + writePolicy + "Block is: " + block + " NO REQ Sent to lower level.");
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
            resultSummary.setMiss(resultSummary.getMiss() + 1);
            if (allocatePolicy.equalsIgnoreCase("WriteAllocate")) {
                memoryReq = MemoryRefReq.READ_REQ; // As policy is Write Allocate, so bring the block to the cache from lower level. Send a READ REQ.
                CacheLine lruBlock = Utils.getLRUBlock(cacheLine); //Get the least recently used block.
                lruBlock.setTag(tagIntegerValue);
                lruBlock.setValidBit(1);
                lruBlock.setDirtyBit(1);
                lruBlock.setTime(System.nanoTime());

                System.out.println("Write Miss. Policy is: " + allocatePolicy + " READ REQ send to lower level");
                System.out.println("Changed Blocks :" + cacheLine);
            }
            if (allocatePolicy.equalsIgnoreCase("NoWriteAllocate")) {
                memoryReq = MemoryRefReq.WRITE_REQ;
                System.out.println("Write Miss. Policy is: " + allocatePolicy + " WRITE REQ sent to lower level");
            }

            System.out.println();
        }

        return memoryReq;
    }
}
