package org.armada.galileo.servlet;

import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.loader.ClassHelper;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.mvc_plus.util.MimeTypes;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;


/**
 * @author xiaobo
 * @date 2021/10/26 4:15 下午
 */

@Slf4j
public class VueRedirectServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doDispath(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doDispath(request, response);
    }

    private static String indexVm = CommonUtil.readFileToString("views/screen/galileo_portal_index.vm");


    private static String contextPath;

    public static String[] initUrlMappings(String contextPath) {
        String[] urlMappings = new String[]{
                "/" + contextPath + "/",
                "/" + contextPath + "/api",
                "/" + contextPath + "/page/*",
                "/" + contextPath + "/system/*",
                "/" + contextPath + "/statics/*",
                "/" + contextPath + "/tmp/statics/*"
                //,
                //"*.js",
                //"*.css",
                //"*.woff"
        };
        return urlMappings;
    }


    private byte[] indexBytes;

    public VueRedirectServlet(String contextPath) {

        this.contextPath = contextPath;

        String newIndexVm = new String(indexVm);

        newIndexVm = CommonUtil.replaceAll(newIndexVm, "/armada-galileo", "/" + contextPath);

        newIndexVm = CommonUtil.replaceAll(newIndexVm, "statics/js/", "/" + contextPath + "/statics/js/");

        newIndexVm = CommonUtil.replaceAll(newIndexVm, "statics/css/", "/" + contextPath + "/tmp/statics/css/");

        indexBytes = newIndexVm.getBytes(StandardCharsets.UTF_8);
    }


    // private List<String> exts = CommonUtil.asList(".css", ".js", ".woff");

    private void doDispath(HttpServletRequest request, HttpServletResponse response) {

        try {
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
            response.setHeader("Access-Control-Max-Age", "3600");
            response.setHeader("Access-Control-Allow-Headers", "x-requested-with");

            String uri = request.getRequestURI();
            int tmpIndex = uri.lastIndexOf(".");

            if (tmpIndex != -1) {
                String ext = uri.substring(tmpIndex);

                //if (exts.contains(ext)) {

                int index = uri.indexOf("/statics/");

                if (index == -1) {
                    return;
                }

                String resourcePath = uri.substring(index, uri.length());
                renderStaticFiles(resourcePath, response);

                return;
                //}
            }

            response.getOutputStream().write(indexBytes);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    static ClassLoader classLoader = ClassHelper.getClassLoader();

    private void renderStaticFiles(String uri, HttpServletResponse response) {

        try {
            int index = uri.lastIndexOf(".");

            String mimeType = null;
            if (index != -1) {
                String fileExt = uri.substring(index + 1);
                mimeType = MimeTypes.getMimeType(fileExt);
            } else {
                mimeType = "text/plain";
            }
            if (uri.startsWith("/")) {
                uri = uri.substring(1, uri.length());
            }

            InputStream is = classLoader.getResourceAsStream(uri);
            if (is == null) {
                response.getWriter().println("404");
                return;
            }

            response.setHeader("Cache-Control", "max-age=3600, s-maxage=31536000");
            response.setContentType(mimeType);

            byte[] buf = new byte[1024];
            int len = 0;
            while ((len = is.read(buf)) != -1) {
                response.getOutputStream().write(buf, 0, len);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

}