package com.eip.fraud.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class VelocityService {

    private final RedisTemplate<String, String> redisTemplate;

    public long getCustomerClaimCount(String customerId, int days) {
        String key = "fraud:velocity:customer:" + customerId + ":claims:" + days + "d";
        String val = redisTemplate.opsForValue().get(key);
        return val != null ? Long.parseLong(val) : 0L;
    }

    public long getAddressClaimCount(String addressHash, int days) {
        if (addressHash == null || addressHash.isEmpty()) return 0L;
        String key = "fraud:velocity:address:" + addressHash.hashCode() + ":claims:" + days + "d";
        String val = redisTemplate.opsForValue().get(key);
        return val != null ? Long.parseLong(val) : 0L;
    }

    public long getBrokerClaimCount(String brokerId, int days) {
        if (brokerId == null || brokerId.isEmpty()) return 0L;
        String key = "fraud:velocity:broker:" + brokerId + ":claims:" + days + "d";
        String val = redisTemplate.opsForValue().get(key);
        return val != null ? Long.parseLong(val) : 0L;
    }

    public void incrementClaimCount(String customerId, String address, String brokerId) {
        increment("fraud:velocity:customer:" + customerId + ":claims:30d", Duration.ofDays(30));
        increment("fraud:velocity:customer:" + customerId + ":claims:7d", Duration.ofDays(7));

        if (address != null && !address.isEmpty()) {
            increment("fraud:velocity:address:" + address.hashCode() + ":claims:90d", Duration.ofDays(90));
        }
        if (brokerId != null && !brokerId.isEmpty()) {
            increment("fraud:velocity:broker:" + brokerId + ":claims:7d", Duration.ofDays(7));
        }
    }

    private void increment(String key, Duration ttl) {
        Long newVal = redisTemplate.opsForValue().increment(key);
        if (newVal != null && newVal == 1) {
            // First increment - set the expiry
            redisTemplate.expire(key, ttl.toSeconds(), TimeUnit.SECONDS);
        }
    }
}
