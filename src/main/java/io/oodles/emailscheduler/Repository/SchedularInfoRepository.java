package io.oodles.emailscheduler.Repository;

import org.springframework.data.repository.CrudRepository;

import io.oodles.emailscheduler.modle.ScheduledInfo;

public interface SchedularInfoRepository extends  CrudRepository<ScheduledInfo, Long>{
	
	public ScheduledInfo findByJobName(String jobName);
}
