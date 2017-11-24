package ris.arch.util;

import ris.arch.domain.CacheConf;
import ris.arch.domain.MemoryConf;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class FileProcessorUtil {

    private BufferedReader bufferedReader = null;
    private FileReader fileReader = null;
    private List<CacheConf> listOfCacheConf = null;
    private MemoryConf mainMemoryConf = null;

    private void initializeFile() {

        try {
            fileReader = new FileReader(FileName.CONF_FILE);
            bufferedReader = new BufferedReader(fileReader);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void processConfFile() {
        String line;
        String inputConf;
        listOfCacheConf = new LinkedList<>();
        try {
            initializeFile();

            while ((line = bufferedReader.readLine()) != null) {
                inputConf = line.split(":")[1];
                if (inputConf.startsWith("L")) {
                    CacheConf cacheConf = new CacheConf();
                    cacheConf.setLevel(line.split(":")[1]);
                    cacheConf.setLine(bufferedReader.readLine().split(":")[1]);
                    cacheConf.setWay(Integer.parseInt(bufferedReader.readLine().split(":")[1]));
                    cacheConf.setSize(bufferedReader.readLine().split(":")[1]);
                    cacheConf.setHitTime(Long.parseLong(bufferedReader.readLine().split(":")[1]));
                    cacheConf.setWritePolicy(bufferedReader.readLine().split(":")[1]);
                    cacheConf.setAllocationPolicy(bufferedReader.readLine().split(":")[1]);

                    listOfCacheConf.add(cacheConf);
                    System.out.println(cacheConf);
                }

                if (inputConf.startsWith("M")) {
                    mainMemoryConf = new MemoryConf();
                    mainMemoryConf.setLevel(line.split(":")[1]);
                    mainMemoryConf.setHitTime(Long.parseLong(bufferedReader.readLine().split(":")[1]));

                    System.out.println(mainMemoryConf);
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public List<CacheConf> getListOfCacheConf() {
        return listOfCacheConf;
    }

    public void setListOfCacheConf(List<CacheConf> listOfCacheConf) {
        this.listOfCacheConf = listOfCacheConf;
    }

    public MemoryConf getMainMemoryConf() {
        return mainMemoryConf;
    }

    public void setMainMemoryConf(MemoryConf mainMemoryConf) {
        this.mainMemoryConf = mainMemoryConf;
    }

    //    private CacheConf fileToCacheConverter() throws IOException {
//        Stream<String> lines = Files.lines(Paths.get(FileName.CONF_FILE)).skip(7).limit(7);
//        lines.forEachOrdered(System.out::println);
////
//        return null;
//
//    }
//
//    private MemoryConf fileToMemoryConverter(String line) {
//
//        return null;
//
//    }

}
