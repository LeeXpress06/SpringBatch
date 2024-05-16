package com.SpringBatchDemo.SpringBatch;


import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;


@Configuration
@EnableBatchProcessing
public class CustomerConfig {

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @Autowired
    private StepBuilder stepBuilder;


      @Bean
      public FlatFileItemReader<Customer> customerReader( ){

          FlatFileItemReader<Customer> itemReader = new FlatFileItemReader<>();
           // resource location
           itemReader.setName("Customer");
           itemReader.setLinesToSkip(1);
           itemReader.setLineMapper(lineMapper( ));

          return itemReader;

      }

    private LineMapper<Customer> lineMapper() {

        DefaultLineMapper<Customer>lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(" , ");     // how each record is separated
        lineTokenizer.setStrict(false);       // how to deal with null values
        lineTokenizer.setNames("\"id\",\"name\",\"department\",\"gender");    // what are the columns

         BeanWrapperFieldSetMapper <Customer> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
         fieldSetMapper.setTargetType(Customer.class);

         lineMapper.setFieldSetMapper(fieldSetMapper);
         lineMapper.setLineTokenizer(lineTokenizer);
    return lineMapper;
    }
    @Bean
    public CustomerProcessor customerProcessor( ){
          return new CustomerProcessor();
    }

    @Bean
    public RepositoryItemWriter<Customer> itemWriter( ){
       RepositoryItemWriter<Customer> writer = new RepositoryItemWriter<>();
       writer.setRepository(customerRepository);
       writer.setMethodName("save");
       return writer;
    }


    @Bean
    public Step workstep ( ){
          return  new StepBuilder("Stp-01", jobRepository)
                  .<Customer,Customer>chunk(10,platformTransactionManager)
                  .processor(customerProcessor())
                  .writer(itemWriter())
                  .reader(customerReader())
                  .taskExecutor(getTask())
                  .build();
    }

    @Bean
    public Job customerJob ( ){
          return new JobBuilder("Orthee", jobRepository)
                  .start(workstep())
                  .build();
    }

    @Bean
    public TaskExecutor getTask( ){

        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
        executor.setConcurrencyLimit(10);

        return executor;
    }






}
