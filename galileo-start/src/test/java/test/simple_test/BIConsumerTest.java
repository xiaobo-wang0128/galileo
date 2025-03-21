package test.simple_test;

import java.util.function.Consumer;

/**
 * @author xiaobo
 * @date 2022/3/3 4:47 下午
 */
public class BIConsumerTest {

    public static void main(String[] args) {

        Consumer<Integer> consumer = x -> {
            System.out.println(x);
        };


        int i = 10;
        consumer.accept(i++);
        System.out.println(i);

        consumer.accept(i++);
        System.out.println(i);

        consumer.accept(i++);
        System.out.println(i);

        consumer.accept(i++);
        System.out.println(i);

        consumer.accept(i++);
        System.out.println(i);


    }

}
