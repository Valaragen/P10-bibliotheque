package com.rudy.bibliotheque.batch.job.retrieveExpired;

import com.rudy.bibliotheque.batch.dto.BorrowDTO;
import com.rudy.bibliotheque.batch.dto.search.LoanSearchDTO;
import com.rudy.bibliotheque.batch.proxy.BookApiProxy;
import com.rudy.bibliotheque.batch.util.Constant;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableBatchProcessing
public class RetrieveExpiredJobConfiguration {

    private JobBuilderFactory jobBuilderFactory;

    private StepBuilderFactory stepBuilderFactory;

    private BookApiProxy bookApiProxy;

    @Autowired
    public RetrieveExpiredJobConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, BookApiProxy bookApiProxy) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.bookApiProxy = bookApiProxy;
    }

    @Bean
    public ItemReader<BorrowDTO> retrieveExpiredReader() {
        LoanSearchDTO loanSearchDTO = new LoanSearchDTO();
        List<String> status = new ArrayList<>();
        status.add(Constant.STATUS_PENDING);
        loanSearchDTO.setStatus(status);
        return new ListItemReader<>(bookApiProxy.getAllLoans(loanSearchDTO));
    }

    @Bean
    public RetrieveExpiredProcessor retrieveExpiredProcessor() {
        return new RetrieveExpiredProcessor();
    }

    @Bean
    public RetrieveExpiredWriter retrieveExpiredWriter() {
        return new RetrieveExpiredWriter();
    }

    @Bean
    public Job retrieveExpiredJob() {
        return jobBuilderFactory.get("retrieveExpiredJob")
                .incrementer(new RunIdIncrementer())
                .flow(retrieveExpiredStep1())
                .end()
                .build();
    }

    @Bean
    public Step retrieveExpiredStep1() {
        return stepBuilderFactory.get("retrieveExpiredStep1")
                .<BorrowDTO, BorrowDTO>chunk(20)
                .reader(retrieveExpiredReader())
                .processor(retrieveExpiredProcessor())
                .writer(retrieveExpiredWriter())
                .build();
    }


}
