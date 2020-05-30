package com.rudy.bibliotheque.batch.job.lateReturn;

import com.rudy.bibliotheque.batch.dto.BorrowDTO;
import com.rudy.bibliotheque.batch.dto.search.LoanSearchDTO;
import com.rudy.bibliotheque.batch.processing.JobCompletionNotificationListener;
import com.rudy.bibliotheque.batch.processing.MailBatchItemWriter;
import com.rudy.bibliotheque.batch.proxy.BookApiProxy;
import com.rudy.bibliotheque.batch.util.Constant;
import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate;
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
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;

import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableBatchProcessing
public class LateUserJobConfiguration {

    private JobBuilderFactory jobBuilderFactory;

    private StepBuilderFactory stepBuilderFactory;

    private BookApiProxy bookApiProxy;

    @Autowired
    public LateUserJobConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, BookApiProxy bookApiProxy) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.bookApiProxy = bookApiProxy;
    }

    @Value("${spring.mail.username}")
    private String sender;

    @Value("${config.batch.notifications.email}")
    private String email;


    @Bean
    public ItemReader<BorrowDTO> lateReturnReader() {
        LoanSearchDTO loanSearchDTO = new LoanSearchDTO();
        List<String> status = new ArrayList<>();
        status.add(Constant.STATUS_LATE);
        loanSearchDTO.setStatus(status);
        return new ListItemReader<>(bookApiProxy.getAllLoans(loanSearchDTO));
    }

    @Bean
    public LateReturnProcessor lateReturnProcessor() {
        return new LateReturnProcessor(sender);
    }

    @Bean
    public MailBatchItemWriter lateReturnWriter() {
        return new MailBatchItemWriter();
    }

    @Bean
    public JobExecutionListener lateReturnListener() {
        return new JobCompletionNotificationListener(email);
    }

    @Bean
    public Job lateReturnJob() {
        return jobBuilderFactory.get("lateReturnJob")
                .incrementer(new RunIdIncrementer())
                .listener(lateReturnListener())
                .flow(lateReturnStep1())
                .end()
                .build();
    }

    @Bean
    public Step lateReturnStep1() {
        return stepBuilderFactory.get("lateReturnStep1")
                .<BorrowDTO, MimeMessage> chunk(50)
                .reader(lateReturnReader())
                .processor(lateReturnProcessor())
                .writer(lateReturnWriter())
                .build();
    }


}
