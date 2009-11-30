<?php
            
include_once('../../arc/ARC2.php');


/* ARC RDF store config */
$config = array(
	'db_name' => 'arc2',
	'db_user' => 'root',
	'db_pwd' => 'root',
	'store_name' => 'dady',
); 

$store = ARC2::getStore($config);

if (!$store->isSetUp()) {
  $store->setUp();
  echo 'set up';
}

if(isset($_GET['void'])){  // load chartr data from URI
	
	$URI = $_GET['void'];

	if(!inCache($URI)){	
		loadData($URI);
	}
	
	echo discoverUpdate($URI); 
}


if(isset($_GET['reset'])){ 
	$store->reset();
	echo 'reset store';
}


function inCache($URI){
	global $store;
	
	$cmd = "SELECT * FROM <$URI> WHERE { ?s ?p ?o .}";

	$results = $store->query($cmd);
	
	if($results['result']['rows']) return true;
	else return false;
}

function loadData($URI){
	global $store;

	$cmd = "LOAD <$URI> INTO <$URI>";
	$rs = $store->query($cmd);
}

function discoverUpdate($URI){
	global $store;
	
	$notification = "";
			
	$cmd = "
		PREFIX void: <http://rdfs.org/ns/void#> .
		PREFIX dady: <http://purl.org/NET/dady#> .

		SELECT ?dynaType ?updateSrcType ?notificationDoc FROM <$URI> WHERE {
			?ds	a void:Dataset ; 
				dady:dynamics ?dynamics .
			?dyna	a ?dynaType ;				
					dady:update ?updatesrc .
			?updatesrc	a ?updateSrcType ;
						dady:notification ?notificationDoc .
		}
	";

	if ($rows = $store->query($cmd, 'rows')) {
		foreach ($rows as $row) {
			$dynaType = $row['dynaType'];
			$updateSrcType = $row['updateSrcType'];
			$notificationDoc = $row['notificationDoc'];
			
			if($dynaType == "http://purl.org/NET/dady#RegularUpdates") {
				if($updateSrcType == "http://purl.org/NET/dady#AtomUpdateSource") { // an Atom feed
					$notification = $notificationDoc;
				}
			}
		}
	}
		
	return $notification;
}



?>