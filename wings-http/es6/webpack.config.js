var webpack = require('webpack');

module.exports = {
    eslint: {
        configFile: '.eslintrc'
    },
    entry: './src/main.js',
    output: {path: __dirname + '/../public/javascript/', filename: 'bundle.js'},
    module: {
        loaders: [
            {
                test: /.js$/,
                loader: 'babel-loader',
                exclude: /node_modules/,
                query: {
                    presets: ['es2015']
                }
            },
            {
                test: /\.js$/,
                loader: "eslint-loader",
                exclude: /node_modules/
            }
        ]
    },
};