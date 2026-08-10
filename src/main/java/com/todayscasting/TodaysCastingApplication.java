package com.todayscasting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TodaysCastingApplication {

	public static void main(String[] args) {
		SpringApplication.run(TodaysCastingApplication.class, args);
	}

}
