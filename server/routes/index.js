var router = require('express').Router();
const express = require('express');
const aws = require('aws-sdk');
var form = require('express-form');
var field = form.field;

router.get('/', function(req, res) {
    res.send("test123");
});

router.get('/sign-s3', 
    form(
        field("file-name").required().isAlphanumeric(),
        field("file-type").required().isAlphanumeric()
    ), (req, res) => {
        if (!req.form.isValid)
            return res.json({ success: false, message: req.form.errors });
        const s3 = new aws.S3();
        const fileName = req.query['file-name'];
        const fileType = req.query['file-type'];
        const s3Params = {
            Bucket: process.env.S3_BUCKET_NAME,
            Key: fileName,
            Expires: 60,
            ContentType: fileType,
            ACL: 'public-read'
        };
    
        s3.getSignedUrl('putObject', s3Params, (err, data) => {
        if(err){
            console.log(err);
            return res.end();
        }
        const returnData = {
            signedRequest: data,
            url: `https://${process.env.S3_BUCKET_NAME}.s3.amazonaws.com/${fileName}`
        };
        res.write(JSON.stringify(returnData));
        res.end();
        });
    });

module.exports = router;