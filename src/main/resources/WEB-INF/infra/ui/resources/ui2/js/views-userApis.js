const UserApisList = Vue.component('userApis-list', function(resolve, reject) {

  const url = 'ui2/views/userApisList.html'

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

        createAdmin : function() {
          var c = this
          c.formResult = {}
          console.log('UserApi - Create Admin')
          httpPost('/api/userApi/admin', null, function(data) {
            c.formResult = data
            if (data.success) {
              successShow(c.$t('userApi.created') + data.userId + ' / ' + data.password)
              c.refresh()
            }
          })
        },

        edit : function() {
          var c = this
          c.formResult = {}
          var clonedForm = JSON.parse(JSON.stringify(c.form))
          console.log('UserApi - Edit roles', clonedForm)
          httpPost('/api/userApi/' + clonedForm.userId + '/roles', clonedForm, function(data) {
            c.formResult = data
            if (data.success) {
              jQuery('#editModal .btn-secondary').click()
              successShow(c.$t('prompt.edit.success', [ c.form.name ]))
              c.refresh()
            }
          })
        },

        editShow : function(userApi) {
          var c = this
          c.formResult = {}
          c.form = {}
          c.form = JSON.parse(JSON.stringify(userApi))
          if (!c.form.roles) {
            c.form.roles = []
          }
          jQuery('#editModal').modal()
        },

        refresh : function(pageId) {
          if (pageId === undefined) {
            pageId = 1
          }
          var c = this
          c.queries.pageId = pageId
          console.log('UserApis - Load', c.queries)
          httpGetQueries('/api/userApi/', c.queries, function(data) {
            c.pagination = data.pagination
            c.items = data.items
            if (c.items == null) {
              c.items = []
            }
          })
        },

        userApiRoleAdd : function() {
          var c = this
          c.form.roles.push('')
          c.form = JSON.parse(JSON.stringify(c.form))
        },

        userApiRoleRemove : function(index) {
          var c = this
          c.form.roles.splice(index, 1)
          c.form = JSON.parse(JSON.stringify(c.form))
        },

        userApiRoleUpdate : function(index, event) {
          var c = this
          c.form.roles[index] = event.target.value
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
