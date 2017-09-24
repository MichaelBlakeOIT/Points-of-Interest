var passport = require('passport');
var LocalStrategy = require('passport-local').Strategy;
var config = require('./data');
var bcrypt = require('bcrypt');

module.exports = function(passport) {
    passport.serializeUser(function(user, done) {
        done(null, user);
    });
      
    passport.deserializeUser(function(user, done) {
        done(null, user);
    });

    passport.use('local', new LocalStrategy({ 
        passReqToCallback: true 
    }, 
    function(req, username, password, done) {
        console.log("1");
        config.pool.query("SELECT * FROM users WHERE username = '" + username + "'", function(err, rows) {
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
