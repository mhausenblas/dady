package com.googlecode.dady;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.WireFeedOutput;

public class ChangesService extends HttpServlet{

    private static final String DEFAULT_FEED_TYPE = "default.feed.type";
    private static final String FEED_TYPE = "type";
    private static final String MIME_TYPE = "application/atom+xml";
    private static final String COULD_NOT_GENERATE_FEED_ERROR = "Could not generate feed";

    private static final DateFormat DATE_PARSER = new SimpleDateFormat("yyyy-MM-dd");

    private String _defaultFeedType = "atom_1.0";
    
    private HashMap<Long, Feed> _changesMap;


    public void init() throws ServletException {
	_changesMap = (HashMap<Long, Feed>) getServletContext().getAttribute(Initialiser.CHANGES);
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
	//get parameter values

	String idString = req.getParameter("id");
	Long id = Long.parseLong(idString);
	Feed feed = _changesMap.get(id);
	if(feed == null){
	    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Could not found a feed for id "+id);
	    return;
	}
	try{
	    resp.setContentType(MIME_TYPE);
	    
	    WireFeedOutput output = new WireFeedOutput();
	    output.output(feed,resp.getWriter());
	}
	catch (FeedException ex) {
	    String msg = COULD_NOT_GENERATE_FEED_ERROR;
	    log(msg,ex);
	    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,msg);
	}
    }
}

