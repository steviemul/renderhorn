var global = this;
var self = this;
var window = this;
var process = { env: {} };

var console = {
  info: function(pMessage) {
    occ.console.info(pMessage);
  },
  error: function(pMessage) {
    occ.console.error(pMessage);
  } ,
  warn: function(pMessage) {
    occ.console.warn(pMessage);
  },
  debug: function(pMessage) {
    occ.console.debug(pMessage);
  },
  assert: function (pExpr, pMessage) {
    if (pExpr == false) {
      occ.console.error(pMessage);
    }
  }
};


