<!DOCTYPE html>
<html>
  <head>
    <title>RequestSender - SyncWatcher</title>
    <script>

function send_request() {
          var select_m = document.getElementById("select_method");
          var method = select_m.options[select_m.selectedIndex].value;
          var select_f = document.getElementById("select_field");
          var field = select_f.options[select_f.selectedIndex].value;
          var target = document.getElementById("text_target").value;
          var path = "api/" + field + "/" + target;
          var payload = document.getElementById("text_payload").value;
          document.getElementById("div_response").innerHTML = "";
          document.getElementById("div_request").innerHTML = method + " " + path + " " + payload;
          var xmlhttp = new XMLHttpRequest();
          xmlhttp.onreadystatechange = function() {
                    if (xmlhttp.readyState == 4) {
                      document.getElementById("div_response").innerHTML = xmlhttp.responseText;
                    }
          }
          xmlhttp.open(method, path, true);
          if (method == "GET") xmlhttp.send();
          else {
                    xmlhttp.setRequestHeader("Content-type","application/json");
                    xmlhttp.send(payload);
          }
}

function add_json() {
        var key = document.getElementById("text_key").value;
        if (key == "") return;
        var value = document.getElementById("text_value").value;
        if (value == "") return;
        var payload = document.getElementById("text_payload").value;
        var data = {};
        if (payload != "") data = JSON.parse(payload);
        data[key] = value;
        document.getElementById("text_payload").value = JSON.stringify(data);
}

function clear_json() {
        document.getElementById("text_payload").value = "";
}

    </script>
  </head>
  <body>
    <h2>SyncWatcher XMLhttprequest Sender</h2>
    <br>
    <form>
      Method:
      <select id="select_method"> 
        <option value="GET">GET</option>
        <option value="POST">POST</option>
        <option value="PUT">PUT</option>
        <option value="DELETE">DELETE</option>
      </select>
      Action Field:
      <select id="select_field"> 
        <option value="users">USER</option>
        <option value="login">LOGIN</option>
        <option value="rooms">ROOM</option>
        <option value="devices">DEVICE</option>
        <option value="plays">PLAY</option>
      </select>
      Target:
      <input type="text" id="text_target">
      <p>Payload:<br>
        <textarea id="text_payload" rows="3" cols="75" readonly></textarea><br>
        Key:<input type="text" id="text_key">
        Value:<input type="text" id="text_value">
        <input type="button" onclick="add_json()" value="Add To JSON">
        <input type="button" onclick="clear_json()" value="Clear">
      </p>
      <p><input type="button" onclick="send_request()" value="SEND"></p>
      <p>Request:</p>
      <div id="div_request"></div>
      <p>Response:</p>
      <div id="div_response"></div>
    </form>
    <br>
    <p> <small> Developed by Chenghu He/Wanjun Fan. 2015 </small> </p>
  </body>
</html>
