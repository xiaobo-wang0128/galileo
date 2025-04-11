package org.armada.galileo.open.util.api_scan;

import org.armada.galileo.open.util.api_scan.domain.DocumentGenerateTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xiaobo
 * @date 2022/12/21 11:17
 */
public class WebxUrlScan {


//    private DocumentGenerateTask documentGenerateTask;

    private List<String> srcPaths = null;

    private String pathRegex = null;

    private List<String> rpcFiles = null;

    public WebxUrlScan(DocumentGenerateTask documentGenerateTask, List<String> srcPaths) {
        this.pathRegex = ".*?"+documentGenerateTask.getRootPackage().replaceAll("\\.", "/") + "/.*?/.*?/web/rpc/.*?\\.java";
        this.srcPaths = srcPaths;
        this.rpcFiles = new ArrayList<>();
    }

    public List<String> doScan() {

        List<String> webPaths = srcPaths.stream().filter(e -> e.endsWith("-web/src/main/java/")).collect(Collectors.toList());

        // find rpc class
        for (String webPath : webPaths) {
            scanRpcClass(webPath);
        }
        // System.out.println(JsonUtil.toJsonPretty(rpcFiles));
        return rpcFiles;

    }

    private void scanRpcClass(String path) {
        File file = new File(path);

        if (file.isFile()) {

            String filePath = file.getPath();
            if (filePath.matches(pathRegex)) {
                rpcFiles.add(filePath);
            }

        } else {

            for (File listFile : file.listFiles()) {
                scanRpcClass(listFile.getAbsolutePath());
            }
        }
    }

}
