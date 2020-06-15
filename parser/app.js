const http = require('http');
const puppeteer = require('puppeteer');
const {parse} = require("./parse.js");

parse("https://pixabay.com/ru/").then((resolve) => {
    console.log(resolve)
}, reason => {
    console.log(reason)
});