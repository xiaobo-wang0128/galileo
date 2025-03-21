package post_test;

import com.esotericsoftware.kryo.io.Output;
import lombok.Data;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.JsonUtil;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author xiaobo
 * @date 2022/11/23 10:01
 */
public class StockQueryExcelExport {

    public static void main(String[] args) throws Exception {

        Date now = new Date();

        String today = CommonUtil.format(now, "yyyy-MM-dd");
        String startTime = CommonUtil.format(now, "yyyy-MM-dd HH:mm:ss");
        String endTime = null;

        long ll1 = System.currentTimeMillis();

        CloseableHttpClient client = HttpClients.createDefault();

        {
            List<NameValuePair> param = new ArrayList<>();
            param.add(new BasicNameValuePair("userName", "wangxiaobo"));
            param.add(new BasicNameValuePair("userPass", "123456"));
            HttpUriRequest post = RequestBuilder.post("http://wms.imlb2c.com/login.html").setEntity(new UrlEncodedFormEntity(param, "utf-8")).build();

            HttpResponse res = client.execute(post);

            Header[] cookies = res.getHeaders("Set-Cookie");
        }

        int page = 1;
        int pageSize = 2000;

        List<Record> list = new ArrayList<>();

        String path = System.getProperty("user.home") + "/excel_stock_export/";

        List<String> filePaths = new ArrayList<>();

        int fileIndex = 1;

        String sheetName = "库存导出";
        List<String> title = CommonUtil.asList("仓库", "库位", "库位类型", "库存类型", "客户代码", "产品代码", "货品名称", "规格型号", "当前库存", "库龄", "单位体积(CBCM)", "单位重量(KG)", "高(CM)", "宽(CM)", "长(CM)", "时间", "入库单号", "是否投保");
        List<List<String>> values = new ArrayList<>();

        while (true) {

            List<NameValuePair> param = new ArrayList<>();
            param.add(new BasicNameValuePair("E13", ""));
            param.add(new BasicNameValuePair("E19", ""));
            param.add(new BasicNameValuePair("lcType", ""));
            param.add(new BasicNameValuePair("is_insurance", ""));
            param.add(new BasicNameValuePair("E8", "7"));
            param.add(new BasicNameValuePair("customerCodeType", "equal"));
            param.add(new BasicNameValuePair("customerCode", ""));
            param.add(new BasicNameValuePair("product_sku", ""));
            param.add(new BasicNameValuePair("operationType", "E1_like"));
            param.add(new BasicNameValuePair("operationCode", ""));
            param.add(new BasicNameValuePair("time_type", "rdb_add_time"));
            param.add(new BasicNameValuePair("dateFor", ""));
            param.add(new BasicNameValuePair("dateTo", ""));
            param.add(new BasicNameValuePair("dateSelected", ""));


            long l1 = System.currentTimeMillis();

            HttpUriRequest post = RequestBuilder.post("http://wms.imlb2c.com/warehouse/inventory-batch/list/page/" + page + "/pageSize/" + pageSize)  //
                    .setEntity(new UrlEncodedFormEntity(param, "utf-8"))  //
                    .build();

            HttpResponse res = null;

            while (true) {
                try {
                    Thread.sleep(1000);
                    res = client.execute(post);
                    break;
                } catch (Exception e) {

                    try {
                        Thread.sleep(5000);
                    } catch (Exception exx) {
                        System.err.println(exx.getMessage());
                    }
                    continue;
                }
            }

            String text = EntityUtils.toString(res.getEntity());

            Result reuslt = JsonUtil.fromJson(text, Result.class);

            int returnSize = reuslt.getData() != null ? reuslt.getData().size() : 0;

            long l2 = System.currentTimeMillis();
            String logInfo = CommonUtil.format("第 {} 次查询, 返回 {} 条记录, 耗时: {}ms", page, returnSize, (l2 - l1));
            System.out.println(logInfo);

            if (returnSize > 0) {

                int row = 0;
                for (Record r : reuslt.getData()) {

                    List<String> tmp = CommonUtil.asList(reuslt.getWarehouse().get(Integer.valueOf(r.getE8())),// "仓库"
                            r.getE1(), //"库位",
                            reuslt.getWarehouse().get(Integer.valueOf(r.getE8())), //"库位类型",
                            "0".equals(r.getE19()) ? "标品" : "不良品", //    "库存类型",
                            r.getE20(),  //"客户代码",
                            r.getE4(), //"产品代码",
                            r.getProduct_title_en(), // "货品名称",
                            r.getProduct_model(), // "规格型号",
                            r.getE14(), // "当前库存",
                            (System.currentTimeMillis() - CommonUtil.parse(r.getE15(), "yyyy-MM-dd").getTime()) / CommonUtil.millDay + "天", //  "库龄",
                            getVolume(r), //"单位体积(CBCM)",
                            r.getProduct_weight(), // "单位重量(KG)",
                            r.getProduct_height(), // "高(CM)",
                            r.getProduct_width(), // "宽(CM)",
                            r.getProduct_length(), //"长(CM)",
                            r.getE15(), // "时间",
                            r.getE21(), //  "入库单号",
                            "1".equals(r.getIs_insurance()) ? "是" : "否" // "是否投保"
                    );

                    values.add(tmp);

                    if (values.size() >= 20000 || (row == reuslt.getData().size() - 1 && reuslt.getData().size() < pageSize)) {

                        String filePath = path + "/" + today + "-" + fileIndex + ".xlsx";
                        CommonUtil.makeSureFolderExists(filePath);

                        OutputStream os = new FileOutputStream(filePath);
                        ExcelUtil.export(os, sheetName, title, values);

                        values.clear();

                        filePaths.add(filePath);

                        fileIndex++;
                    }

                    row++;

                }
            }

            if (returnSize < pageSize) {
                break;
            }
            page++;
        }

        long ll2 = System.currentTimeMillis();

        System.out.println("done, total cost: " + (ll2 - ll1) / 1000 + "s");

        System.out.println("start send email");

        endTime = CommonUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");

        SendExcelEmail.sendEmail(filePaths, today, startTime, endTime);

    }


