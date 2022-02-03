package io.oodles.emailscheduler.scheduler;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
public class ScheduledEmailJob extends QuartzJobBean{

	@Autowired
	private Email email;
	@Autowired
	private MailProperties mailProperties;
	 
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

        JobDataMap jobDataMap = context.getMergedJobDataMap();
        
        String subject = jobDataMap.getString("subject");
        String body = jobDataMap.getString("body");
        String recipientEmail = jobDataMap.getString("email");

        email.sendMail(mailProperties.getUsername(), recipientEmail, subject, body);
	}
	
	
	
	
	

}
