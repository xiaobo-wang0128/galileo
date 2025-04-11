package es_test;

import com.haigui.common.pdf.util.PdfGeneratorUtil;
import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.pdf.PdfWriter;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.TimerCheck;

import java.io.FileOutputStream;

/**
 * @author xiaobo
 * @date 2024/8/5 19:01
 */
public class PdfGenerateTest {

    static float oneMmA = 31.49606299F;

    static float oneMmB = 23.62204724F;


    public static void main(String[] args) throws Exception {

        TimerCheck.checkpoint("eee");
        for (int i = 0; i < 1; i++) {
            dotest();
            TimerCheck.checkpoint("abc + " + i);
        }
    }

    public static void dotest() throws Exception {

        float width = 22 * oneMmA;
        float height = 22 * oneMmA;

        byte[] bufs = CommonUtil.readFileFromLocal("/Users/wangxiaobo/Downloads/untitled.html");

        String html = new String(bufs);

        byte[] result = PdfGeneratorUtil.generatePdfByHtml(html, null, true, width, height, 12);

        // PDF 文件路径
        String dest = "/Users/wangxiaobo/Downloads/aaaa.pdf";

        new FileOutputStream(dest).write(result);

        // 关闭文档
        //pdf.close();

    }
}
