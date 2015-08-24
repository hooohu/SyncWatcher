<!DOCTYPE html>
<html>
  <head>
    <title>SyncWatcher Monitor</title>
    <script>

function addUser() {
  var uname = document.getElementById("my_uname").value; 
  var pw = document.getElementById("my_pw").value; 
  send_request("ins=rc_add&user="+uname+"&pw="+pw);
}
function delUser() {
  var uname = document.getElementById("my_uname").value; 
  send_request("ins=rc_del&user="+uname);
}
function listUsers() {
  send_request("ins=rc_list");
} 
function send_request(args) {
  var xmlhttp = new XMLHttpRequest();
  xmlhttp.onreadystatechange = function() {
    if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
      document.getElementById("my_list").innerHTML = xmlhttp.responseText;
    }
  }
  xmlhttp.open("GET", "sync.php?"+args,true);
  xmlhttp.send();
}

    </script>
  </head>
  <body>
    <h2>SyncWatcher Server and Database Monitor</h2>
    <form>
    </form>
    <br>
    <p> <small> Developed by Chenghu He/Wanjun Fan. 2015 </small> </p>
  </body>
</html>
