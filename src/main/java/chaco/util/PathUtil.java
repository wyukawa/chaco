package chaco.util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathUtil {

    public static Path getResultFilePath(String queryId) {
        String currentPath = new File(".").getAbsolutePath();
        String key1 = queryId.substring(0, 1);
        String key2 = queryId.substring(1, 2);
        File key1Dir = new File(String.format("%s/results/%s", currentPath, key1));
        if (!key1Dir.isDirectory()) {
            key1Dir.mkdir();
        }

        File resultDir = new File(String.format("%s/results/%s/%s", currentPath, key1, key2));
        if (!resultDir.isDirectory()) {
            resultDir.mkdir();
        }

        return Paths.get(String.format("%s/results/%s/%s/%s.tsv", currentPath, key1, key2, queryId));

    }
}
