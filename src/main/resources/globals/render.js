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
    render : function(pHttpResponse) {
      try {
        var state = occ.wapi.getPage('allProducts');

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