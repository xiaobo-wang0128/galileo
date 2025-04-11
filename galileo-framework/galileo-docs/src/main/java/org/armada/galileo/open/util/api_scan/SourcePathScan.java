package org.armada.galileo.open.util.api_scan;

import org.armada.galileo.common.util.JsonUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xiaobo
 * @date 2022/12/21 11:24
 */
public class SourcePathScan {

    public List<String> srcPaths = new ArrayList<>();

    public static void main(String[] args) {
        String projectPath = "/Users/wangxiaobo/project/_codes/aml_2022/bronze.git";
        List<String> srcPaths = new SourcePathScan(projectPath).generateSrcPath();
        System.out.println(JsonUtil.toJsonPretty(srcPaths));
    }

    public List<String> generateSrcPath() {
        Collections.sort(srcPaths);
        List<String> needDelete = new ArrayList<>();

        for (String path : srcPaths) {
            for (String s : srcPaths) {
                if (!s.equals(path) && s.contains(path)) {
                    needDelete.add(path);
                }
            }
        }
        for (String s : needDelete) {
            srcPaths.remove(s);
        }

        srcPaths = srcPaths.stream().map(e -> e + "src/main/java/").collect(Collectors.toList());

        return srcPaths;
    }


    public SourcePathScan(String projectRootPath) {
        this.scanSrcPaths(projectRootPath);
    }

    public void scanSrcPaths(String folder) {
        File file = new File(folder);
        if (!file.exists()) {
            return;
        }

        if (folder.indexOf("/target/") != -1 || folder.indexOf("/src/") != -1 || folder.indexOf("/.git/") != -1) {
            return;
        }

        if (file.isFile()) {
            if (file.getName().equals("pom.xml")) {
                String head = file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - "pom.xml".length());
                srcPaths.add(head);
            }
            return;
        }

        if (file.isDirectory()) {
            for (File listFile : file.listFiles()) {
                scanSrcPaths(listFile.getAbsolutePath());
            }
        }
    }

}
