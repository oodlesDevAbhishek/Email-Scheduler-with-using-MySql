package io.oodles.emailscheduler.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.time.ZonedDateTime;
import io.oodles.emailscheduler.payload.EmailRequest;
import io.oodles.emailscheduler.payload.EmailResponse;
import io.oodles.emailscheduler.service.SchedularService;

@RestController
public class EmailSchedulerController {

	@Autowired
	SchedularService schedularService;

	@PostMapping("/scheduleEmail")
	public ResponseEntity<EmailResponse> scheduleEmail(@Valid @RequestBody EmailRequest emailRequest) {

		ZonedDateTime dateTime = ZonedDateTime.of(emailRequest.getDateTime(), emailRequest.getTimeZone());
		if (dateTime.isBefore(ZonedDateTime.now())) {
			EmailResponse EmailResponse = new EmailResponse(false, "dateTime must be after current time");
			return new ResponseEntity<>(EmailResponse, HttpStatus.BAD_REQUEST);
		} else
			return schedularService.scheduleEmailJob(emailRequest, dateTime);
	}

}
