package io.oodles.emailscheduler.payloadDTO;

import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.validation.constraints.NotNull;

import lombok.Data;
@Data
public class EmailRescheduleRequest {
	@NotNull
	private String jobName;
	@NotNull
	private String jobGroup;
	@NotNull
	private String triggerKey;
	@NotNull
	private String TriggerDescription;
	@NotNull
	private LocalDateTime dateTime;
	@NotNull
	private ZoneId timeZone;
}
