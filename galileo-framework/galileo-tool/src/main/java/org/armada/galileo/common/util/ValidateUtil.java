package org.armada.galileo.common.util;

import lombok.SneakyThrows;
import org.armada.galileo.exception.BizException;
import org.springframework.util.CollectionUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author laiyanbin
 * @date 2022/12/30 11:37
 * @description ValidateUtil
 */
public class ValidateUtil {

    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    /**
     * 校验实体类
     */
    public static void validate(Object t) {
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(t);
        if (constraintViolations.size() > 0) {
            List<String> error = new ArrayList<>();
            for (ConstraintViolation<Object> constraintViolation : constraintViolations) {
                error.add(constraintViolation.getMessage());
            }
            throw new BizException(CommonUtil.join(error, "; "));
        }
        return;
    }

    @SneakyThrows
    public static String validateUnique(Object data, Map<String, Map<String, Object>> fieldUniqueMap, Map<String, HashSet> validateUniqueMap) {
        if (CollectionUtils.isEmpty(fieldUniqueMap) || CollectionUtils.isEmpty(validateUniqueMap)) {
            return null;
        }
        List<String> errorList = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> fieldEntry : fieldUniqueMap.entrySet()) {
            Field field = (Field) fieldEntry.getValue().get("field");
            String cellName = (String) fieldEntry.getValue().get("name");
            String fieldName = fieldEntry.getKey();
            field.setAccessible(true);
            Object value = field.get(data);
            if (!validateUniqueMap.get(fieldName).add(value)) {
                errorList.add(cellName + " is duplicate");
            }
        }
        if (!CollectionUtils.isEmpty(errorList)) {
            return errorList.stream().collect(Collectors.joining(";"));
        }

        return null;
    }


}
