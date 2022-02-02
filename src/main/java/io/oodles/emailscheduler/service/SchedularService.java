package io.oodles.emailscheduler.service;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;//this is a imp 
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import io.oodles.emailscheduler.payload.EmailRequest;
import io.oodles.emailscheduler.payload.EmailResponse;
import io.oodles.emailscheduler.service.jobs.EmailJob;

@Service
public class SchedularService {

	@Autowired
	private Scheduler scheduler;

	public ResponseEntity<EmailResponse> scheduleEmailJob(EmailRequest emailRequest, ZonedDateTime dateTime) {

		try {
			JobDetail jobDetail = createJobDetail(emailRequest);
			Trigger trigger = createJobTrigger(jobDetail, dateTime);
			scheduler.scheduleJob(jobDetail, trigger);
			EmailResponse emailResponse = new EmailResponse(true, jobDetail.getKey().getName(),
					jobDetail.getKey().getGroup(), "Email Scheduled Successfully!");
			return ResponseEntity.ok(emailResponse);

		}

		catch (SchedulerException ex) {
			EmailResponse emailResponse = new EmailResponse(false, "Error scheduling email. Please try later!");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(emailResponse);
		}

	}

	private JobDetail createJobDetail(EmailRequest emailRequest) {

		JobDataMap jobDataMap = new JobDataMap();

		jobDataMap.put("email", emailRequest.getEmail());
		jobDataMap.put("subject", emailRequest.getSubject());
		jobDataMap.put("body", emailRequest.getBody());

		return JobBuilder.newJob(EmailJob.class).withIdentity(UUID.randomUUID().toString(), "email-jobs")
				.withDescription("Send Email Job").usingJobData(jobDataMap).storeDurably().build();
	}

	private Trigger createJobTrigger(JobDetail jobDetail, ZonedDateTime startAt) {
		return TriggerBuilder.newTrigger().forJob(jobDetail)
				.withIdentity(jobDetail.getKey().getName(), "email-triggers").withDescription("Send Email Trigger")
				.startAt(Date.from(startAt.toInstant()))
				.withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow()).build();
	}
}