package com.googlecode.dady;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;

public class ListService extends HttpServlet{
    private Scheduler _sched;
    private HashMap<URI, RegisterObject> _registeredMap;

    public void init() throws ServletException {
	_sched = (Scheduler) getServletContext().getAttribute(Initialiser.SCHEDULER);
	_registeredMap = (HashMap<URI, RegisterObject>) getServletContext().getAttribute(Initialiser.REGISTERED);
	
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
	//get parameter values
	PrintWriter pw = resp.getWriter();
	pw.append("<html><head><title>DCM  scheduled jobss</title></head>");
	pw.append("<body><h1>Scheduled Jobs</h1>");
	pw.append("<table><tr><td>URI</td><td>Job</td><td>Trigger</td></tr>");
	for(Entry<URI, RegisterObject> ent: _registeredMap.entrySet()){
	    pw.append("<tr><td>"+ent.getKey()+"</td><td>"+ent.getValue().getJob()+"</td><td>"+ent.getValue().getTrigger()+"</td></tr>");
	}
	pw.append("</table></html>");
	pw.close();
    }
}

