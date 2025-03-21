package test.code_check;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import org.armada.galileo.common.util.CommonUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author xiaobo
 * @date 2021/11/18 3:44 下午
 */
public class CodeCheck {

    public static void main(String[] args) {


        // String folder = "/Users/wangxiaobo/project/_hairou/iwms2021/";

        String folder = "/Users/wangxiaobo/project/_hairou/iwms2022_op";

        File f = new File(folder);

        List<JavaFile> list = new ArrayList<>();

        checkCodeRows(list, f);

        Collections.sort(list, new Comparator<JavaFile>() {
            @Override
            public int compare(JavaFile o1, JavaFile o2) {
                return o1.getRows().compareTo(o2.getRows());
            }
        });

        list.stream().forEach(e -> {
            String path = e.getFile();
            path = path.substring(folder.length());
            System.out.println(path + ":" + e.getRows());
        });


        System.out.println("rows: " + rows);
    }

    static int rows = 0;

    static String rootPath = "";

    public static void checkCodeRows(List<JavaFile> files, File f) {

        if (f.isDirectory()) {
            for (File f1 : f.listFiles()) {
                checkCodeRows(files, f1);
            }
        }


        if (f.getAbsolutePath().indexOf("/src/main") == -1) {
            return;
        }

        if (f.getName().endsWith(".java") || f.getName().endsWith(".xml")) {

            byte[] bufs = CommonUtil.readFileFromLocal(f.getAbsolutePath());

            String str = new String(bufs);

            int line = str.split("\n").length;

            rows += line;

            files.add(new JavaFile(f.getAbsolutePath(), line));
        }
    }


    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    private static class JavaFile {
        private String file;

        private Integer rows;
    }

}
