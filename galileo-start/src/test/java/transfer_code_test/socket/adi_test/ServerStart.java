package transfer_code_test.socket.adi_test;

/**
 * @author xiaobo
 * @date 2022/3/25 8:38 PM
 */
public class ServerStart {

    public static void main(String[] args) {

        CustomerToHaiqServerThread t = new CustomerToHaiqServerThread();
        t.run();

    }
}
