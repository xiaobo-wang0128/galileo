package post_test;

import lombok.Data;
import lombok.experimental.Accessors;
import org.armada.galileo.common.util.CommonUtil;

import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author xiaobo
 * @date 2022/11/23 14:10
 */
public class SendExcelEmail {


    static String userName = "wxb0128@163.com";

    static String passWord = "IGAXFWBONBEKYQGO";

//    static String receiver = "263060052@qq.com";
//    static List<String> receivers = CommonUtil.asList(
//            "wxb0128@163.com"
//    );


    static String receiver = "lichanglong@imlb2c.com";

    static List<String> receivers = CommonUtil.asList(
            "wangyali@imlb2c.com",
            "chenren@imlb2c.com",
            "lihongrong@imlb2c.com",
            "263060052@qq.com");

    public static void sendEmail(List<String> paths, String day, String startTime, String endTime) {
        while (true) {
            try {
                MessageBody body = new MessageBody();
                body.setFileName("inventory_" + CommonUtil.format(new Date(), "yyyy-MM-dd") + ".zip")
                        .setSubject("库存导出 " + day)
                        .setContent("库存导出，执行时间: " + startTime + " - " + endTime)
                        .setFileContent(generateZip(paths));

                sendEmail(body);
            } catch (Exception e) {
                e.printStackTrace();

                try {
                    Thread.sleep(5000);
                } catch (Exception ex) {
                }
                continue;
            }
            break;
        }
    }

    public static void main(String[] args) throws Exception {

        List<String> paths = CommonUtil.asList("/Users/wangxiaobo/excel_stock_export/2022-11-23-1.xlsx",
                "/Users/wangxiaobo/excel_stock_export/2022-11-23-2.xlsx",
                "/Users/wangxiaobo/excel_stock_export/2022-11-23-3.xlsx",
                "/Users/wangxiaobo/excel_stock_export/2022-11-23-4.xlsx",
                "/Users/wangxiaobo/excel_stock_export/2022-11-23-5.xlsx",
                "/Users/wangxiaobo/excel_stock_export/2022-11-23-6.xlsx",
                "/Users/wangxiaobo/excel_stock_export/2022-11-23-7.xlsx"

        );

        sendEmail(paths, CommonUtil.format(new Date(), "yyyy-MM-dd"), "1", "2");


//        MessageBody body = new MessageBody();
//        body.setFileName("export_" + CommonUtil.format(new Date(), "yyyy-MM-dd_HHmmss") + ".zip")
//                .setSubject("库存导出 " + CommonUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"))
//                .setContent("库存导出， 执行时间 222 - 3333")
//                .setFileContent(generateZip(paths));
//
//        sendEmail(body);
    }

    @Data
    @Accessors(chain = true)
    public static class MessageBody {
        private String subject;
        private String content;
        private String fileName;
        private byte[] fileContent;
    }

    public static Session creatSession() {

        String smtp = "smtp.163.com";

        // SMTP服务器连接信息
        Properties properties = new Properties();
        properties.put("mail.smtp.host", smtp);// SMTP主机名
        properties.put("mail.smtp.port", "25");// 主机端口号
        properties.put("mail.smtp.auth", "true");// 是否需要用户认证
        properties.put("mail.smtp.starttls.enable", "false");// 启用TLS加密

        // 创建Session
        // 参数1：SMTP服务器的连接信息
        // 参数2：用户认证对象（Authenticator接口的匿名实现类）
        Session session = Session.getInstance(properties, new Authenticator() {
            protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                return new javax.mail.PasswordAuthentication(userName, passWord);
            }

        });

        // 开启调试模式
        session.setDebug(true);

        return session;
    }

    public static void sendEmail(MessageBody messageBody) throws Exception {
        //用try-catch块来解决程序运行时可能会抛出的（Address，Message）异常
        try {

            Session session = creatSession();
            //创建Message对象
            MimeMessage message = new MimeMessage(session);

            // 设置发送方地址:
            message.setFrom(new InternetAddress(userName));

            // 设置接收方地址:
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(receiver));
            //通过setRecipients中的抄写者。来实现同时发送给多个人
            //把网络邮件地址设置成一个集合，存放多个邮箱地址
            InternetAddress[] ccs = new InternetAddress[receivers.size()];
            for (int i = 0; i < receivers.size(); i++) {
                ccs[i] = new InternetAddress(receivers.get(i));
            }
            message.setRecipients(Message.RecipientType.CC, ccs);

            // 设置邮件主题:
            //使用utf-8的编码格式
            message.setSubject(messageBody.getSubject(), "UTF-8");

            // 设置邮件正文:
            // message.setText("Hi Xiaoming...", "UTF-8");

            // 创建Multipart复合对象
            Multipart multipart = new MimeMultipart();

            // 添加text:
            BodyPart textpart = new MimeBodyPart();
            textpart.setContent(messageBody.getContent(), "text/html;charset=utf-8");
            multipart.addBodyPart(textpart);

            // 添加image:
            BodyPart imagepart = new MimeBodyPart();
            imagepart.setFileName(messageBody.getFileName());
            imagepart.setDataHandler(new DataHandler(new ByteArrayDataSource(messageBody.getFileContent(), "application/octet-stream")));
            multipart.addBodyPart(imagepart);

            // 设置邮件内容为multipart:
            message.setContent(multipart);

            // 发送:
            Transport.send(message);

        } catch (AddressException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }


    public static byte[] generateZip(List<String> filePaths) throws Exception {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        // 创建zip输出流
        ZipOutputStream out = new ZipOutputStream(bos, StandardCharsets.UTF_8);
        for (String filePath : filePaths) {

            String zipTitle = filePath.substring(filePath.lastIndexOf("/"));
            byte[] bytes = CommonUtil.readFileFromLocal(filePath);
            out.putNextEntry(new ZipEntry(zipTitle));
            out.write(bytes);
            out.closeEntry();
        }
        out.flush();
        out.close();

        return bos.toByteArray();
    }
}
