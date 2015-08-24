<?php

function httpStatus($code) {
        $status = array(  
            200 => 'OK',
            404 => 'Not Found',   
            405 => 'Method Not Allowed',
            500 => 'Internal Server Error',
        ); 
        return ($status[$code])?$status[$code]:$status[500]; 
}

function httpResponse($data, $status = 200) {
        header("HTTP/1.1 " . $status . " " . httpStatus($status));
        echo json_encode($data);
}

function echoError($info) {
        $result = array();
        $result['status'] = 'error';
        $result['info'] = $info;
        httpResponse($result, 200);
}

function echoSuccess($info) {
        $result = array();
        $result['status'] = 'success';
        $result['info'] = $info;
        httpResponse($result, 200);
}

function dbConnect() {
        $servername = "localhost";
        $username = "syncserver";
        $password = "serverpw";
        $dbname = "sync_users";

        $conn = new mysqli($servername, $username, $password, $dbname);

        if ($conn->connect_error) {
                echoError('database');
                exit;
        } 
        return $conn;
}

function dbInsert($table, $field, $values) {
        $conn = dbConnect();
        $sql = "INSERT INTO " . $table . " (" . $field . ") VALUES (" . $values .")";
        if ($conn->query($sql) != TRUE) {
                echoError("insert" . $table);
                $conn->close();
                exit;
        }
        $conn->close();
}

function dbDelete($table, $cond) {
        $conn = dbConnect();
        $sql = "DELETE FROM " . $table . " WHERE " . $cond;
        if ($conn->query($sql) != TRUE) {
                echoError("delete" . $table);
                $conn->close();
                exit;
        }
        $conn->close();
}

function dbUpdate($table, $set, $cond) {
        $conn = dbConnect();
        $sql = "UPDATE " . $table . " SET " . $set . " WHERE " . $cond;
        if ($conn->query($sql) != TRUE) {
                echoError("update" . $table);
                $conn->close();
                exit;
        }
        $conn->close();
}

function dbSelect($table, $target, $cond) {
        $conn = dbConnect();
        $sql = "SELECT " . $target . " FROM " . $table . " WHERE " . $cond;
        $result = $conn->query($sql);
        if (!$result) {
                echoError("select" . $table);
                $conn->close();
                exit;
        }
        return $result;
        $conn->close();
}

function checkNULL($str, $field) {
        if ($str == "") {
                echoError("null_string_" . $field);
                exit;
        }        
}

function checkOnline($user) {
        $result = dbSelect('users', 'online', "username='" . $user . "'");
        if ($result->num_rows == 0) {
                echoError("no_user");
                exit;
        }
        $row = $result->fetch_assoc();
        if (0 == $row['online']) {
                echoError("not_online");
                exit;
        }
}

function checkUser($user, $flag) {
        $result = dbSelect('users', 'username', "username='" . $user . "'");
        if ($result->num_rows > 0 && $flag == "no_user") {
                echoError("user_existed");
                exit;
        }
        if ($result->num_rows == 0 && $flag == "has_user" ) {
                echoError("no_user");
                exit;
        }
}

function checkRoom($room, $flag) {
        $result = dbSelect('rooms', 'roomname', "roomname='" . $room . "'");
        if ($result->num_rows > 0 && $flag == "no_room") {
                echoError("room_existed");
                exit;
        }
        if ($result->num_rows == 0 && $flag == "has_room" ) {
                echoError("no_room");
                exit;
        }
}

function checkDevice($device, $flag) {
        $result = dbSelect('devices', 'tag', "tag='" . $device . "'");
        if ($result->num_rows > 0 && $flag == "no_device") {
                echoError("device_existed");
                exit;
        }
        if ($result->num_rows == 0 && $flag == "has_device" ) {
                echoError("no_device");
                exit;
        }
}

function checkPassword($user, $pw) {
        $result = dbSelect('users', 'password', "username='" . $user . "'");
        if ($result->num_rows == 0) {
                echoError("no_user");
                exit;
        }
        $row = $result->fetch_assoc();
        if ($pw != $row['password']) {
                echoError("wrong_password");
                exit;
        }
}

function fieldUsers($method, $target, $payload) {
        switch ($method) {
        case "GET":
                $cond = '1';
                if ($target != "") $cond = "username='" . $target . "'";
                $result = dbSelect('users', '*', $cond);
                $response = array();
                while ($row = $result->fetch_assoc()) {
                        $response[$row['username']] = $row['online'];
                }
                httpResponse($response, 200);
                break;
        case "POST":
        case "PUT":
                checkNULL($target, 'user');
                checkUser($target, "no_user");
                checkNULL($payload['pw']);
                dbInsert('users', 'username, password, online', "'" . $target . "', '" . $payload['pw'] . "', 0");  
                echoSuccess("create_user");
                break;
        case "DELETE":
                checkNULL($targeti, 'user');
                checkNULL($payload['pw']);
                checkPassword($target, $payload['pw']);
                dbDelete('users', "username='" . $target . "'");       
                echoSuccess("delete_user");
                break;
        default:
                echoError('Invalid Method');
                break;
        }
}

