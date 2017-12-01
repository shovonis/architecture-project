import ris.arch.domain.CacheConf;
import ris.arch.domain.CacheLine;
import ris.arch.domain.MainMemory;
import ris.arch.domain.ResultSummary;
import ris.arch.service.CacheManager;
import ris.arch.service.ConfFileProcessor;
import ris.arch.service.InstructionManager;
import ris.arch.util.FileName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is Run Class that will run the whole project.
 *
 **/
public class Run {

    public static void main(String[] args) {

        //Config Processor for processing the config file and setup the cache and memory
        ConfFileProcessor confFileProcessor = new ConfFileProcessor();
        CacheManager cacheManager = new CacheManager();
        Map<String, List<List<CacheLine>>> cacheLevelMap = new HashMap<>();

        confFileProcessor.processConfFile(FileName.CONF_FILE);
        List<CacheConf> cacheConfList = confFileProcessor.getListOfCacheConf();
        MainMemory mainMemory = confFileProcessor.getMainMainMemory(); //Main memory doesn't need any special initialization so we can uses it as it is.
        //Setting up each cache level
        for (CacheConf cacheConf : cacheConfList) {
            cacheLevelMap.put(cacheConf.getLevel(), cacheManager.getCacheLines(cacheConf));
        }

        //Instruction manager to Read from and process the input file.
        InstructionManager instructionManager = new InstructionManager(cacheLevelMap, mainMemory, cacheConfList);
        Map<String, ResultSummary> resultSummaryMap = instructionManager.processInstructionFromFile(FileName.ACCESS_FILE);

        //Printing out the Results
        final int[] totalTime = {0};
        resultSummaryMap.forEach((k, v) -> {
            totalTime[0] += v.getTotalTime();
            System.out.println(v);
        });

        System.out.println("\n Total Time: " + totalTime[0]);
    }
}
