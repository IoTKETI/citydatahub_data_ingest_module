
var stompClient = null;
function connect() {
  var socket = new SockJS('/ws-applyForApp');
  stompClient = Stomp.over(socket);
  stompClient.connect({}, function(frame) {
    stompClient.subscribe('/topic/broadMessage', showApplyForApp);
  });
}

function disconnect() {
  if (stompClient !== null) {
    stompClient.disconnect();
  }
  console.log("Disconnected");
}
function sendMessage(msg) {
  stompClient.send("/app/sendMessage", {}, msg);
}

$(document).ready(function() {
  connect();
});
