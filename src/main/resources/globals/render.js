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

        // use a copy of the state for rendering.
        var stateToRender = Object.assign({}, state);

        var html = server.renderToString(stateToRender);

        console.info("Successfully called renderToString");

        pushResources(pHttpResponse, stateToRender.pageRepository.packages);

        return html;
      }
      catch(e) {
        console.error("Unable to render " + e);
      }
    }
  };

})(CouldleakServer);