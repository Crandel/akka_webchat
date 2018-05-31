$(document).ready(function(){
  var sock = {};
  try{
    sock = new WebSocket('ws://' + window.location.host + '/ws_api');
  }
  catch(err){
    sock = new WebSocket('wss://' + window.location.host + '/ws_api');
  }

  function wrapWithClass(message, classname) {
    return '<span class="' + classname + '">' + message + '</span>';
  }

  // show message in div#subscribe
  function showMessage(message) {
    var messageElem = $('#subscribe'),
        messageObj = {},
        height = 0,
        date = new Date(),
        options = { hour12: false },
        show = true,
        htmlText = '[' + date.toLocaleTimeString('en-US', options) + '] ';

    try{
      messageObj = JSON.parse(message);
    } catch (e){
      if (typeof message === 'string'){
        htmlText = htmlText + message;
      } else {
        console.log("Wrong message");
        console.log(message);
        show = false;
      }
    }
    if (messageObj.hasOwnProperty('$type')){
      var m_type = messageObj.$type;
      if (m_type === "joined") {
        htmlText = htmlText + wrapWithClass(messageObj.member, 'user') + ' has ' + wrapWithClass('joined', 'join');
      } else if (m_type === "left") {
        htmlText = htmlText + wrapWithClass(messageObj.member, 'user') + ' has ' + wrapWithClass('left', 'left');
      } else if (m_type === "chat_message")
        htmlText = htmlText  +
        wrapWithClass(messageObj.sender, 'user') + ': ' +
        messageObj.message;
    }
    if (show) {
      htmlText = htmlText + '\n';
      messageElem.append($('<p>').html(htmlText));

      messageElem.find('p').each(function(i, value){
        height += parseInt($(this).height());
      });
      messageElem.animate({scrollTop: height});
    }
  }

  function sendMessage(){
    var msg = $('#message');
    sock.send(msg.val());
    msg.val('').focus();
  }

  sock.onopen = function(){
    showMessage('Connection to server started');
  };

  // send message from form
  $('#submit').click(function() {
    sendMessage();
  });

  $('#message').keyup(function(e){
    if(e.keyCode == 13){
      sendMessage();
    }
  });

  // income message handler
  sock.onmessage = function(event) {
    showMessage(event.data);
  };

  sock.onclose = function(event){
    if(event.wasClean){
      showMessage('Clean connection end');
    }else{
      showMessage('Connection broken');
    }
  };

  sock.onerror = function(error){
    showMessage(error);
  };
});
