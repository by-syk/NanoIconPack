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

// 数据库连接配置
//var connection = mysql.createConnection({
//  host: 'localhost',
//  user: 'root',
//  password: '',
//  database: 'nanoiconpack'
//});
// 数据库连接池配置
var pool = mysql.createPool({
  host: 'localhost',
  user: 'test',
  password: 'abc123',
  database: 'nanoiconpack'
});

var query = function(sql, callback) {
  pool.getConnection(function(err, conn) {
    if (err) {
      callback(err, null, null);
    } else {
      conn.query(sql, function(err1, rows) {
        conn.release();
        callback(err1, rows);
      });
    }
  });
};

module.exports = query;