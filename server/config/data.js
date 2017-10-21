var mysql = require('mysql');

var pool = mysql.createPool({
    host: 'localhost',
    user: 'root',
    port: 3306,
    password: 'a102646a',
    database: "pio"
});

var jwtSecret = '9f8(SD*F8SF9*HV9SkdsLs*2l';

module.exports = {
    pool,
    jwtSecret
}