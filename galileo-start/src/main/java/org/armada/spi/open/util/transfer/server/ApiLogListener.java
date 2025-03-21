package org.armada.spi.open.util.transfer.server;

import org.armada.spi.open.util.HaiqActionResult;
import org.armada.spi.open.util.HaiqRequestDomain;

import java.util.Date;

/**
 * @author xiaobo
 * @description: TODO
 * @date 2021/6/18 10:55 上午
 */
public interface ApiLogListener {

    public void afterRequest(HaiqRequestDomain requestDomain,
                             HaiqActionResult responseResult,
                             Date happenTime,
                             int costTime,
                             int requestByteSize,
                             int responseByteSize,
                             Exception ex);

}
