package ris.arch.service;

import ris.arch.domain.CacheConf;
import ris.arch.domain.CacheLine;
import ris.arch.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class CacheManager {

    private List<CacheLine> cacheLine;
    private List<List<CacheLine>> cacheList;

    public List<List<CacheLine>> getCacheLines(CacheConf cacheConf) {
        int numberOfCacheLine = Utils.getNumberOfCacheLine(cacheConf.getLine(), cacheConf.getWay(), cacheConf.getSize());
        System.out.println("Number of " + cacheConf.getLevel() + " cache Line : " + numberOfCacheLine);
        System.out.println();
        cacheList = new ArrayList<>();
        for (int i = 0; i < numberOfCacheLine; i++) {
            cacheLine = new ArrayList<>();
            for (int j = 0; j < cacheConf.getWay(); j++) {
                intiCacheLine(cacheConf);
            }

            cacheList.add(cacheLine);
        }

        return cacheList;
    }

    /**
     * This Function initialize the cache line from the cache config file
     * The meta data information is set intentionally for ease of use.
     *
     * @param cacheConf the cache config information
     */
    private void intiCacheLine(CacheConf cacheConf) {
        CacheLine cacheBlock = new CacheLine();
        cacheBlock.setLevel(cacheConf.getLevel());
        cacheBlock.setDirtyBit(0);
        cacheBlock.setValidBit(0);
        cacheBlock.setTag(-1);
        cacheBlock.setTime(System.nanoTime());

        cacheBlock.setLine(cacheConf.getLine());
        cacheBlock.setWay(cacheConf.getWay());
        cacheBlock.setSize(cacheConf.getSize());
        cacheBlock.setHitTime(cacheConf.getHitTime());
        cacheBlock.setWritePolicy(cacheConf.getWritePolicy());
        cacheBlock.setAllocationPolicy(cacheConf.getAllocationPolicy());

        cacheLine.add(cacheBlock);
    }
}
