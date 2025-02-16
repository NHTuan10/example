package com.example.vtweb.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.redis.core.RedisHash;

//@SuperBuilder
@RedisHash("Person")
@Data
@AllArgsConstructor
public class RedisPerson {
    private String id;
    private String name;
    private Integer age;
}
