/* This file handles dynamic variables for AJAX styled content loading. */
var previous = "";
setInterval(function(){
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200) {
            document.getElementById("dynamic").innerHTML = this.responseText;
            previous = this.responseText;
        }
    };
    xhttp.open("GET", "/dynamic", true);
    xhttp.send();
}, 1000);