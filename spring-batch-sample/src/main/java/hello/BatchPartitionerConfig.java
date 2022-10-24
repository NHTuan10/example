package hello;

import lombok.extern.slf4j.Slf4j;
import org.h2.mvstore.tx.Transaction;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;

import javax.sql.DataSource;
import java.net.MalformedURLException;

//@Configuration
@Slf4j

public class BatchPartitionerConfig {
    @Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory steps;

    @Bean(name = "partitionerJob")
    public Job partitionerJob(Step partitionStep) throws UnexpectedInputException, MalformedURLException, ParseException {
        return jobs.get("partitionerJob")
                .start(partitionStep)
                .build();
    }

    @Bean
    public Step partitionStep(TaskExecutor taskExecutor, Step workerStep) throws UnexpectedInputException, MalformedURLException, ParseException {
        return steps.get("partitionStep")
                .partitioner("workerStep", partitioner())
                .gridSize(20)
                .step(workerStep)
                .taskExecutor(taskExecutor)
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<Person> workerReader(@Value("#{stepExecutionContext[offset]}") int offset, @Value("#{stepExecutionContext[size]}") int size) {
        log.info("Read from Offset {}", offset);
        return new FlatFileItemReaderBuilder<Person>()
                .name("personItemReader")
                .resource(new ClassPathResource("sample-data.csv"))
                .linesToSkip(offset)
                .maxItemCount(size)
                .delimited()
                .names(new String[]{"lineNo", "firstName", "lastName"})
                .fieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
                    setTargetType(Person.class);
                }})
                .build();
    }

    @Bean
    @StepScope
    public JdbcBatchItemWriter<Person>  workerJdbcWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Person>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO people (line_no,first_name, last_name) VALUES (:lineNo,:firstName, :lastName)")
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public Step workerStep(JdbcBatchItemWriter workerJdbcWriter, FlatFileItemReader<Person> workerReader) throws UnexpectedInputException, MalformedURLException, ParseException {
        return steps.get("workerStep")
                .<Person, Person>chunk(10)
                .reader(workerReader)
                .processor((ItemProcessor<Person, Person>) item -> {
                    log.info("Processing {}", item);
                    Thread.sleep(100);
                    return item;
                })
                .writer(workerJdbcWriter)
                .build();
    }

    public CustomMultiResourcePartitioner partitioner() {
        return new CustomMultiResourcePartitioner();
    }

}
