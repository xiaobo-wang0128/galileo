package org.armada.galileo.rainbow_gate.transfer.discovery.zk;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.rainbow_gate.transfer.discovery.domain.AppServiceDomain;
import org.armada.galileo.rainbow_gate.transfer.discovery.domain.AppServiceDomain.Customer;
import org.armada.galileo.rainbow_gate.transfer.discovery.domain.AppServiceDomain.CustomerItem;
import org.armada.galileo.rainbow_gate.transfer.discovery.domain.AppServiceDomain.Provider;
import org.armada.galileo.rainbow_gate.transfer.domain.data.AppDeploy;
import org.armada.galileo.rainbow_gate.transfer.gate_point.app_client.AppClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

public class ZkCLient {


    private static Logger log = LoggerFactory.getLogger(ZkCLient.class);

    private static AtomicBoolean hasInit = new AtomicBoolean(false);

    private static List<Closeable> closeableList = new ArrayList<Closeable>();

    // Curator客户端
    private static CuratorFramework client = null;

    private static Executor executor = new Executor() {
        public void execute(Runnable command) {
            new Thread(command, "rainbow-zookeeper").start();
        }
    };

    public static void init(String zkAddress) {
        if (hasInit.compareAndSet(false, true)) {
            doInit(zkAddress);
        }
    }

