package io.oodles.emailscheduler.payload;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailResponse {
	private boolean isSuccess;
	private String jobId;
	private String jobGroup;
	private String message;
	
	public EmailResponse(boolean isSuccess, String message) {
		super();
		this.isSuccess = isSuccess;
		this.message = message;
	}
}
