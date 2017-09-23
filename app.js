var app = require('express')();

app.use('/session', './routes/session');
app.use('/user', './routes/user');