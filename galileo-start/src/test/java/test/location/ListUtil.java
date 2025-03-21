package test.location;

import org.armada.galileo.common.util.JsonUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xiaobo
 * @date 2021/12/7 6:03 下午
 */
public class ListUtil {


    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 13; i++) {
            list.add(i);
        }

        System.out.println(JsonUtil.toJsonPretty(split(list, 3)));

    }


    private static <E> List<List<E>> split(List<E> list, int size) {

        List<List<E>> result = new ArrayList<List<E>>();

        int start = 0;
        int limit = 0;
        while (true) {

            limit = start + size;

            if (limit >= list.size()) {
                limit = list.size();
            }

            List<E> sub = new ArrayList<>(list.subList(start, limit));

            result.add(sub);

            start += size;

            if (start >= list.size()) {
                break;
            }
        }

        return result;
    }

}
