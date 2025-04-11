package org.armada.galileo.open.util;

import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.exception.BizException;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xiaobo
 * @date 2021/11/17 1:42 下午
 */
@Slf4j
public class JsExecutorUtil {

    static ScriptEngineManager manager = new ScriptEngineManager();

    static ScriptEngine engine = manager.getEngineByName("js");

    private static final String jsTempateContent = CommonUtil.readFileToString("sdk_execute_js_template.js");

    public static void main(String[] args) {

        String funcValue = "return {\"aa\": input.d};";

        Map<String, Object> input = new HashMap<>();
        input.put("abc", "ee");
        input.put("d", 11);

        String output = executeJs(JsonUtil.toJson(input), funcValue);

        System.out.println(output);
    }

    public synchronized static String executeJs(String inputJson, String jsInput) {

        String jsContent = new String(jsTempateContent);

        jsContent = CommonUtil.replaceAll(jsContent, "#input#", inputJson);
        jsContent = CommonUtil.replaceAll(jsContent, "#function_body#", jsInput);

        String output = null;
        try {
            output = executeJsTemplate(jsContent);
        } catch (Exception e) {
            throw new BizException(e);
        }
        return output;
    }

    private synchronized static String executeJsTemplate(String jsContent) {
        try {
            engine.eval(jsContent);
            if (engine instanceof Invocable) {
                Invocable invocable = (Invocable) engine;
                JsExecuteInterface executeMethod = invocable.getInterface(JsExecuteInterface.class);
                return executeMethod.execute();
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            throw new BizException(e);
        }
        return null;
    }

    public interface JsExecuteInterface {
        public String execute();
    }

}
