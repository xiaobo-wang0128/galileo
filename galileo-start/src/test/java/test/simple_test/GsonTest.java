package test.simple_test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.armada.galileo.common.util.JsonUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xiaobo
 * @date 2021/12/22 12:50 下午
 */
public class GsonTest {

    public static void main(String[] args) {

        Map<String, Object> aaa = new HashMap<>(
        );

        aaa.put("a", 3333.33D);
        aaa.put("b", 3333.32F);
        aaa.put("c", 3333);

        String json = JsonUtil.toJson(aaa);

        Test t = JsonUtil.fromJson(json, Test.class );

        System.out.println(t);

    }


    @Data
    public static class Test {
        Double a;
        Float b;
        Integer c;

        @Override
        public String toString() {
            return "Test{" +
                    "a=" + a +
                    ", b=" + b +
                    ", c=" + c +
                    '}';
        }
    }

    public static void main2222(String[] args) {

        List<AlgoOrderResult> out = new ArrayList<>();
        for (int m = 1; m < 4; m++) {
            List<AlgoOrderResult.AlgoDetailResult> list2 = new ArrayList<>();

            for (int i = 1; i < 4; i++) {
                AlgoOrderResult.AlgoDetailResult algoDetailResult = new AlgoOrderResult.AlgoDetailResult();
                algoDetailResult.setAlgoDetailId((long) (m * 100 + i));

                List<AlgoOrderResult.PickTaskItem> list = new ArrayList<>();
                for (int k = 1; k < 4; k++) {
                    AlgoOrderResult.PickTaskItem pickTaskItem = new AlgoOrderResult.PickTaskItem();
                    pickTaskItem.setSkuId(((long) (m * 100 + i * 10 + k)));
                    list.add(pickTaskItem);
                }
                algoDetailResult.setPickTaskItems(list);

                list2.add(algoDetailResult);
            }

            AlgoOrderResult result = new AlgoOrderResult();
            result.setAlgoDetailResults(list2);
            out.add(result);
        }

        // System.out.println(JsonUtil.toJsonPretty(out));

        List<Long> skuIds = new ArrayList<>();
        out.stream()
                .forEach(a -> a.getAlgoDetailResults().stream()
                        .forEach(b -> b.getPickTaskItems().stream()
                                .forEach(c -> skuIds.add(c.getSkuId()))));

        Collections.sort(skuIds);

        System.out.println(JsonUtil.toJsonPretty(skuIds));
    }

    public static void main22(String[] args) {

        CalssA c = new CalssA(OutboundOrderStatusEnum.ASSIGNED, "aa", "bb");

        System.out.println(JsonUtil.toJsonPretty(c));
    }

    @AllArgsConstructor
    @Data
    static class CalssA {

        OutboundOrderStatusEnum status;

        String aa;

        String bb;


    }
}

@Getter
@AllArgsConstructor
enum OutboundOrderStatusEnum {

    NEW("新单据"),

    // todo pengboran 这个状态没用到，应该要被去掉。
    ASSIGNED("分配完成(库区)"),

    DISPATCHED("派单完成"),

    PICKING("拣货中"),

    PICKED("拣货完成"),

    SHORT_WAITING("缺货等待"),

    CANCELED("已取消");

    private final String descCN;

    private final String label = "出库订单状态";
}
