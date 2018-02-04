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
    ), function(req, res) {

        //format insert
        var insert = "INSERT INTO point_of_interests (coordinates, user_id, title, description) VALUES " +
                     "(ST_GeomFromText('POINT(" + req.body.lat + " " + req.body.long + ")'), " + req.user.user_id +
                     ", " + config.pool.escape(req.body.title);
        if (req.body.description != '') {
            insert +=  ", " + config.pool.escape(req.body.description);
        }
        insert += ");";

        config.pool.query(insert, function(err, rows) {
            if (err) {
                console.log(err);
                return res.json({ success: false, message: "Unknown error" });
            }
            res.json({ success: true, message: "successfully added point" });
        });
});

router.get('/', requireAuth, function(req, res) {
    var getPOIs = "SELECT point_of_interests.user_id, username, pio_id, ST_X(coordinates) AS \"lat\", ST_Y(coordinates) AS \"long\", title, description FROM point_of_interests, users WHERE point_of_interests.user_id = users.user_id;";

    config.pool.query(getPOIs, function(err, rows) {
        if(err) {
            console.log(err);
            return res.json({ success: false, message: "Unknown error" });
        }
        res.json({ success: true, data: rows });
    });

});

router.get('/:id', requireAuth, 
    form(
        field("req.params.id").isNumeric()
    ),
    function(req, res) {
        var getPOI = "SELECT point_of_interests.user_id, pio_id, ST_X(coordinates) AS \"lat\", ST_Y(coordinates) AS \"long\", title, description FROM point_of_interests, users WHERE pio_id = " + req.params.id + " AND point_of_interests.user_id = users.user_id;";

        config.pool.query(getPOI, function(err, rows) {
            if(err) {
                console.log(err);
                return res.json({ success: false, message: "Unknown error" });
            }
            res.json({ success: true, data: rows[0] });
        });

});

module.exports = router;