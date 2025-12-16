/*package com.cfs.config;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.sql.DataSource;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

import com.cfs.jobs.CfsStartReviewNotification;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
@EnableTransactionManagement
@EnableJpaRepositories(entityManagerFactoryRef = "entityManagerFactory", transactionManagerRef = "transactionManager", basePackages = "com.cfs")
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class CFSConfigurationQuartz {

    @Autowired
    private ApplicationContext applicationContext;

    @Primary
	@Bean(name = "dataSource")
	@ConfigurationProperties(prefix = "spring.datasource")
	public DataSource dataSource() {
		return DataSourceBuilder.create().build();
	}

	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2).select()
				.apis(RequestHandlerSelectors.basePackage("com.cfs.controller"))
				.paths(PathSelectors.any()).build().apiInfo(apiEndPointsInfo());
	}

	private ApiInfo apiEndPointsInfo() {
		return new ApiInfoBuilder().title("Coach For Success Service API").description("Coach For Success Service API")
				.contact(new Contact("Shankar Rajendran", "cfs-url", "")).build();
	}

	@Primary
	@PersistenceUnit(name = "cfs")
	@Bean(name = "entityManagerFactory")
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder,
			@Qualifier("dataSource") DataSource dataSource) {
		return builder.dataSource(dataSource).packages("com.cfs.pojo.entity").persistenceUnit("cfs").build();
	}

	@Primary
	@Bean(name = "transactionManager")
	public PlatformTransactionManager transactionManager(
			@Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {
		return new JpaTransactionManager(entityManagerFactory);
	}

	@Scheduled(initialDelay = 10 * 60 * 1000, fixedRate = 10 * 60 * 1000)
	public void scheduleFixedRateTasks() {
	}
	
    @Bean
    public SimpleMailMessage templateSimpleMessage() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setText("This is the test email template for your email:\n%s\n");
        return message;
    }

    @Bean
    public MessageSource messageSource () {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages/mailMessages");
        return messageSource;
    }

    @Bean
    public RestTemplate getRestTemplate() {
       return new RestTemplate();
    }*/

    /*@Bean
    public JobDetail jobDetail() {
        return JobBuilder.newJob().ofType(CfsStartReviewNotification.class)
          .storeDurably()
          .withIdentity("Qrtz_Job_Detail")  
          .withDescription("Invoke Sample Job service...")
          .build();
    }*/

    
    /*@Bean
    public JobDetailFactoryBean jobDetail() {
        JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
        jobDetailFactory.setJobClass(CfsStartReviewNotification.class);
        jobDetailFactory.setDescription("Cfs Job running...");
        jobDetailFactory.setDurability(true);
        return jobDetailFactory;
    }

    @Bean
    public Trigger trigger(JobDetail job) {
        return TriggerBuilder.newTrigger().forJob(job)
          .withIdentity("Qrtz_Trigger")
          .withDescription("Sample trigger")
          .withSchedule(simpleSchedule().repeatForever().withIntervalInMilliseconds(30000))
          .build();
    }*/

    /*@Bean
    public JobDetail job() {
        return JobBuilder.newJob().ofType(CfsStartReviewNotification.class)
          .storeDurably()
          .withIdentity("Start Review Job")  
          .withDescription("Start Review Job Service...")
          .build();
    }

    @Bean
    public SpringBeanJobFactory springBeanJobFactory() {
    	AutoWiringSpringBeanJobFactory jobFactory = new AutoWiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    @Bean
    public Trigger trigger(JobDetail job) {
        return TriggerBuilder.newTrigger().forJob(job).startNow()
          .withSchedule(CronScheduleBuilder.cronSchedule("0/2 * * ? * * *"))
          .build();
    }

    
    @Bean
    public SchedulerFactoryBean factory(Trigger trigger, JobDetail job, DataSource quartzDataSource) {
        SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
        //schedulerFactory.setConfigLocation(new ClassPathResource("quartz.properties"));

        schedulerFactory.setJobFactory(springBeanJobFactory());
        schedulerFactory.setJobDetails(job);
        schedulerFactory.setAutoStartup(true);

        schedulerFactory.setTriggers(trigger);
        //schedulerFactory.setDataSource(quartzDataSource);
        return schedulerFactory;
    }


    @Bean
    public Scheduler scheduler(Trigger trigger, JobDetail job, SchedulerFactoryBean factory) 
      throws SchedulerException {
    	Scheduler scheduler = factory.getScheduler();

    	try {

            scheduler.start();

            if (scheduler.checkExists(job.getKey())){
                scheduler.deleteJob(job.getKey());
            }
            
            
            scheduler.scheduleJob(job, trigger);

            
    		
    	} catch (Exception e) {
    		e.printStackTrace();
		}

        return scheduler;

    }
}*/
