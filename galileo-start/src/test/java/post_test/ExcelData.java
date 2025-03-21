package post_test;

import org.armada.galileo.common.util.CommonUtil;

import java.util.List;


public class ExcelData {

	private String filePath;

	private List<String> title;

	public List<List<String>> data;

	public String toString() {

		StringBuilder sb = new StringBuilder();

		sb.append(CommonUtil.join(title, ", ")).append("\n");

		for (List<String> list : data) {
			sb.append(CommonUtil.join(list, ", ")).append("\n");
		}

		return sb.toString();
	}

	public List<String> getTitle() {
		return title;
	}

	public void setTitle(List<String> title) {
		this.title = title;
	}

	public List<List<String>> getData() {
		return data;
	}

	public void setData(List<List<String>> data) {
		this.data = data;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

}
