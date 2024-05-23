<?php

$zipfilename='hotwheels_loop.zip';
$filename='videos/loop_barely.mov';

$zip = new ZipArchive;
if ($zip->open($zipfilename) === TRUE) {
      header('Access-Control-Allow-Origin: *');
   	  header('Content-Type: application/octet-stream');
  	  header('Content-Description: File Transfer');
  	  header("Content-Disposition: attachment; filename=\"$filename\"");
      header('Content-Transfer-Encoding: binary');
      header('Expires: 0');
      header('Cache-Control: must-revalidate');
      header('Pragma: public');
    echo $zip->getFromName($filename);
    $zip->close();
} else {
    echo 'failed';
}
?>
