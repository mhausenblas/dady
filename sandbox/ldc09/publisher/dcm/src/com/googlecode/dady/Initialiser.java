package com.googlecode.dady;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.semanticweb.yars.nx.Node;

import com.sun.syndication.feed.synd.SyndFeed;

public class Initialiser implements ServletContextListener{
    private final static Logger log = Logger.getLogger(Initialiser.class.getSimpleName());
    public static final String SCHEDULER = "scheduler";
    public static final String CHANGES = "changes";
    public static final String CONTENT = "content";
    public static File configFilePath;
  
    
    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
	System.out.println("CONTAXT DESTROYED");
	 Scheduler sched = (Scheduler) arg0.getServletContext().getAttribute(SCHEDULER);
	try {
	    sched.shutdown();
	} catch (SchedulerException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
	SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
	try {
	    Scheduler sched = schedFact.getScheduler();
	    sched.start();
	    arg0.getServletContext().setAttribute(SCHEDULER, sched);
	} catch (SchedulerException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	HashMap<Long,SyndFeed> changesMap = new HashMap<Long, SyndFeed>();
//	HashMap<URI, Set<Node[]>> contentMap = new HashMap<URI, Set<Node[]>>();
	
	arg0.getServletContext().setAttribute(CHANGES, changesMap);
	configFilePath = new File(arg0.getServletContext().getRealPath("/"));
	System.out.println(configFilePath);
	
//	arg0.getServletContext().setAttribute(CONTENT, contentMap);
	
    }

}
