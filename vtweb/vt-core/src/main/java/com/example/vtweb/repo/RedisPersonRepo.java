package com.example.vtweb.repo;

import com.example.vtweb.entity.RedisPerson;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RedisPersonRepo extends CrudRepository<RedisPerson, String> {

}
