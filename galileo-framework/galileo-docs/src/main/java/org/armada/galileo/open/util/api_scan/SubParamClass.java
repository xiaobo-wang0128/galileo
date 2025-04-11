package org.armada.galileo.open.util.api_scan;

import com.github.javaparser.ast.body.FieldDeclaration;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xiaobo
 * @date 2023/1/4 14:30
 */
@Data
@Accessors(chain = true)
public class SubParamClass {

    private Boolean isEnum = false;

    private String enumDesc;

    List<FieldDeclaration> fieldDeclarations;

    public SubParamClass(){
        this.fieldDeclarations = new ArrayList<>();
    }
}
