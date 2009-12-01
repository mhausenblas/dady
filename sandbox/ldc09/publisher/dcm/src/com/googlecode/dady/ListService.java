package com.googlecode.dady;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;

public class ListService extends HttpServlet{
    private Scheduler _sched;

    public void init() throws ServletException {
	_sched = (Scheduler) getServletContext().getAttribute(Initialiser.SCHEDULER);
	
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
	//get parameter values
	PrintWriter pw = resp.getWriter();
	try {
	    for(Object o: _sched.getCurrentlyExecutingJobs()){
	        pw.append(o.toString()).append("\n");
	    }
	} catch (SchedulerException e) {
	    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
	}
	pw.close();
    }
}

