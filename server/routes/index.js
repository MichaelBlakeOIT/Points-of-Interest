var router = require('express').Router();
const express = require('express');
const aws = require('aws-sdk');
var form = require('express-form');
var passport = require('passport');
const requireAuth = passport.authenticate('jwt', { session: false });
var field = form.field;

router.get('/', function(req, res) {
    res.send("test123");
});

router.get('/sign-s3', requireAuth, function(req, res) {
        if (!req.form.isValid)
            return res.json({ success: false, message: req.form.errors });
        const s3 = new aws.S3();
        const s3Params = {
            Bucket: process.env.S3_BUCKET_NAME,
            Key: `profile/${req.user.username}`,
            Expires: 60,
            ContentType: "image/jpeg",
            ACL: 'public-read'
        };
    
        s3.getSignedUrl('putObject', s3Params, (err, data) => {
        if(err){
            console.log(err);
            return res.end();
        }

        return res.json({ success: true, data: { postUrl: data, getUrl: data.split("?")[0] } });

        /*const returnData = {
            signedRequest: data,
            url: `https://${process.env.S3_BUCKET_NAME}.s3.amazonaws.com/${fileName}`
        };
        res.write(JSON.stringify(returnData));
        res.end();*/
        });
    });

module.exports = router;