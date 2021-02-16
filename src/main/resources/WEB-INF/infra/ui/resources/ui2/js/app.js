// Vue Router: https://router.vuejs.org
const routes = [
  {
    path: '/',
    component: Home
  }, {
    path: '/audits',
    component: AuditsList
  }, {
    path: '/resources',
    component: ResourcesList
  }, {
    path: '/ownerRules',
    component: OwnerRulesList
  }, {
    path: '/userApis',
    component: UserApisList
  }, {
    path: '/userHumans',
    component: UserHumansList
  }, {
    path: '/roles',
    component: RolesList
  },
]

const router = new VueRouter({
  routes: routes
})

// Vue i18n
const messages = {
}

const i18n = new VueI18n({
  locale: 'en',
  messages: messages,
})

// Main app
var app = new Vue({
  i18n: i18n,
  router: router,
  el: '#app',
  data: {
    pendingAjax: 0,
    appDetails: {
      lang: 'en',
      userId: '',
      userEmail: '',
      userAdmin: false,
      version: '',
    },
    loadedExtraJs: [],
  },
  computed: {},
  methods: {
    changeLang: function () {
      // Change
      var nextLang = this.$t('navbar.nextlang.id')
      i18n.locale = nextLang

      // Persist
      jQuery.get('/index.html?lang=' + nextLang)

      // Change extra JS
      var loadedExtraJs = app.loadedExtraJs
      if (!loadedExtraJs) {
        loadedExtraJs = []
      }
      console.log('Already loaded: ', loadedExtraJs)

      var extraJsToLoad = app.appDetails.externalJsScripts[i18n.locale]
      if (!extraJsToLoad) {
        extraJsToLoad = []
      }
      console.log('To load: ', extraJsToLoad)

      if (JSON.stringify(loadedExtraJs) != JSON.stringify(extraJsToLoad)) {
        console.log('Refresh the page')
        location.reload()
      }
    },
    refresh: function () {
      httpGet('/api/app/details', function (data) {
        app.appDetails = data.item
        messages.en = app.appDetails.translations.en
        messages.fr = app.appDetails.translations.fr
        i18n.locale = app.appDetails.lang

        // Load extra JS
        if (app.appDetails.externalJsScripts == undefined) {
          app.appDetails.externalJsScripts = {}
        }
        var extraJsToLoad = app.appDetails.externalJsScripts[i18n.locale]
        if (!extraJsToLoad) {
          extraJsToLoad = []
        }

        extraJsToLoad.forEach(scriptUri => {
          console.log('Loading', scriptUri)
          var script = document.createElement('script')
          script.src = scriptUri
          document.head.appendChild(script)
        })

        app.loadedExtraJs = extraJsToLoad

      })
    }
  },
  mounted: function () {
    app = this
    this.refresh()
  }
})