function fieldLogin($method, $target, $payload) {
        switch ($method) {
        case "GET":
                checkNULL($target, 'user');
                $result = dbSelect('users', 'online', "username='" . $target . "'");
                if ($result->num_rows == 0) {
                        echoError("no_user");
                }
                else {
                        $row = $result->fetch_assoc();
                        httpResponse(array("online" => $row['online']), 200);
                }
                break;
        case "POST":
        case "PUT":
                checkNULL($target, 'user');
                checkNULL($payload['pw'], 'password');
                checkPassword($target, $payload['pw']);
                dbUpdate('users', 'online=1', "username='" . $target . "'");
                echoSuccess("login_user");
                break;
        case "DELETE":
                checkNULL($target, 'user');
                checkOnline($target);
                dbUpdate('users', 'online=0', "username='" . $target . "'");
                echoSuccess("logout_user");
                break;
        default:
                echoError('Invalid Method');
                break;
        }
}

function fieldRooms($method, $target, $payload) {
        switch ($method) {
        case "GET":
                $rcond = '1';
                if ($target != "") $rcond = "owner='" . $target . "'";
                $result = dbSelect('rooms', '*', $rcond);
                $response = array();
                while ($row = $result->fetch_assoc()) {
                        $response[$row['roomname']] = $row['owner'];
                }
                httpResponse($response, 200);
                break;
        case "POST":
        case "PUT":
                checkNULL($target, 'room');
                checkRoom($target, 'no_room');
                checkNULL($payload['user'], 'user');
                checkOnline($payload['user']);
                dbInsert('rooms', 'roomname, owner', "'" . $target . "', '" . $payload['user'] . "'");  
                echoSuccess("create_room");
                break;
        case "DELETE":
                checkNULL($target, 'room');
                checkRoom($target, 'has_room');
                dbDelete('rooms', "roomname='" . $target . "'");       
                echoSuccess("delete_room");
                break;
        default:
                echoError('Invalid Method');
                break;
        }

}

function fieldDevices($method, $target, $payload) {
        switch ($method) {
        case "GET":
                $rcond = '1';
                if ($target != "") $rcond = "location='" . $target . "'";
                $result = dbSelect('devices', 'tag, devicetime', $rcond);
                $response = array();
                while ($row = $result->fetch_assoc()) {
                        $response[$row['tag']] = $row['devicetime'];
                }
                httpResponse($response, 200);
                break;
        case "POST":
                checkNULL($target, 'device');
                checkDevice($target, 'no_device');
                dbInsert('devices', 'tag, location, command, timestamp, devicetime', 
                        "'" . $target . "', '', '', '', ''");  
                echoSuccess("register_device");
                break;
        case "PUT":
                checkNULL($target, 'device');
                checkDevice($target, 'has_device');
                checkNULL($payload['room'], 'room');
                checkRoom($payload['room'], 'has_room');
                dbUpdate('devices', "location='" . $payload['room'] . "'", "tag='" . $target . "'");
                echoSuccess("update_device");
                break;
        case "DELETE":
                checkNULL($target, 'device');
                checkDevice($target, 'has_device');
                dbDelete('devices', "tag='" . $target . "'");       
                echoSuccess("delete_device");
                break;
        default:
                echoError('Invalid Method');
                break;
        }
}

function fieldPlays($method, $target, $payload) {
        switch ($method) {
        case "GET":
                checkNULL($target, 'device');
                checkDevice($target, 'has_device');
                $result = dbSelect('devices', 'devicetime', "tag='" . $target . "'");
                $response = $result->fetch_assoc();
                httpResponse($response, 200);
                break;
        case "POST":
                checkNULL($target, 'room');
                checkRoom($target, 'has_room');
                checkNULL($payload['user'], 'user');
                checkOnline($payload['user']);
                dbUpdate('devices', "command='" . $payload['cmd'] . "', timestamp='" . $payload['ts'] . "'", 
                        "location='" . $target . "'");
                echoSuccess("set_play");
                break;
        case "PUT":
                checkNULL($target, 'device');
                checkDevice($target, 'has_device');
                dbUpdate('devices', "devicetime='" . $payload['dt'] . "'", "tag='" . $target . "'");
                $result = dbSelect('devices', 'command, timestamp', "tag='" . $target . "'");
                $response = $result->fetch_assoc();
                httpResponse($response, 200);
                dbUpdate('devices', "command=''", "tag='" . $target . "'");
                break;
        case "DELETE":
                checkNULL($target, 'room');
                checkRoom($target, 'has_room');
                dbUpdate('devices', "command='', timestamp=''", 
                        "location='" . $target . "'");
                echoSuccess("reset_play");
                break;
        default:
                echoError('Invalid Method');
                break;
        }

}

header("Access-Control-Allow-Orgin: *");
header("Access-Control-Allow-Methods: *");
header("Content-Type: application/json");
$ins = $_GET['ins'];
list($field, $target) = explode("/", $ins);
$method = $_SERVER['REQUEST_METHOD'];
$request_body = file_get_contents('php://input');
$payload = json_decode($request_body, true);

switch ($field) {
case 'users':
        fieldUsers($method, $target, $payload);
        break;
case 'login':
        fieldLogin($method, $target, $payload);
        break;
case 'rooms':
        fieldRooms($method, $target, $payload);
        break;
case 'devices':
        fieldDevices($method, $target, $payload);
        break;
case 'plays':
        fieldPlays($method, $target, $payload);
        break;
default:
        echoError('Invalid Field');
        break;
}

?>
