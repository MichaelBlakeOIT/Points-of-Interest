var passport = require('passport');
var JwtStrategy = require('passport-jwt').Strategy;
var ExtractJwt = require('passport-jwt').ExtractJwt;
var config = require('./data');

//const requireAuth = passport.authenticate('jwt', { session: false });

passport.use(
    new JwtStrategy({ 
        jwtFromRequest: ExtractJwt.fromAuthHeaderAsBearerToken(),
        secretOrKey: config.jwtSecret
    }, 
    function(jwtPayload, done) {
        //console.log(jwtPayload);
        config.pool.query("SELECT * FROM users WHERE username = '" + jwtPayload.username + "';", function(err, rows) {
            if (err)
                return done(err);
            if (!rows.length)
                return done(null, false);
            return done(null, true);
        });
}));

