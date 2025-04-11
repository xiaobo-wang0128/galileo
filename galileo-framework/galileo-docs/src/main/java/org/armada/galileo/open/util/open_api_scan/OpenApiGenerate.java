package org.armada.galileo.open.util.open_api_scan;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.Convert;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.open.util.api_scan.*;
import org.armada.galileo.open.util.api_scan.domain.DocItem;
import org.armada.galileo.open.util.api_scan.domain.DocumentGenerateTask;
import org.armada.galileo.open.util.api_scan.domain.ParamType;
import org.armada.galileo.mvc_plus.domain.JsonResult;
import org.armada.galileo.open.util.api_scan.group_domain.InnerGroup;
import org.armada.galileo.open.util.api_scan.group_domain.MainGroup;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * sdk 文档生成器
 *
 * @author xiaobo
 * @date 2021/4/25 10:45 上午
 */
@Slf4j
public class OpenApiGenerate {

    /**
     * 任务参数
     */
    private DocumentGenerateTask documentGenerateTask;

    /**
     * 源码路径，精确到 src/main/java
     */
    private List<String> srcPaths;

    private String urlHead = null;

    public static List<String> Base_Type = Arrays.asList("short", "byte", "int", "float", "double", "long", "boolean", "string", "Short", "Byte", "Long", "Integer", "Float", "Double", "String", "BigDecimal", "Boolean", "Date");

    // public static Map<String, String> Base_Type_Map = new HashMap<>();

//    static {
//        Base_Type_Map.put("short", "int");
//        Base_Type_Map.put("byte", "int");
//        Base_Type_Map.put("int", "int");
//        Base_Type_Map.put("float", "decimal");
//        Base_Type_Map.put("double", "decimal");
//        Base_Type_Map.put("long", "long");
//        Base_Type_Map.put("boolean", "bool");
//        Base_Type_Map.put("string", "varchar");
//        Base_Type_Map.put("integer", "int");
//        Base_Type_Map.put("bigDecimal", "decimal");
//        Base_Type_Map.put("boolean", "bool");
//    }
//
//    public static void main(String[] args) {
//        System.out.println(JsonUtil.toJson(Base_Type_Map));
//    }

    public static List<String> Ignore_Type = Arrays.asList("HttpServletRequest", "HttpServletResponse");

    // public static List<String> Collection_Type = Arrays.asList("HttpServletRequest", "HttpServletResponse");

    public OpenApiGenerate() {
    }

    public OpenApiGenerate(DocumentGenerateTask documentGenerateTask) {
        if (CommonUtil.isEmpty(documentGenerateTask.getProjectRootPath())) {
            System.exit(0);
        }
        this.documentGenerateTask = documentGenerateTask;
    }

