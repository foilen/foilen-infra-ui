function errorShow(message) {

  if (!(message instanceof String)) {
    message = message.timestamp + ' ' + message.uniqueId + ': ' + message.message
  }

  const errors = document.getElementById("errors");

  const error = document.createElement('div');
  error.innerHTML = message + '<button type="button" class="close" data-dismiss="alert">&times;</button>';
  error.setAttribute('class', 'alert alert-danger alert-dismissible fade show')

  errors.appendChild(error);

  console.log('ERROR', message)
}

function successShow(message) {

  const successes = document.getElementById("successes");

  const success = document.createElement('div');
  success.innerHTML = message + '<button type="button" class="close" data-dismiss="alert">&times;</button>';
  success.setAttribute('class', 'alert alert-success alert-dismissible fade show')

  successes.appendChild(success);

  jQuery(success).delay(20000).fadeOut();

  console.log('SUCCESS', message)
}

function httpDelete(url, successCallback) {
  ++app.pendingAjax

  jQuery.ajax({
    type : "DELETE",
    url : url,
    dataType : 'json',
    success : function(data) {
      --app.pendingAjax
      if (data.error) {
        errorShow(data.error)
      } else {
        successCallback(data)
      }
    },
    error : function(jqXHR, textStatus, errorThrown) {
      --app.pendingAjax
      var escapedResponseText = new Option(jqXHR.responseText).innerHTML;
      var error = 'Could not call ' + url + ' . Error: ' + textStatus + ' ' + errorThrown + '<br/>' + escapedResponseText;
      errorShow(error)
    }
  })
}

function httpGet(url, successCallback) {
  ++app.pendingAjax

  jQuery.get(url).done(function(data) {
    --app.pendingAjax
    if (data.error) {
      errorShow(data.error)
    } else {
      successCallback(data)
    }
  }).fail(function(jqXHR, textStatus, errorThrown) {
    --app.pendingAjax
    var escapedResponseText = new Option(jqXHR.responseText).innerHTML;
    var error = 'Could not call ' + url + ' . Error: ' + textStatus + ' ' + errorThrown + '<br/>' + escapedResponseText;
    errorShow(error)
  })
}

function httpGetQueries(url, queries, successCallback) {
  ++app.pendingAjax

  jQuery.ajax({
    url : url,
    data : queries,
    success : function(data) {
      --app.pendingAjax
      if (data.error) {
        errorShow(data.error)
      } else {
        successCallback(data)
      }
    },
    error : function(jqXHR, textStatus, errorThrown) {
      --app.pendingAjax
      var escapedResponseText = new Option(jqXHR.responseText).innerHTML;
      var error = 'Could not call ' + url + ' . Error: ' + textStatus + ' ' + errorThrown + '<br/>' + escapedResponseText;
      errorShow(error)
    }
  })
}

function httpPost(url, form, successCallback) {
  ++app.pendingAjax

  jQuery.ajax({
    type : "POST",
    url : url,
    data : JSON.stringify(form),
    dataType : 'json',
    headers : {
      'X-XSRF-TOKEN' : Cookies.get('XSRF-TOKEN'),
      'Content-Type' : 'application/json',
    },
    success : function(data) {
      --app.pendingAjax
      if (data.error) {
        errorShow(data.error)
      } else {
        successCallback(data)
      }
    },
    error : function(jqXHR, textStatus, errorThrown) {
      --app.pendingAjax
      var escapedResponseText = new Option(jqXHR.responseText).innerHTML;
      var error = 'Could not call ' + url + ' . Error: ' + textStatus + ' ' + errorThrown + '<br/>' + escapedResponseText;
      errorShow(error)
    }
  })
}
