package hello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.item.ItemProcessor;

public class PersonItemProcessor implements ItemProcessor<Person, Person> {

    private static final Logger log = LoggerFactory.getLogger(PersonItemProcessor.class);

    @Override
    public Person process(final Person person) throws Exception {
        try {
            Thread.sleep(10);
            System.out.println("Sleep for 1 seconds");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final String firstName = person.getFirstName().toUpperCase();
        final String lastName = person.getLastName().toUpperCase();
        if (firstName.equals("JILL")){
            return null;
        }
        final Person transformedPerson = new Person(person.getLineNo(), firstName, lastName);

        log.info("Converting (" + person + ") into (" + transformedPerson + ")");

        return transformedPerson;
    }

}
