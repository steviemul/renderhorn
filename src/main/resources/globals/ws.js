var socket;

if (window.WebSocket) {

  socket = new WebSocket("ws://localhost:10444/ws");

  socket.onmessage = function (event) {
    if (event.data) {
      try {
        var data = JSON.parse(event.data);

        if (data.reload) {
          console.info("Received reload notification.");
          setTimeout(function() {
            window.location.reload(true);
          }, 2000);
        }
      }
      catch (e) {
        console.error(e);
      }
    }
  };
  
} 
else {
  console.error("Your browser does not support Websockets.");
}