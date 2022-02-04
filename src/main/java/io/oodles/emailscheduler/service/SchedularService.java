package io.oodles.emailscheduler.service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;//this is a imp 
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import io.oodles.emailscheduler.Repository.SchedularInfoRepository;
import io.oodles.emailscheduler.modle.ScheduledInfo;
import io.oodles.emailscheduler.payloadDTO.EmailRequest;
import io.oodles.emailscheduler.payloadDTO.EmailRescheduleRequest;
import io.oodles.emailscheduler.payloadDTO.EmailResponse;
import io.oodles.emailscheduler.scheduler.MySchedulerListener;
import io.oodles.emailscheduler.scheduler.ScheduledEmailJob;

@Service
public class SchedularService {

	@Autowired
	private Scheduler scheduler;
	@Autowired
	private SchedularInfoRepository schedularInfoRepository;
	@Autowired
	private MySchedulerListener mySchedulerListener;

	@PostConstruct
	public void postContruct() {
		try {
			scheduler.getListenerManager().addSchedulerListener(mySchedulerListener);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

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
			ScheduledInfo scheduledInfo = createScheduledInfo(jobName, emailRequest, dateTime, trigger.getKey(),
					jobDetail.getKey());
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

	public ResponseEntity<EmailResponse> reScheduleEmailJob(EmailRescheduleRequest emailRescheduleRequest,
			ZonedDateTime startAt) {
		try {
			TriggerKey triggerKey = TriggerKey.triggerKey(emailRescheduleRequest.getTriggerKey());
			JobKey jobKey = new JobKey(emailRescheduleRequest.getJobName(), emailRescheduleRequest.getJobGroup());
			JobDetail jobDetail = scheduler.getJobDetail(jobKey);
			Trigger newTrigger = TriggerBuilder.newTrigger().forJob(jobDetail)
					.withIdentity(jobDetail.getKey().getName(), "email-triggers")
					.withDescription(emailRescheduleRequest.getTriggerDescription())
					.startAt(Date.from(startAt.toInstant()))
					.withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
					.build();

			scheduler.rescheduleJob(triggerKey, newTrigger);
			ScheduledInfo scheduledInfo = schedularInfoRepository.findByJobName(emailRescheduleRequest.getJobName());
			scheduledInfo.setDateTime(emailRescheduleRequest.getDateTime());
			scheduledInfo.setTimeZone(emailRescheduleRequest.getTimeZone());
			scheduledInfo.setTriggerKey(newTrigger.getKey().getName());
			scheduledInfo.setStartAt(startAt);
			schedularInfoRepository.save(scheduledInfo);
			EmailResponse emailResponse = new EmailResponse(true, jobDetail.getKey().getName(),
					jobDetail.getKey().getGroup(), "Email ReScheduled Successfully!");
			return ResponseEntity.ok(emailResponse);
		}

		catch (SchedulerException e) {
			e.printStackTrace();
			EmailResponse emailResponse = new EmailResponse(false, "Error Rescheduling Trigger. Please try later!");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(emailResponse);
		}

	}

	public ResponseEntity<?> deleteJob(String jobName, String jobGroup) {
		JobKey jobKey = new JobKey(jobName, jobGroup);
		try {
			scheduler.deleteJob(jobKey);
			ScheduledInfo scheduledInfo = schedularInfoRepository.findByJobName(jobName);
			if (scheduledInfo != null) {
				schedularInfoRepository.delete(scheduledInfo);
				return new ResponseEntity<>("Successfully Deleted with JonName " + jobName, HttpStatus.OK);
			} else {
				return new ResponseEntity<>("Record not present with JobName" + jobName, HttpStatus.NOT_FOUND);

			}

		} catch (SchedulerException e) {
			e.printStackTrace();
			return new ResponseEntity<>("Error... !", HttpStatus.INTERNAL_SERVER_ERROR);

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

	private ScheduledInfo createScheduledInfo(String jobName, EmailRequest emailRequest, ZonedDateTime startAt,
			TriggerKey triggerKey, JobKey jobKey) {
		ScheduledInfo scheduledInfo = new ScheduledInfo();
		scheduledInfo.setJobName(jobName);
		scheduledInfo.setJobKey(jobKey.toString());
		scheduledInfo.setTriggerKey(triggerKey.toString());
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