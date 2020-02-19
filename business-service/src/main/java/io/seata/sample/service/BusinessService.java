package io.seata.sample.service;

import io.seata.sample.feign.OrderFeignClient;
import io.seata.sample.feign.StorageFeignClient;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

/**
 * Description：
 *
 * @author fangliangsheng
 * @date 2019-04-05
 */
@Service
public class BusinessService {

    @Autowired
    private StorageFeignClient storageFeignClient;
    @Autowired
    private OrderFeignClient orderFeignClient;

    @Autowired
    private CircuitBreakerFactory cbFactory;

    /**
     * 减库存，下订单
     *
     * @param userId
     * @param commodityCode
     * @param orderCount
     */
    @GlobalTransactional
    public void purchase(String userId, String commodityCode, int orderCount) {
        cbFactory.create("CircuitBreakerName_storage").run(
                new Supplier<Boolean>() {
                    @Override
                    public Boolean get() {
                      storageFeignClient.deduct(commodityCode, orderCount);
                      orderFeignClient.create(userId, commodityCode, orderCount);
                      return true;
                    }
                }
        );

    }
}
