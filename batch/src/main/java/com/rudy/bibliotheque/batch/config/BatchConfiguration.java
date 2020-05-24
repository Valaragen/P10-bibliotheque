package com.rudy.bibliotheque.batch.config;

import com.rudy.bibliotheque.batch.dto.BorrowDTO;
import com.rudy.bibliotheque.batch.dto.search.LoanSearchDTO;
import com.rudy.bibliotheque.batch.processing.BorrowItemProcessor;
import com.rudy.bibliotheque.batch.processing.JobCompletionNotificationListener;
import com.rudy.bibliotheque.batch.processing.MailBatchItemWriter;
import com.rudy.bibliotheque.batch.proxy.BookApiProxy;
import com.rudy.bibliotheque.batch.util.Constant;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    private JobBuilderFactory jobBuilderFactory;

    private StepBuilderFactory stepBuilderFactory;

    private BookApiProxy bookApiProxy;

    @Autowired
    public BatchConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, BookApiProxy bookApiProxy) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.bookApiProxy = bookApiProxy;
    }

    @Value("${spring.mail.username}")
    private String sender;

    @Value("${config.batch.notifications.email}")
    private String email;


    @Bean
    public ItemReader<BorrowDTO> reader() {
        LoanSearchDTO loanSearchDTO = new LoanSearchDTO();
        List<String> status = new ArrayList<>();
        status.add(Constant.LOAN_STATUS_LATE);
        loanSearchDTO.setStatus(status);
        return new ListItemReader<>(bookApiProxy.getAllLoans(loanSearchDTO));
    }

    @Bean
    public BorrowItemProcessor processor() {
        return new BorrowItemProcessor(sender);
    }

    @Bean
    public MailBatchItemWriter writer() {
        return new MailBatchItemWriter();
    }

    @Bean
    public JobExecutionListener listener() {
        return new JobCompletionNotificationListener(email);
    }

    @Bean
    public Job importUserJob() {
        return jobBuilderFactory.get("importUserJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener())
                .flow(step1())
                .end()
                .build();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .<BorrowDTO, MimeMessage> chunk(50)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }




}
