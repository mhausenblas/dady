<?php

include_once("../../arc/ARC2.php");

$config = array(
  'db_host' => 'localhost',
  'db_name' => 'arc2',
  'db_user' => 'root',
  'db_pwd' => 'root',
  'store_name' => 'dady',
  //'sem_html_formats' => 'adr-foaf dc erdf hcard-foaf openid rdfa rel-tag-skos xfn',
  /* endpoint */
  'endpoint_features' => array( 'select', 'ask', 'construct', 'load', 'insert', 'delete'),
  'endpoint_timeout' => 60, /* not implemented in ARC2 preview */
);

/* instantiation */
$ep = ARC2::getStoreEndpoint($config);

/* request handling */
$ep->go();