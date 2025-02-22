package com.example.vt.web;

import com.example.vt.common.service.Service1;
import com.example.vt.common.service.SomeData;
import com.example.vt.modular.classloader.ModuleLoader;
import com.example.vt.modular.model.ModularContext;
import com.example.vt.web.entity.RedisPerson;
import com.example.vt.web.repo.PersonRepo;
import com.example.vt.web.repo.RedisPersonRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;

//@SpringBootConfiguration
//@EnableAutoConfiguration
@SpringBootApplication
@EnableJpaRepositories
@Slf4j
public class VtwebApplication {
    //    static Logger log = LoggerFactory.getLogger(VtwebApplication.class);
    public static void main(String[] args) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

//        log.info("Test new method");
        System.out.println("Test new method");
        ModularContext context = ModuleLoader.getContext();
        context.<Service1>getModularServices(Service1.class).forEach(service -> {
            System.out.println(service.message(new SomeData("data1")));
        });
        SpringApplication.run(VtwebApplication.class, args);
    }
}

@Configuration
class AppConfig {
    @Bean
    public RedisConnectionFactory lettuceConnectionFactory() {
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration("localhost", 6379));
    }

    @Bean
    RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        return template;
    }
}

@RestController
@RequestMapping("/thread")
class ThreadController {
    @Autowired
    PersonRepo personRepo;

    @Autowired
    RedisPersonRepo redisPersonRepo;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Lazy
//    @Autowired
    Service1 service1;

    @GetMapping("/name")
    public String getThreadName() throws InterruptedException {
//		throw new RuntimeException("This is a test exception");
//		Thread.sleep(1000);
        int i = 1 + (int) (Math.random() * 10);
//		String name = personRepo.findById(i).orElse(
//				Person.builder().name("Unknown").build()).getName();
//		String name = "Unknown";
        String name = redisTemplate.opsForValue().get(String.valueOf(i));
//        return (service1 != null ? service1.message() : "") + " " + Thread.currentThread().toString() + ", Person Name: " + name;
        return Thread.currentThread().toString() + ", Person Name: " + name;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadDataToRedisCache() {
        for (int i = 1; i <= 10; i++) {
            redisTemplate.opsForValue().set(String.valueOf(i), "John Doe " + i);
        }
        RedisPerson p = new RedisPerson("11", "Jane 11", 11);
        redisPersonRepo.save(p);
    }


}
