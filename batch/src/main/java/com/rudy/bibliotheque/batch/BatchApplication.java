package com.rudy.bibliotheque.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
@SpringBootApplication
@EnableFeignClients("com.rudy.bibliotheque.batch")
@EnableDiscoveryClient
@EnableScheduling
public class BatchApplication {
	@Autowired
	private JobLauncher jobLauncher;

	@Qualifier("lateReturnJob")
	@Autowired
	private Job lateReturnJob;

	@Qualifier("retrieveExpiredJob")
	@Autowired
	private Job retrieveExpiredJob;

	public static void main(String[] args) {
		SpringApplication.run(BatchApplication.class, args);
	}

	@Scheduled(cron = "0 30 11 * * ?")
	public void performLateReturnJob() throws Exception {
		JobParameters params = new JobParametersBuilder()
				.addString("lateReturnJob", String.valueOf(System.currentTimeMillis()))
				.toJobParameters();
		jobLauncher.run(lateReturnJob, params);
	}

	@Scheduled(cron = "0 0 0/6 * * ?")
	public void performRetrieveExpiredJob() throws Exception {
		JobParameters params = new JobParametersBuilder()
				.addString("retrieveExpiredJob", String.valueOf(System.currentTimeMillis()))
				.toJobParameters();
		jobLauncher.run(retrieveExpiredJob, params);
	}

}
