package io.oodles.emailscheduler.service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;//this is a imp 
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import io.oodles.emailscheduler.Repository.SchedularInfoRepository;
import io.oodles.emailscheduler.modle.ScheduledInfo;
import io.oodles.emailscheduler.payloadDTO.EmailRequest;
import io.oodles.emailscheduler.payloadDTO.EmailResponse;
import io.oodles.emailscheduler.scheduler.ScheduledEmailJob;

@Service
public class SchedularService {

	@Autowired
	private Scheduler scheduler;
	@Autowired
	private SchedularInfoRepository schedularInfoRepository;

	public List<ScheduledInfo> findAllJob() {
		Iterable<ScheduledInfo> findAll = schedularInfoRepository.findAll();
		Iterator<ScheduledInfo> iterator = findAll.iterator();
		List<ScheduledInfo> allScheduledInfo = new ArrayList<ScheduledInfo>();
		while (iterator.hasNext())
			allScheduledInfo.add(iterator.next());
		return allScheduledInfo;
	}

	public ResponseEntity<EmailResponse> scheduleEmailJob(EmailRequest emailRequest, ZonedDateTime dateTime) {

		try {
			String jobName = UUID.randomUUID().toString();
			JobDetail jobDetail = createJobDetail(emailRequest, jobName);
			Trigger trigger = createJobTrigger(jobDetail, dateTime, emailRequest);
			ScheduledInfo scheduledInfo = createScheduledInfo(jobName, emailRequest, dateTime);
			scheduler.scheduleJob(jobDetail, trigger);
			schedularInfoRepository.save(scheduledInfo);
			EmailResponse emailResponse = new EmailResponse(true, jobDetail.getKey().getName(),
					jobDetail.getKey().getGroup(), "Email Scheduled Successfully!");
			return ResponseEntity.ok(emailResponse);
		}

		catch (SchedulerException ex) {
			EmailResponse emailResponse = new EmailResponse(false, "Error scheduling email. Please try later!");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(emailResponse);
		}

	}

	
	private JobDetail createJobDetail(EmailRequest emailRequest, String jobName) {

		JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put("email", emailRequest.getEmail());
		jobDataMap.put("subject", emailRequest.getSubject());
		jobDataMap.put("body", emailRequest.getBody());

		return JobBuilder.newJob(ScheduledEmailJob.class).withIdentity(jobName, emailRequest.getJobGroup())
				.withDescription(emailRequest.getJobDescription()).usingJobData(jobDataMap).storeDurably().build();
	}

	private Trigger createJobTrigger(JobDetail jobDetail, ZonedDateTime startAt, EmailRequest emailRequest) {
		return TriggerBuilder.newTrigger().forJob(jobDetail)
				.withIdentity(jobDetail.getKey().getName(), "email-triggers")
				.withDescription(emailRequest.getTriggerDescription()).startAt(Date.from(startAt.toInstant()))
				.withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow()).build();
	}

	private ScheduledInfo createScheduledInfo(String jobName, EmailRequest emailRequest, ZonedDateTime startAt) {
		ScheduledInfo scheduledInfo = new ScheduledInfo();
		scheduledInfo.setJobName(jobName);
		scheduledInfo.setJobGroup(emailRequest.getJobGroup());
		scheduledInfo.setEmail(emailRequest.getEmail());
		scheduledInfo.setJobDescription(emailRequest.getJobDescription());
		scheduledInfo.setTriggerDescription(emailRequest.getTriggerDescription());
		scheduledInfo.setDateTime(emailRequest.getDateTime());
		scheduledInfo.setTimeZone(emailRequest.getTimeZone());
		scheduledInfo.setStartAt(startAt);
		return scheduledInfo;
	}
}