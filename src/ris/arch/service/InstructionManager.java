package ris.arch.service;

import ris.arch.domain.*;
import ris.arch.util.Log;
import ris.arch.util.MemoryRefReq;
import ris.arch.util.Utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * This class Process all the input from the access file.
 * This class Also implements all the Cache Read Write policies and records the miss/hit
 */
public class InstructionManager {
    public static final int MACHINE_BIT = 32;
    private BufferedReader bufferedReader = null;
    private FileReader fileReader = null;
    private Map<String, List<List<CacheLine>>> cacheLevelMap = null;
    private List<CacheConf> cacheConfList = null;
    private MainMemory mainMemory = null;

    /**
     * Constructor of the Instruction Manager
     *
     * @param cacheLevelMap Cache
     * @param mainMemory    Main Memory
     * @param cacheConfList Cache Config List
     */
    public InstructionManager(Map<String, List<List<CacheLine>>> cacheLevelMap, MainMemory mainMemory, List<CacheConf> cacheConfList) {
        this.cacheLevelMap = cacheLevelMap;
        this.mainMemory = mainMemory;
        this.cacheConfList = cacheConfList;
    }

    /**
     * Read From the Access file
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
     * @return The result summary
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
                    processReq(instructionValue, resultSummaryHashMap, MemoryRefReq.READ_REQ);
                }
                if (instructionType.equalsIgnoreCase("st")) {
                    processReq(instructionValue, resultSummaryHashMap, MemoryRefReq.WRITE_REQ);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return resultSummaryHashMap;
    }


    /**
     * Process the instruction request from the file.
     *
     * @param instructionValue the value of the instruction
     * @param resultSummaries  Result summary to store result
     * @param type             type of the instruction
     */
    private void processReq(String instructionValue, Map<String, ResultSummary> resultSummaries, int type) {
        String binaryValue = Utils.get32BitBinFromInt(Integer.parseInt(instructionValue)); //The binary representation of the input
        if (Log.DEBUG_MODE) {
            System.out.println("Instruction is [ld " + instructionValue + "]" + " binary value : " + binaryValue);
        }

        int accessTime = 0;
        int nextAccess = 0;
        ResultSummary resultSummary;
        ReferenceRequest referenceRequest = new ReferenceRequest(type, nextAccess);
        Queue<ReferenceRequest> memoryReferenceQueue = new LinkedList<>();
        memoryReferenceQueue.add(referenceRequest);

        //Loop continues until no other instruction to available
        while (!memoryReferenceQueue.isEmpty()) {
            ReferenceRequest referenceReq = memoryReferenceQueue.remove();

            if (referenceReq.getNextAccess() > cacheConfList.size() - 1) {
                if (referenceReq.getMemoryRefReq() != MemoryRefReq.NO_REQ) {
                    resultSummary = resultSummaries.get("Main");
                    if (resultSummary == null) {
                        resultSummary = new ResultSummary();
                        resultSummary.setLevel(mainMemory.getLevel());
                    }
                    accessTime += mainMemory.getHitTime();
                    resultSummary.setHit(resultSummary.getHit() + 1);
                    resultSummary.setAccess(resultSummary.getAccess() + 1);
                    resultSummary.setTotalTime((int) (resultSummary.getTotalTime() + mainMemory.getHitTime()));
                    resultSummaries.put("Main", resultSummary);
                    if (Log.DEBUG_MODE) {
                        System.out.println("Main Memory accessed");
                    }
                }
                break; //Must break as next access is memory. No cache available
            }

            //Get the next Cache
            CacheConf cacheConf = cacheConfList.get(referenceReq.getNextAccess());
            List<List<CacheLine>> cacheLevelX = cacheLevelMap.get(cacheConf.getLevel());
            resultSummary = resultSummaries.get(cacheConf.getLevel());

            //Init Result summary
            if (resultSummary == null) {
                resultSummary = new ResultSummary();
                resultSummary.setLevel(cacheConf.getLevel());
            }

            //Get the bits and data from the instruction and config
            int numberOfSetIndex = Utils.getNumberOfCacheLine(cacheConf.getLine(), cacheConf.getWay(), cacheConf.getSize()); //Total number of set index counting from 0
            int numberOfBitForBlockOffset = Utils.getNumberOfBitForBlockOffset(cacheConf.getLine()); //Number of bit for block offset
            int numberOfBitsForSetIndex = Utils.getNumberOfBitsForSetIndex(numberOfSetIndex); //Number of bits for set index
            int numberOfBitsForTag = MACHINE_BIT - (numberOfBitForBlockOffset + numberOfBitsForSetIndex); //Considering 32 bit by default

            //Extract bits from binaryValue of the input
            String bitsForTag = binaryValue.substring(0, numberOfBitsForTag);
            String bitsForSetIndex = binaryValue.substring(numberOfBitsForTag, numberOfBitsForTag + numberOfBitsForSetIndex);
            String bitsForBlockOffset = binaryValue.substring(numberOfBitsForTag + numberOfBitsForSetIndex,
                    numberOfBitsForTag + numberOfBitsForSetIndex + numberOfBitForBlockOffset);
            if (Log.DEBUG_MODE) {
                System.out.println("Number of set index: " + numberOfSetIndex + "Number of bit for block offset: "
                        + numberOfBitForBlockOffset + "Number of Bit for Set index" + numberOfBitsForSetIndex + "Number of bit for Tag " + numberOfBitsForTag);
            }

            /// Implementation of the Cache Policies Here.

            //Hit in the previous cache so NO REQ and Break
            if (referenceReq.getMemoryRefReq() == MemoryRefReq.NO_REQ) {
                break;
            }

            //Read Req to the cache
            if (referenceReq.getMemoryRefReq() == MemoryRefReq.READ_REQ) {
                referenceReq = processCacheLevelRead(cacheLevelX, bitsForTag, bitsForSetIndex, bitsForBlockOffset, resultSummary, referenceReq.getNextAccess());

                memoryReferenceQueue.add(referenceReq);
                accessTime += cacheConf.getHitTime();
                resultSummary.setTotalTime((int) (resultSummary.getTotalTime() + cacheConf.getHitTime()));
                resultSummaries.put(cacheConf.getLevel(), resultSummary);
                continue;
            }

            //Write Request to the Cache
            if (referenceReq.getMemoryRefReq() == MemoryRefReq.WRITE_REQ) {
                referenceReq = processWriteLevelCache(cacheLevelX, bitsForTag, bitsForSetIndex, bitsForBlockOffset,
                        cacheConf.getWritePolicy(), cacheConf.getAllocationPolicy(), resultSummary, referenceReq.getNextAccess());

                memoryReferenceQueue.add(referenceReq);
                accessTime += cacheConf.getHitTime();
                resultSummary.setTotalTime((int) (resultSummary.getTotalTime() + cacheConf.getHitTime()));
                resultSummaries.put(cacheConf.getLevel(), resultSummary);
                continue;
            }

            //Read And Write req to the Cache. (Might be needed in case of WriteAllocate and WriteBack
            if (referenceReq.getMemoryRefReq() == MemoryRefReq.READ_REQ + MemoryRefReq.WRITE_REQ) {
                referenceReq = processWriteLevelCache(cacheLevelX, bitsForTag, bitsForSetIndex, bitsForBlockOffset,
                        cacheConf.getWritePolicy(), cacheConf.getAllocationPolicy(), resultSummary, referenceReq.getNextAccess());
                memoryReferenceQueue.add(referenceReq);

                referenceReq = processCacheLevelRead(cacheLevelX, bitsForTag, bitsForSetIndex, bitsForBlockOffset, resultSummary, referenceReq.getNextAccess());
                memoryReferenceQueue.add(referenceReq);
                accessTime += cacheConf.getHitTime();
                resultSummary.setTotalTime((int) (resultSummary.getTotalTime() + cacheConf.getHitTime()));
                resultSummaries.put(cacheConf.getLevel(), resultSummary);
            }
        }

        if (Log.DEBUG_MODE) {
            System.out.println("Total Access Time for the Read operation[" + instructionValue + "] is : " + accessTime);
            System.out.println("----------------------------------------------------------------------------------------------------");
        }
    }

