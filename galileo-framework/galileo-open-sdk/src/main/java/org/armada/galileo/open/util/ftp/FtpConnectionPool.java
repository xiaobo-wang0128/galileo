package org.armada.galileo.open.util.ftp;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ftp连接池
 *
 * @author xiaobowang 2019年7月12日
 */
public class FtpConnectionPool {

    private static Logger log = LoggerFactory.getLogger(FtpConnectionPool.class);

    static long checkExpireTime = 10000;

    // 客户端定时检测
    static {
        Runnable r = new Runnable() {
            public void run() {

                while (true) {

                    try {

                        Thread.sleep(checkExpireTime);

                        for (Map.Entry<String, LinkedList<FtpConnection>> entry : cachedConnection.entrySet()) {

                            LinkedList<FtpConnection> list = entry.getValue();

                            int size = list.size();

                            for (int i = 0; i < size; i++) {
                                FtpConnection conn = list.poll();
                                if (conn == null) {
                                    break;
                                }

                                String connId = conn.getConnectionId();
                                Long time = connectionLastAccessTime.get(connId);
                                if (time == null) {
                                    time = System.currentTimeMillis();
                                    connectionLastAccessTime.put(connId, time);
                                }

                                if (System.currentTimeMillis() - time > checkExpireTime) {

                                    if (!conn.getIsConnected()) {
                                        conn = null;
                                        continue;
                                    }
                                    try {
                                        conn.ls("/");
                                    } catch (Exception e) {
                                        log.error(e.getMessage(), e);
                                        try {
                                            conn.logout();
                                            conn = null;
                                        } catch (Exception e2) {
                                            log.error(e2.getMessage(), e2);
                                        }
                                        continue;
                                    }
                                }

                                list.add(conn);

                            }
                        }

                    } catch (InterruptedException e) {
                        break;
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        };
        new Thread(r, "rainbow-ftp-status-check").start();

    }

    /**
     * 连接池缓存
     */
    private static Map<String, LinkedList<FtpConnection>> cachedConnection = new ConcurrentHashMap<String, LinkedList<FtpConnection>>();

    /**
     * 缓存每个连接上次访问的时间
     */
    private static Map<String, Long> connectionLastAccessTime = new ConcurrentHashMap<String, Long>();

    /**
     * 当前并发请求数
     */
    // private static AtomicInteger concurrentNums = new AtomicInteger(0);

    /**
     * 最大并发请求数
     */
    private static Integer MaxConcurrentNums = 30;

    /**
     * 最大缓冲连接数
     */
    private static Integer MaxCacheConnectionNums = 3;


    /**
     * 创建一个ftp连接， 连接数超出时会报错
     *
     * @param ftpAuth
     * @param timeout 超时时间
     * @return
     */
    public static FtpConnection getResource(FtpAuth ftpAuth, int timeout) {
        return getResource(ftpAuth, true, timeout);
    }

    /**
     * 创建一个ftp连接
     *
     * @param ftpAuth
     * @return
     */
    public static FtpConnection getResource(FtpAuth ftpAuth) {
        return getResource(ftpAuth, true, -1);
    }

//    /**
//     * 创建一个 ftp 连接
//     *
//     * @param ftpAuth
//     * @param checkLimit 是否校验当前连接数上限
//     * @return
//     */
//    private static FtpConnection getResource(FtpAuth ftpAuth, boolean checkLimit) {
//        return getResource(ftpAuth, checkLimit, -1);
//    }

    /**
     * 获取一个连接
     *
     * @param ftpAuth
     * @param checkLimit 是否校验缓存池大小
     * @return
     */
    private static FtpConnection getResource(FtpAuth ftpAuth, boolean checkLimit, int timeout) {

        LinkedList<FtpConnection> link = cachedConnection.get(ftpAuth.getConnectionString());
        if (link == null) {
            synchronized (log) {
                if (link == null) {
                    link = new LinkedList<>();
                    cachedConnection.put(ftpAuth.getConnectionString(), link);
                }
            }
        }

        FtpConnection conn = link.poll();
        if (conn == null || !conn.getIsConnected()) {
            conn = new FtpConnection(ftpAuth.getFtpUsername(), ftpAuth.getFtpPassword(), ftpAuth.getFtpHost(), ftpAuth.getFtpPort(), timeout);
            conn.login();
        }

        connectionLastAccessTime.put(conn.getConnectionId(), System.currentTimeMillis());

        return conn;
    }

    /**
     * 回收一个连接
     *
     * @param conn
     */
    public static void returnResource(FtpConnection conn) {
        if (conn == null) {
            return;
        }

        if (!conn.getIsConnected()) {
            conn = null;
            return;
        }

        LinkedList<FtpConnection> link = cachedConnection.get(conn.getConnectionString());
        if (link == null) {
            synchronized (log) {
                if (link == null) {
                    link = new LinkedList<>();
                    cachedConnection.put(conn.getConnectionString(), link);
                }
            }
        }

        if (link.size() < MaxCacheConnectionNums) {
            link.add(conn);
        } else {

            connectionLastAccessTime.remove(conn.getConnectionId());

            try {
                conn.logout();
            } catch (Exception e) {
            }
            conn = null;
        }

        // } finally {
        // // concurrentNums.decrementAndGet();
        // }

    }

    public static void clearPool(FtpAuth ftpAuth) {
        if (ftpAuth == null) {
            return;
        }
        LinkedList<FtpConnection> link = cachedConnection.get(ftpAuth.getConnectionString());
        if (link != null && link.size() > 0) {
            for (FtpConnection sftpConnection : link) {
                try {
                    sftpConnection.logout();
                    sftpConnection = null;
                } catch (Exception e) {
                }
            }
        }

        link = null;
        cachedConnection.remove(ftpAuth.getConnectionString());

    }
}