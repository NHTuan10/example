package com.example.vt.web;

import com.example.vt.common.service.DaggerSampleService;
import com.example.vt.common.service.Service1;
import com.example.vt.common.service.Service2;
import com.example.vt.common.service.SomeData;
import com.example.vt.web.entity.Person;
import com.example.vt.web.entity.RedisPerson;
import com.example.vt.web.entity.Vet;
import com.example.vt.web.repo.PersonRepo;
import com.example.vt.web.repo.RedisPersonRepo;
import com.example.vt.web.repo.VetRepo;
import io.github.nhtuan10.modular.api.Modular;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

//@SpringBootConfiguration
//@EnableAutoConfiguration
@SpringBootApplication(scanBasePackages = "com.example.vt")
@EnableJpaRepositories
@Slf4j
public class VtwebApplication {

    //    static Logger log = LoggerFactory.getLogger(VtwebApplication.class);
    public static void main(String[] args) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        SpringApplication.run(VtwebApplication.class, args);
//        System.out.println("Test new method");
//        ModularContext context = ModuleLoader.getContext();
//        context.<Service1>getModularServices(Service1.class).forEach(service -> {
//            System.out.println(service.message(new SomeData("data1")));
//        });
        Thread.currentThread().getContextClassLoader();
    }

    @Bean
    public CommandLineRunner clr(VetRepo vetRepository) {
        return args -> {
            vetRepository.deleteAll();

            Vet john = new Vet(UUID.randomUUID(), "John", "Doe", new HashSet<>(List.of("surgery")));
            Vet jane = new Vet(UUID.randomUUID(), "Jane", "Doe", new HashSet<>(List.of("radiology, surgery")));

            Vet savedJohn = vetRepository.save(john);
            vetRepository.save(jane);

            vetRepository.findAll()
                    .forEach(v -> log.info("Vet: {}", v.getFirstName()));

            vetRepository.findById(savedJohn.getId())
                    .ifPresent(v -> log.info("Vet by id: {}", v.getFirstName()));

            Vet j = vetRepository.findByFirstName("John");
            log.info("Vet by name: {}", j);
        };
    }


    @RestController
    @RequestMapping("/")
    public static class ThreadController {
        @Autowired
        PersonRepo personRepo;

        @Autowired
        RedisPersonRepo redisPersonRepo;

        @Autowired
        RedisTemplate<String, String> redisTemplate;

        //    @Autowired
        public static ApplicationContext applicationContext;

        //    @Lazy
        @Autowired
        List<Service1> services;

        public ThreadController(@Autowired ApplicationContext applicationContext) {
            ThreadController.applicationContext = applicationContext;
        }

        @GetMapping(value = "/name", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
        public ResultData getThreadName() throws InterruptedException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
//		throw new RuntimeException("This is a test exception");
//		Thread.sleep(1000);
            int i = 1 + (int) (Math.random() * 10);
            String name = personRepo.findById(i).orElse(
                    Person.builder().name("Unknown").build()).getName();
            CompletableFuture.runAsync(() -> {
                StringWriter sw = new StringWriter();
                CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                        .setHeader("name", "age")
                        .setSkipHeaderRecord(true)
                        .build();
                try (final CSVPrinter printer = new CSVPrinter(sw, csvFormat)) {
                    printer.printRecord("Arthur", 14);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            services.parallelStream().forEach(service -> {
                System.out.println(service.message(new SomeData("data1")));
            });

            DaggerSampleService d = Modular.getModularServices(DaggerSampleService.class).get(0);
            d.test();

            Service2 service2 = Modular.getModularServices(Service2.class).get(0);
            service2.test();

//        Modular.getModularServices("sampleServiceImpl" , Service1.class, "vt-plugin2", ExternalContainer.SPRING, true)
//                .forEach(s -> s.message(new SomeData("data1")));
            Modular.getModularServicesFromSpring("sampleServiceImpl", Service1.class, "vt-plugin2")
                    .forEach(s -> s.message(new SomeData("data1")));
//		String name = "Unknown";
//        String name = redisTemplate.opsForValue().get(String.valueOf(i));
//        return (service1 != null ? service1.message() : "") + " " + Thread.currentThread().toString() + ", Person Name: " + name;
//        return new ResultData(Thread.currentThread() ,  " Person Name: " + name);
            return new ResultData(Thread.currentThread().getName(), name);
        }

        record ResultData(String threadName, String personName) {
        }

        @PostMapping(value = "/code")
        public String execCode(@RequestBody String code) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
            final String CLASSNAME = "GeneratedClass";
//            return DynamicCompilerExample.execJavaCode(CLASSNAME, code);
            return DynamicCompilerExample.execGroovyCode(code, applicationContext);
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

    @Bean
    List<Service1> services() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        return Modular.getModularServicesFromSpring("anotherService1", Service1.class);
    }
}

