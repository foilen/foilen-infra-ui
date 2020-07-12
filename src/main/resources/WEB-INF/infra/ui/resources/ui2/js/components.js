Vue.component('error-results', function(resolve, reject) {

  const url = 'ui2/components/error-results.html'

  jQuery.get(url).done(function(htmlTemplate) {

    resolve({
      data : function() {
        return {}
      },
      props : [ 'formResult' ],
      computed : {},
      methods : {},
      template : htmlTemplate,
    })

  }).fail(function(jqXHR, textStatus, errorThrown) {
    var escapedResponseText = new Option(jqXHR.responseText).innerHTML;
    var error = 'Could not get the template ' + url + ' . Error: ' + textStatus + ' ' + errorThrown + '<br/>' + escapedResponseText;
    errorShow(error)
    reject(error)
  })

})

Vue.component('pagination', function(resolve, reject) {

  const url = 'ui2/components/pagination.html'

  jQuery.get(url).done(function(htmlTemplate) {

    resolve({
      data : function() {
        return {}
      },
      props : [ 'pagination' ],
      computed : {},
      methods : {
        previous : function() {
          this.$emit('changePage', {
            pageId : this.pagination.currentPageUi - 1,
          })
        },
        next : function() {
          this.$emit('changePage', {
            pageId : this.pagination.currentPageUi + 1,
          })
        },
      },
      template : htmlTemplate,
    })

  }).fail(function(jqXHR, textStatus, errorThrown) {
    var escapedResponseText = new Option(jqXHR.responseText).innerHTML;
    var error = 'Could not get the template ' + url + ' . Error: ' + textStatus + ' ' + errorThrown + '<br/>' + escapedResponseText;
    errorShow(error)
    reject(error)
  })

})

Vue.component('table-header-search', function(resolve, reject) {

  const url = 'ui2/components/table-header-search.html'

  jQuery.get(url).done(function(htmlTemplate) {

    resolve({
      data : function() {
        return {}
      },
      props : [ 'title', 'value', 'width' ],
      computed : {},
      methods : {
        changed : function(event) {
          if (event.keyCode === 13) {
            this.$emit('input', event.target.value)
          }
        }
      },
      template : htmlTemplate,
    })

  }).fail(function(jqXHR, textStatus, errorThrown) {
    var escapedResponseText = new Option(jqXHR.responseText).innerHTML;
    var error = 'Could not get the template ' + url + ' . Error: ' + textStatus + ' ' + errorThrown + '<br/>' + escapedResponseText;
    errorShow(error)
    reject(error)
  })

})