    /**
     * This function manages the Read Policies for the Cache level.
     *
     * @param cacheLevelX       Level of the cache
     * @param bitForTags        the bits for Tag
     * @param bitForSetIndex    the bit for set index
     * @param bitForBlockOffset the bits for block offset.
     * @return the Reference request to the next level.
     */
    private ReferenceRequest processCacheLevelRead(List<List<CacheLine>> cacheLevelX, String bitForTags, String bitForSetIndex,
                                                   String bitForBlockOffset, ResultSummary resultSummary, int nextAccess) {
        int setIndexIntegerValue;
        if (bitForSetIndex.equalsIgnoreCase("")) {
            setIndexIntegerValue = 0;
        } else {
            setIndexIntegerValue = Integer.parseInt(bitForSetIndex, 2);
        }

        int tagIntegerValue = Integer.parseInt(bitForTags, 2);
        List<CacheLine> cacheLine = cacheLevelX.get(setIndexIntegerValue);
        int hitFlag = 0;
        int memoryRefeReq = MemoryRefReq.NO_REQ;
        resultSummary.setAccess(resultSummary.getAccess() + 1);

        for (CacheLine block : cacheLine) {
            if(Log.DEBUG_MODE){
                System.out.println(block);

            }
            if (block.getValidBit() == 1 && block.getTag() == tagIntegerValue) { //Cache hit not need to check further
                hitFlag = 1;
                resultSummary.setHit(resultSummary.getHit() + 1);
                block.setTime(System.nanoTime());
                memoryRefeReq = MemoryRefReq.NO_REQ; //Hit so no memory ref request to lower.
                break;
            }
            if (block.getValidBit() == 0 || block.getTag() != tagIntegerValue) {
                hitFlag = 0;
            }
        }

        if (hitFlag == 0) { //Read Miss
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
            nextAccess++;

            if (Log.DEBUG_MODE) {
                System.out.println("Changed Blocks :" + cacheLine);
                System.out.println();
            }
        }

        return new ReferenceRequest(memoryRefeReq, nextAccess);
    }

