package es_test;

import com.haigui.common.pdf.constant.PaginationConfig;
import com.haigui.common.pdf.util.PdfPageUtil;
import org.armada.galileo.common.util.CommonUtil;

import java.io.FileOutputStream;

/**
 * @author xiaobo
 * @date 2024/9/19 18:27
 */
public class PdfPageTest {

    public static void main(String[] args) throws Exception{
        byte[] pdfs = CommonUtil.readFileFromLocal("/Users/wangxiaobo/Downloads/香洲区荣华小学 普查报告.pdf");


        byte[] pdfs2 = PdfPageUtil.addPagination(pdfs, new PaginationConfig("center", 1));


        new FileOutputStream("/Users/wangxiaobo/Downloads/22.pdf").write(pdfs2);


    }

}
