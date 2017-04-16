/*
 * Copyright 2017 By_syk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var mysql = require('mysql'); // npm install mysql

// 数据库连接池配置
var pool = mysql.createPool({
  host: 'localhost',
  //port: 3306,
  database: 'nanoiconpack',
  user: 'nanoiconpack',
  password: 'nanoiconpack',
  connectionLimit: 100 // important
});

var query = function(sql, options, callback) {
  pool.getConnection(function(err, conn) {
    if (err) {
      callback(err, null, null);
    } else {
      conn.query(sql, options, function(err1, rows) {
        conn.release();
        callback(err1, rows);
      });
    }
  });
};

module.exports = query;