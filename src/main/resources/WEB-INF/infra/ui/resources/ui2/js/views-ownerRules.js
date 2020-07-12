const OwnerRulesList = Vue.component('ownerRules-list', function(resolve, reject) {

  const url = 'ui2/views/ownerRulesList.html'

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

        create : function() {
          var c = this
          c.formResult = {}
          var clonedForm = JSON.parse(JSON.stringify(c.form))
          console.log('OwnerRule - Create', clonedForm)
          httpPost('/api/ownerRule/', clonedForm, function(data) {
            c.formResult = data
            if (data.success) {
              jQuery('#createModal .btn-secondary').click()
              successShow(c.$t('prompt.create.success', [ c.form.id ]))
              c.refresh()
            }
          })
        },

        edit : function() {
          var c = this
          c.formResult = {}
          var clonedForm = JSON.parse(JSON.stringify(c.form))
          console.log('OwnerRule - Edit', clonedForm)
          httpPost('/api/ownerRule/' + clonedForm.id, clonedForm, function(data) {
            c.formResult = data
            if (data.success) {
              jQuery('#editModal .btn-secondary').click()
              successShow(c.$t('prompt.edit.success', [ c.form.id ]))
              c.refresh()
            }
          })
        },

        editShow : function(ownerRule) {
          var c = this
          c.formResult = {}
          c.form = {}
          httpGet('/api/ownerRule/' + ownerRule.id, function(data) {
            if (data.success) {
              c.form = data.item
              jQuery('#editModal').modal()
            }
          })
        },

        deleteOne : function(ownerRule) {

          if (confirm(this.$t('prompt.delete.confirm', [ ownerRule.id ]))) {
            var c = this
            console.log('OwnerRule - Delete', ownerRule.id)
            httpDelete('/api/ownerRule/' + ownerRule.id, function(data) {
              successShow(c.$t('prompt.delete.success', [ ownerRule.id ]))
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
          console.log('OwnerRules - Load', c.queries)
          httpGetQueries('/api/ownerRule/', c.queries, function(data) {
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
