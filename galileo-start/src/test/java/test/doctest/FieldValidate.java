package test.doctest;

import org.armada.spi.param.base.UserCreateParam;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * @author xiaobo
 * @date 2022/3/8 5:20 PM
 */

public class FieldValidate {

    public static void main(String[] args) {

        Field[] fields = UserCreateParam.class.getDeclaredFields();

        for (Field field : fields) {

            Annotation[] annos = field.getAnnotations();
            if (annos == null || annos.length == 0) {
                continue;
            }

            for (Annotation annotation : annos) {
                String value = null;
                try {
                    value = annotation.getClass().getMethod("notNull").invoke(annotation).toString();
                } catch (Exception e) {
                }
                System.out.println(value);
            }

        }

    }


}
