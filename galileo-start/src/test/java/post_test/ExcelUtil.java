package post_test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Description: Excel操作
 * <p>
 * CreateTime: 2017年12月11日 下午3:08:09
 * <p>
 * Change History:
 * <p>
 * Date CR Number Name Description of change
 */
public class ExcelUtil {

    public static final String EXCEL_XLS = "xls";
    public static final String EXCEL_XLSX = "xlsx";

    private static Logger log = LoggerFactory.getLogger(ExcelUtil.class);

    public static void main(String[] args) throws Exception {

        File excelFile = new File("/Users/xiaobowang/document/_haigui/_testfile/5个案件模板.xls");
        FileInputStream is = new FileInputStream(excelFile);

        System.out.println(excelFile.getName());
        ExcelData data = ExcelUtil.readByInputstream(excelFile.getName(), is, false);

        System.out.println(JsonUtil.toJsonPretty(data));
    }

    public static ExcelData uploadExcel(HttpServletRequest request, HttpServletResponse response) {
        try {
            String encoding = "utf-8";
            request.setCharacterEncoding(encoding);
            response.setContentType("text/html; charset=utf-8");

            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload sevletFileUpload = new ServletFileUpload(factory);
            sevletFileUpload.setSizeMax(2 * 1024 * 1024 * 1024);
            List<FileItem> fileItems = sevletFileUpload.parseRequest(request);

            // 依次处理每个上传的文件
            for (Iterator<FileItem> it = fileItems.iterator(); it.hasNext(); ) {
                final FileItem item = (FileItem) it.next();
                if (!item.isFormField()) {
                    return doReadCaseExcel(item);
                }
            }
            return null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);

            throw new BizException(e);
        }
    }

    private static ExcelData doReadCaseExcel(FileItem item) {
        String fileFullName = item.getName();
        int tmpIndex = fileFullName.lastIndexOf(".");
        if (tmpIndex == -1) {
            throw new BizException("上传类型不支持");
        }
        ExcelData excelData = null;
        InputStream is = null;
        try {
            is = item.getInputStream();
            // 先上传至阿里云
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            int len = 0;
            byte[] buf = new byte[4000];
            while ((len = is.read(buf)) != -1) {
                bos.write(buf, 0, len);
            }

            byte[] excelByte = bos.toByteArray();

            String excelpath = "tmp/" + CommonUtil.format(new Date(), "yyyyMMdd") + "/" + UUID.randomUUID().toString() + fileFullName.substring(tmpIndex);

            ByteArrayInputStream bis = new ByteArrayInputStream(excelByte);
            excelData = readByInputstream(item.getName(), bis, false);
            excelData.setFilePath(excelpath);
        } catch (IOException e) {
            throw new BizException("文件上传处理失败: " + e.getMessage());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    log.error("close input stream error");
                }
            }
        }
        return excelData;
    }

    /**
     * excel数据获取
     *
     * @param inputFileName excel文件名，用于判断 2003 / 2007版本
     * @param is            输入流
     * @param ingoreError   是否忽略错误（列中的空单元格）
     * @return
     */
    public static ExcelData readByInputstream(String inputFileName, InputStream is, boolean ingoreError) {

        checkExcelVaild(inputFileName);

        Workbook workbook = getWorkbok(is, inputFileName);
        if (workbook == null) {
            throw new BizException("excel解析异常");
        }

        // int sheetCount = workbook.getNumberOfSheets(); // Sheet的数量
        /**
         * 设置当前excel中sheet的下标：0开始
         */
        // 遍历第一个Sheet
        Sheet sheet = workbook.getSheetAt(0);

        // 获取总行数
        // System.out.println(sheet.getLastRowNum());

        List<String> title = new ArrayList<String>();
        List<List<String>> content = new ArrayList<List<String>>();

        // 为跳过第一行目录设置count
        int rowIndex = 0;

        for (Row row : sheet) {

            // 列头
            if (rowIndex == 0) {
                int columns = row.getLastCellNum();
                for (int i = 0; i < columns; i++) {
                    Cell cell = row.getCell(i);
                    if (cell == null && !ingoreError) {
                        throw new BizException("excel列头中存在空数据");
                    }

                    String value = getValue(cell);
                    if (value != null) {
                        value = value.trim();
                    }

                    title.add(value);
                }
                rowIndex++;
                continue;
            }

            int columns = row.getLastCellNum();

            List<String> contentRow = new ArrayList<String>();
            for (int i = 0; i < columns; i++) {
                Cell cell = row.getCell(i);

                String value = null;
                if (cell != null) {
                    value = getValue(cell);
                }
                if (value != null) {
                    value = value.trim();
                }
                contentRow.add(value);
            }
            content.add(contentRow);

            rowIndex++;
        }

        ExcelData excelData = new ExcelData();
        excelData.setData(content);
        excelData.setTitle(title);

        return excelData;
    }

    private static DecimalFormat df = new DecimalFormat("0.##");

    private static String getValue(Cell cell) {
        if (cell == null || cell.getCellType() == null) {
            return null;
        }
        if (cell.getCellType().equals(CellType.STRING)) {
            return cell.getStringCellValue();
        } else if (cell.getCellType().equals(CellType.NUMERIC)) {
            return df.format(cell.getNumericCellValue());
        } else if (cell.getCellType().equals(CellType.BLANK)) {
            return null;
        } else {
            throw new BizException("excel单元格格式不支持:" + cell.getCellType().toString() + ", 请将excel中所有单元格格式设置为文本格式");
        }
    }

    /**
     * 判断Excel的版本,获取Workbook
     *
     * @param in
     * @param fileName
     * @return
     * @throws IOException
     */
    public static Workbook getWorkbok(InputStream in, String fileName) {
        try {
            Workbook wb = null;
            // Excel 2003
            if (fileName.endsWith(EXCEL_XLS)) {
                wb = new HSSFWorkbook(in);
            }
            // Excel 2007/2010
            else if (fileName.endsWith(EXCEL_XLSX)) {
                wb = new XSSFWorkbook(in);
            }
            return wb;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 判断文件是否是excel
     *
     * @throws Exception
     */
    public static void checkExcelVaild(File file) {
        if (!file.exists()) {
            throw new BizException("文件不存在");
        }
        if (!(file.isFile() && (file.getName().endsWith(EXCEL_XLS) || file.getName().endsWith(EXCEL_XLSX)))) {
            throw new BizException("文件不是Excel");
        }
    }

    /**
     * 判断文件是否是excel
     *
     * @throws Exception
     */
    public static void checkExcelVaild(String fileName) {
        if (!fileName.endsWith(EXCEL_XLS) && !fileName.endsWith(EXCEL_XLSX)) {
            throw new BizException("文件不是Excel");
        }
    }

    /**
     * 不上传阿里云
     *
     * @param request
     * @param response
     * @return
     */
    public static ExcelData uploadExcelNonOss(HttpServletRequest request, HttpServletResponse response) {
        try {
            String encoding = "utf-8";
            request.setCharacterEncoding(encoding);
            response.setContentType("text/html; charset=utf-8");

            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload sevletFileUpload = new ServletFileUpload(factory);
            sevletFileUpload.setSizeMax(2 * 1024 * 1024 * 1024);
            List<FileItem> fileItems = sevletFileUpload.parseRequest(request);

            ExcelData excelData = null;
            // 依次处理每个上传的文件
            for (Iterator<FileItem> it = fileItems.iterator(); it.hasNext(); ) {

                final FileItem item = (FileItem) it.next();
                if (!item.isFormField()) {

                    String fileFullName = item.getName();
                    int tmpIndex = fileFullName.lastIndexOf(".");
                    if (tmpIndex == -1) {
                        throw new BizException("上传类型不支持");
                    }

                    InputStream is = item.getInputStream();
                    // 先上传至阿里云
                    java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
                    int len = 0;
                    byte[] buf = new byte[4000];
                    while ((len = is.read(buf)) != -1) {
                        bos.write(buf, 0, len);
                    }

                    byte[] excelByte = bos.toByteArray();

                    String excelpath = "tmp/" + UUID.randomUUID().toString() + fileFullName.substring(tmpIndex);
                    ByteArrayInputStream bis = new ByteArrayInputStream(excelByte);
                    excelData = readByInputstream(item.getName(), bis, false);
                    excelData.setFilePath(excelpath);

                    return excelData;
                }
            }

            return null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);

            throw new BizException(e);
        }
    }

    public static XSSFWorkbook getXSSFWorkbook(String sheetName, List<String> title, List<List<String>> values) {

        XSSFWorkbook wb = new XSSFWorkbook();

        XSSFCellStyle cellStyle = wb.createCellStyle();
        XSSFDataFormat format = wb.createDataFormat();
        cellStyle.setDataFormat(format.getFormat("@"));

        XSSFSheet sheet = wb.createSheet(sheetName);

        DataFormat df = wb.createDataFormat();
        XSSFCellStyle intCellStyle = wb.createCellStyle();
        XSSFCellStyle floatCellStyle = wb.createCellStyle();
        intCellStyle.setDataFormat(df.getFormat("0_ "));//数据格式只显示整数
        floatCellStyle.setDataFormat(df.getFormat("0.000_ "));//保留两位小数点


        // 冻结第一行
        sheet.createFreezePane(0, 1, 0, 1);
        sheet.setDefaultColumnWidth(15);// 设置单元格宽度

        XSSFRow headRow = sheet.createRow(0);
        int column = 0;
        for (String tmp : title) {
            XSSFCell cell = headRow.createCell(column);
            cell.setCellValue(tmp);
            // cell.setCellStyle(style);

            sheet.setDefaultColumnStyle(column, cellStyle);

            column++;
        }

        // excel 内容
        if (values != null && values.size() > 0) {

            int row = 1;
            for (List<String> subList : values) {

                XSSFRow contentRow = sheet.createRow(row);
                column = 0;
                for (String tmp : subList) {
                    XSSFCell cell = contentRow.createCell(column);

                    if (tmp == null) {
                        tmp = "";
                    }
                    try {
                        if (tmp.matches("\\d+")) {
                            // 设置单元格格式
                            cell.setCellValue(Integer.valueOf(tmp));
                            cell.setCellStyle(intCellStyle);
                        } else if (tmp.matches("\\d+\\.\\d+")) {
                            // 设置单元格格式
                            cell.setCellValue(Double.valueOf(tmp));
                            cell.setCellStyle(floatCellStyle);
                        } else {
                            // 设置单元格格式
                            cell.setCellValue(tmp);
                        }
                    } catch (Exception e) {
                        cell.setCellValue(tmp);
                    }
                    column++;
                }
                row++;
            }
        }

        return wb;
    }

    public static void export(OutputStream os, String sheetName, List<String> title, List<List<String>> values) {

        // 创建HSSFWorkbook
        // HSSFWorkbook wb = getHSSFWorkbook(sheetName, title, values, null);
        XSSFWorkbook wb = getXSSFWorkbook(sheetName, title, values);
        // 响应到客户端
        try {
            wb.write(os);
            os.flush();
            os.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                wb.close();
            } catch (Exception e2) {
            }
        }
    }
}