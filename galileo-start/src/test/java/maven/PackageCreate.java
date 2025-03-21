package maven;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author xiaobo
 * @date 2022/12/3 20:10
 */
public class PackageCreate {


    public static void main(String[] args) throws Exception {


        checkFolder(MavenCreate.root);

    }

    public static void checkFolder(String folder) throws Exception {

        File f = new File(folder);

        if (f.isFile()) {
            return;
        }

        File[] subs = f.listFiles();
        if (subs != null && subs.length > 0) {
            for (File sub : subs) {
                checkFolder(sub.getAbsolutePath());
            }
            return;
        }

        String sign = "src/main/java/";

        int t = f.getAbsolutePath().indexOf(sign);

        if (t != -1) {
            String end = f.getAbsolutePath().substring(t + sign.length());

            String head = end.replaceAll("/", ".");

            String content = "package " + head + ";";


            FileOutputStream fos = new FileOutputStream(folder + "/Package.java");

            fos.write(content.getBytes(StandardCharsets.UTF_8));

            fos.close();

        }


    }
}
