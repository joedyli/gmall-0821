package com.atguigu.gmall.index;

import org.junit.jupiter.api.Test;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
class GmallIndexApplicationTests {

    @Autowired
    //private RedisTemplate redisTemplate;
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Test
    void contextLoads() {
        this.redisTemplate.opsForValue().set("test1",  "马蓉");
        System.out.println(this.redisTemplate.opsForValue().get("test1"));
    }

    @Test
    void testRedissonBloomFilter(){
        RBloomFilter<Object> bloomFilter = this.redissonClient.getBloomFilter("bloomFilter");
        bloomFilter.tryInit(20l, 0.3d);
        bloomFilter.add("1");
        bloomFilter.add("2");
        bloomFilter.add("3");
        bloomFilter.add("4");
        bloomFilter.add("5");
        bloomFilter.add("6");
        bloomFilter.add("7");
        bloomFilter.add("8");
        bloomFilter.add("9");
        bloomFilter.add("10");

        System.out.println(bloomFilter.contains("1"));
        System.out.println(bloomFilter.contains("3"));
        System.out.println(bloomFilter.contains("5"));
        System.out.println(bloomFilter.contains("11"));
        System.out.println(bloomFilter.contains("12"));
        System.out.println(bloomFilter.contains("13"));
        System.out.println(bloomFilter.contains("14"));
        System.out.println(bloomFilter.contains("15"));
        System.out.println(bloomFilter.contains("16"));
        System.out.println(bloomFilter.contains("17"));
        System.out.println(bloomFilter.contains("18"));
    }

}
