package com.cfs;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableAutoConfiguration
@EnableJpaAuditing
@PropertySource(value = "classpath:application-${spring.profiles.active}.properties", ignoreResourceNotFound = true)
public class CFSApplication {

    private final static Logger log = Logger.getLogger(CFSApplication.class.getName());

	public static void main(String[] args) {
		SpringApplication.run(CFSApplication.class, args);
		log.info("Coach For Success application started.");
	}

	@PostConstruct
	public void init() {
	}

	@PreDestroy
	public void destroy() {
	}
}