    public void generateOpenApiDoc() throws Exception {
        // 扫描接口
        List<String> srcPaths = new SourcePathScan(documentGenerateTask.getProjectRootPath()).generateSrcPath();
        OpenApiScan scan = new OpenApiScan(documentGenerateTask, srcPaths);
        scan.doScan(documentGenerateTask.getScanPath());
        List<String> apiInterfaces = scan.getApiFiles();
        this.urlHead = scan.getUriHead();

        List<DocItem> allDocs = new ArrayList<>();

        this.srcPaths = srcPaths;

        Map<String, Boolean> exsit = new HashMap<>();

        for (String apiFile : apiInterfaces) {
            List<DocItem> docItems = generateApiDocs(apiFile);
            if (CommonUtil.isNotEmpty(docItems)) {
                for (DocItem docItem : docItems) {
                    if (exsit.get(docItem.getApiUrl()) != null) {
                        System.err.println("出现了重复的 url：" + docItem.getApiUrl());
                        System.exit(1);
                    }
                    exsit.put(docItem.getApiUrl(), true);
                }
                allDocs.addAll(docItems);
            }
        }

        Map<String, List<DocItem>> tmpGrou = allDocs.stream().collect(Collectors.groupingBy(kk -> kk.getGroup()));

        List<InnerGroup> innerGroups = new ArrayList<>();
        for (Map.Entry<String, List<DocItem>> tmpEntry : tmpGrou.entrySet()) {

            List<DocItem> subGroupDocs = tmpEntry.getValue();
            Collections.sort(subGroupDocs, Comparator.comparing(kk -> kk.getSort()));
            InnerGroup g = new InnerGroup();
            g.setIndex(subGroupDocs.get(0).getSort());
            g.setName(tmpEntry.getKey());
            g.setChildren(subGroupDocs);
            innerGroups.add(g);
        }

        // 组内排序
        Collections.sort(innerGroups, Comparator.comparing(kk -> kk.getIndex()));

        MainGroup mg = new MainGroup();
        mg.setName(scan.getAppDesc());
        mg.setGroups(innerGroups);
        mg.setUrlHead(urlHead);
        mg.setAppName(scan.getAppName());
        mg.setIndex(0);

        FileOutputStream fos = new FileOutputStream(documentGenerateTask.getOutputFilePath());

        fos.write((JsonUtil.toJsonPretty(mg)).getBytes(StandardCharsets.UTF_8));

        fos.close();

        System.out.println("文档生成完毕");

    }


