<html>
<head>
<title>Web Player - SyncWatcher
</title>
<script>

var flagReg = false;
var flagJoin = false;
var flagLoad = false;
var deviceName = "";
var roomName = "";
var setTime = null;

function sendHttp(method, path, payload, callback) {
        var xmlhttp = new XMLHttpRequest();
        xmlhttp.onreadystatechange = function() {
                if (xmlhttp.readyState == 4) {
                        callback(xmlhttp.responseText);
                }
        }
        xmlhttp.open(method, path, true);
        if (method == "GET") xmlhttp.send();
        else {
                xmlhttp.setRequestHeader("Content-type","application/json");
                xmlhttp.send(payload);
        }
}

function showRes(area, text) {
        document.getElementById(area).innerHTML = text;
}

function updateRoomList(user) {
        var callback = function(resp) {
                if ("[]" == resp) {
                        showRes("joinRes", "No result in room list!");
                }
                else {
                        var data = JSON.parse(resp);
                        var list = document.getElementById("roomList");
                        for (var i = list.options.length - 1; i >= 0; i--) {
                                list.remove(i);
                        }
                        for (var key in data) {
                                var option = document.createElement("option");
                                option.text = key;
                                list.add(option)
                        }
                        showRes("joinRes", "Room list is updated!");
                }
        };
        sendHttp("GET", "api/rooms/" + user, "", callback); 
        showRes("joinRes", "Connecting...");
}

function regDevice() {
        if (flagReg) return;
        var tag = document.getElementById("deviceTag").value;
        if ("" == tag) {
                showRes("regRes", "Device needs a tag!");
                return;
        }
        deviceName = tag;
        var callback = function(resp) {
                var data = JSON.parse(resp);
                if ("success" == data["status"]) {
                        showRes("regRes", "Registration is successful!");
                        flagReg = true;
                        updateRoomList("");
                }
                else {
                        showRes("regRes", "Registration failed!");
                }
        };
        sendHttp("POST", "api/devices/" + tag, "", callback); 
        showRes("regRes", "Connecting...");
}

function deregDevice() {
        if (!flagReg) return;
        if (flagJoin) {
                showRes("regRes", "Device has joined a room!");
                return;
        }
        var callback = function(resp) {
                var data = JSON.parse(resp);
                if ("success" == data["status"]) {
                        showRes("regRes", "Deregistration is completed!");
                        flagReg = false;
                        deviceName = "";
                }
                else {
                        showRes("regRes", "Deregistration failed!");
                }
        };
        sendHttp("DELETE", "api/devices/" + deviceName, "", callback); 
        showRes("regRes", "Connecting...");
}

function joinRoom() {
        if (flagJoin) return;
        if (!flagReg) {
                showRes("joinRes", "Device didn't register to server!");
                return;
        }
        var list = document.getElementById("roomList");
        var room = list.options[list.selectedIndex].text;
        if ("" == room) {
                showRes("joinRes", "Invalid room name!");
                return;
        }
        roomName = room;
        var callback = function(resp) {
                var data = JSON.parse(resp);
                if ("success" == data["status"]) {
                        showRes("joinRes", "Devices is joined!");
                        flagJoin = true;
                        if (flagLoad) {
                                setTime = window.setInterval(function() {playTimer();}, 2000);
                        }
                }
                else {
                        showRes("joinRes", "Failed to join!");
                }
        };
        var obj = {};
        obj["room"] = room;
        sendHttp("PUT", "api/devices/" + deviceName, JSON.stringify(obj), callback); 
        showRes("joinRes", "Connecting...");
}

function disjoinRoom() {
        if (!flagJoin) return;
        window.clearInterval(setTime);
        flagJoin = false;
        roomName = "";
        if (flagLoad) {
                var video = document.getElementById("videoArea");
                video.pause();
                video.currentTime = 0;
        }
        showRes("joinRes", "Device disjoined from room!");
}

function playTimer() {
        var callback = function(resp) {
                var data = JSON.parse(resp);
                var video = document.getElementById("videoArea");
                switch (data["command"]) {
                        case "":
                                break;
                        case "play":
                                video.currentTime = parseInt(data["timestamp"]);        
                                video.play();
                                break;                 
                        case "sync":
                                video.currentTime = parseInt(data["timestamp"]);        
                                video.pause();
                                break;                 
                        case "pause":
                                video.pause();
                                break;                 
                        case "resume":
                                video.play();
                                break;                 
                        case "stop":
                                video.pause();
                                video.currentTime = 0; 
                                break;                 
                        default:
                                showRes("joinRes", "Server Command Error!");
                                window.clearInterval(setTime);
                }
        };
        var video = document.getElementById("videoArea");
        var obj = {};
        obj["dt"] = video.currentTime.toString();
        sendHttp("PUT", "api/plays/" + deviceName, JSON.stringify(obj), callback); 
}

function loadVideo() {
        var URL = window.URL || window.webkitURL;
        var file = document.getElementById("videoFile").files[0];
        if (!file) return;
        var fileURL = URL.createObjectURL(file);
        var video = document.getElementById("videoArea");
        video.src = fileURL;
        video.load();
        flagLoad = true;
        if (flagJoin) {
                setTime = window.setInterval(function() {playTimer();}, 2000);
        }
}

function increaseWidth() {
        var video = document.getElementById("videoArea");
        if (video.width < 2000) video.width += 10;
}

function decreaseWidth() {
        var video = document.getElementById("videoArea");
        if (video.width > 100) video.width -= 10;
}

function searchRooms() {
        var user = document.getElementById("userName").value;
        updateRoomList(user);
}

</script>
</head>
<body bgcolor="#a0e0ff">
<h2>SyncWatcher Web Player</h2>
<form>
        <input type="button" onclick="increaseWidth()" value="Bigger">
        <input type="button" onclick="decreaseWidth()" value="Smaller">
        <input id="videoFile" type="file" accept="video/*"/>
        <input type="button" onclick="loadVideo()" value="Load">
        <br>
        <video id="videoArea" width="768">
                Video Play Area
        </video>
        <br>
        Device Tag: <input type="text" id="deviceTag">
        <input type="button" onclick="regDevice()" value="Register">
        <input type="button" onclick="deregDevice()" value="Deregister">
        <div id="regRes">Please register your Device!</div>
        <br>
        Rooms of User: <input type="text" id="userName">
        <input type="button" onclick="searchRooms()" value="Search">
        Room List:
        <select id="roomList">
          <option value="">NONE</option>
        </select>
        <input type="button" onclick="joinRoom()" value="Join">
        <input type="button" onclick="disjoinRoom()" value="Disjoin">
        <div id="joinRes"></div>
</form>
<br>
<a href='index.php'>Back</a>
<br>
<p><small>Developed by Chenghu He/Wanjun Fan. 2015</small></p>
</body>
</html>
