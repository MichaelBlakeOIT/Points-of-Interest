var express = require('express');
var bodyParser = require('body-parser')
var router = express.Router();
var app = express();
var sessionController = require('./routes/session');

//app.use('/session', './routes/session');
//app.use('/user', './routes/user');

var port = process.env.PORT || 8080;
var passport = require('passport');
//var flash = require('connect-flash');

//var morgan = require('morgan');
//var cookieParser = require('cookie-parser');
//var bodyParser = require('body-parser');

// set up our express application
//app.use(morgan('dev')); // log every request to the console
//app.use(cookieParser()); // read cookies (needed for auth)
app.use(bodyParser()); // get information from html forms

// required for passport
app.use(passport.initialize());
//app.use(passport.session()); // persistent login sessions
require('./config/passport')(passport); 
//app.use(flash()); // use connect-flash for flash messages stored in session

 // load our routes and pass in our app and fully configured passport
app.use('/user', require('./routes/user.js'));
//app.use('/session', require('./routes/session.js')(app, passport));
app.use('/session', require('./routes/session.js'));
app.listen(port);
console.log('running on port ' + port);