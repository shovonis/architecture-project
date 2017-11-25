import ris.arch.domain.CacheConf;
import ris.arch.domain.CacheLine;
import ris.arch.domain.MainMemory;
import ris.arch.service.CacheManager;
import ris.arch.service.ConfFileProcessor;
import ris.arch.service.InstructionManager;
import ris.arch.util.FileName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class will run the whole project
 **/
public class Run {

    public static void main(String[] args) {

        ConfFileProcessor confFileProcessor = new ConfFileProcessor();
        CacheManager cacheManager = new CacheManager();
        Map<String, List<CacheLine>> cacheLevelMap = new HashMap<>();

        confFileProcessor.processConfFile(FileName.CONF_FILE);
        List<CacheConf> cacheConfList = confFileProcessor.getListOfCacheConf();
        MainMemory mainMemory = confFileProcessor.getMainMainMemory(); //Main memory doesn't need any special initialization so we can uses it as it is.

        for (CacheConf cacheConf : cacheConfList) {
            cacheLevelMap.put(cacheConf.getLevel(), cacheManager.getCacheLines(cacheConf));
        }

        InstructionManager instructionManager = new InstructionManager(cacheLevelMap, mainMemory);
        instructionManager.processInstructionFromFile(FileName.ACCESS_FILE);


//        cacheLevel.forEach((k, v) -> {
//            if (k.equalsIgnoreCase("L2")) {
//                System.out.println("Level : " + k + " Cache : " + v);
//            }
//        });

//        List<CacheLine> cacheLines = cacheLevelMap.get("L2");
//        System.out.println(cacheLines.get(1000));
    }
}
