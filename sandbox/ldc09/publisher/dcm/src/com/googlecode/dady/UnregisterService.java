package com.googlecode.dady;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;
import org.semanticweb.yars.nx.Nodes;

import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.feed.atom.Person;

public class UnregisterService extends HttpServlet{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    //URL parameter name for the dataset
    private static final String DATASET = "dataset";

    //URL parameter for the monitoring interval (values are seconds)
    private static final String MONITOR_INTERVAL = "update";

    private Scheduler _sched;

    private HashMap<Long, Feed> _changesMap;

    private HashMap<URI, RegisterObject> _registeredMap;


    public void init() throws ServletException {
	_sched = (Scheduler) getServletContext().getAttribute(Initialiser.SCHEDULER);
	_registeredMap = (HashMap<URI, RegisterObject>) getServletContext().getAttribute(Initialiser.REGISTERED);
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
	//get parameter values

	String pollInterval = req.getParameter(MONITOR_INTERVAL);
	URI uri = null;
	try{
	    uri = new URI(req.getParameter(DATASET));
	    log("URI: "+uri);
	}catch(Exception e){
	    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
	}

	try {
	    RegisterObject regObj = _registeredMap.remove(uri);
	    if(regObj != null && uri!=null){
		_sched.unscheduleJob(regObj.getTrigger().getName(), uri.toASCIIString());
		_sched.deleteJob(regObj.getJob().getName(), uri.toASCIIString());
	    }
	    
	} catch (SchedulerException e) {
	    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
	}
	
	resp.setContentType("text/plain");
	resp.getWriter().append("Successful delete change detection for "+uri);
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO Auto-generated method stub
	doGet(req, resp);
    }
}

