var webpack = require('webpack');

// grab the existing config and modify it
module.exports = require('./scalajs.webpack.config');

var jsonLoader = { test: /\.json$/, loader: "json-loader" };

module.exports.node = {
    console: true,
    fs: 'empty',
    net: 'empty',
    tls: 'empty'
};

module.exports.module.loaders = (module.exports.module.loaders || []).concat([
    // add loaders here
    jsonLoader
]);


module.exports.plugins = (module.exports.plugins || []).concat([
    // add plugins here
]);

