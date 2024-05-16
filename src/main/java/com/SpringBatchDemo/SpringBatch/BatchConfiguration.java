package com.SpringBatchDemo.SpringBatch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
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
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

public class BatchConfiguration {

     @Autowired
     private CustomerRepository customerRepository;

     @Autowired
     private JobRepository jobRepository;

     @Autowired
     private PlatformTransactionManager platformTransactionManager;



    @Bean
    public FlatFileItemReader<Customer> fileReader( ){

        FlatFileItemReader<Customer> reader = new FlatFileItemReader<>();

        reader.setName("reader");
        reader.setLinesToSkip(1);
        reader.setLineMapper(lineMapper( ));

      return reader;
    }

    private LineMapper<Customer> lineMapper() {

        DefaultLineMapper<Customer> mapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer token = new DelimitedLineTokenizer();
        token.setNames("\"id\",\"name\",\"department\",\"gender");
        token.setDelimiter(",");
        token.setStrict(false);
        BeanWrapperFieldSetMapper<Customer> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Customer.class);

        mapper.setLineTokenizer(token);
        mapper.setFieldSetMapper(fieldSetMapper);
        return mapper;
    }

    @Bean
    public CustomerProcessor processor( ){

        return new CustomerProcessor();
    }

    @Bean
    public RepositoryItemWriter<Customer> getWriter( ){

        RepositoryItemWriter<Customer> writer = new RepositoryItemWriter<>();
        writer.setRepository(customerRepository);
        writer.setMethodName("save");

        return writer;
    }

    @Bean
    public Step buildStep( ){

        return new StepBuilder("StartAt",jobRepository)
                .<Customer,Customer>chunk(10, platformTransactionManager)
                .reader(fileReader())
                .processor(processor())
                .writer(getWriter())
                .taskExecutor(executor())
                .build();


    }

    @Bean
    public Job jobs( ){

        return new JobBuilder("Orthee", jobRepository)
                .start(buildStep())
                .build();
    }
    @Bean
    public TaskExecutor executor( ){

        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
        executor.setConcurrencyLimit(10);
        return executor;
    }


}
