package io.oodles.emailscheduler.payload;

import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class EmailRequest {
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
