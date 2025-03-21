package test.doctest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.armada.galileo.common.util.JsonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xiaobo
 * @date 2022/2/22 3:47 下午
 */
public class GroupTest {

    public static void main(String[] args) {

        List<AAAA> list = new ArrayList<>();
        list.add(new AAAA("a", "b", "c"));
        list.add(new AAAA("b", "b", "c"));
        list.add(new AAAA("c", "b", "c"));
        list.add(new AAAA("a", "b", "c"));

        Map<String, AAAA> group = list.stream().collect(Collectors.toMap(e -> e.getA(), e -> e));
        System.out.println(JsonUtil.toJson(group));

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AAAA {

        private String a;

        private String b;

        private String c;
    }

}
