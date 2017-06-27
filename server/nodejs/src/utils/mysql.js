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

/*
-- 申请表
CREATE TABLE req(
  -- APP系列，如QQ与QQ轻聊版可归为同一APP系列
  series VARCHAR(128),
  -- 根据APP名自动生成图标名（可能没有）
  icon VARCHAR(128),
  -- 目标APP名
  label VARCHAR(128),
  -- 目标APP名英文（可能没有）
  label_en VARCHAR(128),
  -- 包名
  pkg VARCHAR(128),
  -- 启动项
  launcher VARCHAR(192),
  -- 是否为系统APP
  sys_app TINYINT(1) DEFAULT 0,
  -- 归属图标包包名
  icon_pack VARCHAR(64),
  -- 设备ID（取 ANDROID_ID + SERIAL）
  device_id CHAR(32),
  -- 设备品牌
  device_brand VARCHAR(32),
  -- 设备型号
  device_model VARCHAR(32),
  -- 设备系统版本
  device_sdk TINYINT(1) DEFAULT 0,
  -- 申请时间
  time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY(pkg, launcher, icon_pack, device_id),
  CONSTRAINT fk_series FOREIGN KEY(series) REFERENCES series(name) ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE = InnoDB;
-- 申请过滤表
CREATE TABLE req_filter(
  -- 归属图标包包名
  icon_pack VARCHAR(64),
  -- 用户
  user VARCHAR(64),
  -- 包名
  pkg VARCHAR(128),
  -- 启动项
  launcher VARCHAR(192),
  -- 申请时间
  time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY(icon_pack, user, pkg, launcher)
);
-- APP系列表
CREATE TABLE series(
  -- APP系列，如各系统的电话APP可归为同一系列
  name VARCHAR(128) PRIMARY KEY,
  -- 系列名
  label VARCHAR(128),
  -- 系列名英文
  label_en VARCHAR(128),
  -- 是否为系统APP系列
  sys TINYINT(1) DEFAULT 0
) ENGINE = InnoDB;
-- 图标包（TODO 建立图标包管理机制）
CREATE TABLE icon_pack(
  -- 包名
  pkg VARCHAR(64) PRIMARY KEY,
  -- APP名
  label VARCHAR(128),
  -- APP英文名
  label_en VARCHAR(128),
  -- 作者
  author VARCHAR(32),
  -- 黑名单
  evil TINYINT(1) DEFAULT 0
) ENGINE = InnoDB;
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