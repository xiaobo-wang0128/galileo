package org.armada.galileo.open.util.ftp;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.UUID;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FtpConnection {

    private static Logger log = LoggerFactory.getLogger(FtpConnection.class);

    private FTPClient ftpClient;

    private String ftpUsername;
    private String ftpPassword;
    private String ftpHost;
    private int ftpPort;
    private int timeout = 0;
    /**
     * 连接id
     */
    private String connectionId;

    public FtpConnection(String ftpUsername, String ftpPassword, String ftpHost, int ftpPort, int timeout) {
        this.ftpUsername = ftpUsername;
        this.ftpPassword = ftpPassword;
        this.ftpHost = ftpHost;
        this.ftpPort = ftpPort;
        this.connectionId = UUID.randomUUID().toString();
        this.timeout = timeout;

        this.ftpClient = new FTPClient();

        ftpClient.setControlEncoding("utf-8");
        ftpClient.setRemoteVerificationEnabled(false);

    }

    public void login() {

        try {
            if (timeout > 0) {
                ftpClient.setConnectTimeout(timeout);
                ftpClient.setDataTimeout(timeout);
            } else {
                ftpClient.setConnectTimeout(5000);
            }

            ftpClient.connect(ftpHost, ftpPort); // 连接ftp服务器
            ftpClient.login(ftpUsername, ftpPassword); // 登录ftp服务器

            ftpClient.setAutodetectUTF8(true);
            ftpClient.setControlEncoding("UTF-8");
            ftpClient.setCharset(Charset.forName("UTF-8"));

            // 设置文件类型（二进制） 
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            // 每次数据连接之前，ftp client告诉ftp server开通一个端口来传输数据
            ftpClient.enterLocalPassiveMode();

            ftpClient.setBufferSize(1000000);
            ftpClient.setSendBufferSize(1000000);
            ftpClient.setSendDataSocketBufferSize(1000000);
            ftpClient.setReceiveBufferSize(1000000);
            ftpClient.setReceieveDataSocketBufferSize(1000000);

            int replyCode = ftpClient.getReplyCode(); // 是否成功登录服务器
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                throw new RuntimeException("ftp login fail: " + this.getConnectionString());
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("ftp连接失败：" + this.getConnectionStringHidden() + ", error: " + e.getMessage());
        }
    }

    public String getConnectionId() {
        return connectionId;
    }

    public FTPFile[] listFile(String path) {
        try {
            return ftpClient.listFiles(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setShortTimeout() {
        this.ftpClient.setDataTimeout(5000);
        this.ftpClient.setConnectTimeout(5000);
    }

    public void resetTimeout() {
        this.ftpClient.setDataTimeout(-1);
        this.ftpClient.setConnectTimeout(0);
    }

    public List<String> ls(String path) {
        try {
            // ftpClient.enterLocalPassiveMode();

            String[] folders = ftpClient.listNames(path);
            if (folders == null || folders.length == 0) {
                return null;
            }

            return CommonUtil.asList(folders);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 该方法有可能长时间阻塞
    public void logout() {
        try {
            ftpClient.disconnect();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        ftpClient = null;

//        new Thread() {
//            public void run() {
//                try {
//                    ftpClient.abort();
//                } catch (Exception e) {
//                }
//                try {
//                    ftpClient.logout();
//                } catch (IOException e) {
//                }
//
//                try{
//                    ftpClient.disconnect();
//                }
//                catch(Exception e){
//                    log.error(e.getMessage(), e);
//                }
//
//                // ftpClient.quit();
//                // ftpClient = null;
//            }
//        }.start();
    }

    public boolean getIsConnected() {
        if (ftpClient == null) {
            return false;
        }
        return ftpClient.isConnected();
    }

    public String getConnectionString() {
        return "[ftpHost=" + ftpHost + ", ftpPort=" + ftpPort + ", ftpUsername=" + ftpUsername + ", ftpPassword=" + ftpPassword + "]";
    }

    public String getConnectionStringHidden() {
        return "[ftpHost=" + ftpHost + ", ftpPort=" + ftpPort + ", ftpUsername=" + ftpUsername + ", ftpPassword=******]";
    }

    public void upload(String path, byte[] data) throws IOException {
        // ftpClient.enterLocalPassiveMode();

        ByteArrayInputStream bis = new ByteArrayInputStream(data);

        boolean boo = ftpClient.storeFile(path, bis);
        if (!boo) {
            throw new BizException("上传失败");
        }
        bis.close();
    }

    public void rename(String oldpath, String newpath) throws IOException {
        ftpClient.rename(oldpath, newpath);
    }

    public void rm(String path) {
        try {
            // ftpClient.enterLocalPassiveMode();
            ftpClient.dele(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] downloadToBytes(String path) {
        try {
            // ftpClient.enterLocalPassiveMode();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            ftpClient.retrieveFile(path, bos);

            bos.close();

            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setTimeout(int timeout) {
        ftpClient.setDefaultTimeout(timeout);
    }

    public void reconnect() throws SocketException, IOException {
        try {
            logout();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        this.ftpClient = new FTPClient();
        login();
    }

    public void mkdirs(String path) {
        try {
            ftpClient.makeDirectory(path);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}