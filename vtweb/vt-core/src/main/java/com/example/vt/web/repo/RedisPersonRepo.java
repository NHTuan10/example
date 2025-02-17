package com.example.vt.web.repo;

import com.example.vt.web.entity.RedisPerson;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RedisPersonRepo extends CrudRepository<RedisPerson, String> {

}