    private static void doInit(String zkAddress) {
        if (CommonUtil.isEmpty(zkAddress)) {
            throw new RuntimeException("zookeeper 地址不正确: " + zkAddress);
        }

        log.info("[rainbow zookeeper] 初始化 zk: " + zkAddress);

        // 添加钩子
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            public void run() {
                if (closeableList != null && closeableList.size() > 0) {
                    for (Closeable closeable : closeableList) {
                        try {
                            closeable.close();
                        } catch (Exception e) {
                        }
                    }
                }

                log.info("[rainbow zookeeper] 进程关闭，主动断开连接");
                if (client != null) {
                    client.close();
                    client = null;
                }
            }

        }));

        // 重连策略
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(Integer.MAX_VALUE, Integer.MAX_VALUE);

        // 实例化Curator客户端，Curator的编程风格可以让我们使用方法链的形式完成客户端的实例化
        org.apache.curator.framework.CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder();
        // builder.authorization("digist", "super:haigui_zk_admin".getBytes());
        client = builder
                // 使用工厂类来建造客户端的实例对象
                .threadFactory(new ThreadFactory() {
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "rainbow-zookeeper");
                    }
                })
                // 放入zookeeper服务器ip
                .connectString(zkAddress)
                // 设定会话时间以及重连策略
                .sessionTimeoutMs(30000).retryPolicy(retryPolicy)
                // 建立连接通道
                .build();

        // 启动Curator客户端
        client.start();
    }

    /**
     * 注册一个 RainbowClient
     *
     * @param httpUrl
     */
    public static void registerRainbowClient(String httpUrl) {

        String parentPath = ZkType.GateClient.getPath();
        try {

            // 创建临时子节点
            String subPath = URLEncoder.encode(httpUrl, "utf-8");
            String path = parentPath + "/" + subPath;

            // 创建持久化父节点
            checkAndCreatePath(parentPath);

            Stat stat = client.checkExists().forPath(path);

            // 必须要删除节点后 才能新增
            if (stat != null) {
                client.delete().forPath(path);
            }
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);

            log.info("[gate-client] regist gate-client url to zookeeper: {}", httpUrl);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 初始化远程 rainbow-client 服务器地址列表， 并监听远程地址变化
     */
    public static void listenGateClientAddress() {

        ChangeListener childPathChangeListener = new ChangeListener() {
            public void afterChange(String className, List<String> newChildPaths) {
                AppClient.registerGateClientAddress(newChildPaths);
            }
        };
        String path = ZkType.GateClient.getPath();
        List<String> rainbowClientAddressList = readSubPath(path);
        childPathChangeListener.afterChange(null, rainbowClientAddressList);

        TreeCache treeCache = null;
        try {
            treeCache = new TreeCache(client, path);

            treeCache.start();

            TreeCacheListener treeCacheListener = new TreeCacheListener() {
                public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {

                    log.info("[zookeeper] watchPath event:  " + event);

                    List<String> newChildrens = readSubPath(path);
                    if (newChildrens != null && newChildrens.size() > 0 && childPathChangeListener != null) {
                        childPathChangeListener.afterChange(null, newChildrens);
                    }
                }
            };

            treeCache.getListenable().addListener(treeCacheListener, executor);

            closeableList.add(treeCache);

        } catch (Exception e) {
            log.error(e.getMessage(), e);

            if (treeCache != null) {
                try {
                    treeCache.close();
                } catch (Exception e2) {
                }
            }
        }
    }


    /**
     * 初始化远程 app-server 服务器地址列表， 并监听远程地址变化<br/>
     * * 目录结构： /haigui_rainbow/app_server/接口名/provider/[地址1, 地址2, 地址3]
     *
     * @param pointType
     * @param changeListener
     */
    public static void listenAppServerAddress(final String pointType, ChangeListener changeListener) {

        String serverAddressFolder = ZkType.AppService.getPath();

        TreeCache treeCache = null;
        try {
            treeCache = new TreeCache(client, serverAddressFolder);

            treeCache.start();

            TreeCacheListener listener = new TreeCacheListener() {

                public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {

                    // log.info("[zookeeper] listenAppServerAddress event: " + event);

                    if (event == null) {
                        return;
                    }

                    // 断开后重连，将生产者信息重新写入 zk
                    if (event.getType().equals(TreeCacheEvent.Type.CONNECTION_RECONNECTED)) {

                        log.info("[app-server] zk 断线重连， 开始重新注册生产者信息");

                        // 重新写入 appDeploy信息
                        for (Map.Entry<String, AppDeploy> entry : cachedAppDeploy.entrySet()) {
                            registerAppDeploy(entry.getKey(), entry.getValue());
                        }

                        // 重新写入生产者信息
                        for (Map.Entry<String, String> entry : serviceProviderCache.entrySet()) {
                            registerServiceProvider(entry.getKey(), entry.getValue());
                        }

                        // 重新写入消费者信息
                        for (Map.Entry<String, String> entry : customerCache.entrySet()) {
                            registServiceCusotmer(entry.getKey(), entry.getValue());
                        }

                        return;
                    }


                    if (event.getData() == null || event.getData().getPath() == null) {
                        return;
                    }

                    String watchPath = event.getData().getPath();

                    if (watchPath.indexOf("/provider") != -1) {

                        int startIndex = (serverAddressFolder + "/").length();

                        String className = watchPath.substring(startIndex, watchPath.indexOf("/", startIndex + 1));

                        String classNamePath = serverAddressFolder + "/" + className + "/provider";

                        List<String> newChildrens = readSubPath(classNamePath);

                        // 由于 appClient gateServer 端也会提供一些对外接口供 appClient端调用， 为防止自己监听自己， 这里需要做控制
                        // 如果当前jvm实例拥有该类的实例，则忽略监听
                        if (changeListener != null) {
                            changeListener.afterChange(className, newChildrens);
                        }

                    }
                }
            };

            treeCache.getListenable().addListener(listener, executor);

            closeableList.add(treeCache);

        } catch (Exception e) {
            log.error(e.getMessage(), e);

            try {
                treeCache.close();
            } catch (Exception e2) {
            }
        }

    }

    /**
     * 读取子目录列表
     *
     * @param path
     * @return
     */
    private static List<String> readSubPath(String path) {
        try {

            Stat stat = client.checkExists().forPath(path);
            if (stat == null) {
                log.warn("[zookeeper] 节点不存在, path: {}, stat: {}", path, stat);
                return null;
            }
            List<String> childPaths = client.getChildren().forPath(path);

            List<String> result = new ArrayList<String>(childPaths.size());
            for (String string : childPaths) {
                result.add(URLDecoder.decode(string, "utf-8"));
            }
            return result;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    /**
     * 读取子目录列表
     *
     * @param path
     * @return
     */
    public static byte[] readData(String path) {
        try {

            Stat stat = client.checkExists().forPath(path);
            if (stat == null) {
                log.warn("[zookeeper] 节点不存在, path: {}, stat: {}", path, stat);
                return null;
            }
            byte[] data = client.getData().forPath(path);

            return data;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }


    // 关闭zk客户端连接
    private static void closeZKClient() {
        if (client != null) {
            client.close();
        }
    }

    private static void checkAndCreatePath(String path) {
        try {
            Stat stat = client.checkExists().forPath(path);
            if (stat == null) {

                log.info("[zookeeper] 节点不存在：" + path);
                int index = path.lastIndexOf("/");

                if (index != 0) {

                    String parent = path.substring(0, index);
                    checkAndCreatePath(parent);
                }
                log.info("[zookeeper] 开始创建节点：" + path);
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
            }

            return;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }


    /**
     * 缓存当前应用信息
     */
    private static Map<String, AppDeploy> cachedAppDeploy = new ConcurrentHashMap<>();

    /**
     * 应用信息注册
     *
     * @param appName
     * @param appDeploy
     */
    public static void registerAppDeploy(String appName, AppDeploy appDeploy) {

        try {
            if (cachedAppDeploy.get(appName) == null) {
                cachedAppDeploy.put(appName, appDeploy);
            }

            log.info("[rainbow] regist appDeploy info to zookeeper, appInfo:{}", JsonUtil.toJson(appDeploy));

            String deployMsg = JsonUtil.toJson(appDeploy);

            // 创建服务根目录
            String parentPath = ZkType.AppDeploy.getPath() + "/" + appName;

            // 创建临时子节点
            String subPath = URLEncoder.encode(deployMsg, "utf-8");
            String path = parentPath + "/" + subPath;

            // 检查父节点是否存在
            checkAndCreatePath(parentPath);

            Stat stat = client.checkExists().forPath(path);

            // zk会有缓存，必须要删除节点后 才能新增
            if (stat != null) {
                client.delete().forPath(path);
            }

            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }


    /**
     * 缓存当前应用信息
     */
    private static Map<String, String> serviceProviderCache = new ConcurrentHashMap<>();


    /**
     * 生产者注册
     *
     * @param className
     * @param address
     */
    public static void registerServiceProvider(String className, String address) {

        try {

            log.info("[app-server] regist service to zookeeper: {}, url: {}", className, address);

            if (serviceProviderCache.get(className) == null) {
                serviceProviderCache.put(className, address);
            }

            // 创建服务根目录
            String parentPath = ZkType.AppService.getPath() + "/" + className + "/provider";

            // 创建临时子节点
            String subPath = URLEncoder.encode(address, "utf-8");
            String path = parentPath + "/" + subPath;

            // 检查父节点是否存在
            checkAndCreatePath(parentPath);

            Stat stat = client.checkExists().forPath(path);

            // zk会有缓存，必须要删除节点后 才能新增
            if (stat != null) {
                client.delete().forPath(path);
            }

            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }


    private static Map<String,String> customerCache = new ConcurrentHashMap<>();


    /**
     * 消费者注册
     *
     * @param className
     * @param address
     */
    public static void registServiceCusotmer(String className, String address) {
        try {
            if(customerCache.get(className) == null){
                customerCache.put(className, address);
            }

            // 创建服务根目录
            String parentPath = ZkType.AppService.getPath() + "/" + className + "/customer";

            // 创建临时子节点
            String subPath = URLEncoder.encode(address, "utf-8");
            String path = parentPath + "/" + subPath;

            // 检查父节点是否存在
            checkAndCreatePath(parentPath);

            Stat stat = client.checkExists().forPath(path);

            // zk会有缓存，必须要删除节点后 才能新增
            if (stat != null) {
                client.delete().forPath(path);
            }

            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public static Provider getProviderFromZK(String serviceName) {
        // provider
        String proivderFolder = ZkType.AppService.getPath() + "/" + serviceName + "/provider";

        List<String> providers = readSubPath(proivderFolder);

        if (providers != null && providers.size() > 0) {
            return (new Provider(serviceName, providers));
        }

        return null;
    }

    public static AppServiceDomain getAppServiceDomain() {
        AppServiceDomain domain = new AppServiceDomain();

        List<String> serviceNames = readSubPath(ZkType.AppService.getPath());

        if (serviceNames == null || serviceNames.isEmpty()) {
            return null;
        }

        List<Provider> providerList = new ArrayList<Provider>();

        List<Customer> customerList = new ArrayList<Customer>();

        Collections.sort(serviceNames);

        for (String service : serviceNames) {

            // provider
            String proivderFolder = ZkType.AppService.getPath() + "/" + service + "/provider";

            List<String> providers = readSubPath(proivderFolder);

            if (providers != null && providers.size() > 0) {
                providerList.add(new Provider(service, providers));
            }

            Boolean isOnline = providers != null && providers.size() > 0;

            // customer
            String customerFolder = ZkType.AppService.getPath() + "/" + service + "/customer";

            List<String> customers = readSubPath(customerFolder);

            if (customers != null && customers.size() > 0) {

                List<CustomerItem> customerItems = new ArrayList<CustomerItem>();

                for (String cust : customers) {

                    int tmpIndex = cust.indexOf("?");
                    if (tmpIndex == -1) {
                        continue;
                    }

                    String address = cust.substring(0, tmpIndex - 1);

                    String[] params = cust.substring(tmpIndex + 1).split("&");

                    String appName = null;
                    String connectType = null;

                    for (String param : params) {
                        String[] tmps = param.split("=");

                        if (tmps.length != 2) {
                            continue;
                        }

                        if (tmps[0].equals("appName")) {
                            appName = tmps[1];
                            continue;
                        }
                        if (tmps[0].equals("connectType")) {
                            connectType = tmps[1];
                            continue;
                        }
                    }

                    if (address != null && appName != null && connectType != null) {
                        CustomerItem item = new CustomerItem(address, connectType, appName);
                        customerItems.add(item);
                    }

                }

                customerList.add(new Customer(service, isOnline, customerItems));
            }
        }

        domain.setCustomerList(customerList);
        domain.setProviderList(providerList);

        return domain;
    }


    public static List<AppDeploy> getLocalAppDeploy() {
        List<String> apps = readSubPath(ZkType.AppDeploy.getPath());
        if (apps == null || apps.size() == 0) {
            return null;
        }
        Collections.sort(apps);
        List<AppDeploy> appList = new ArrayList<AppDeploy>();
        for (String appName : apps) {
            List<String> deplyMsgs = readSubPath(ZkType.AppDeploy.getPath() + "/" + appName);
            for (String deployMsg : deplyMsgs) {
                AppDeploy appDeploy = JsonUtil.fromJson(deployMsg, AppDeploy.class);
                appList.add(appDeploy);
            }
        }
        return appList;
    }

    public static CuratorFramework getClient() {
        return client;
    }

}
