package org.armada.galileo.i18n_server.util;

import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.*;
import org.armada.galileo.common.util.CommonUtil;

import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class WriteExcel {
	private static final String EXCEL_XLS = "xls";
	private static final String EXCEL_XLSX = "xlsx";


	/**
	 * 导出报表
	 *
	 * @return
	 */
	public static void export() {

		String sheetName = "要素模板";
		List<String> title = Arrays.asList("序号", "测试2", "测试3", "测试3", "测试5", "测试x");

		List<List<String>> values = Arrays.asList(Arrays.asList("a", "b", "c", "d", "e", "f"));

		// 创建HSSFWorkbook
		// HSSFWorkbook wb = getHSSFWorkbook(sheetName, title, values, null);
		XSSFWorkbook wb = getXSSFWorkbook(sheetName, title, values);
		// 响应到客户端
		try {
			FileOutputStream os = new FileOutputStream("/Users/xiaobowang/Desktop/out/test.xlsx");
			wb.write(os);
			os.flush();
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void testSheet() {

		try (XSSFWorkbook workbook = new XSSFWorkbook();) {
			XSSFSheet sheet = workbook.createSheet("下拉列表测试");
			String[] datas = { "A", "B", "C", "D" };
			XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheet);
			XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper.createExplicitListConstraint(datas);
			CellRangeAddressList addressList = new CellRangeAddressList(0, 1000, 0, 0);
			XSSFDataValidation validation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, addressList);
			sheet.addValidationData(validation);
			FileOutputStream stream = new FileOutputStream("/Users/xiaobowang/Desktop/out/test22.xlsx");
			workbook.write(stream);
			addressList = null;
			validation = null;

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 创建excel表格
	 *
	 * @param sheetName 表格名
	 * @param titles 列名
	 * @param selectMap 下拉框值（ 列名：选项1, 选项2, 选项3, 选项4 ）
	 * @return
	 */
	public static XSSFWorkbook getXSSFWorkbook(String sheetName, List<String> titles, Map<String, String> selectMap, List<String> highlighColumns) {

		XSSFWorkbook wb = new XSSFWorkbook();

		XSSFCellStyle defaultStyle = wb.createCellStyle();
		XSSFDataFormat format = wb.createDataFormat();
		defaultStyle.setDataFormat(format.getFormat("@"));

		XSSFCellStyle hignlighStyle = wb.createCellStyle();
		hignlighStyle.setDataFormat(format.getFormat("@"));
		hignlighStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
		hignlighStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		XSSFSheet sheet = wb.createSheet(sheetName);

		// 冻结第一行
		sheet.createFreezePane(0, 1, 0, 1);
		sheet.setDefaultColumnWidth(15);// 设置单元格宽度

		XSSFRow headRow = sheet.createRow(0);
		int column = 0;
		for (String title : titles) {
			XSSFCell cell = headRow.createCell(column);
			cell.setCellValue(title);
			if (highlighColumns != null && highlighColumns.contains(title)) {
				cell.setCellStyle(hignlighStyle);
			}

			sheet.setColumnWidth(column, (title.length() + 2) * 512);

			sheet.setDefaultColumnStyle(column, defaultStyle);

			// 判断当前列是否为单选下拉
			if (selectMap != null && selectMap.get(title) != null) {

				String selectValues = selectMap.get(title);
				selectValues = selectValues.replaceAll("\\s+", "");

				String[] datas = selectValues.split(",");

				XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheet);
				XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper.createExplicitListConstraint(datas);
				CellRangeAddressList addressList = new CellRangeAddressList(1, 1000, column, column);
				XSSFDataValidation validation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, addressList);
				sheet.addValidationData(validation);
			}

			column++;
		}

		return wb;
	}

	public static XSSFWorkbook getXSSFWorkbook(String sheetName, List<String> title, List<List<String>> values) {

		XSSFWorkbook wb = new XSSFWorkbook();

		XSSFCellStyle cellStyle = wb.createCellStyle();
		XSSFDataFormat format = wb.createDataFormat();
		cellStyle.setDataFormat(format.getFormat("@"));

		XSSFSheet sheet = wb.createSheet(sheetName);

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
					cell.setCellValue(tmp);
					column++;
				}
				row++;
			}
		}

		return wb;
	}

	/**
	 * 追加内容
	 *
	 * @param book
	 * @param sheetName
	 * @param values
	 */
	public static void appendValues(XSSFWorkbook book, String sheetName, List<List<String>> values) {
		XSSFSheet sheet = book.getSheet(sheetName);
		// excel 内容
		if (CommonUtil.isNotEmpty(values)) {
			int row = 1;
			int column;
			for (List<String> subList : values) {
				column = 0;
				XSSFRow contentRow = sheet.createRow(row);
				for (String tmp : subList) {
					XSSFCell cell = contentRow.createCell(column);
					cell.setCellValue(tmp);
					column++;
				}
				row++;
			}
		}
	}

	/**
	 * 多sheet
	 *
	 * @param sheetMap
	 * @param titleMap
	 * @param valuesMap
	 * @return
	 */
	public static XSSFWorkbook getXSSFWorkbook(Map<String, String> sheetMap, Map<String, List<String>> titleMap, Map<String, List<List<String>>> valuesMap) {
		XSSFWorkbook wb = new XSSFWorkbook();

		XSSFCellStyle cellStyle = wb.createCellStyle();
		XSSFDataFormat format = wb.createDataFormat();
		cellStyle.setDataFormat(format.getFormat("@"));

		for (Map.Entry<String, String> sheetEntry : sheetMap.entrySet()) {
			XSSFSheet sheet = wb.createSheet(sheetEntry.getValue());

			// 冻结第一行
			sheet.createFreezePane(0, 1, 0, 1);
			// 设置单元格宽度
			sheet.setDefaultColumnWidth(15);

			XSSFRow headRow = sheet.createRow(0);
			int column = 0;
			List<String> title = titleMap.get(sheetEntry.getKey());
			List<List<String>> values = valuesMap.get(sheetEntry.getKey());
			for (String tmp : title) {
				XSSFCell cell = headRow.createCell(column);
				cell.setCellValue(tmp);

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
						cell.setCellValue(tmp);
						column++;
					}
					row++;
				}
			}
		}

		return wb;
	}

}