package test.flow_link_test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.exception.BizException;
import org.armada.galileo.nova_flow.exception.FlowException;

import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

/**
 * @author xiaobo
 * @date 2021/12/1 2:43 下午
 */
public class TestClose {


    public static void main(String[] args) {

        String startStatus = "start";

        List<String> endStatus = CommonUtil.asList("end1", "end2");

        List<Node> allNodes = new ArrayList<>();
        allNodes.add(new Node("start", "s1"));
        allNodes.add(new Node("s2", "s3"));
        allNodes.add(new Node("s1", "s2"));
        allNodes.add(new Node("s3", "s4"));
        allNodes.add(new Node("s4", "s5"));

        allNodes.add(new Node("s2", "s1"));
        allNodes.add(new Node("s2", "s4"));

        allNodes.add(new Node("s2", "end2"));

        allNodes.add(new Node("s5", "end1"));

        // ----

        allNodes.add(new Node("start", "t1"));
        allNodes.add(new Node("t1", "t2"));
        allNodes.add(new Node("t2", "t3"));
        allNodes.add(new Node("t3", "t4"));
        allNodes.add(new Node("t4", "t5"));
        allNodes.add(new Node("t5", "t6"));
        allNodes.add(new Node("t5", "t6"));
        allNodes.add(new Node("t6", "t7"));
        allNodes.add(new Node("t7", "end2"));

        // ----

        allNodes.add(new Node("start", "e1"));
        allNodes.add(new Node("e1", "e2"));
        allNodes.add(new Node("e2", "e3"));
        allNodes.add(new Node("e3", "e4"));
        allNodes.add(new Node("e4", "end2"));


        checkLinkClose(allNodes, startStatus, endStatus);
    }


    private static void checkLinkClose(List<Node> allNodes, String startStatus, List<String> endStatus) {


        // 去重
        allNodes = allNodes.stream().distinct().filter(e -> !e.pre.equals(e.next)).collect(Collectors.toList());


        BlockingDeque<List<Node>> links = new LinkedBlockingDeque<>();

        for (Iterator<Node> it = allNodes.iterator(); it.hasNext(); ) {
            Node node = it.next();

            if (startStatus.equals(node.getPre())) {
                List<Node> link = new ArrayList<>();

                link.add(node);
                links.add(link);

                it.remove();
            }
        }


        List<List<Node>> allExistLinks = new ArrayList<>();


        while (true) {

            List<Node> link = links.poll();

            if (link == null) {
                break;
            }

            List<Node> snapshot = new ArrayList<>(allNodes);

            while (true) {

                int before = snapshot.size();

                tmp:
                for (Iterator<Node> it = snapshot.iterator(); it.hasNext(); ) {

                    Node n = it.next();

                    try {
                        for (Node tmpNode : link) {
                            if (n.equals(tmpNode)) {
                                it.remove();
                                continue tmp;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    List<String> pres = link.stream().map(e -> e.pre).collect(Collectors.toList());

                    if (n.getPre().equals(link.get(link.size() - 1).next)) {
                        if (!pres.contains(n.next)) {   // || !pres.contains(n.pre)
                            link.add(n);
                        }
                        try {
                            it.remove();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        continue;
                    }
                }


                int after = snapshot.size();

                if (before != after) {
                    continue;
                }
                if (before == after) {

                    List<String> ss = link.stream().map(e -> e.pre).collect(Collectors.toList());
                    ss.add(link.get(link.size() - 1).next);

                    if (!endStatus.contains(link.get(link.size() - 1).next)) {
                        throw new FlowException("流程未闭环：" + CommonUtil.join(ss, " -> "));
                        //System.out.println("流程未闭环：" + CommonUtil.join(ss, " -> "));
                    } else {
                        System.out.println("流程：" + CommonUtil.join(ss, " -> "));
                        allExistLinks.add(link);
                    }

                    if (snapshot.size() > 0) {

                        List<List<Node>> ntmpLinks = new ArrayList<>();

                        //System.out.println("snapshot: " + JsonUtil.toJson(snapshot));

                        for (List<Node> tmpLink : allExistLinks) {
                            List<String> tmpSlist = tmpLink.stream().map(e -> e.next).collect(Collectors.toList());

                            for (Iterator<Node> tmpIt = snapshot.iterator(); tmpIt.hasNext(); ) {
                                Node n = tmpIt.next();
                                if (tmpSlist.contains(n.pre) && tmpSlist.contains(n.next)) {
                                    tmpIt.remove();
                                    continue;
                                }
                            }
                        }

                        for (List<Node> tmpLink : links) {
                            List<String> tmpSlist = tmpLink.stream().map(e -> e.next).collect(Collectors.toList());

                            for (Iterator<Node> tmpIt = snapshot.iterator(); tmpIt.hasNext(); ) {
                                Node n = tmpIt.next();
                                if (tmpSlist.contains(n.pre) && tmpSlist.contains(n.next)) {
                                    tmpIt.remove();
                                    continue;
                                }
                            }
                        }

                        for (Iterator<Node> tmpIt = snapshot.iterator(); tmpIt.hasNext(); ) {

                            Node n = tmpIt.next();


                            for (int i = link.size() - 1; i >= 0; i--) {

                                if (n.pre.equals(link.get(i).next)) {

                                    List<Node> tmpNewLink = new ArrayList<>(link.subList(0, i + 1));
                                    tmpNewLink.add(n);

                                    if (endStatus.contains(tmpNewLink.get(tmpNewLink.size() - 1).next)) {
                                        ntmpLinks.add(tmpNewLink);
                                    } else {
                                        links.push(tmpNewLink);
                                    }

                                    //System.out.println(links.size());
                                }
                            }
                        }

                        if (ntmpLinks != null && ntmpLinks.size() > 0) {
                            allExistLinks.addAll(ntmpLinks);
                        }
                    }


                    // System.out.println(JsonUtil.toJson(snapshot));
                    //System.out.println("links: " + JsonUtil.toJson(links));

                    break;
                }

            }

        }


        System.out.println("done: ");
        for (List<Node> link : allExistLinks) {

            List<String> ss = link.stream().map(e -> e.pre).collect(Collectors.toList());
            ss.add(link.get(link.size() - 1).next);
            System.out.println(CommonUtil.join(ss, "->"));
        }
    }


    @Data
    private static class Node {
        public String pre;
        public String next;

        public Node(String pre, String next) {
            this.pre = pre;
            this.next = next;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return Objects.equals(pre, node.pre) && Objects.equals(next, node.next);
        }

        public String toString() {
            return pre + ">" + next;
        }
    }


}
