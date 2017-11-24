import ris.arch.util.FileProcessorUtil;

/**
 * This class will run the whole project
 **/
public class Run {

    public static void main(String[] args) {
        FileProcessorUtil fileProcessorUtil = new FileProcessorUtil();
        fileProcessorUtil.processConfFile();
    }
}
