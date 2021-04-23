const proxy = require('http-proxy-middleware');

/** proxy configuration for local development. */
module.exports = function(app) {
  //first register proxy for webmodelica backend
  app.use(
    '/api/v1/webmodelica',
    proxy({
      target: 'http://localhost:8888',
      logLevel: 'debug'
    })
  );

  //then route all other to traefik
  app.use(
    '/api/v1',
    proxy({
      target: 'http://localhost:9000',
      logLevel: 'debug'
    })
  );
};
