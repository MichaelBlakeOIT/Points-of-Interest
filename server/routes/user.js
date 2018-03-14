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

        var user_query = `SELECT user_id, username, first_name, last_name, bio, profile_photo 
                          FROM Users 
                          WHERE Username = ${config.pool.escape(req.params.username)};`;

        config.pool.query(user_query, function (err, rows) {
            if (err) {
                console.log(err);
                res.end();
                return;
            }
            if (rows.length) {
                var user = rows[0];

                var poi_query = `SELECT pio_id, ST_X(coordinates) AS "lat", ST_Y(coordinates) AS "long", title, description, IFNULL((SELECT AVG(rating) FROM pio_ratings WHERE poi_id = point_of_interests.pio_id), 0) AS rating 
                                 FROM point_of_interests 
                                 WHERE user_id = ${rows[0].user_id};`;

                config.pool.query(poi_query, function (err2, rows2) {
                    if (err) {
                        console.log(err2);
                        res.end();
                        return;
                    }
                    res.json({ success: true, message: { user: user, pois: rows2 }});
                    return;
                });
            }
        });
    });


router.get('/', requireAuth, function (req, res) {
    config.pool.query("SELECT user_id, username, first_name, last_name, bio, profile_photo FROM Users WHERE Username = " + config.pool.escape(req.user.username) + ";", function (err, rows) {
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
    var getPOIs = `SELECT users.user_id, username, point_of_interests.user_id, pio_id, ST_X(coordinates) AS "lat", ST_Y(coordinates) AS "long", title, description,  IFNULL((SELECT AVG(rating) FROM pio_ratings WHERE poi_id = point_of_interests.pio_id), 0) as rating
    FROM point_of_interests 
    INNER JOIN following ON point_of_interests.user_id = following_id
    INNER JOIN users ON users.user_id = following_id
    WHERE follower_id = ${req.user.user_id};`;

    config.pool.query(getPOIs, function (err, rows) {
        if (err) {
            console.log(err);
            return res.json({ success: false, message: "Unknown error" });
        }
        return res.json({ success: true, data: rows });
    });

});

router.post('/reset', function (req, res) {
    var code = "";
    var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    var email = "";

    for (var i = 0; i < 5; i++)
      code += possible.charAt(Math.floor(Math.random() * possible.length));

    var getEmail = `SELECT email FROM users WHERE username = ${config.pool.escape(req.body.username)};`
    var reset = `UPDATE users SET reset = '${code}' WHERE username = ${config.pool.escape(req.body.username)};`

    config.pool.query(getEmail, function(err, rows) {
        if (err) {
            console.log(err);
            return res.json({ success: false, message: "Unknown error" });
        }
        if (rows.length) {
            email = rows[0].email;
            config.pool.query(reset, function (err, rows1) {
                if (err) {
                    console.log(err);
                    return res.json({ success: false, message: "Unknown error" });
                }
                sendResetEmail(code, email);
                return res.json({ success: true, message: "If this account exists, an email will be sent."});
            });
        }
    });
});

router.put('/reset', 
    form(field("username").isAlphanumeric().maxLength(16),
         field("password").required(),
         field("code").minLength(5).required()), 
    function (req, res) {
        var check = `SELECT user_id FROM users WHERE username = ${config.pool.escape(req.body.username)} AND reset = ${config.pool.escape(req.body.code)};`;

        config.pool.query(check, function(err, rows) {
            if (err) {
                console.log(err);
                return res.json({ success: false, message: "Unknown error" });
            }
            if (rows.length) {
                //password can be reset
                var update = `UPDATE users SET password = '${bcrypt.hashSync(req.body.password, bcrypt.genSaltSync(8), null)}' WHERE username = ${config.pool.escape(req.body.username)};`;
                console.log("update: ", update);
                config.pool.query(update, function(err, rows) {
                    if (err) {
                        console.log(err);
                        return res.json({ success: false, message: "Unknown error" });
                    } 
                    return res.json({ success: true, message: "Successfully reset password" });
                });
            } else {
                return res.json({ success: false, message: "Code invalid" });
            }
        });
});

function sendResetEmail(code, email) {
    console.log("email: ", email);
    var mailOptions = {
        from: 'helpatpoi@gmail.com',
        to: email,
        subject: 'Password Reset Requested For POI',
        text: 'Hello. This is where you reset your password.\n' + code
    };
    
    config.transporter.sendMail(mailOptions, function(error, info) {
        if (error) {
            console.log(error);
        } else {
          console.log('Email sent: ' + info.response);
        }
    });
}



module.exports = router;