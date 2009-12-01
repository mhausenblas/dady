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

public class MonitorService extends HttpServlet{

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


    public void init() throws ServletException {
	_sched = (Scheduler) getServletContext().getAttribute(Initialiser.SCHEDULER);
	_changesMap = (HashMap<Long, Feed>) getServletContext().getAttribute(Initialiser.CHANGES);
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

	if(pollInterval==null || pollInterval.length()==0)
	    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "missing parameter \"update\"");

	int updateInterval = 0;
	try{
	    updateInterval = Integer.parseInt(pollInterval);
	    log("updateInterval: "+updateInterval);
	}catch(NumberFormatException e){
	    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot parse the value of \"update=\""+pollInterval+" not a number.");
	}

	//create id
	Long id = System.currentTimeMillis();

	//generate Quartz job
	JobDetail jobDetail = new JobDetail(id.toString(),
		null,
		ChangeDetector.class);
	jobDetail.getJobDataMap().put("uri",uri);
	jobDetail.getJobDataMap().put("content",new HashSet<Nodes>());
	
	URL baseURI = new URL(req.getScheme(),
		req.getServerName(),
		req.getServerPort(),
                req.getContextPath());
	System.out.println(">>>>> "+baseURI);
	jobDetail.getJobDataMap().put("baseURI",baseURI);
	
	Feed feed = new Feed();
	feed.setFeedType("atom_1.0");
	feed.setTitle("Dataset change feed for "+uri);
	Link link = new Link();
	if(uri!=null)
	    link.setHref(uri.toASCIIString());
	feed.setOtherLinks(Collections.singletonList(link));
	Person author = new Person();
	author.setName("dady DCM");
	author.setUri("http://code.google.com/p/dady/");
	feed.setAuthors(Collections.singletonList(author));
	
	Content c = new Content();
	c.setValue("Notification feed for changes in "+uri+".\nMonitor intervall is "+pollInterval+" seconds.");
	feed.setSubtitle(c);
	
	jobDetail.getJobDataMap().put("changes",feed);
	_changesMap.put(id,feed);
	
	Trigger trigger = TriggerUtils.makeSecondlyTrigger(updateInterval);
	trigger.setName(id.toString());
	
	try {
	    _sched.scheduleJob(jobDetail, trigger);
	    
	} catch (SchedulerException e) {
	    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
	}
	
	resp.setContentType("text/plain");
	resp.getWriter().append(baseURI+"/changes?id="+id);
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO Auto-generated method stub
	doGet(req, resp);
    }
}