    public List<DocItem> generateApiDocs(String javaFilePath) throws Exception {

        if (CommonUtil.isEmpty(javaFilePath)) {
            return null;
        }
        System.out.println("开始生成文档: " + javaFilePath);

        /** 获取注释中的信息 */
        //RpcDoc docAnnotation = method.getDeclaringClass().getAnnotation(RpcDoc.class);

        // --------------------------------------------------------------
        // --------------------------- 解析 rpc 注释信息 ------------------
        // --------------------------------------------------------------

        // 读取方法的注释
        // Map<String, String> methodComments = javaFileMehtodComments.get(javaFilePath);
        List<JavaFileItem.JavaMethodItem> JavaMethodItems = new ArrayList<>();

        // log.info("读取java文件注释信息，路径：{}", javaFilePath);
        JavaFileItem javaFileItem = new JavaFileItem();
        javaFileItem.setFilePath(javaFilePath);
        javaFileItem.setJavaMethodItems(JavaMethodItems);
        javaFileItem.setImports(new ArrayList<>());

        FileInputStream in = new FileInputStream(javaFilePath);
        // 读取方法注释
        final AtomicInteger tmpIndex = new AtomicInteger(0);

        // 读取文件注释
        JavaParser.parse(in).accept(new VoidVisitorAdapter<JavaFileItem>() {

            public void visit(final AnnotationDeclaration n, final JavaFileItem arg) {
                for (AnnotationExpr annotation : n.getAnnotations()) {
                    System.out.println(annotation.getName());
                }
            }

            public void visit(final PackageDeclaration n, final JavaFileItem arg) {
                if (!arg.getImports().contains(n.getName().asString() + ".*")) {
                    arg.getImports().add(n.getName().asString() + ".*");
                }
                arg.setJavaPakcage(n.getName().asString());
            }

            public void visit(final ImportDeclaration n, final JavaFileItem arg) {
                String tmp = n.toString().trim();
                tmp = tmp.substring("import".length()).trim();
                tmp = tmp.replaceAll(";", "");
                arg.getImports().add(tmp);
                super.visit(n, arg);
            }

            public void visit(final ClassOrInterfaceDeclaration n, JavaFileItem arg) {

                arg.setJavaClassName(n.getName().asString());

                if (CommonUtil.isNotEmpty(n.getAnnotations())) {

                    List<AnnotationExpr> tmps = n.getAnnotations().stream().filter(e -> {
                        return e.getName().asString().equals("OpenApi");
                    }).collect(Collectors.toList());
                    if (tmps.size() > 0) {

                        for (MemberValuePair pair : ((NormalAnnotationExpr) tmps.get(0)).getPairs()) {

                            if ("group".equals(pair.getName().asString())) {
                                arg.setGroup(pair.getValue().toString().replaceAll("\"", ""));
                            }
                            if ("sort".equals(pair.getName().asString())) {
                                arg.setGroupIndex(Integer.valueOf(pair.getValue().toString()));
                            }

                            if ("apiFrom".equals(pair.getName().asString())) {
                                String v = pair.getValue().toString();
                                if (v.lastIndexOf(".") != -1) {
                                    v = v.substring(v.lastIndexOf(".") + 1);
                                }
                                arg.setApiFrom(v);
                            }

                            if ("apiTo".equals(pair.getName().asString())) {
                                String v = pair.getValue().toString();
                                if (v.lastIndexOf(".") != -1) {
                                    v = v.substring(v.lastIndexOf(".") + 1);
                                }
                                arg.setApiTo(v);
                            }

                        }
                    }
                }

                n.getComment().ifPresent(m -> {
                    arg.setFileDesc(m.getContent());
                });


                arg.setJavaFileName(n.getName().asString());

                super.visit(n, arg);
            }


            public void visit(MethodDeclaration n, JavaFileItem arg) {

                JavaFileItem.JavaMethodItem methodItem = new JavaFileItem.JavaMethodItem();

                try {
                    methodItem.setParameters(n.getParameters());
                    // 方法在文件中出现的顺序
                    methodItem.setApiIndex(tmpIndex.incrementAndGet());
                    methodItem.setMethodName(n.getName().asString());
                    if (n.getComment().isPresent()) {
                        String content = n.getComment().get().getContent();
                        // 方法的注释
                        methodItem.setMethodComment(content);
                    }
                    // 方法的返回类型
                    methodItem.setReturnType(n.getType().asString());

                    methodItem.setApplicationJsonRequest(true);

                    boolean deprecated = n.getAnnotations().stream().filter(e -> e.getName().asString().equals("Deprecated")).collect(Collectors.counting()) > 0;
                    methodItem.setDeprecated(deprecated);

                    AnnotationExpr annotationExpr = n.getAnnotationByName("OpenApiMethod").orElse(null);
                    if (annotationExpr == null) {
                        return;
                    }

                    for (MemberValuePair pair : ((NormalAnnotationExpr) annotationExpr).getPairs()) {

                        if ("code".equals(pair.getName().asString())) {
                            String apiCode = pair.getValue().toString().trim().replaceAll("\"", "");
                            methodItem.setMethodName(apiCode);
                        }
                        if ("sort".equals(pair.getName().asString())) {
                            methodItem.setApiIndex(Integer.valueOf(pair.getValue().toString()));
                        }
                        if ("group".equals(pair.getName().asString())) {
                            methodItem.setApiGroup((pair.getValue().toString()).replaceAll("\"", ""));
                        }
                        if ("async".equals(pair.getName().asString())) {
                            methodItem.setAsync(Boolean.valueOf(pair.getValue().toString()));
                        }

                        // if ("name".equals(pair.getName().asString())) {
                        //     methodItem.setApiName((pair.getValue().toString()).replaceAll("\"", ""));
                        // }

                    }

                    arg.getJavaMethodItems().add(methodItem);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
                super.visit(n, arg);
            }
        }, javaFileItem);

        in.close();

        List<DocItem> result = new ArrayList<>();

        for (JavaFileItem.JavaMethodItem javaMethodItem : JavaMethodItems) {
            try {
                result.add(generateMethodDoc(javaFileItem, javaMethodItem));
            } catch (Exception e) {
                System.err.println(CommonUtil.format("文档扫描出错, class:{}, method:{}", javaFileItem.getFilePath(), javaMethodItem.getMethodName()));
                e.printStackTrace();
            }
        }

        // 过滤、添加通用参数
        // filterInputParams(doc, method, appSecret);

        return result;
    }


    private DocItem generateMethodDoc(JavaFileItem javaFileItem, JavaFileItem.JavaMethodItem method) {

        // 接口code
        String apiCode = method.getMethodName();
        // 接口类名
        String apiClassName = javaFileItem.getJavaPakcage() + "." + javaFileItem.getJavaClassName();
        // 接口名
        String apiName = null;
        // 接口详情描述
        String apiDesc = null;
        // 返回值说明
        String outputDesc = null;
        // 所属分组
        String group = CommonUtil.isNotEmpty(javaFileItem.getGroup()) ? javaFileItem.getGroup() : "other";
        // 分组的排序
        Integer sort = javaFileItem.getGroupIndex();
        if (group.equals("other")) {
            sort = 9999;
        }
        // 子排序 方法在类中出现的顺序
        int subSort = method.getApiIndex();
        String uri = this.urlHead + "/" + method.getMethodName();

        // 是否废弃
        boolean isDeprecated = method.getDeprecated();

        boolean async = method.getAsync();

        String apiFrom = javaFileItem.getApiFrom();
        String apiTo = javaFileItem.getApiTo();


//        if (method.getMethodName().equals("create_overseas_inbound")) {
//            log.debug("");
//        }

        /** -------------------------------------------------------------- */
        /** --------------------------- 输入参数 -------------------------- */
        /** -------------------------------------------------------------- */

//        if (method.getMethodName().equals("remove_overseas_inbound")) {
//            log.debug("");
//        }


        Map<String, String> methodParamComment = new HashMap<>();

        // 读取 method 注释
        {
            String methodComment = method.getMethodComment();
            if (methodComment == null) {
                log.info("接口:{},方法:{} 没有注释信息", method.getMethodName());
                return null;
            }

            // methodComment = methodComment.replaceAll("\\s+\\*", "");
            methodComment = methodComment.replaceAll("\\*", "");

            String[] strs = methodComment.split("@param");

            String apiNameDesc = strs[0].trim();

            if (apiNameDesc.indexOf("@return") != -1) {
                apiNameDesc = apiNameDesc.substring(0, apiNameDesc.indexOf("@return"));
            }

            int tmpIndex = apiNameDesc.indexOf(".");
            if (tmpIndex != -1) {
                apiName = apiNameDesc.substring(0, tmpIndex);
                apiDesc = apiNameDesc.substring(tmpIndex + 1);
            } else {
                apiName = apiNameDesc;
            }
            apiName = apiName.trim();

            if (apiName.indexOf("@description") != -1) {
                apiName = apiName.substring("@description".length()).trim();
            }
            if (apiName.startsWith(":")) {
                apiName = apiName.substring(1);
            }
            if (apiName.indexOf("@") != -1) {
                apiName = apiName.substring(0, apiName.indexOf("@")).trim();
            }

            // 读取注释中的参数注释信息
            if (strs.length > 0) {
                for (int i = 1; i < strs.length; i++) {
                    String tmpStr = strs[i].trim();
                    // like: inputVo ddd @return
                    if (tmpStr.contains("@return")) {
                        String tmpStr1 = tmpStr.substring(0, tmpStr.indexOf("@return"));
                        outputDesc = tmpStr.substring(tmpStr.indexOf("@return") + "@return".length());

                        tmpStr = tmpStr1;
                    }

                    // like: inputVo ddd
                    String pName = "";
                    String pComment = "";
                    tmpIndex = tmpStr.indexOf(" ");
                    if (tmpIndex != -1) {
                        pName = tmpStr.substring(0, tmpIndex).trim();
                        pComment = tmpStr.substring(tmpIndex).trim();
                    } else {
                        pName = tmpStr.trim();
                    }
                    methodParamComment.put(pName, pComment);
                }
            }
        }

        // 输入参数{参数名：注释}
        List<ParamType> inputParams = new ArrayList<ParamType>();

        // 输入参数列表

        // 根据输入参数的实际类型 进行深度处理
        int i = 0;
        NodeList<Parameter> parameters = method.getParameters();
        // for (Class<?> paramType : method.getParameterTypes()) {

        outerLoop:
        for (Parameter parameter : parameters) {

            Type type = parameter.getType();

            // 开放平台上下文字段不做为文档参数
            if (type.asString().equals("OpenApiContext")) {
                continue;
            }

            if (parameter.getAnnotations() != null && parameter.getAnnotations().size() > 0) {
                for (AnnotationExpr annotation : parameter.getAnnotations()) {
                    if (annotation.getName().asString().equals("AppId")) {
                        continue outerLoop;
                    }
                }
            }

            ParamType pt = new ParamType();

            pt.setName(parameter.getName().asString());
            pt.setDescription(methodParamComment.get(pt.getName()));

            String typeName = type.asString();

            // servlet对象忽略
            if (Ignore_Type.contains(typeName)) {
                continue;
            }
            // base type
            else if (Base_Type.contains(typeName)) {
                pt.setType(typeName.toLowerCase());
                inputParams.add(pt);
            }
            // 泛型类型
            else if (typeName.indexOf("<") != -1) {
                try {
                    if (pt != null) {

                        int i1 = typeName.indexOf("<");
                        int i2 = typeName.lastIndexOf(">");

                        String innerTypeName = typeName.substring(i1 + 1, i2);

                        if (innerTypeName.indexOf("<") != -1) {
                            continue;
                        }

                        String typeShow = "";

                        // 泛型类型为基础类型
                        if (Base_Type.contains(innerTypeName)) {
                            typeShow = "array<" + innerTypeName.toLowerCase() + ">";

                        } else {
                            typeShow = "array<object>";
                            Bean bean = getBean(javaFileItem, innerTypeName);
                            List<ParamType> subParam = bean.getParamTypes();
                            pt.setSubParams(subParam);
                        }
                        pt.setType(typeShow);
                        //fillAnnotationValue(parameters[i].getAnnotations(), pt);
                        inputParams.add(pt);
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            }
            // 对象类型需要解析class内部 field
            else {
                Bean bean = getBean(javaFileItem, type.asString());
                List<ParamType> subParam = bean.getParamTypes();
                pt.setSubParams(subParam);
                pt.setType("object");
                inputParams.add(pt);
            }
            i++;
        }

        if (inputParams.size() == 1) {
            ParamType first = inputParams.get(0);
            if (first.getSubParams() != null && first.getSubParams().size() > 0) {
                List<ParamType> subParam = inputParams.get(0).getSubParams();
                inputParams.clear();
                inputParams.addAll(subParam);
            }
        }

        /** -------------------------------------------------------------- */
        /** --------------------------- 返回值 ---------------------------- */
        /** -------------------------------------------------------------- */

        // 用于显示的类型
        String outputShowType = null;

        // 获取方法返回值类型
        String outputObjCls = null;

        String returnType = method.getReturnType();

        Bean returnBean = getBean(javaFileItem, returnType);
        outputObjCls = returnBean.getTypeName();
        outputShowType = returnBean.getTypeShowName();

        // output param
        Bean outputBean = getBean(javaFileItem, outputObjCls);

        /** -------------------------------------------------------------- */
        /** --------------------------- 生成 mock象 ----------------------- */
        /** -------------------------------------------------------------- */


        // 输入参数的 json mock数据
        Object inputMock = MockInputValue.generateMockValue(inputParams);

        Object outputMock = null;
        if (outputBean != null) {
            outputMock = MockInputValue.generateMockValue(outputBean.getParamTypes());
        }

        /** -------------------------------------------------------------- */
        /** --------------------------- 生成文档对象 ----------------------- */
        /** -------------------------------------------------------------- */

        DocItem doc = new DocItem();

        // 接口名称
        doc.setApiName(apiName);
        // 接口地址
        doc.setApiUrl(uri);
        // 接口描述
        if (StringUtils.isNotEmpty(apiDesc)) {
            doc.setApiDesc(apiDesc);
        }
        // 返回类型 void Lis<object> ..
        doc.setOutputType(outputShowType);
        // 输出对象参数列表
        if (outputBean != null) {
            doc.setOutputParams(outputBean.getParamTypes());
        }

        if (outputShowType.startsWith("array")) {
            OpenOutputObject responseBody = new OpenOutputObject(0, CommonUtil.asList(outputMock));
            doc.setOutputMock(JsonUtil.toJsonPretty(responseBody));
        } else {
            OpenOutputObject responseBody = new OpenOutputObject(0, outputMock);
            doc.setOutputMock(JsonUtil.toJsonPretty(responseBody));
        }

        // 输出对象描述
        doc.setOutputDesc(outputDesc);
        doc.setIsDeprecated(isDeprecated);
        doc.setGroup(group);
        doc.setSort(sort);
        doc.setSubSort(subSort);

        doc.setInputParams(inputParams);
        doc.setInputMock(JsonUtil.toJsonPretty(inputMock));
        // doc.setApiType(apiType);
        doc.setApiFrom(apiFrom);
        doc.setApiTo(apiTo);
        doc.setInputIsJson(method.getApplicationJsonRequest());

        doc.setApiCode(apiCode);
        doc.setApiClassName(apiClassName);

        doc.setAsync(async);

        return doc;

    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OpenOutputObject {
        private int code;
        private Object data;
    }

    private Map<String, Bean> beanCache = new HashMap<>();


    @Data
    @Accessors(chain = true)
    public static class Bean {
        private String typeName;
        private String typeShowName;
        private List<ParamType> paramTypes;
        private Boolean hasInnerClass = false;
        private String innerType;

        private Boolean isEnum = false;

        private String enumDesc;

    }

    public Bean getBean(JavaFileItem javaFileItem, String cls) {
        if (cls == null) {
            return null;
        }
        String typeName = cls;

        Bean bean = beanCache.get(typeName);
        if (bean != null) {
            return bean;
        }
        bean = new Bean();
        beanCache.put(typeName, bean);

        String typeShowName = typeName;
        String innerType = null;
        Boolean hasInnerClass = false;
        List<ParamType> paramTypes = new ArrayList<ParamType>();
        Boolean isEnum = false;
        String enumDesc = null;

        // 基础类型
        if (Ignore_Type.contains(typeName)) {
            typeShowName = typeName;
        }
        // void
        if (typeName.equals("void")) {
            typeShowName = "void";
        }
        // base type
        else if (Base_Type.contains(typeName)) {
            typeShowName = typeName.toLowerCase(Locale.ROOT);
        }

        else if (typeName.indexOf("Map")!=-1){
            typeShowName = "map";
        }
        // 集合类型
        else if (typeName.indexOf("<") != -1) {

            String innerTypeName = typeName.substring(typeName.indexOf("<") + 1, typeName.lastIndexOf(">"));

            // 基础类型集合
            if (Base_Type.contains(innerTypeName)) {
                typeShowName = "array<" + innerTypeName.toLowerCase() + ">";
            }
            // 集合类型
            else if (typeName.matches("List<.*?>") || typeName.matches("Set<.*?>")) {
                innerType = innerTypeName;
                //hasInnerClass = true;
                typeShowName = "array<object>";

                if(!"Object".equals(innerTypeName)){
                    Bean innerBean = getBean(javaFileItem, innerTypeName);
                    paramTypes = innerBean.getParamTypes();
                }

            }
            // 简单对象
            else {
                typeShowName = "array<object>";
            }

        } else if (typeName.endsWith("[]")) {
            typeShowName = "array<object>";
        }
        // 对象类型
        else {

            typeShowName = "object";
            innerType = typeName;

            // 文件路径
            String javaFilePath = findJavaFilePath(javaFileItem, cls);

            if (javaFilePath == null) {
                throw new RuntimeException(typeName + " not exist");
            }

            try {

                FileInputStream in = new FileInputStream(javaFilePath);

                SubParamClass subParamClass = new SubParamClass();

                JavaParser.parse(in).accept(new VoidVisitorAdapter<SubParamClass>() {

                    public void visit(final EnumDeclaration n, SubParamClass arg) {
//                        if (typeName.equals("UserStatusEnum")) {
//                            // log.debug("xxxx");
//                        }

                        List<String> descs = new ArrayList<>();
                        for (EnumConstantDeclaration entry : n.getEntries()) {
                            if(CommonUtil.isNotEmpty(entry.getArguments())){
                                String desc = entry.getArguments().get(0).toString().replaceAll("\"", "");
                                descs.add(entry.getName() + ":" + desc);
                            }
                        }
                        arg.setIsEnum(true);
                        arg.setEnumDesc(CommonUtil.join(descs, ", "));
                    }

                    public void visit(final ClassOrInterfaceDeclaration n, SubParamClass arg) {

//                        if (typeName.equals("UserStatusEnum")) {
//                            //log.debug("xxxx");
//                        }

                        arg.getFieldDeclarations().addAll(n.getFields());
                    }

                    public void visit(ClassOrInterfaceType n, SubParamClass arg) {
                        super.visit(n, arg);
                    }


                    public void visit(final PackageDeclaration n, SubParamClass arg) {

                        if (!javaFileItem.getImports().contains(n.getName().asString() + ".*")) {
                            javaFileItem.getImports().add(n.getName().asString() + ".*");
                        }
                    }

                    public void visit(final ImportDeclaration n, SubParamClass arg) {

                        String tmp = n.toString().trim();
                        tmp = tmp.substring("import".length()).trim();
                        tmp = tmp.replaceAll(";", "");

                        if (!javaFileItem.getImports().contains(tmp)) {
                            javaFileItem.getImports().add(tmp);
                        }

                    }

                }, subParamClass);

                if (subParamClass.getIsEnum()) {
                    isEnum = true;
                    enumDesc = subParamClass.getEnumDesc();
                } else {
                    for (FieldDeclaration field : subParamClass.getFieldDeclarations()) {

                        if (field.getModifiers().contains(Modifier.STATIC)) {
                            continue;
                        }

//                        @NotNull 不为空
//                        @Size 字符长度控制
//                        @Max 最大值控制
//                        @Min 最小值控制
//                        @Email  邮箱地址

                        ParamType pt = new ParamType();


                        for (AnnotationExpr annotation : field.getAnnotations()) {
                            // System.out.println(annotation);

                            String annotationName = annotation.getName().asString();

                            if (annotationName.equals("Valid")) {
                                continue;
                            }

                            Map<String, Object> annotationValues = new HashMap<>();
                            for (Node childNode : annotation.getChildNodes()) {
                                if (childNode instanceof Name) {
                                    continue;
                                }
                                if (childNode instanceof MemberValuePair) {
                                    MemberValuePair memberValuePair = (MemberValuePair) childNode;
                                    annotationValues.put(memberValuePair.getName().asString(), memberValuePair.getValue().toString());
                                } else {
                                    annotationValues.put(annotationName, childNode.toString());
                                }
//                                if(childNode instanceof FieldAccessExpr){
//                                    FieldAccessExpr accessExpr  = (FieldAccessExpr) childNode;
//                                    annotationValues.put(annotationName, accessExpr.toString());
//                                }
                                // IntegerLiteralExpr
                            }
                            // System.out.println(annotationName);

                            if ("NotNull".equals(annotationName)) {
                                pt.setNotNull(true);
                            }

                            if ("Size".equals(annotationName)) {
                                int maxSize = Convert.asInt(annotationValues.get("max"));
                                // pt.setLength(maxSize);
                            }

                            if ("Max".equals(annotationName)) {
                                int v = Convert.asInt(annotationValues.get("Max"));
                                pt.setMax(v);
                            }

                            if ("Min".equals(annotationName)) {
                                int v = Convert.asInt(annotationValues.get("Min"));
                                pt.setMin(v);
                            }

                            if ("NoDoc".equals(annotationName)){
                                pt.setNoDoc(true);
                            }

                        }


                        String fieldName = field.getVariables().get(0).getName().asString();
                        String fieldClassName = field.getElementType().asString();

                        String fieldComment = "";
                        Optional<Comment> comment = field.getComment();
                        if (comment.isPresent()) {
                            fieldComment = comment.get().getContent();
                            // fieldComment = fieldComment.replaceAll("\\s+", "");
                             fieldComment = fieldComment.replaceAll("\\*", "");
                        }

                        Bean subBean = getBean(javaFileItem, fieldClassName);


                        pt.setName(fieldName);
                        pt.setDescription(fieldComment);
                        pt.setType(subBean.getTypeShowName());
                        if (subBean.isEnum) {
                            pt.setType("string");
                            // pt.setDescription(subBean.getEnumDesc());
                        }

                        if (CommonUtil.isNotEmpty(subBean.getParamTypes())) {
                            pt.setSubParams(subBean.getParamTypes());
                        }

                        if(pt.getNoDoc()!=null && pt.getNoDoc()){
                            continue;
                        }

                        paramTypes.add(pt);
                    }

                }

//                }


            } catch (Exception e) {
                // JavaFileItem javaFileItem, JavaFileItem.JavaMethodItem method
                // log.error("文档扫描出错, class: {}, method:{}", javaFileItem.getFilePath() , method.g);
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }

        }

        bean.setTypeName(typeName);
        bean.setTypeShowName(typeShowName);
        bean.setInnerType(innerType);
        bean.setHasInnerClass(hasInnerClass);
        bean.setParamTypes(paramTypes);
        bean.setIsEnum(isEnum);
        bean.setEnumDesc(enumDesc);

        return bean;

    }


    private String findByName(String folder, String name) {

        File file = new File(folder);
        if (!file.exists()) {
            return null;
        }
        if (file.isFile()) {
            if (file.getName().equals(name)) {
                return file.getAbsolutePath();
            }
        } else {
            for (File listFile : file.listFiles()) {
                String tmp = findByName(listFile.getAbsolutePath(), name);
                if (tmp != null) {
                    return tmp;
                }
            }
        }

        return null;
    }

    public String findJavaFilePath(JavaFileItem javaFileItem, String paramType) {

        // rpc 当前目录
        {
            String path = javaFileItem.getFilePath().substring(0, javaFileItem.getFilePath().lastIndexOf("/") + 1) + paramType + ".java";
            if (new File(path).exists()) {
                return path;
            }
        }
        String clsName = null;
        {

            final String regex = ".*?\\." + paramType;
            List<String> matches = javaFileItem.getImports().stream().filter(e -> e.matches(regex)).collect(Collectors.toList());

            // 精确 import
            if (matches != null && matches.size() > 0) {
                String tmp = matches.get(0);

                String tmpPath = tmp.replaceAll("\\.", "/");
                for (String source : srcPaths) {

                    String targetFile = source + tmpPath + ".java";

                    if (new File(targetFile).exists()) {
                        return targetFile;
                    }
                }
            }
        }

        // 模糊 import
        if (clsName == null) {

            List<String> matches = javaFileItem.getImports().stream().filter(e -> e.endsWith("*")).collect(Collectors.toList());

            if (CommonUtil.isNotEmpty(matches)) {
                for (String match : matches) {

                    String tmpPath = match.replaceAll("\\*", "");
                    tmpPath = tmpPath.replaceAll("import", "");
                    tmpPath = tmpPath.replaceAll("\\.", "/");

                    for (String source : srcPaths) {

                        String folder = source + tmpPath;

                        String jfilePath = findByName(folder, paramType + ".java");

                        if (jfilePath != null) {
                            return jfilePath;
                        }
                    }
                }
            }
        }

        // 全路径搜索
        if (clsName == null) {
        }

        return null;
    }


}
