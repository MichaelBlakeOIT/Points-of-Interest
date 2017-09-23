var LocalStrategy   = require('passport-local').Strategy;

var mysql = require('mysql');

var connection = mysql.createConnection({
    host: 'localhost',
	user: 'root',
    password: 'test'
});