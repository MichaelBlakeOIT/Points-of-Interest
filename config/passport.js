var passport = require('passport');
var LocalStrategy = require('passport-local').Strategy;
var config = require('./data');
var bcrypt = require('bcrypt');
var form = require('express-form');
var field = form.field;

module.exports = function(passport) {
    passport.serializeUser(function(user, done) {
        done(null, user);
    });
      
    passport.deserializeUser(function(user, done) {
        done(null, user);
    });

    passport.use('local', 
    form(
        field("username").required(),
        field("password").required()
    ), 
    new LocalStrategy({ 
        passReqToCallback: true 
    }, 
    function(req, username, password, done) {
        if (!req.form.isValid)
            res.send(req.form.errors)
        config.pool.query("SELECT * FROM users WHERE username = " + config.pool.escape(username) + ";", function(err, rows) {
            if (err)
                return done(err);
            if (!rows.length)
                return done(null, false, 'incorrect password');
            bcrypt.compare(password, rows[0].password, function(err, res) {
                if (res === false)
                    return done(null, false, 'incorrect password');
                return done(null, rows[0]);
            });
        });
    }));
};
