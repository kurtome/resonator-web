var webpack = require('webpack');

// grab the existing config and modify it
module.exports = require('./scalajs.webpack.config');


var jsonLoader = { test: /\.json$/, loader: "json-loader" };

module.exports.module.loaders = (module.exports.module.loaders || []).concat([
    jsonLoader
]);


module.exports.plugins = (module.exports.plugins || []).concat([
    new webpack.DefinePlugin({ 'process.env.NODE_ENV': JSON.stringify('production') }),
    new webpack.optimize.UglifyJsPlugin()
]);

