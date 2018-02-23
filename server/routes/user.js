var router = require('express').Router();
var config = require('./../config/data');
var bcrypt = require('bcrypt');
var form = require('express-form');
var passport = require('passport');
const requireAuth = passport.authenticate('jwt', { session: false });
var field = form.field;

router.post('/',
    form(
        field("username").trim().required().isAlphanumeric().maxLength(16),
        field("password").required().minLength(6),
        field("firstname").trim().required().maxLength(35),
        field("lastname").trim().required().maxLength(35),
        field("email").trim().required().isEmail().maxLength(255)
    ),
    function (req, res) {
        if (!req.form.isValid) {
            res.json({ success: false, message: req.form.errors });
            return;
        }
        config.pool.query("SELECT * FROM Users WHERE Username = " + config.pool.escape(req.body.username) + ";", function (err, rows) {
            console.log("no errors3");
            if (err) {
                console.log(err);
                res.end();
                return;
            }
            if (rows.length) {
                res.json({ success: false, message: 'Username taken' });
                return;
            }

            config.pool.query("SELECT * FROM Users WHERE Email = " + config.pool.escape(req.body.email) + ";", function (err, rows) {
                if (err) {
                    console.log(err);
                    res.end();
                }
                if (rows.length) {
                    res.json({ success: false, message: 'Email taken' });
                    return;
                }

                var hash = bcrypt.hashSync(req.body.password, bcrypt.genSaltSync(8), null);

                var insertQuery = "INSERT INTO users ( username, email, password, first_name, last_name ) \
                                    values (" + config.pool.escape(req.body.username) + "," + config.pool.escape(req.body.email) + ",'" + hash + "'," + config.pool.escape(req.body.firstname) + "," + config.pool.escape(req.body.lastname) + ");";

                config.pool.query(insertQuery, function (err, rows) {
                    if (err) {
                        console.log(err);
                        res.end();
                    }
                    else
                        res.json({ success: true, message: 'Account created' });
                });
            });
        });

    });

router.put('/', requireAuth,
    form(
        field("password").minLength(6),
        field("bio").required().maxLength(600)
    ),
    function (req, res) {
        if (!req.form.isValid) {
            res.json({ success: false, message: req.form.errors });
            return;
        }
        var query = "UPDATE users SET bio = " + config.pool.escape(req.body.bio);
        if (req.body.password) {
            var hash = bcrypt.hashSync(req.body.password, bcrypt.genSaltSync(8), null);
            query += ", password = '" + hash;
        }
        query += " WHERE user_id = " + req.user.user_id;
        config.pool.query(query, function (err, rows) {
            if (err) {
                console.log(err);
                res.end();
            }
            else {
                res.json({ success: true, message: 'Profile updated' });
            }
        });

    });

router.get('/user/:username', requireAuth,
    form(
        field("req.params.username").isAlphanumeric().maxLength(16)
    ), function (req, res) {
        if (!req.form.isValid) {
            res.json({ success: false, message: req.form.errors });
            return;
        }

        config.pool.query("SELECT user_id, username, first_name, last_name, bio, profile_photo FROM Users WHERE Username = " + config.pool.escape(req.params.username) + ";", function (err, rows) {
            console.log("no errors2");
            if (err) {
                console.log(err);
                res.end();
                return;
            }
            if (rows.length) {
                res.json({ success: true, message: rows[0] });
                return;
            }
        });
    });


router.get('/', requireAuth, function (req, res) {
    config.pool.query("SELECT user_id, username, first_name, last_name, bio, profile_photo FROM Users WHERE Username = " + config.pool.escape(req.user.username) + ";", function (err, rows) {
        console.log("no errors1");
        if (err) {
            console.log(err);
            res.end();
            return;
        }
        if (rows.length) {
            res.json({ success: true, message: rows[0] });
            return;
        }
    });
});

router.post('/user/:username/follow', requireAuth,
    form(field("req.params.username").isAlphanumeric().maxLength(16)),
    function (req, res) {
        if (!req.form.isValid) {
            res.json({ success: false, message: req.form.errors });
            return;
        }

        var user_id;

        config.pool.query("SELECT user_id FROM users WHERE username = " + config.pool.escape(req.params.username) + ";", function (err, rows) {
            if (err) {
                console.log(err);
                res.end();
                return;
            }
            if (!rows.length) {
                res.json({ success: false, message: "Username doesn't exist" });
                return;
            }

            user_id = rows[0].user_id;

            config.pool.query("SELECT * FROM following WHERE follower_id = " + req.user.user_id + " AND following_id = " + user_id + ";", function (err, rows) {
                if (err) {
                    console.log(err);
                    res.end();
                    return;
                }

                if (rows.length) {
                    res.json({ success: false, message: "Already following user" });
                }

                config.pool.query("INSERT INTO following (follower_id, following_id) VALUES (" + req.user.user_id + "," + user_id + ");", function (err, rows) {

                    if (err) {
                        console.log(err);
                        res.end();
                        return;
                    }

                    res.json({ success: true, message: "Now following user" });
                });

            });
        });
    });

router.delete('/user/:username/follow', requireAuth,
    form(field("req.params.username").isAlphanumeric().maxLength(16)),
    function (req, res) {
        if (!req.form.isValid) {
            res.json({ success: false, message: req.form.errors });
            return;
        }

        var user_id;

        config.pool.query("SELECT user_id FROM users WHERE username = " + config.pool.escape(req.params.username) + ";", function (err, rows) {
            if (err) {
                console.log(err);
                res.end();
                return;
            }
            if (!rows.length) {
                res.json({ success: false, message: "Username doesn't exist" });
                return;
            }

            user_id = rows[0].user_id;

            config.pool.query("DELETE FROM following WHERE follower_id = " + req.user.user_id + " AND following_id = " + user_id + ";", function (err, rows) {
                if (err) {
                    console.log(err);
                    res.end();
                    return;
                }
                if (rows.affectedRows == 0) {
                    res.status(400).json({ success: false, message: "Not currently following user" });
                    return;
                }
                res.json({ success: true, message: "Unfollowed User" });
            });
        });
    });

router.get('/pois/saved', requireAuth, function (req, res) {
    var getPOIs = `SELECT point_of_interests.user_id AS "creator_id", username AS creator, point_of_interests.pio_id, ST_X(coordinates) AS "lat", ST_Y(coordinates) AS "long", point_of_interests.title, point_of_interests.description FROM saved_pois INNER JOIN point_of_interests ON point_of_interests.pio_id = saved_pois.poi_id INNER JOIN users ON point_of_interests.user_id = users.user_id WHERE saved_pois.user_id = ${req.user.user_id};`;

    config.pool.query(getPOIs, function (err, rows) {
        if (err) {
            console.log(err);
            return res.json({ success: false, message: "Unknown error" });
        }
        return res.json({ success: true, data: rows });
    });

});

router.get('/following', requireAuth, function (req, res) {
    var getPOIs = `SELECT point_of_interests.user_id, pio_id, ST_X(coordinates) AS "lat", ST_Y(coordinates) AS "long", title, description 
    FROM point_of_interests 
    INNER JOIN following
    ON point_of_interests.user_id = following_id
    WHERE follower_id = ${req.user.user_id};`;

    config.pool.query(getPOIs, function (err, rows) {
        if (err) {
            console.log(err);
            return res.json({ success: false, message: "Unknown error" });
        }
        return res.json({ success: true, data: rows });
    });

});

module.exports = router;