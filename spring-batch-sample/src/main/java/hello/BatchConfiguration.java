package hello;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.sql.DataSource;
import java.util.Arrays;

@Configuration
@Slf4j
//@EnableBatchProcessing
public class BatchConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    // tag::readerwriterprocessor[]
    @Bean
    public FlatFileItemReader<Person> flatFileReader() {
        return new FlatFileItemReaderBuilder<Person>()
            .name("personItemReader")
            .resource(new ClassPathResource("sample-data.csv"))
                .linesToSkip(1)
            .delimited()
            .names(new String[]{"lineNo", "firstName", "lastName"})
            .fieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
                setTargetType(Person.class);
            }})
            .build();
    }
    @Bean
    public JdbcCursorItemReader<Person> jdbcReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<Person>()
                .name("personJdbcItemReader")
                .dataSource(dataSource)
                .sql("SELECT * FROM PEOPLE")
                .fetchSize(10)
                .queryTimeout(10000)
                .beanRowMapper(Person.class)
                .build();
    }

    @Bean
    public PersonItemProcessor processor()  {
        return new PersonItemProcessor();
    }

    @Bean
    public ItemProcessor step2Processor()  {
        return item -> {log.info("Step 2Processing {} " , item); return item;};
    }

    @Bean
    public JdbcBatchItemWriter<Person> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Person>()
            .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
            .sql("INSERT INTO people (line_no,first_name, last_name) VALUES (:lineNo,:firstName, :lastName)")
            .dataSource(dataSource)
            .build();
    }
    // end::readerwriterprocessor[]

    // tag::jobstep[]
    @Bean(name = "importUserJob")
    public Job importUserJob(JobCompletionNotificationListener listener, Step step1, Step step2, Step step4, Step step3, TaskExecutor taskExecutor) {
        return jobBuilderFactory.get("importUserJobV2")
//                .incrementer()
//            .incrementer(new RunIdIncrementer())
            .listener(listener)
                .start(new FlowBuilder<SimpleFlow>("Splitt").split(taskExecutor).add(
                    new FlowBuilder<SimpleFlow>("flow 1").start(step1).next(step2).build(),
                    new FlowBuilder<SimpleFlow>("flow 1").start(step3).build()).build())
                .next(step4)
            .end()
            .build();
    }

    @Bean
    public Step step1( CompositeItemWriter<Person> writer, TaskExecutor myTaskExecutor) {
        return stepBuilderFactory.get("step1")
            .<Person, Person> chunk(10)
            .reader(flatFileReader())
            .processor(processor())
            .writer(writer)
                .faultTolerant()
                .retryLimit(2)
                .retry(Exception.class)
                .taskExecutor(myTaskExecutor)
                .build();
    }
    // end::jobstep[]

    @Bean
    public Step step4(JdbcBatchItemWriter<Person> writer, ThreadPoolTaskExecutor myTaskExecutor) {
        return stepBuilderFactory.get("step4")
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("This is step 4");
                    myTaskExecutor.getThreadPoolExecutor().shutdown();
//                    throw new RuntimeException("Failed at step 2");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step step3(JdbcBatchItemWriter<Person> writer, TaskExecutor myTaskExecutor) {
        return stepBuilderFactory.get("step3")
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("This is step 3");
//                    throw new RuntimeException("Failed at step 3");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step step2(RestItemWriter<Person> restWriter, JdbcCursorItemReader jdbcReader, TaskExecutor myTaskExecutor) {
        return stepBuilderFactory.get("step2")
                .<Person, Person> chunk(10)
                .reader(jdbcReader)
                .processor(step2Processor())
                .writer(restWriter)
                .taskExecutor(myTaskExecutor)
                .build();
    }
    @Bean
    @StepScope
    public CompositeItemWriter<Person> compositeWriter(JdbcBatchItemWriter<Person> jdbcWriter, RestItemWriter<Person> restWriter) {
        CompositeItemWriter<Person> writer = new CompositeItemWriter<>();
        writer.setDelegates(Arrays.asList(jdbcWriter, restWriter));
        return writer;
    }
}
