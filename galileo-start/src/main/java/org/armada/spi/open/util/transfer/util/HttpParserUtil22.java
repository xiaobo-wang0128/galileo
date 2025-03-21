package org.armada.spi.open.util.transfer.util;

import org.armada.spi.open.util.HaiqSdkPostParam;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.List;


public class HttpParserUtil22 {

	public static HaiqSdkPostParam parseRequestData(HttpServletRequest request) throws Exception {

		String encoding = "utf-8";
		request.setCharacterEncoding(encoding);

		DiskFileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload sevletFileUpload = new ServletFileUpload(factory);
		// 最大500M
		sevletFileUpload.setSizeMax(500 * 1024 * 1024);

		@SuppressWarnings("unchecked")
		List<FileItem> fileItems = (List<FileItem>) sevletFileUpload.parseRequest(request);

		// 依次处理每个上传的文件

		String sign = null;
		String appId = null;
		String dc = null;
		String userId = null;
		String token = null;

		byte[] bufs = null;

		for (Iterator<FileItem> it = fileItems.iterator(); it.hasNext();) {

			final FileItem item = (FileItem) it.next();

			if (item.isFormField()) {
				if ("sign".equals(item.getFieldName())) {
					sign = item.getString();
					continue;
				}
				if ("appId".equals(item.getFieldName())) {
					appId = item.getString();
					continue;
				}
				if ("dc".equals(item.getFieldName())) {
					dc = item.getString();
					continue;
				}
				if ("userId".equals(item.getFieldName())) {
					userId = item.getString();
					continue;
				}
				if ("token".equals(item.getFieldName())) {
					token = item.getString();
					continue;
				}
				continue;
			}

			if (!item.isFormField()) {
				java.io.InputStream is = item.getInputStream();
				ByteArrayOutputStream bos = new ByteArrayOutputStream();

				int len = -1;
				byte[] buf = new byte[10240];
				while ((len = is.read(buf)) != -1) {
					bos.write(buf, 0, len);
				}
				is.close();
				bos.flush();
				bos.close();
				bufs = bos.toByteArray();
			}
		}

		HaiqSdkPostParam httpPostParam = new HaiqSdkPostParam(appId, sign, dc, bufs);

		return httpPostParam;
	}

}
