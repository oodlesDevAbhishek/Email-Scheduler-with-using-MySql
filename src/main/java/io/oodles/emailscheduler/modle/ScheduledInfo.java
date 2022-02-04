package io.oodles.emailscheduler.modle;

import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ScheduledInfo {
	
	@Id
	@GeneratedValue
	private long id;
	@NotNull
	@Column(unique = true)
	private String jobName;
	private String jobGroup;
	private String jobKey;
	private String triggerKey;
	@Email
	private String email;
	private String subject;
	private LocalDateTime dateTime;
	private ZoneId timeZone;
	private String jobDescription;
	private String triggerDescription;
	private ZonedDateTime startAt;
	
	
}
