package com.example.demo.config;



import java.util.concurrent.Executor;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig {

	@Bean
	 Executor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(8);
		executor.setMaxPoolSize(10);
		executor.setQueueCapacity(1000);
		executor.setThreadNamePrefix("csv-thread");
		executor.initialize();
		return executor;
		
	}

}
