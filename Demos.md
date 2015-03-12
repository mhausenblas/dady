# Demos #

## LDC 09 ##

See [LDC09 dataset dynamics hacking](http://www.linkeddatacamp.org/wiki/LinkedDataCampVienna2009/DatasetDynamics#Simple_Demo_.28by_LiDRC.29) for the background and related blog posts [1](http://webofdata.wordpress.com/2009/12/04/ldc09-dataset-dynamcis-screencast/) and [2](http://webofdata.wordpress.com/2009/12/02/ldc09-wrap-up/).


### Description ###

![http://dady.googlecode.com/hg/sandbox/ldc09/ldc09-demo-setup.png](http://dady.googlecode.com/hg/sandbox/ldc09/ldc09-demo-setup.png)


  * DCM interface:
    * register observation ... http://uldis.deri.ie/dcm/register?dataset={URI}&update={time in sec}
    * unregister observation ...  http://uldis.deri.ie/dcm/unregister?dataset={URI}
    * view list of current observations ... http://uldis.deri.ie/dcm/list
  * DCW interface:
    * http://ld2sd.deri.org/dady/consumer/dcw.html

### Delta computation ###
The current implementation computes only the explicit changes in the observed dataset.
That is the statements that are added or removed directly to/from the file.
The entries in the [Atom](http://tools.ietf.org/html/rfc4287) change feed contain links to the list of statements, which are either detected as being removed or added. Each file contains a line separated list of statements that changed; the diff representation returned is in [Ntriples](http://www.w3.org/2001/sw/RDFCore/ntriples/) format.

Semantics:
  * single @rel="data-added" in a single Atom entry ... data has been added, diff available in @href
  * single @rel="data-removed" in a single Atom entry ... data has been removed, diff available in @href
  * a @rel="data-added" and a @rel="data-removed" in a single Atom entry ... data has been updated, diffs are available in the respective @hrefs

### Example ###

  1. PUBLISHER: give the DCM an HTTP URI of an RDF/XML document and specify the surveillance interval
    * EXAMPLE: http://140.203.155.46/dcm/register?dataset=http://ld2sd.deri.org/dady/publisher/test-dataset.rdf&update=10
  1. CONSUMER (direct): use the return value of the DCM to follow changes
    * EXAMPLE: http://140.203.155.46/dcm/changes?id=1259750030627
  1. CONSUMER (DCW): use the DCW to consume updates
    * EXAMPLE: visit http://ld2sd.deri.org/dady/consumer/dcw.html and provide the URI of a voiD+dady document


### Code and Dependencies ###

  * http://code.google.com/p/dady/source/browse/#hg/sandbox/ldc09
  * requires:
    * for the publisher (DCM): Java servlet container such as Tomcat, jetty, Glassfish, etc.
    * for the consumer (DCW): PHP, [ARC2](http://arc.semsol.org/)
  * notes:
    * DCM: input for the dataset is currently limited to one RDF/XML document

Libraries used:

  * Linked Data Spider: http://code.google.com/p/ldspider/ (contained in dcm/lib)
  * Quartz scheduler: http://www.quartz-scheduler.org/ (contained in dcm/lib)
  * ROME Atom lib: https://rome.dev.java.net/ (contained in dcm/lib)