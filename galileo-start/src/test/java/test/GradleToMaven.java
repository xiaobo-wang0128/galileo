package test;

import java.util.ArrayList;
import java.util.List;

import org.armada.galileo.common.util.CommonUtil;

public class GradleToMaven {

    public static void main(String[] args) throws Exception {

        //String input = "/Users/wangxiaobo/project/_hairou/wan_yi_tong_java/build.gradle";

        String input = "/Users/wangxiaobo/project/_hairou/iwms-op/iwms-op-service/build.gradle";

        byte[] bufs = CommonUtil.readFileFromLocal(input);

        String str = new String(bufs, "utf-8");

        // System.out.println(str);

        String[] tmps = str.split("\n");

        List<String> list = new ArrayList<>();


        List<String> starts = CommonUtil.asList("compile", "dependency",
                "implementation", "annotationProcessor","compileOnly"
                );

        for (String tmp : tmps) {
            tmp = tmp.trim();

            for (String start : starts) {
                if (tmp.matches(start + "\\s+.*?")) {
                    tmp = tmp.substring(start.length()).trim();
                    list.add(tmp);
                }
            }

        }

        for (String compile : list) {

            if (compile == null) {
                continue;
            }
            compile = CommonUtil.replaceAll(compile, "'", "");
            compile = CommonUtil.replaceAll(compile, "\"", "");

            // System.out.println(compile);

            if (compile.indexOf("group") != -1) {

                String group = "";
                String name = "";
                String version = "";

                for (String s : compile.split(",")) {
                    String[] tag = s.split(":");
                    if (tag.length == 2) {
                        String fff = tag[0].trim();
                        if ("group".equals(fff)) {
                            group = tag[1].trim();
                        }
                        if ("name".equals(fff)) {
                            name = tag[1].trim();
                        }
                        if ("version".equals(fff)) {
                            version = tag[1].trim();
                        }
                    }
                }

                StringBuilder sb = new StringBuilder();
                sb.append("<dependency>\n");
                sb.append("\t<groupId>" + group + "</groupId>\n");
                sb.append("\t<artifactId>" + name + "</artifactId>\n");
                sb.append("\t<version>" + version + "</version>\n");
                sb.append("</dependency>\n");
                System.out.println(sb.toString());

                continue;
            }


            String[] ss = compile.split(":");
            String group = ss.length > 0 ? ss[0].trim() : "";
            String name = ss.length > 1 ? ss[1].trim() : "";
            String version = ss.length > 2 ? ss[2].trim() : "xxxxx";

            StringBuilder sb = new StringBuilder();
            sb.append("<dependency>\n");
            sb.append("\t<groupId>" + group + "</groupId>\n");
            sb.append("\t<artifactId>" + name + "</artifactId>\n");
            sb.append("\t<version>" + version + "</version>\n");
            sb.append("</dependency>\n");
            System.out.println(sb.toString());

        }


    }
}
