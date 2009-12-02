package com.googlecode.dady;

import org.quartz.JobDetail;
import org.quartz.Trigger;

public class RegisterObject {

    private JobDetail _job;
    private Trigger _trigger;

    public RegisterObject(JobDetail detail, Trigger trigger) {
	_job = detail;
	_trigger = trigger;
	
    }
    
    public JobDetail getJob(){
	return _job;
    }
    
    public Trigger getTrigger(){
	return _trigger;
    }
}
