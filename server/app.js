var express = require('express');
var bodyParser = require('body-parser')
var router = express.Router();
var app = express();
var sessionController = require('./routes/session');
var morgan = require('morgan');

var passport = require('passport');

app.use(morgan('dev'));
app.use(bodyParser.urlencoded({ extended: true }) ); // get information from html forms
app.use(passport.initialize());
app.use('/user', require('./routes/user.js'));
app.use('/session', require('./routes/session.js'));
app.use('/poi', require('./routes/poi.js'));
app.listen(3000, "127.0.0.1");
console.log('running on port ' + 3000);