var router = require('express').Router();
var config = require('./../config/data');
var bcrypt = require('bcrypt');
var form = require('express-form');
var field = form.field;

router.post('/',
    form(
        field("username").trim().required().isAlphanumeric().maxLength(16),
        field("password").required().minLength(6),
        field("firstname").required().maxLength(35),
        field("lastname").required().maxLength(35),
        field("email").trim().required().isEmail().maxLength(255)
    ),
    function (req, res) {
        if (!req.form.isValid) {
            res.send(req.form.errors).end();
        }
        config.pool.query("SELECT * FROM Users WHERE Username = " + config.pool.escape(req.body.username) + ";", function (err, rows) {
            if (err) {
                console.log(err);
                res.end();
            }
            if (rows.length)
                res.send('That username is already taken.');

            config.pool.query("SELECT * FROM Users WHERE Email = " + config.pool.escape(req.body.email) + ";", function (err, rows) {
                if (err) {
                    console.log(err);
                    res.end();
                }
                if (rows.length)
                    res.send('That email is already taken.');

                var hash = bcrypt.hashSync(req.body.password, bcrypt.genSaltSync(8), null);

                var insertQuery = "INSERT INTO users ( username, email, password, first_name, last_name ) \
                                values (" + config.pool.escape(req.body.username) + "," + config.pool.escape(req.body.email) + ",'" + hash + "'," + config.pool.escape(req.body.firstname) + "," + config.pool.escape(req.body.lastname) + ");";

                config.pool.query(insertQuery, function (err, rows) {
                    if (err) {
                        console.log(err);
                        res.end();
                    }
                    else
                        res.send('created user');
                });
            });
        });

    });

module.exports = router;