var router = require('express').Router();
var config = require('./../config/data');
var bcrypt = require('bcrypt');

router.post('/', function (req, res) {
    config.pool.query("SELECT * FROM Users WHERE Username = '" + req.body.username + "'", function (err, rows) {
        if (err)
            console.log(err);
        if (rows.length)
            res.send('That username is already taken.');

        config.pool.query("SELECT * FROM Users WHERE Email = '" + req.body.email + "'", function (err, rows) {
            if (err)
                console.log(err);
            if (rows.length)
                res.send('That email is already taken.');

            var hash = bcrypt.hashSync(req.body.password, bcrypt.genSaltSync(8), null);

            var insertQuery = "INSERT INTO users ( username, email, password, first_name, last_name ) \
                                    values ('" + req.body.username + "','" + req.body.email + "','" + hash + "','" + req.body.firstname + "','" + req.body.lastname + "')";

            config.pool.query(insertQuery, function (err, rows) {
                if(err)
                    console.log(err);
                else
                    res.send('created user');
            });
        });
    });

});

module.exports = router;