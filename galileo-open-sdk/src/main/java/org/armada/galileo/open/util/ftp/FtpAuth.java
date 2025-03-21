package org.armada.galileo.open.util.ftp;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author xiaobo
 * @date 2022/1/10 5:47 下午
 */

@Data
@Accessors(chain = true)
public class FtpAuth implements Serializable {

    private String ftpHost;

    private int ftpPort;

    private String ftpUsername;

    private String ftpPassword;

    public FtpAuth() {
    }

    public FtpAuth(String ftpHost, int ftpPort, String ftpUsername, String ftpPassword) {
        this.ftpHost = ftpHost;
        this.ftpPort = ftpPort;
        this.ftpUsername = ftpUsername;
        this.ftpPassword = ftpPassword;
    }

    public String getConnectionString() {
        return "[host=" + ftpHost + ", port=" + ftpPort + ", uname=" + ftpUsername + ", password=" + ftpPassword + "]";
    }

    public String getConnectionHidden() {
        return "[host=" + ftpHost + ", port=" + ftpPort + ", uname=***, password=*** ]";
    }


}