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
 * 代码行数扫描
 *
 * @author xiaobo
 * @date 2021/11/18 3:44 下午
 */
public class CodeScanJava {

    static List<JavaFile> fileList = new ArrayList<>();

    public static void main(String[] args) {


         String folder = "/Users/wangxiaobo/project/_codes/vot_2024/galaxy-java";

//        String folder = "/Users/wangxiaobo/project/_codes/aml_2022/galileo";

        File f = new File(folder);

        checkCodeRows(new JavaFile(f));

        Collections.sort(fileList, new Comparator<JavaFile>() {
            @Override
            public int compare(JavaFile o1, JavaFile o2) {
                return o1.getRows().compareTo(o2.getRows());
            }
        });


        int rows = 0;
        for (JavaFile e : fileList) {
            String path = e.getFile().getAbsolutePath();
            path = path.substring(folder.length());
            System.out.println(path + ":" + e.getRows());

            rows += e.getRows();
        }

        System.out.println("rows: " + rows);
    }


    static String rootPath = "";

    public static void checkCodeRows(JavaFile f) {

        if (f.getFile().isDirectory()) {
            for (File f1 : f.getFile().listFiles()) {
                checkCodeRows(new JavaFile(f1));
            }
        }

        if (f.getFile().getAbsolutePath().indexOf("/src/main") == -1) {
            return;
        }

        if (f.getFile().getName().endsWith(".java")) {

            byte[] bufs = CommonUtil.readFileFromLocal(f.getFile().getAbsolutePath());

            String str = new String(bufs);

            int row = 0;
            String[] tmps = str.split("\n");
            for (String tmp : tmps) {
                if (tmp.trim().startsWith("//") || tmp.trim().startsWith("/*") || tmp.trim().startsWith("*")) {
                    continue;
                }
                row++;
            }
            f.setRows(row);

            fileList.add(f);
        }
    }


    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    private static class JavaFile {

        public JavaFile(File file) {
            this.file = file;
            this.rows = 0;
        }

        private File file;

        private Integer rows;
    }

}
