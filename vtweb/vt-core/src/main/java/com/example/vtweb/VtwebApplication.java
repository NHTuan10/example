package com.example.vtweb;

import com.example.vtcommon.service.Service1;
import com.example.vtweb.annotation.ModularMethodAnnotationProcessor;
import com.example.vtweb.classloader.CustomClassLoader;
import com.example.vtweb.classloader.MavenArtifactsResolver;
import com.example.vtweb.entity.RedisPerson;
import com.example.vtweb.repo.PersonRepo;
import com.example.vtweb.repo.RedisPersonRepo;
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

import java.net.URL;
import java.util.List;

@SpringBootApplication
@EnableJpaRepositories
@Slf4j
public class VtwebApplication {
    public static void main(String[] args) throws ClassNotFoundException {
        List<URL> depUrls = new MavenArtifactsResolver<URL>().resolveMavenDeps(List.of("com.example:vt-plugin:0.0.1-SNAPSHOT"), URL.class);
        CustomClassLoader classLoader
                = new CustomClassLoader(depUrls);
        Class c = classLoader.loadClass("com.example.vtplugin.service.MyService");
        var m = new ModularMethodAnnotationProcessor(classLoader);
        m.annotationProcess("com.example");
        Service1 service1 = (Service1) m.getModularServices(Service1.class).get(0);
        log.info("Messages: {}", service1.message());
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
        return (service1 != null ? service1.message() : "") + " " + Thread.currentThread().toString() + ", Person Name: " + name;
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
