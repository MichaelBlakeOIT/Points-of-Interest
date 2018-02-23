var express = require('express');
var bodyParser = require('body-parser')
var router = express.Router();
var app = express();
var sessionController = require('./routes/session');
var morgan = require('morgan');

var passport = require('passport');

var port = process.env.PORT || 3000;

app.use(morgan('dev'));
app.use(bodyParser.urlencoded({ extended: true }) ); // get information from html forms
app.use(passport.initialize());
app.use('/users', require('./routes/user.js'));
app.use('/session', require('./routes/session.js'));
app.use('/poi', require('./routes/poi.js'));
app.use('/', require('./routes/index.js'));
app.listen(port);
console.log('running on port ' + port);