    public static void main22(String[] args) {
        Record r = new Record();
        r.setProduct_width("123.434");
        r.setProduct_length("3434.343");
        r.setProduct_height("1203.33");
        System.out.println(getVolume(r));
    }

    private static String getVolume(Record r) {

        try {
            BigDecimal b = BigDecimal.valueOf(Double.valueOf(r.getProduct_height())).multiply(BigDecimal.valueOf(Double.valueOf(r.getProduct_length()))).multiply(BigDecimal.valueOf(Double.valueOf(r.getProduct_width())));

            b.setScale(0, RoundingMode.FLOOR);
            //510109268.56496046
            return b.intValue() + "";

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }


    @Data
    public static class Result {
        private List<Record> data;
        private Map<Integer, String> warehouse;
    }

    @Data
    public static class Record {
        private String E0; //; // "1520246",
        private String E1; //; // "40-09-01-7-1",
        private String E2; // "1178684",
        private String E4; // "81677-IML-5-PH5",
        private String E5; // "QC7221120728677",
        private String E8; // "7",
        private String E9; // "RSO22101371271",
        private String E12; // "1",
        private String E13; // "0",
        private String E14; // "6",
        private String E15; // "2022-11-23 01:23:01",
        private String E17; // "2022-11-23 01:23:01",
        private String E18; // "0000-00-00 00:00:00",
        private String E19; // "0",
        private String E20; // "81677",
        private String E21; // "RV81677-221005-0006",
        private String E22; // "0000-00-00 00:00:00",
        private String E31; // "0000-00-00 00:00:00",
        private String customer_code; // "81677",
        private String product_title_en; // "faucet",
        private String product_model; // "",
        private String product_length; // "25.00",
        private String product_width; // "15.00",
        private String product_height; // "5.00",
        private String product_weight; // "1.000",
        private String reference_no; // "15ge1j26",
        private String warning_days; // "0",
        private String product_id; // "1178684",
        private String lc_type; // "1",
        private String lt_code; // "RUS2",
        private String is_insurance; // "0",
        private Integer out_quantity; // 0,
        private Integer bonded; // 0,
        private String warning_time; // "-0001-11-30 00:00:00",
        private String date; // "2022-11-23 10:17:58"
    }

}
