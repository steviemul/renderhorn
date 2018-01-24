function pushResources(pHttpResponse, pPackages) {

  for (packageName in pPackages) {
    var package = pPackages[packageName];

    if (package.lazyload === false) {
      pHttpResponse.pushResource(package.src);
    }
  }
}

(function(server) {

  return {
    getState : function() {
      return occ.wapi.getPage('allProducts');
    },
    render : function(pHttpResponse, state) {
      try {
        var html = server.renderToString(state);

        console.info("Successfully called renderToString");

        pushResources(pHttpResponse, state.pageRepository.packages);

        return html;
      }
      catch(e) {
        console.error("Unable to render " + e);
      }
    }
  };

})(CouldleakServer);