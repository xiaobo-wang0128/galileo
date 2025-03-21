package test.code_check;

import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.open.dal.entity.OpenRequestMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author xiaobo
 * @date 2022/5/20 00:01
 */
public class GroupSortIndexTest {

    public static void main(String[] args) {

        List<String> groups = CommonUtil.asList("a", "b", "c", "d", "e");


        List<OpenRequestMessage> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {

            OpenRequestMessage msg = new OpenRequestMessage();
            msg.setMsgGroup(groups.get(i % groups.size()));
            msg.setRequestId(UUID.randomUUID().toString());

            list.add(msg);
        }

        List<String> groupsNames = list.stream().filter(e -> CommonUtil.isNotEmpty(e.getMsgGroup())).map(e -> e.getMsgGroup()).distinct().collect(Collectors.toList());

        System.out.println(JsonUtil.toJson(groupsNames));

    }
}
