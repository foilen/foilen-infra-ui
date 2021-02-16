     </div><!-- /Middle -->

    </div><!-- /Main -->

    <!-- footer -->
    <hr/>
    <footer>
      <p>&copy; <a href="https://foilen.com" target="_blank">Foilen</a> 2017-2021</p>
      <p>Infra UI Version: ${version}</p>
    </footer>

  </div>
  
  <!-- Extra JS -->
  <#list externalJsScripts as externalJsScript>
    <script type="text/javascript" src="${externalJsScript}"></script>
  </#list>

</body>
</html>
