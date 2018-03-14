var router = require('express').Router();
var config = require('./../config/data');
var jwt = require('jsonwebtoken');
var middleware = require('./../middleware/middleware');
var form = require('express-form');
var field = form.field;
var passport = require('passport');
const requireAuth = passport.authenticate('jwt', { session: false });

router.post('/', requireAuth,
    form(
        field("lat").required().isNumeric(),
        field("long").required().isNumeric(),
        field("title").required().minLength(1).maxLength(45),
        field("description").maxLength(600)
    ), function (req, res) {
        if (!req.form.isValid)
            return res.json({ success: false, message: req.form.errors });
        //format insert
        var insert = "INSERT INTO point_of_interests (coordinates, user_id, title, description) VALUES " +
            "(ST_GeomFromText('POINT(" + config.pool.escape(req.body.lat) + " " + config.pool.escape(req.body.long) + ")'), " + req.user.user_id +
            ", " + config.pool.escape(req.body.title);
        if (req.body.description != '') {
            insert += ", " + config.pool.escape(req.body.description);
        }
        insert += ");";

        config.pool.query(insert, function (err, rows) {
            if (err) {
                console.log(err);
                return res.json({ success: false, message: "Unknown error" });
            }
            res.json({ success: true, message: "successfully added point" });
        });
    });

router.post('/:id/rating', requireAuth,
    form(field("rating").required().isInt(),
        field("id").required().isInt()),
    function (req, res) {
        if (!req.form.isValid)
            return res.json({ success: false, message: req.form.errors });

        if (req.body.rating < 1 || req.body.rating > 5)
            return res.json({ success: false, message: "invalid value" });

        var check = "SELECT * FROM pio_ratings WHERE user_id = " + req.user.user_id + " AND poi_id = " + req.params.id;
        var rateQuery;

        config.pool.query(check, function (err, rows) {
            if (err) {
                console.log(err);
                return res.json({ success: false, message: "Unknown error" });
            }

            console.log("rows.length: ", rows.length);

            if (rows.length)
                rateQuery = `INSERT INTO pio_ratings (poi_id, user_id, rating) VALUES (${req.params.id}, ${req.user.user_id}, ${req.body.rating});`;
            else
                rateQuery = `UPDATE pio_ratings SET rating = ${req.body.rating} WHERE user_id = ${req.user.user_id} AND poi_id = ${req.params.id};`;

            console.log("rate: ", rateQuery);

            config.pool.query(rateQuery, function (err, rows) {
                if (err) {
                    console.log(err);
                    return res.json({ success: false, message: "Unknown error" });
                }
                res.json({ success: true, message: "successfully rated point" });
            });
        });
    });


router.post('/:id/save', requireAuth, form(field("id").required().isInt()),
    function (req, res) {
        if (!req.form.isValid)
            return res.json({ success: false, message: req.form.errors });

        var check = `SELECT * FROM saved_pois WHERE user_id = ${req.user.user_id} AND poi_id = ${req.params.id}`;
        var saveQuery = `INSERT INTO saved_pois (user_id, poi_id) VALUES (${req.user.user_id}, ${req.params.id});`;

        config.pool.query(check, function (err, rows) {
            if (err) {
                console.log(err);
                return res.json({ success: false, message: "Unknown error" });
            }

            if (rows.length)
                return res.json({ success: false, message: "You already have this POI saved" });

            config.pool.query(saveQuery, function (err, rows) {
                if (err) {
                    console.log(err);
                    return res.json({ success: false, message: "Unknown error" });
                }
                res.json({ success: true, message: "successfully saved point" });
            });
        });
    });

router.get('/', requireAuth, function (req, res) {
    var getPOIs = `SELECT point_of_interests.user_id, username, pio_id, ST_X(coordinates) AS "lat", ST_Y(coordinates) AS "long", title, description, IFNULL((SELECT AVG(rating) FROM pio_ratings WHERE poi_id = point_of_interests.pio_id), 0) AS rating 
                   FROM point_of_interests
                   INNER JOIN users ON point_of_interests.user_id = users.user_id;`;

    config.pool.query(getPOIs, function (err, rows) {
        if (err) {
            console.log(err);
            return res.json({ success: false, message: "Unknown error" });
        }
        res.json({ success: true, data: rows });
    });

});

router.get('/:id', requireAuth, form(field("id").required().isInt()), function (req, res) {
    if (!req.form.isValid)
        return res.json({ success: false, message: req.form.errors });

    var getPOI = `SELECT point_of_interests.user_id, pio_id, ST_X(coordinates) AS "lat", ST_Y(coordinates) AS "long", title, description, IFNULL((SELECT AVG(rating) FROM pio_ratings WHERE poi_id = ${req.params.id}), 0) AS rating
    FROM point_of_interests
    INNER JOIN users ON point_of_interests.user_id = users.user_id 
    WHERE pio_id = ${req.params.id};`;

    config.pool.query(getPOI, function (err, rows) {
        if (err) {
            console.log(err);
            return res.json({ success: false, message: "Unknown error" });
        }
        res.json({ success: true, data: rows[0] });
    });
});

router.get('/:id/rating', form(field("id").required().isInt()), function (req, res) {
    var getRating = `SELECT avg(rating) AS rating, COUNT(*) AS votes FROM pio_ratings WHERE poi_id = ${req.params.id};`;

    config.pool.query(getRating, function (err, rows) {
        if (err) {
            console.log(err);
            return res.json({ success: false, message: "Unknown error" });
        }
        res.json({ success: true, data: { poi_id: req.params.id, average_rating: rows[0].rating, votes: rows[0].votes } });
    });
});

router.post('/:id/comments', requireAuth,
    form(
        field("id").required().isInt(),
        field("comment").required().minLength(1).maxLength(255)
    ), function (req, res) {

        if (!req.form.isValid)
            return res.json({ success: false, message: req.form.errors });

        var createComment = `INSERT INTO comments (point_of_interest_id, user_id, comment) VALUES (${req.params.id}, ${req.user.user_id}, ${config.pool.escape(req.body.comment)} )`;

        config.pool.query(createComment, function (err, rows) {
            if (err) {
                console.log(err);
                return res.json({ success: false, message: "Unknown error" });
            }
            res.json({ success: true, message: "Successfully created comment" });
        });
});

router.get('/:id/comments', requireAuth, form(field("id").required().isInt()), function (req, res) {
    if (!req.form.isValid) {
        return res.json({ success: false, message: req.form.errors });
    }
    var selectComment = `SELECT username, comment, point_of_interest_id, comment_id, comments.user_id FROM comments 
                         INNER JOIN users ON users.user_id = comments.user_id
                         WHERE point_of_interest_id = ${req.params.id};`;

    config.pool.query(selectComment, function (err, rows) {
        if (err) {
            console.log(err);
            return res.json({ success: false, message: "Unknown error" });
        }
        res.json({ success: true, data: rows });
    });
});

module.exports = router;