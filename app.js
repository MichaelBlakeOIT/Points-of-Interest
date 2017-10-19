var express = require('express');
var session = require('express-session');
var bodyParser = require('body-parser')
var router = express.Router();
var app = express();
var sessionController = require('./routes/session');

var port = process.env.PORT || 8080;
var passport = require('passport');

app.use(bodyParser.urlencoded({ extended: true }) ); // get information from html forms
//app.use(session({ secret: "cats" }));
app.use(passport.initialize());
//require('./config/passport')(passport); 
app.use('/user', require('./routes/user.js'));
app.use('/session', require('./routes/session.js'));
app.listen(port);
console.log('running on port ' + port);