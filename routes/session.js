var router = require('express').Router();
var config = require('./../config/data');

module.exports = function (app, passport) {
    router.post('/', passport.authenticate('local', { failureRedirect: '/login' }), function(req, res) {
        res.send("success");
    });
    return router;
}