const ResourcesList = Vue.component('resources-list', function(resolve, reject) {

  const url = 'ui2/views/resourcesList.html'

  jQuery.get(url).done(function(htmlTemplate) {

    resolve({
      data : function() {
        return {
          queries : {},
          items : [],
          pagination : {
            currentPageUi : 1,
            totalPages : 1,
            firstPage : true,
            lastPage : true,
          },
          form : {},
          formResult : {},
        }
      },
      props : [],
      computed : {},
      methods : {

        deleteOne : function(resource) {

          if (confirm(this.$t('prompt.delete.confirm', [ resource.resourceDetails.resource.resourceName ]))) {
            var c = this
            console.log('Resource - Delete', resource.resourceDetails.resource.resourceName)
            httpDelete('/api/resource/' + resource.resourceDetails.resourceId, function() {
              successShow(c.$t('prompt.delete.success', [ resource.resourceDetails.resource.resourceName ]))
              c.refresh()
            })
          }

        },

        refresh : function(pageId) {
          if (pageId === undefined) {
            pageId = 1
          }
          var c = this
          c.queries.pageId = pageId
          console.log('Resources - Load', c.queries)
          httpGetQueries('/api/resource/', c.queries, function(data) {
            c.pagination = data.pagination
            c.items = data.items
            if (c.items == null) {
              c.items = []
            }
          })
        },

      },
      mounted : function() {
        this.refresh()
      },
      template : htmlTemplate
    })

  }).fail(function(jqXHR, textStatus, errorThrown) {
    var escapedResponseText = new Option(jqXHR.responseText).innerHTML;
    var error = 'Could not get the template ' + url + ' . Error: ' + textStatus + ' ' + errorThrown + '<br/>' + escapedResponseText;
    errorShow(error)
    reject(error)
  })

})
