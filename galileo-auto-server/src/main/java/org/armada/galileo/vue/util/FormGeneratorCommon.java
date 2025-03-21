package org.armada.galileo.vue.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.exception.BizException;
import org.armada.galileo.mvc_plus.util.VelocityUtil;
import org.armada.galileo.vue.domain.CodeParams;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FormGeneratorCommon {

    /**
     * 通用弹窗式表单
     */
    private static String common_tpl = "tpl/vue_common.html";

    private static String new_list_tpl = "tpl/vue_new_list.html";
    private static String new_form_tpl = "tpl/vue_new_form.html";

    @Data
    public static class CodeOutput {

        private String fileName;

        private String fileContent;

        public CodeOutput(String fileName, String fileContent) {
            this.fileName = fileName;
            this.fileContent = fileContent;
        }

    }

    public static List<CodeOutput> generateCode(CodeParams codeParams) throws Exception {

        //
        String fileName = codeParams.getFileName(); // 文件名
        String fileDir = codeParams.getFileDir(); // 文件名

        String listUrl = codeParams.getApiUrlHead() + "/selectAll.json";
        String detailUrl = codeParams.getApiUrlHead() + "/detail.json";
        String delUrl = codeParams.getApiUrlHead() + "/remove.json";
        String submitUrl = codeParams.getApiUrlHead() + "/saveUpdate.json";
        String statusChangeUrl = codeParams.getApiUrlHead() + "/changeStatus.json";

        String domainCnName = codeParams.getDomainCnName(); //
        String codeContent = codeParams.getCodeContent(); //
        String formType = codeParams.getFormType();
        String idName = codeParams.getIdName();
        String systemType = codeParams.getSystemType();

        // ByteArrayInputStream bis = new ByteArrayInputStream(codeContent.getBytes("utf-8"));

        Map<String, String> fieldComment = new LinkedHashMap<String, String>();

        try {

            JavaParser.parse(codeContent).accept(new VoidVisitorAdapter<Map<String, String>>() {

                public void visit(final FieldDeclaration n, Map<String, String> arg) {
                    try {
                        String fieldName = n.getVariables().get(0).getName().asString();
                        String fieldContent = n.getComment().get().getContent();

                        fieldContent = fieldContent.replaceAll("\\n\\s+\\*", "");
                        fieldContent = fieldContent.replaceAll("\\s+", " ");

                        arg.put(fieldName, fieldContent);

                        arg.put(fieldName, n.getElementType().asString() + "|" + fieldContent);

                    } catch (Exception e) {
                        log.warn("缺少注释：{}, {}", n.getVariables().get(0).getName().asString(), e.getMessage());

                    }
                }
            }, fieldComment);
        } catch (Exception e) {
            throw new BizException("java bean 代码格式不正确");
        }

        // System.out.println(JsonUtil.toJsonPretty(fieldComment));

        List<Map<String, String>> fields = new ArrayList<>();
        for (Map.Entry<String, String> entry : fieldComment.entrySet()) {
            String prop = entry.getKey();

            String[] tmps = entry.getValue().split("\\|");
            String type = tmps[0].trim();
            String label = tmps[1].trim();

            Map<String, String> row = new HashMap<>();
            row.put("prop", prop);
            row.put("type", type);
            row.put("label", label);

            if (type.equals("Long") &&
                    (prop.toLowerCase().indexOf("date") != -1 || prop.toLowerCase().indexOf("time") != -1)) {
                row.put("formatRender", "{{$pub.formatTimestamp(scope.row." + prop + ", 'YYYY-MM-DD HH:mm')}}");
                row.put("elType", "date");
            }
            else if (type.indexOf("Enum") != -1) {
                row.put("formatRender", "{{ $pub.enumEcho('" + type + "', scope.row." + prop + " ) }}");
                row.put("elType", "enum");
            }
            else{
                row.put("elType", "text");
            }

            fields.add(row);
        }

        Map<String, Object> params = new HashMap<>();
        params.put("fileName", fileName);
        params.put("fileDir", fileDir);
        params.put("fields", fields);
        params.put("domainCnName", domainCnName);

        params.put("listUrl", listUrl);
        params.put("detailUrl", detailUrl);
        params.put("delUrl", delUrl);
        params.put("submitUrl", submitUrl);
        params.put("statusChangeUrl", statusChangeUrl);

        params.put("idName", idName);

        params.put("systemType", systemType);


        params.put("util", CommonUtil.instance());

        List<CodeOutput> list = new ArrayList<CodeOutput>();

        if ("dialog".equals(formType)) {
            String htmlTemplate = CommonUtil.readFileToString(common_tpl);

//			for (Map.Entry<String, Object> entry : params.entrySet()) {
//				htmlTemplate = CommonUtil.replaceAll(htmlTemplate, "${" + entry.getKey() + "}", entry.getValue().toString());
//			}

            String html = VelocityUtil.render(htmlTemplate, params);

            CodeOutput file1 = new CodeOutput(fileName, html);

            list.add(file1);
        } else if ("page".equals(formType)) {

            // new_list_tpl
            // new_form_tpl

            // 一行3列，对 fields 进行分组
            List<List<Map<String, String>>> fieldsGroup = new ArrayList<List<Map<String, String>>>();
            for (int i = 0; ; i += 3) {

                if (fields.size() >= i + 3) {
                    fieldsGroup.add(fields.subList(i, i + 3));
                } else {
                    if (fields.size() > i) {
                        fieldsGroup.add(fields.subList(i, fields.size()));
                    }

                    break;
                }
            }
            params.put("fieldsGroup", fieldsGroup);

            log.info(JsonUtil.toJsonPretty(fieldsGroup));

            {
                String htmlTemplate = CommonUtil.readFileToString(new_list_tpl);

                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    htmlTemplate = CommonUtil.replaceAll(htmlTemplate, "${" + entry.getKey() + "}", entry.getValue().toString());
                }

                String html = VelocityUtil.render(htmlTemplate, params);

                CodeOutput file1 = new CodeOutput(fileName + ".vue", html);

                list.add(file1);
            }

            {
                String htmlTemplate = CommonUtil.readFileToString(new_form_tpl);

                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    htmlTemplate = CommonUtil.replaceAll(htmlTemplate, "${" + entry.getKey() + "}", entry.getValue().toString());
                }

                String html = VelocityUtil.render(htmlTemplate, params);

                CodeOutput file1 = new CodeOutput(fileName + "_form.vue", html);

                list.add(file1);
            }

        }

        return list;

    }

//	public static String format(String unformattedXml) {
//		try {
//			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//			DocumentBuilder db = dbf.newDocumentBuilder();
//			InputSource is = new InputSource(new StringReader(unformattedXml));
//			final Document document = db.parse(is);
//			OutputFormat format = new OutputFormat(document);
//			format.setLineWidth(65);
//			format.setIndenting(true);
//			format.setIndent(2);
//			Writer out = new StringWriter();
//			XMLSerializer serializer = new XMLSerializer(out, format);
//			serializer.serialize(document);
//			return out.toString();
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}

}
