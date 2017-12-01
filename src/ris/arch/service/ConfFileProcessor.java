package ris.arch.service;

import ris.arch.domain.CacheConf;
import ris.arch.domain.MainMemory;
import ris.arch.util.Log;
import ris.arch.util.Utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * This Processor Process the Conf file into the entity class.
 * Map all the properties to the corresponding config props in the class.
 */
public class ConfFileProcessor {
    private BufferedReader bufferedReader = null;
    private List<CacheConf> listOfCacheConf = null;
    private MainMemory mainMainMemory = null;

    /**
     * Private method to access the file
     *
     * @param fileName name of the file
     */
    private void initializeFile(String fileName) {
        try {
            FileReader fileReader = new FileReader(fileName);
            bufferedReader = new BufferedReader(fileReader);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Process the file from the parameter.
     * Converts the conf file to the CacheConf class.
     *
     * @param fileName name of the file
     */
    public void processConfFile(String fileName) {
        String line;
        String inputConf;
        listOfCacheConf = new LinkedList<>();
        try {
            initializeFile(fileName);
            while ((line = bufferedReader.readLine()) != null) {
                inputConf = line.split(":")[1];

                //For Cache
                if (inputConf.startsWith("L")) {
                    CacheConf cacheConf = new CacheConf();
                    cacheConf.setLevel(line.split(":")[1]);
                    cacheConf.setLine(Integer.parseInt(bufferedReader.readLine().split(":")[1]));
                    cacheConf.setTmpWay(bufferedReader.readLine().split(":")[1]);
                    cacheConf.setSize(bufferedReader.readLine().split(":")[1]);
                    cacheConf.setHitTime(Long.parseLong(bufferedReader.readLine().split(":")[1]));
                    cacheConf.setWritePolicy(bufferedReader.readLine().split(":")[1]);
                    cacheConf.setAllocationPolicy(bufferedReader.readLine().split(":")[1]);
                    cacheConf.setWay(Utils.getWay(cacheConf.getTmpWay(), cacheConf.getLine(), cacheConf.getSize()));

                    listOfCacheConf.add(cacheConf);

                    if (Log.DEBUG_MODE) {
                        System.out.println(cacheConf);
                    }
                }

                //For Main memory
                if (inputConf.startsWith("M")) {
                    mainMainMemory = new MainMemory();
                    mainMainMemory.setLevel(line.split(":")[1]);
                    mainMainMemory.setHitTime(Long.parseLong(bufferedReader.readLine().split(":")[1]));

                    if (Log.DEBUG_MODE) {
                        System.out.println(mainMainMemory);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public List<CacheConf> getListOfCacheConf() {
        return listOfCacheConf;
    }

    public MainMemory getMainMainMemory() {
        return mainMainMemory;
    }

}
