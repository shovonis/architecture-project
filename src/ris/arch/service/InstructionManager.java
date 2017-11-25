package ris.arch.service;

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
    private BufferedReader bufferedReader = null;
    private FileReader fileReader = null;
    Map<String, List<CacheLine>> cacheLevelMap = null;
    MainMemory mainMemory = null;

    public InstructionManager(Map<String, List<CacheLine>> cacheLevelMap, MainMemory mainMemory) {
        this.cacheLevelMap = cacheLevelMap;
        this.mainMemory = mainMemory;
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

    }

    private void processWriteOperations(String instructionValue) {

    }
}
