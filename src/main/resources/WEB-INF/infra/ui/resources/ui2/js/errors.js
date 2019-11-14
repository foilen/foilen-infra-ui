function errorShow(message) {

  const errors = document.getElementById("errors");

  const error = document.createElement('div');
  error.innerHTML = message + '<button type="button" class="close" data-dismiss="alert">&times;</button>';
  error.setAttribute('class', 'alert alert-danger alert-dismissible fade show')

  errors.appendChild(error);

  console.log(message)
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
        errorShow(data.error.message)
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
      errorShow(data.error.message)
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
        errorShow(data.error.message)
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
    data : form,
    dataType : 'json',
    success : function(data) {
      --app.pendingAjax
      if (data.error) {
        errorShow(data.error.message)
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
