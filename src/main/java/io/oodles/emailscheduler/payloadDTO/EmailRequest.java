package io.oodles.emailscheduler.payloadDTO;


import java.time.ZoneId;
import java.time.LocalDateTime;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class EmailRequest {
	@NotEmpty
	private String jobGroup;
	@NotEmpty
	private String jobDescription;
	@NotEmpty
	private String triggerDescription;
	@NotEmpty
	@Email
	private String email;
	@NotEmpty
	private String subject;
	@NotEmpty
	private String body;
	@NotNull
	private LocalDateTime dateTime;
	@NotNull
	private ZoneId timeZone;
}
