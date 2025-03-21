package org.armada.galileo.open.util;

import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.open.domain.doc.ModuleApiDocs;
import org.armada.galileo.open.util.api_scan.domain.DocItem;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author xiaobo
 * @date 2021/11/18 2:24 下午
 */
@Slf4j
public class ApiDataCache {

    private static final String docFilePath = "api_doc.json";

    private static List<ModuleApiDocs> moduleApiDocsList = new ArrayList<>();

    private static Map<String, DocItem> docCacheMap = new HashMap<>();

    private static AtomicBoolean hasInit = new AtomicBoolean(false);

    static {
        if (hasInit.compareAndSet(false, true)) {
            log.info("[auto-doc] 开始加载接口文档");
            byte[] bufs = CommonUtil.readFileToBuffer(docFilePath);

            if (bufs != null) {
                try {

                    ModuleApiDocs moduleApiDocs = JsonUtil.fromJson(new String(bufs, StandardCharsets.UTF_8), ModuleApiDocs.class);

                    moduleApiDocsList.add(moduleApiDocs);

                    if (moduleApiDocsList != null && moduleApiDocsList.size() > 0) {
                        moduleApiDocsList.stream().forEach(e -> {
                            e.getGroups().forEach(k -> k.getChildren().forEach(f -> {
                                docCacheMap.put(f.getApiUrl(), f);
                            }));

                        });
                    }

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


    static {
        log.info("[auto-doc] 开始加载接口文档");
        byte[] bufs = CommonUtil.readFileToBuffer("api_doc2.json");

        if (bufs != null) {
            try {

                ModuleApiDocs moduleApiDocs = JsonUtil.fromJson(new String(bufs, StandardCharsets.UTF_8), ModuleApiDocs.class);

                moduleApiDocsList.add(moduleApiDocs);

                if (moduleApiDocsList != null && moduleApiDocsList.size() > 0) {
                    moduleApiDocsList.stream().forEach(e -> {
                        e.getGroups().forEach(k -> k.getChildren().forEach(f -> {
                            docCacheMap.put(f.getApiUrl(), f);
                        }));

                    });
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


    public static List<ModuleApiDocs> getDocGroupList() {
        return moduleApiDocsList;
    }

    public static void setModuleApiDocsList(List<ModuleApiDocs> moduleApiDocsList) {
        ApiDataCache.moduleApiDocsList = moduleApiDocsList;
    }

    public static DocItem getByApiUrl(String apiUrl) {
        return docCacheMap.get(apiUrl);
    }

}
