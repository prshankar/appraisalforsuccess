package com.cfs.jobs;

import java.io.IOException;
import java.util.Map;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cfs.email.EmailService;

import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
public class ForgotPasswordNotificationTask implements Runnable {
 
	@Autowired
    public EmailService emailService;

	private Map<String, Object> templateModel;
	private String primaryEmail;

    public void run() {
        try {
        	emailService.sendMessageUsingThymeleafTemplate(
					primaryEmail,
			        "Coach For Success - Reset Password",
			        templateModel, "../templates/email/forgotpasswordemail.html");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}