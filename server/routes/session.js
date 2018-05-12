var router = require('express').Router();
var config = require('./../config/data');
var form = require('express-form');
var bcrypt = require('bcrypt');
var field = form.field;
var jwt = require('jsonwebtoken');
var passport = require('passport');
var middleware = require('./../middleware/middleware');
var passportConfig = require('./../config/passport');
const requireAuth = passport.authenticate('jwt', { session: false });

router.post('/', 
    form
    (
        field("username").required(),
        field("password").required()
    ), function (req, res) {
        console.log(req.body);
        if (!req.form.isValid)
            return res.json({ success: false, data: req.form.errors });
        config.pool.query(`SELECT * FROM users WHERE username = ${config.pool.escape(req.body.username)};`, function (err, rows) {
            if (err) {
                console.log(err);
                return res.json({ success: false, data: "Unknown error" });
            }
            if (!rows.length)
                return res.json({ success: false, data: "Incorrect password" });
            bcrypt.compare(req.body.password, rows[0].password, function (err, result) {
                if (result === false)
                    return res.json({ success: false, data: "Incorrect password" });
                return res.json({ success: true, token: jwt.sign({ id: rows[0].user_id, username: rows[0].username, email: rows[0].email, first: rows[0].first_name, last: rows[0].last_name }, config.jwtSecret) });
            });
        });
});

module.exports = router;