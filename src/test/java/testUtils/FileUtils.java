package testUtils;

import main.exceptions.RPException;
import org.apache.poi.openxml4j.opc.internal.FileHelper;

import java.io.File;

public class FileUtils {

    public static String getResourceFileAsString(String fileName) {
        ClassLoader classLoader = FileHelper.class.getClassLoader();

        File file = new File(classLoader.getResource(fileName).getFile());
        try {
            String fp = file.getPath();
            fp = fp.replaceAll("%20", " ");
            main.utils.FileUtils fu = new main.utils.FileUtils();
            return fu.readFile(fp);
        } catch (RPException e) {
            e.printStackTrace();
        }
        return null;
    }
}
