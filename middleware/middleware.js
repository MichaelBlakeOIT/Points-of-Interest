module.exports.checkAuthentication = function(req,res,next)
{
    //console.log(req.headers.authorization)
    if(req.isAuthenticated()){
        //if user is looged in, req.isAuthenticated() will return true 
        next();
    }
    else
    {
        res.send("auth failed");
    }
}
