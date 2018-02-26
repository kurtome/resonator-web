@(jsLibraryBundleUrl: String, jsAppBundleUrl: String)

// Load the libraries
importScripts('@jsLibraryBundleUrl');

// Setup require
var exports = self;
exports.require = self["ScalaJSBundlerLibrary"].require;

importScripts('@jsAppBundleUrl');

AsyncLocalCacheWorker.main();
