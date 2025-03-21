package org.armada.galileo.vue.web.rpc;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.annotation.mvc.UserDefine;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.exception.BizException;
import org.armada.galileo.vue.domain.CodeParams;
import org.armada.galileo.vue.util.FormGeneratorCommon;
import org.springframework.stereotype.Controller;


@Slf4j
@Controller
public class CodeRpc {


	public List<FormGeneratorCommon.CodeOutput>  generateCodeStr(HttpServletRequest request, HttpServletResponse response, CodeParams codeParams) throws Exception{
		codeParams.setCodeContent("public class Test {" + codeParams.getCodeContent() + "}");
		return FormGeneratorCommon.generateCode(codeParams);
	}

		@UserDefine
	public void generateCode(HttpServletRequest request, HttpServletResponse response, CodeParams codeParams) {

		if (CommonUtil.isEmpty(codeParams.getCodeContent())) {
			throw new BizException("代码内容不能为空");
		}

		codeParams.setCodeContent("public class Test {" + codeParams.getCodeContent() + "}");

		try {
			List<FormGeneratorCommon.CodeOutput> list = FormGeneratorCommon.generateCode(codeParams);

			if (list.size() == 1) {
				FormGeneratorCommon.CodeOutput co = list.get(0);
				download(co.getFileName(), co.getFileContent(), response);
			}
			else {

				String fileName = "vue_" + CommonUtil.format(new Date(), "yyyy_MM_dd_HH_mm_ss") + ".zip";
				response.setContentType("application/zip");
				response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
				response.addHeader("Pargam", "no-cache");
				response.addHeader("Cache-Control", "no-cache");

				// 创建zip输出流
				ZipOutputStream out = new ZipOutputStream(response.getOutputStream(), Charset.forName("utf-8"));

				for (FormGeneratorCommon.CodeOutput codeOutput : list) {

					byte[] bytes = codeOutput.getFileContent().getBytes("utf-8");
					ZipEntry entry = new ZipEntry(codeOutput.getFileName());

					out.putNextEntry(entry);
					out.write(bytes);
					out.closeEntry();
				}

				out.flush();
				out.close();

			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new BizException(e.getMessage());
		}

	}

	public void download(String fileName, String fileContent, HttpServletResponse response) {
		try {

			String mimeType = "text/plain";

			response.setContentType(mimeType);

			if (fileName.indexOf(".") == -1) {
				fileName = fileName + ".vue";
			}

			response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

			response.addHeader("Pargam", "no-cache");
			response.addHeader("Cache-Control", "no-cache");

			response.getOutputStream().write(fileContent.getBytes("utf-8"));

		} catch (Exception e) {
			throw new BizException(e);
		}
	}

}
