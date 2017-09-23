var localStrategy = require('passport-local').Strategy;
var passportData = require('data').passport;

var mysql = require('mysql');

var connection = mysql.createConnection({
    host: 'localhost',
	user: passportData.user,
    password: passportData.password
});