    /**
     * This function implements the write policies
     * @param cacheLevelX cache
     * @param bitForTags Bits for tag
     * @param bitForSetIndex bits for set index
     * @param bitForBlockOffset bits for block offset
     * @param writePolicy write policy
     * @param allocatePolicy allocate policy
     * @param resultSummary the result summary
     * @param nextAccess next level access
     * @return the Reference Request for the next level
     */
    private ReferenceRequest processWriteLevelCache(List<List<CacheLine>> cacheLevelX, String bitForTags, String bitForSetIndex,
                                                    String bitForBlockOffset, String writePolicy, String allocatePolicy, ResultSummary resultSummary, int nextAccess) {

        int setIndexIntegerValue;
        if (bitForSetIndex.equalsIgnoreCase("")) {
            setIndexIntegerValue = 0;
        } else {
            setIndexIntegerValue = Integer.parseInt(bitForSetIndex, 2);
        }

        int tagIntegerValue = Integer.parseInt(bitForTags, 2);
        List<CacheLine> cacheLine = cacheLevelX.get(setIndexIntegerValue);
        int hitFlag = 0;
        int memoryReq = MemoryRefReq.NO_REQ;

        resultSummary.setAccess(resultSummary.getAccess() + 1);
        for (CacheLine block : cacheLine) {
            if(Log.DEBUG_MODE){
                System.out.println(block);
            }

            if (block.getValidBit() != 0 && block.getTag() == tagIntegerValue) {
                hitFlag = 1;
                block.setTime(System.nanoTime());
                resultSummary.setHit(resultSummary.getHit() + 1);
                if (writePolicy.equalsIgnoreCase("WriteThrough")) {
                    memoryReq = MemoryRefReq.WRITE_REQ;
                    nextAccess++;
                    if (Log.DEBUG_MODE) {
                        System.out.println("Write Hit. Policy is: " + writePolicy + "Block is: " + block + " WRITE REQ Sent to lower level");
                    }
                }
                if (writePolicy.equalsIgnoreCase("WriteBack")) {
                    if (block.getDirtyBit() == 1) {
                        memoryReq = MemoryRefReq.NO_REQ;
                        block.setDirtyBit(0);
                        nextAccess++;

                        if (Log.DEBUG_MODE) {
                            System.out.println("Write Hit. Policy is: " + writePolicy + "Block is: " + block + " WRITE REQ Sent to lower level");
                        }

                    } else {
                        memoryReq = MemoryRefReq.NO_REQ;
                        block.setDirtyBit(1);
                        if (Log.DEBUG_MODE) {
                            System.out.println("Write Hit. Policy is: " + writePolicy + "Block is: " + block + " NO REQ Sent to lower level.");
                        }
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
                if(lruBlock.getDirtyBit() == 1){
                    memoryReq = MemoryRefReq.WRITE_REQ;
                }
                lruBlock.setTag(tagIntegerValue);
                lruBlock.setValidBit(1);
                lruBlock.setDirtyBit(1);
                lruBlock.setTime(System.nanoTime());
                if (Log.DEBUG_MODE) {
                    System.out.println("Write Miss. Policy is: " + allocatePolicy + " READ REQ send to lower level");
                    System.out.println("Changed Blocks :" + cacheLine);
                }
            }
            if (allocatePolicy.equalsIgnoreCase("NoWriteAllocate")) {
                memoryReq = MemoryRefReq.WRITE_REQ;
                if(Log.DEBUG_MODE){
                    System.out.println("Write Miss. Policy is: " + allocatePolicy + " WRITE REQ sent to lower level");
                }
            }
            nextAccess++;
            if(Log.DEBUG_MODE){
                System.out.println();
            }
        }

        return new ReferenceRequest(memoryReq, nextAccess);
    }
}
