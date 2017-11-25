package ris.arch.service;

import ris.arch.domain.CacheConf;
import ris.arch.domain.CacheLine;
import ris.arch.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class CacheManager {

    private List<CacheLine> cacheLineList;

    public List<CacheLine> getCacheLines(CacheConf cacheConf) {
        int numberOfCacheLine = Utils.getNumberOfCacheLine(cacheConf.getLine(), cacheConf.getWay(), cacheConf.getSize());
        System.out.println("Number of " + cacheConf.getLevel() + " cache Line : " + numberOfCacheLine);

        cacheLineList = new ArrayList<>();
        for (int i = 0; i < numberOfCacheLine; i++) {
            intiCacheLine(cacheConf.getLevel());
        }

        return cacheLineList;
    }

    private void intiCacheLine(String level) {
        CacheLine cacheLine = new CacheLine();
        cacheLine.setLevel(level);
        cacheLine.setDirtyBit(0);
        cacheLine.setValidBit(0);
        cacheLine.setTag(-1);
        cacheLine.setTime(0);

        cacheLineList.add(cacheLine);
    }


}
