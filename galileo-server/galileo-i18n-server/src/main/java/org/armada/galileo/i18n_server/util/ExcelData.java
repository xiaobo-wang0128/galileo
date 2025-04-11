package org.armada.galileo.i18n_server.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.armada.galileo.common.util.CommonUtil;

import java.util.List;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
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

}
