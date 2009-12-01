package com.googlecode.dady;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Nodes;
import org.semanticweb.yars.nx.parser.Callback;

import com.ontologycentral.ldspider.Crawler;
import com.ontologycentral.ldspider.hooks.error.ErrorHandler;
import com.ontologycentral.ldspider.hooks.error.ErrorHandlerLogger;
import com.ontologycentral.ldspider.hooks.fetch.FetchFilterRdfXml;
import com.ontologycentral.ldspider.hooks.links.LinkFilterDefault;
import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.feed.synd.SyndLink;
import com.sun.syndication.feed.synd.SyndLinkImpl;

public class ChangeDetector implements Job{
Logger log = Logger.getLogger(ChangeDetector.class.getName());
    @Override
    public void execute(JobExecutionContext ctx) throws JobExecutionException {
	
	log.info(">>>Checking for updates");
	//fetch content
	URI uri = (URI) ctx.getJobDetail().getJobDataMap().get("uri");
	URL baseURI = (URL) ctx.getJobDetail().getJobDataMap().get("baseURI");
	List<URI> seeds = new ArrayList<URI>();
	seeds.add(uri);
	
	Crawler c = new Crawler();

	
	ErrorHandler eh = new ErrorHandlerLogger();
	c.setErrorHandler(eh);
	
	NodeCollector nc = new NodeCollector();
	c.setOutputCallback(nc);
	c.setLinkSelectionCallback(new LinkFilterDefault(eh));
	c.setFetchFilter(new FetchFilterRdfXml(eh));
	c.evaluate(seeds, 0, 1);
	
	Set<Nodes> content = nc.getContent();
	//fetch content
	
	
	//detect changes
	Set<Nodes> old = (Set<Nodes>)ctx.getJobDetail().getJobDataMap().get("content");
	
	if(old.size() != 0){
	    log.info("Old content of size "+old.size());
	    //compare
	    Set<Nodes> addedTriples = getAddedTriples(old,content);
	    Set<Nodes> deletedTriples = getDeletedTriples(old,content);
	    
	    //get feed for this uri
	    Feed changes = (Feed) ctx.getJobDetail().getJobDataMap().get("changes");
	    
	    List entries = changes.getEntries();
	    
	    if(addedTriples.size()!=0){
		updateFeed("data-added", uri, baseURI, addedTriples, entries);
	    }
	    if(deletedTriples.size() != 0){
		updateFeed("data-removed", uri, baseURI, deletedTriples, entries);
		
	    }
	  //update changes
	    changes.setEntries(entries);
	}
	old.clear();
	old.addAll(content);
	log.info(">>>Add now new changes of size "+content.size());
//	ctx.getJobDetail().getJobDataMap().put("content",content);
    }

    private void updateFeed(String relValue, URI uri, URL baseURI, Set<Nodes> addedTriples,
	    List entries) {
	//add update
	
	Long time = System.currentTimeMillis();
	File changeFile =new File(Initialiser.configFilePath, time.toString());
	try {
	    FileOutputStream fos = new FileOutputStream(changeFile);
	    for(Nodes n: addedTriples){
		fos.write((n.toN3()+"\n").getBytes());
		log.info(n.toN3());
	    }
	    fos.close();
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	
	Entry entry = new Entry();
	
	//set title
	
	//set id
	entry.setId(time.toString());
	
	Content c1 = new Content();
	

	//set title and summary
	if(relValue.contains("added")){
	    entry.setTitle("add event:"+time.toString());
	    c1.setValue(addedTriples.size()+" statements have been added to "+uri);
	}
	else{
	    entry.setTitle("remove event:"+time.toString());
	    c1.setValue(addedTriples.size()+" statements have been removed from "+uri);
	}
	entry.setSummary(c1);

	//set change URI value
	Link l = new Link();
	l.setRel(relValue);
	l.setHref(baseURI.toString()+"/"+time.toString());
	
//	System.out.println(l);
//	System.out.println(">>>>>"+baseURI.toString()+"/"+time.toString());
	
	entry.setOtherLinks(Collections.singletonList(l));
	entry.setCreated(new Date(System.currentTimeMillis()));
	entries.add(entry);
    }

    private Set<Nodes> getDeletedTriples(Set<Nodes> old, Set<Nodes> content) {
	Set<Nodes> clone = new HashSet<Nodes>();
	for(Nodes n: old)
	    clone.add(n);
	clone.removeAll(content);
	return clone;
    }

    private Set<Nodes> getAddedTriples(Set<Nodes> old, Set<Nodes> content) {
	Set<Nodes> clone = new HashSet<Nodes>();
	for(Nodes n: content)
	    clone.add(n);
	clone.removeAll(old);
	return clone;
    }
}

class NodeCollector implements Callback{

    
    private HashSet<Nodes> _content;

    public NodeCollector(){
	_content = new HashSet<Nodes>();
    }
    @Override
    public void endDocument() {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void processStatement(Node[] arg0) {
	_content.add(new Nodes(arg0[0],arg0[1],arg0[2]));
	
    }

    @Override
    public void startDocument() {
	// TODO Auto-generated method stub
	
    }
    
    public Set<Nodes> getContent(){
	return _content;
    }
}
