package com.cfs.jobs;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cfs.email.EmailService;
import com.cfs.pojo.model.StartReviewDto;
import com.cfs.service.CFSService;

@Component
public class CfsStartReviewNotification {

	@Autowired
	private CFSService cfsService;

	@Autowired
	public EmailService emailService;

	public void sendStartReviewNotification() {
		List<StartReviewDto> startReviewDtos = cfsService.sendStartReviewNotification();
		Map<String, Object> templateModel = new HashMap<String, Object>();

		startReviewDtos.forEach(startReviewDto -> {
			try {
				emailService.sendMessageUsingThymeleafTemplate(startReviewDto.getEmailId(),
						"Your review for current financial year " + startReviewDto.getYear() + " has been started by : "
								+ startReviewDto.getReviewerName(),
						templateModel, "../templates/email/startreview.html");
			} catch (IOException | MessagingException e) {
				e.printStackTrace();
			}
		});
	}
}
