package hello;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class RestItemWriter<Person> implements ItemWriter<Person> {
    @Autowired
    RestTemplate restTemplate;

    public RestItemWriter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void write(List<? extends Person> items) throws Exception {
        System.out.println("Items is: " + items);
        ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:8080/ping", String.class);

        System.out.println("Status code is: " + response.getStatusCode());
    }
}
