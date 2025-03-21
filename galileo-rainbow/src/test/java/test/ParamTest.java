package test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * @author xiaobo
 * @date 2023/2/23 20:00
 */
public class ParamTest {

    public void methodTest(byte[] param1, String app) {

    }


    public static void main(String[] args) {

        for (Method method : ParamTest.class.getMethods()) {

            if (!method.getName().equals("methodTest")) {
                continue;
            }

            for (TypeVariable<Method> typeParameter : method.getTypeParameters()) {
                System.out.println(typeParameter);
            }

            for (Type parameterType : method.getGenericParameterTypes()) {
                System.out.println(parameterType);
            }
        }

    }

}
