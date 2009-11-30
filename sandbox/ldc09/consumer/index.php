<?php
            
include_once('../../arc/ARC2.php');
include_once('lib/simplepie/simplepie.inc');


/* ARC RDF store config */
$config = array(
	'db_name' => 'arc2',
	'db_user' => 'root',
	'db_pwd' => 'root',
	'store_name' => 'dady',
); 

$store = ARC2::getStore($config);

$feed = new SimplePie();
	
if (!$store->isSetUp()) {
  $store->setUp();
  echo 'set up';
}

if(isset($_GET['void'])){  // load voiD data from URI
	
	$URI = $_GET['void'];

	if(!inCache($URI)){	
		loadData($URI);
	}
	
	$notification = discoverUpdate($URI); // evaluate voiD to learn about supported notification mechanism
	
	if($notification["type"] == "http://purl.org/NET/dady#AtomUpdateSource") { 
		echo "polling <a href='" . $notification["src"] . "'>" .  $notification["src"] . "</a> for changes:";
		updateViaAtom($notification["src"]);
	}
	else echo "is not supported yet";
	
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
	
	$ret = array();
			
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
			$notificationSrcDoc = $row['notificationDoc'];
			
			if($dynaType == "http://purl.org/NET/dady#RegularUpdates") {
				$ret["src"] = $notificationSrcDoc;
				$ret["type"] = $updateSrcType;
			}
		}
	}
		
	return $ret;
}

function updateViaAtom($feedURI){
	global $feed;
	
	$feed->set_feed_url($feedURI);
	$feed->enable_cache(true);
	$feed->set_cache_location('cache');
	$feed->set_cache_duration(60);
	$feed->init();
	if ($feed->error()) {
		echo $feed->error();
	}
	foreach ($feed->get_items() as $item) {
		echo "<p>";
		echo "title: " . $item->get_title() . "<br />\n";
 		echo "date: " . $item->get_date('Y-m-d@H:i:s') . "<br />\n"; 
		echo "type: " .  $item->get_link(0, "data-add") . "<br />\n";
		echo "</p>";		
	}
}

?>