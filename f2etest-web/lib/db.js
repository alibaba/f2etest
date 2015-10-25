var mysql = require('mysql');

var siteInfo = require('../conf/site.json');

pool = mysql.createPool({
  host     : siteInfo.dbHost,
  user     : siteInfo.dbUser,
  password : siteInfo.dbPass,
  database : siteInfo.dbTable
});

module.exports = pool;