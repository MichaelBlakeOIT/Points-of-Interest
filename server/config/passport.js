var passport = require('passport');
var JwtStrategy = require('passport-jwt').Strategy;
var ExtractJwt = require('passport-jwt').ExtractJwt;
var config = require('./data');

passport.use(
    new JwtStrategy({ 
        jwtFromRequest: ExtractJwt.fromAuthHeaderAsBearerToken(),
        secretOrKey: config.jwtSecret
    }, 
    function(jwtPayload, done) {
        config.pool.query(`SELECT username, user_id FROM users WHERE username = ${config.pool.escape(jwtPayload.username)};`, function(err, rows) {
            if (err)
                return done(err);
            if (!rows.length)
                return done(null, false);
            return done(null, rows[0]);
        });
}));

