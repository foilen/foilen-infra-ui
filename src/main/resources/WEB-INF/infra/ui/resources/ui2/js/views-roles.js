const RolesList = Vue.component('roles-list', function(resolve, reject) {

  const url = 'ui2/views/rolesList.html'

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
          console.log('Role - Create', clonedForm)
          httpPost('/api/role/', clonedForm, function(data) {
            c.formResult = data
            if (data.success) {
              jQuery('#createModal .btn-secondary').click()
              successShow(c.$t('prompt.create.success', [ c.form.name ]))
              c.editShow(c.form)
              c.refresh()
            }
          })
        },

        edit : function() {
          var c = this
          c.formResult = {}
          var clonedForm = JSON.parse(JSON.stringify(c.form))
          console.log('Role - Edit', clonedForm)
          httpPost('/api/role/' + clonedForm.name, clonedForm, function(data) {
            c.formResult = data
            if (data.success) {
              jQuery('#editModal .btn-secondary').click()
              successShow(c.$t('prompt.edit.success', [ c.form.name ]))
              c.refresh()
            }
          })
        },

        editShow : function(role) {
          var c = this
          c.formResult = {}
          c.form = {}
          httpGet('/api/role/' + role.name, function(data) {
            if (data.success) {
              c.form = data.item
              if (!c.form.resources) {
                c.form.resources = []
              }
              if (!c.form.links) {
                c.form.links = []
              }
              jQuery('#editModal').modal()
            }
          })
        },

        deleteOne : function(role) {

          if (confirm(this.$t('prompt.delete.confirm', [ role.name ]))) {
            var c = this
            console.log('Role - Delete', role.name)
            httpDelete('/api/role/' + role.name, function(data) {
              successShow(c.$t('prompt.delete.success', [ role.name ]))
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
          console.log('Roles - Load', c.queries)
          httpGetQueries('/api/role/', c.queries, function(data) {
            c.pagination = data.pagination
            c.items = data.items
            if (c.items == null) {
              c.items = []
            }
          })
        },

        roleLinkAdd : function() {
          var c = this
          c.form.links.push({})
          c.form = JSON.parse(JSON.stringify(c.form))
        },

        roleLinkRemove : function(index) {
          var c = this
          c.form.links.splice(index, 1)
          c.form = JSON.parse(JSON.stringify(c.form))
        },

        roleResourceAdd : function() {
          var c = this
          c.form.resources.push({})
          c.form = JSON.parse(JSON.stringify(c.form))
        },

        roleResourceRemove : function(index) {
          var c = this
          c.form.resources.splice(index, 1)
          c.form = JSON.parse(JSON.stringify(c.form))
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
