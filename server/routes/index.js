var router = require('express').Router();

router.get('/', function(req, res) {
    res.send("test123");
});

module.exports = router;