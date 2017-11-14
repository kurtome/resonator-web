var webpack = require('webpack');

module.exports = require('./scalajs.webpack.config');


var jsonLoader = { test: /\.json$/, loader: "json-loader" };
if (module.exports.module.loaders) {
    module.exports.module.loaders.push(jsonLoader);
} else {
    module.exports.module.loaders = [jsonLoader];
}
