<?php
/*
BH from https://gist.github.com/el3zahaby/9e60f1ae3168c38cc0f0054c15cd6a83
BH from https://stackoverflow.com/questions/31566835/how-to-get-the-mp4-url-for-youtube-videos-using-youtube-v3-api
Made by [egy.js](https://www.instagram.com/egy.js/);
v2.0
*/
header('Access-Control-Allow-Origin: *');
header('Content-Type: application/json');


function get_error($ErrorExaction){
    $myObj = new stdClass();
    $myObj->error = true;
    $myObj->msg = $ErrorExaction;

    $myObj->madeBy = "A.El-zahaby";
    $myObj->instagram = "egy.js";
    $myJSON = json_encode($myObj,JSON_PRETTY_PRINT);
    echo $myJSON;
    exit;
}

function get_log($dump){
    if (isset($_GET['log'])) var_dump($dump).'\n\n\n';
}
if(isset($_GET['url']) && $_GET['url'] != ""){
    parse_str( parse_url( $_GET['url'], PHP_URL_QUERY ), $vars );


    $id=$vars['v'];
    $dt=file_get_contents("https://www.youtube.com/get_video_info?video_id=$id&el=embedded&ps=default&eurl=&gl=US&hl=en");
    if (strpos($dt, 'status=fail') !== false) {

        $x=explode("&",$dt);
        $t=array(); $g=array(); $h=array();

        foreach($x as $r){
            $c=explode("=",$r);
            $n=$c[0]; $v=$c[1];
            $y=urldecode($v);
            $t[$n]=$v;
        }

        $x=explode("&",$dt);
        foreach($x as $r){
            $c=explode("=",$r);
            $n=$c[0]; $v=$c[1];
            $h[$n]=urldecode($v);
        }
        $g[]=$h;
        $g[0]['error'] = true;
        $g[0]['instagram'] = "egy.js";
        $g[0]['apiMadeBy'] = 'El-zahaby';
        echo json_encode($g,JSON_PRETTY_PRINT);

    }else{

        $x=explode("&",$dt);
        $t=array(); $g=array(); $h=array();

        foreach($x as $r){
            $c=explode("=",$r);
            $n=$c[0]; $v=$c[1];
            $y=urldecode($v);
            $t[$n]=$v;
        }
        $streams = explode(',',urldecode($t['url_encoded_fmt_stream_map']));
//        if(empty($streams[0])){ get_error('ops! this video has something wrong! :( '); }
        if(empty($streams[0])){
            get_log($streams);
        }
        foreach($streams as $dt){
            $x=explode("&",$dt);
            foreach($x as $r){
                $c=explode("=",$r);
                if ($c[0]  == 'itag'){ // reference:  https://superuser.com/q/1386658
                    switch ($c[1]){
                        case '18':
                            $h['mimeType'] = "mp4";
                            $h['width'] = "640";
                            $h['height'] = "360";
                            $h['qualityLabel'] = '360p';
                            break;
                        case '22':
                            $h['mimeType'] = "mp4";
                            $h['width'] = "1280";
                            $h['height'] = "720";
                            $h['qualityLabel'] = '720p';
                            break;
                        case '43':
                            $h['mimeType'] = "webm";
                            $h['width'] = "640";
                            $h['height'] = "360";
                            $h['qualityLabel'] = '360p';
                            break;
                        default:
                            $h['mimeType'] = null;
                            $h['width'] = null;
                            $h['height'] = null;
                            $h['qualityLabel'] = '';
                    }
                }
                $n=$c[0]; /* => */ $v=$c[1];
                $h[$n]=urldecode($v);

            }
            $g[]=$h;
        }
        echo json_encode($g,JSON_PRETTY_PRINT);
        // var_dump( $g[1]["quality"],true);
    }
}else{

     get_error("Ops, there is no youtube link!");

}