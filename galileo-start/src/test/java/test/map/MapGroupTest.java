package test.map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import org.armada.galileo.common.util.JsonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xiaobo
 * @date 2021/12/25 6:01 下午
 */
public class MapGroupTest {


    @Data
    @Accessors(chain = true)
    public static class TaskGroup {

        protected String containerCode;

        protected String stationCode;

        @Override
        public String toString() {
            return "TaskGroup{" +
                    "containerCode='" + containerCode + '\'' +
                    ", stationCode='" + stationCode + '\'' +
                    '}';
        }
    }


    @Data
    @Accessors(chain = true)
    public static class Task extends TaskGroup {

        public String f;

    }

    public static void main(String[] args) {

        List<Task> taskList = new ArrayList<>();

        for (int i = 0; i < 20; i++) {

            String containerCode = "" + ((int) (Math.random() * 3));

            String stationCode = "" + ((int) (Math.random() * 3));

            Task task = new Task();
            task.setContainerCode(containerCode);
            task.setStationCode(stationCode);
            task.setF(i + "");

            taskList.add(task);
        }


        Map<TaskGroup, List<Task>> taskGroup = taskList.stream().collect(Collectors.groupingBy(e -> {
            TaskGroup tg = new TaskGroup();
            tg.setContainerCode(e.getContainerCode());
            tg.setStationCode(e.getStationCode());
            return tg;
        }));


        System.out.println(JsonUtil.toJsonPretty(taskGroup));
    }
}


