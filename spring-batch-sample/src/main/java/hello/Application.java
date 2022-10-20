package hello;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Optional;

@EnableBatchProcessing
@SpringBootApplication
public class Application implements CommandLineRunner {
    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier(value="importUserJob")
    private Job importUserJob;

    @Autowired
    JobManager jobManager;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // Pass the required Job Parameters from here to read it anywhere within
        // Spring Batch infrastructure


        JobParameters jobParameters = new JobParametersBuilder().addString("b","19").addString("date", "2022-11-29").toJobParameters();
//        JobExecution execution = jobLauncher.run(importUserJob, jobParameters);
        jobManager.registerJob(importUserJob);
        Optional<JobExecution> optionalJobExecution = jobManager.runJob(importUserJob, jobParameters);
        optionalJobExecution.ifPresent(jobExecution -> System.out.println("STATUS :: " + jobExecution.getStatus()));
    }
}
