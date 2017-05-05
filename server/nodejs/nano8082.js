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
-- APP代码表（TODO 移除）
CREATE TABLE code(
  -- APP系列，如QQ与QQ轻聊版可归为同一APP系列
  series VARCHAR(128),
  -- 图标名
  icon VARCHAR(128),
  -- 目标APP名
  label VARCHAR(128),
  -- 目标APP名英文
  label_en VARCHAR(128),
  -- 包名
  pkg VARCHAR(128),
  -- 启动项
  launcher VARCHAR(192),
  -- 添加时间
  time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY(pkg, launcher)
);
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
  -- 申请时间
  time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY(icon_pack, user, pkg)
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

var express = require('express'); // npm install express
var bodyParser = require('body-parser'); // npm install body-parser
var log4js = require('log4js'); // npm install log4js
var query = require('./mysql');
var utils = require('./utils');

// 服务运行目标端口
var serverPort = 8082;

var app = express();

// 解析 POST application/x-www-form-urlencoded
app.use(bodyParser.urlencoded({ extended: false }));
// 解析 POST JSON
//app.use(bodyParser.json({ limit: '1mb' }));

// 支持静态文件
app.use(express.static('public'));

// console log is loaded by default, so you won't normally need to do this
//log4js.loadAppender('console');
log4js.loadAppender('file');
//log4js.addAppender(log4js.appenders.console());
log4js.addAppender(log4js.appenders.file('logs/nano' + serverPort + '.log'), 'nano' + serverPort);
var logger = log4js.getLogger('nano' + serverPort);
logger.setLevel('INFO'); // TRACE, DEBUG, INFO, WARN, ERROR, FATAL

// 用到的SQL命令
var sqlCmds = {
  queryByIcon: 'SELECT series, label, label_en, pkg, launcher FROM code WHERE icon = ? LIMIT 128',
  queryBySeries: 'SELECT icon, label, label_en, pkg, launcher FROM code WHERE series = ?',
  queryByPkg: 'SELECT series, label, label_en, launcher, icon FROM code WHERE pkg = ? LIMIT 128',
  queryByLabel: 'SELECT label, label_en, pkg, launcher, icon FROM code WHERE label LIKE ? OR label_en LIKE ? LIMIT 128',
  req: 'INSERT IGNORE INTO req(icon, label, label_en, pkg, launcher, sys_app, icon_pack, device_id, device_brand, device_model, device_sdk) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
  sumByIpP: 'SELECT COUNT(*) AS num FROM req WHERE icon_pack = ? AND pkg = ?',
  sumByIpPDi: 'SELECT device_id, COUNT(*) AS num FROM req WHERE icon_pack = ? AND pkg = ? GROUP BY device_id = ?',
  reqTopFilterMarked: 'SELECT label, pkg, COUNT(*) AS sum, 0 AS filter FROM req AS r WHERE icon_pack = ? AND pkg NOT IN (SELECT pkg FROM req_filter AS rf WHERE rf.icon_pack = r.icon_pack AND user = ?) GROUP BY pkg ORDER BY sum DESC, pkg ASC LIMIT ?',
  reqTopOnlyMarked: 'SELECT label, pkg, COUNT(*) AS sum, 1 AS filter FROM req AS r WHERE icon_pack = ? AND pkg IN (SELECT pkg FROM req_filter AS rf WHERE rf.icon_pack = r.icon_pack AND user = ?) GROUP BY pkg ORDER BY sum DESC, pkg ASC',
  reqTop: 'SELECT label, pkg, COUNT(*) AS sum, 1 AS filter FROM req WHERE icon_pack = ? GROUP BY pkg ORDER BY sum DESC, pkg ASC LIMIT ?',
  reqFilter: 'INSERT IGNORE INTO req_filter(icon_pack, user, pkg) VALUES(?, ?, ?)',
  reqUndoFilter: 'DELETE FROM req_filter WHERE icon_pack = ? AND user = ? AND pkg = ?',
  queryByPkg2: 'SELECT label, label_en AS labelEn, pkg, launcher, icon, COUNT(*) AS sum FROM req WHERE pkg = ? GROUP BY label, label_en, launcher',
  queryByLabel2: 'SELECT label, label_en AS labelEn, pkg, launcher, icon, COUNT(*) AS sum FROM req WHERE label LIKE ? OR label_en LIKE ? GROUP BY label, label_en, launcher LIMIT 128',
  sumReqTimes: 'SELECT COUNT(*) AS sum FROM req',
  sumApps: 'SELECT COUNT(*) AS sum FROM (SELECT pkg FROM req GROUP BY pkg, launcher) AS pkgs',
  sumIconPacks: 'SELECT COUNT(*) AS sum FROM (SELECT icon_pack FROM req GROUP BY icon_pack HAVING COUNT(icon_pack) > 32) AS iconPacks',
  baseApps: 'SELECT s.label, s.label_en, s.name, r.pkg, r.launcher, r.device_brand FROM series AS s LEFT JOIN req AS r ON s.name = r.series WHERE s.sys = 1 GROUP BY s.name, r.pkg, r.launcher ORDER BY s.name, r.pkg, r.launcher'
};


// ====================================== API BLOCK START ======================================= //


// 接口：按图标名精确检索（TODO 移除）
/*app.get('/nanoiconpack/icon/:icon', function(req, res) {
  var icon = req.params.icon;
  logger.info('GET /nanoiconpack/icon/' + icon);
  query(sqlCmds.queryByIcon, [icon], function(err, rows) {
    if (err) {
      res.send(err);
      return;
    }
    rows.sort(utils.sortBy('icon', false, String)); // 按图标名排序
    var codes = '';
    for (var i in rows) {
      codes += utils.getCode(rows[i].label, rows[i].label_en, rows[i].pkg,
        rows[i].launcher, icon) + '\n\n';
    }
    if (rows.length >= 128) {
      codes += rows.length + ' in total and more is omitted.';
    } else {
      codes += rows.length + ' in total.';
    }

    if (rows.length > 0) {
      query(sqlCmds.queryBySeries, [rows[0].series], function(err1, rows1) {
        if (err1 || rows1.length == 0) {
          res.set('Content-Type', 'text/plain; charset=utf-8');
          res.send(codes);
          return;
        }
        var codes1 = '';
        for (var i in rows1) {
          var tmp = utils.getCode(rows1[i].label, rows1[i].label_en, rows1[i].pkg,
            rows1[i].launcher, rows1[i].icon);
          if (codes.indexOf(tmp) == -1) {
            codes1 += '\n\n' + tmp;
          }
        }
        if (codes1.length > 0) {
          codes += ' And the related:' + codes1;
        }
        res.set('Content-Type', 'text/plain; charset=utf-8');
        res.send(codes);
      });
    } else {
      res.set('Content-Type', 'text/plain; charset=utf-8');
      res.send(codes);
    }
  });
});*/

// 接口：按包名精确检索（TODO 移除）
/*app.get('/nanoiconpack/pkg/:pkg', function(req, res) {
  var pkg = req.params.pkg;
  logger.info('GET /nanoiconpack/pkg/' + pkg);
  query(sqlCmds.queryByPkg, [pkg], function(err, rows) {
    if (err) {
      res.send(err);
      return;
    }
    rows.sort(utils.sortBy('icon', false, String)); // 按图标名排序
    var codes = '';
    for (var i in rows) {
      codes += utils.getCode(rows[i].label, rows[i].label_en, req.params.pkg,
        rows[i].launcher, rows[i].icon) + '\n\n';
    }
    if (rows.length >= 128) {
      codes += rows.length + ' in total and more is omitted.';
    } else {
      codes += rows.length + ' in total.';
    }

    if (rows.length > 0) {
      query(sqlCmds.queryBySeries, [rows[0].series], function(err1, rows1) {
        if (err1 || rows1.length == 0) {
          res.set('Content-Type', 'text/plain; charset=utf-8');
          res.send(codes);
          return;
        }
        var codes1 = '';
        for (var i in rows1) {
          var tmp = utils.getCode(rows1[i].label, rows1[i].label_en, rows1[i].pkg,
                rows1[i].launcher, rows1[i].icon);
          if (codes.indexOf(tmp) == -1) {
            codes1 += '\n\n' + tmp;
          }
        }
        if (codes1.length > 0) {
          codes += ' And the related:' + codes1;
        }
        res.set('Content-Type', 'text/plain; charset=utf-8');
        res.send(codes);
      });
    } else {
      res.set('Content-Type', 'text/plain; charset=utf-8');
      res.send(codes);
    }
  });
});*/

// 接口：按目标APP中文名、英文名模糊检索（TODO 移除）
/*app.get('/nanoiconpack/label/:label', function(req, res) {
  var label = req.params.label;
  logger.info('GET /nanoiconpack/label/' + label);
  var sqlOptions = ['%' + label + '%', '%' + label + '%'];
  query(sqlCmds.queryByLabel, sqlOptions, function(err, rows) {
    if (err) {
      res.send(err);
      return;
    }
    rows.sort(utils.sortBy('icon', false, String)); // 按图标名排序
    var codes = '';
    for (var i in rows) {
      codes += utils.getCode(rows[i].label, rows[i].label_en, rows[i].pkg,
        rows[i].launcher, rows[i].icon) + '\n\n';
    }
    if (rows.length >= 128) {
      codes += rows.length + ' in total and more is omitted.';
    } else {
      codes += rows.length + ' in total.';
    }
    res.set('Content-Type', 'text/plain; charset=utf-8');
    res.send(codes);
  });
});*/

// 接口：申请适配图标
app.post('/nanoiconpack/req/:iconpack([A-Za-z\\d\._]+)', function(req, res) {
  var iconPack = req.params.iconpack;
  logger.info('POST /nanoiconpack/req/' + iconPack);
  var icon = req.body.icon;
  if (!icon) {
    icon = null;
  }
  var label = req.body.label;
  if (!label) {
    label = null;
  }
  var labelEn = req.body.labelEn;
  if (!labelEn) {
    labelEn = null;
  }
  var pkg = req.body.pkg;
  if (!pkg) {
    logger.warn('REJECT: No req.body.pkg');
    res.jsonp(utils.getResRes(2));
    return;
  }
  var launcher = req.body.launcher;
  if (!launcher) {
    logger.warn('REJECT: No req.body.launcher');
    res.jsonp(utils.getResRes(2));
    return;
  }
  var sysApp = req.body.sysApp;
  if (sysApp == '1' || sysApp == 'true') { // typeof sysApp string
    sysApp = 1;
  } else {
    sysApp = 0;
  }
  var deviceId = req.body.deviceId;
  if (!deviceId) {
    logger.warn('REJECT: No req.body.deviceId');
    res.jsonp(utils.getResRes(2));
    return;
  }
  var deviceBrand = req.body.deviceBrand;
  if (!deviceBrand) {
    deviceBrand = null;
  }
  var deviceModel = req.body.deviceModel;
  if (!deviceModel) {
    deviceModel = null;
  }
  var deviceSdk = parseInt(req.body.deviceSdk);
  if (!deviceSdk) {
    deviceSdk = 0;
  }
  var sqlOptions = [icon, label, labelEn, pkg, launcher, sysApp, iconPack, deviceId, deviceBrand, deviceModel, deviceSdk];
  query(sqlCmds.req, sqlOptions, function(err, rows) {
    if (err) {
      logger.warn(err);
      res.jsonp(utils.getResRes(3));
      return;
    }
    var sqlOptions1 = [iconPack, pkg];
    query(sqlCmds.sumByIpP, sqlOptions1, function(err1, rows1) {
      if (err1) {
        logger.warn(err1);
        res.jsonp(utils.getResRes(3));
        return;
      }
      res.jsonp(utils.getResRes(rows.affectedRows > 0 ? 0 : 4, undefined, rows1[0].num));
    });
  });
});

// 接口：查询对目标APP的请求适配次数
app.get('/nanoiconpack/reqnum/:iconpack([A-Za-z\\d\._]+)/:pkg([A-Za-z\\d\._]+)', function(req, res) {
  var iconPack = req.params.iconpack;
  var pkg = req.params.pkg;
  var deviceId = req.query.deviceid;
  if (!deviceId) {
    deviceId = null;
  }
  logger.info('GET /nanoiconpack/reqnum/' + iconPack + '/' + pkg + '?deviceid=' + deviceId);
  var sqlOptions = [iconPack, pkg, deviceId];
  query(sqlCmds.sumByIpPDi, sqlOptions, function(err, rows) {
    if (err) {
      logger.warn(err);
      res.jsonp(utils.getResRes(3));
      return;
    }
    if (rows.length == 2) { // 该设备此前已申请过
      res.jsonp(utils.getResRes(0, undefined, { num: rows[0].num + rows[1].num, reqed: 1 }));
    } else if (rows.length == 1) {
      if (rows[0].device_id == deviceId) { // 该设备此前已申请过
        res.jsonp(utils.getResRes(0, undefined, { num: rows[0].num, reqed: 1 }));
      } else {
        res.jsonp(utils.getResRes(0, undefined, { num: rows[0].num, reqed: 0 }));
      }
    } else {
      res.jsonp(utils.getResRes(0, undefined, { num: 0, reqed: 0 }));
    }
  });
});

// 接口：查询请求数TOP的APP
app.get('/nanoiconpack/reqtop/:iconpack([A-Za-z\\d\._]+)/:user', function(req, res) {
  var iconPack = req.params.iconpack;
  var user = req.params.user;
  var limitNum = parseInt(req.query.limit);
  if (!limitNum) {
    limitNum = 32;
  } else if (limitNum < 0) {
    limitNum = 0;
  } else if (limitNum > 128) {
    limitNum = 128;
  }
  var filterMarked = req.query.filter; // 过滤掉已标记的APP（默认false）
  if (filterMarked == 1 || filterMarked == 'true') {
    filterMarked = 1;
  } else {
    filterMarked = 0;
  }
  logger.info('GET /nanoiconpack/reqtop/' + iconPack + '/' + user + '?limit=' + limitNum + '&filter=' + filterMarked);
  var sqlOptions = [iconPack, user, limitNum];
  query(sqlCmds.reqTopFilterMarked, sqlOptions, function(err, rows) {
    if (err) {
      logger.warn(err);
      res.jsonp(utils.getResRes(3));
      return;
    }
    if (filterMarked == 1) {
      res.jsonp(utils.getResRes(0, undefined, rows));
      return;
    }
    var sqlOptions1 = [iconPack, limitNum];
    query(sqlCmds.reqTop, sqlOptions1, function(err1, rows1) {
      if (err1) {
        logger.warn(err1);
        res.jsonp(utils.getResRes(3));
        return;
      }
      var j = 0;
      for (var i in rows) {
        for (; j < rows1.length; ++j) {
          if (rows[i].pkg == rows1[j].pkg) {
            rows1[j].filter = 0;
            break;
          }
        }
      }
      res.jsonp(utils.getResRes(0, undefined, rows1));
    });
  });
});

// 接口：在已过滤的APP中查询请求数TOP的APP
app.get('/nanoiconpack/reqtopfiltered/:iconpack([A-Za-z\\d\._]+)/:user', function(req, res) {
  var iconPack = req.params.iconpack;
  var user = req.params.user;
  logger.info('GET /nanoiconpack/reqtopfiltered/' + iconPack + '/' + user);
  var sqlOptions = [iconPack, user];
  query(sqlCmds.reqTopOnlyMarked, sqlOptions, function(err, rows) {
    if (err) {
      logger.warn(err);
      res.jsonp(utils.getResRes(3));
      return;
    }
    res.jsonp(utils.getResRes(0, undefined, rows));
  });
});

// 接口：对申请适配的APP标记已处理
app.post('/nanoiconpack/reqfilter/:iconpack([A-Za-z\\d\._]+)/:user', function(req, res) {
  var iconPack = req.params.iconpack;
  var user = req.params.user;
  logger.info('POST /nanoiconpack/reqfilter/' + iconPack + '/' + user);
  var pkg = req.body.pkg;
  if (!pkg) {
    logger.warn('REJECT: No req.body.pkg');
    res.jsonp(utils.getResRes(2));
    return;
  }
  var sqlOptions = [iconPack, user, pkg];
  query(sqlCmds.reqFilter, sqlOptions, function(err, rows) {
    if (err) {
      logger.warn(err);
      res.jsonp(utils.getResRes(3));
      return;
    }
    if (rows.affectedRows > 0) {
      res.jsonp(utils.getResRes(0));
    } else {
      res.jsonp(utils.getResRes(4));
    }
  });
});

// 接口：对申请适配的APP标记未处理
app.delete('/nanoiconpack/reqfilter/:iconpack([A-Za-z\\d\._]+)/:user', function(req, res) {
  var iconPack = req.params.iconpack;
  var user = req.params.user;
  logger.info('DELEET /nanoiconpack/reqfilter/' + iconPack + '/' + user);
  var pkg = req.query.pkg;
  if (!pkg) {
    logger.warn('REJECT: No req.body.pkg');
    res.jsonp(utils.getResRes(2));
    return;
  }
  var sqlOptions = [iconPack, user, pkg];
  query(sqlCmds.reqUndoFilter, sqlOptions, function(err, rows) {
    if (err) {
      logger.warn(err);
      res.jsonp(utils.getResRes(3));
      return;
    }
    if (rows.affectedRows > 0) {
      res.jsonp(utils.getResRes(0));
    } else {
      res.jsonp(utils.getResRes(5));
    }
  });
});

// 接口：根据包名、APP中英文名查询APP代码
app.get('/nanoiconpack/code/:keyword', function(req, res) {
  var keyword = req.params.keyword;
  if (keyword.length == 1 && keyword.charCodeAt(0) < 128) {
    res.jsonp(utils.getResRes(2));
    return;
  }
  logger.info('GET /nanoiconpack/code/' + keyword);
  var sql;
  var sqlOptions;
  if ((new RegExp('^[a-zA-Z\\d_]+\\.[a-zA-Z\\d_\\.]+$')).test(keyword)) {
    sql = sqlCmds.queryByPkg2;
    sqlOptions = [keyword];
  } else {
    sql = sqlCmds.queryByLabel2;
    sqlOptions = ['%' + keyword + '%', '%' + keyword + '%'];
  }
  query(sql, sqlOptions, function(err, rows) {
    if (err) {
      logger.warn(err);
      res.jsonp(utils.getResRes(3));
      return;
    }
    res.jsonp(utils.getResRes(0, undefined, rows));
  });
});

// 接口：查询请求总数和APP总数
app.get('/nanoiconpack/sum', function(req, res) {
  logger.info('GET /nanoiconpack/sum');
  query(sqlCmds.sumReqTimes, [], function(err, rows) {
    if (err) {
      logger.warn(err);
      res.jsonp(utils.getResRes(3));
      return;
    }
    query(sqlCmds.sumApps, [], function(err1, rows1) {
      var result = {
        reqTimes: rows[0].sum,
        apps: -1,
        iconPacks: -1
      };
      if (err1) {
        logger.warn(err1);
        res.jsonp(utils.getResRes(0, undefined, result));
        return;
      }
      result.apps = rows1[0].sum;
      query(sqlCmds.sumIconPacks, [], function(err2, rows2) {
        if (err2) {
          logger.warn(err2);
          res.jsonp(utils.getResRes(0, undefined, result));
          return;
        }
        result.iconPacks = rows2[0].sum;
        res.jsonp(utils.getResRes(0, undefined, result));
      });
    });
  });
});

// 接口：获取各系统常用APP代码（如电话、信息、相机等）
app.get('/nanoiconpack/base', function(req, res) {
  logger.info('GET /nanoiconpack/base');
  query(sqlCmds.baseApps, [], function(err, rows) {
    if (err) {
      logger.warn(err);
      res.jsonp(utils.getResRes(3));
      return;
    }
    var result = [];
    var lastRow = {};
    for (var i in rows) {
      var row = rows[i];
      if (row.name != lastRow.name) {
        lastRow = row;
        result.push({icon: row.name, label: row.label, labelEn: row.label_en, more: []});
      }
      if (row.pkg && row.launcher) {
        result[result.length - 1].more.push({pkg: row.pkg, launcher: row.launcher, brand: row.device_brand});
      }
    }
    res.jsonp(utils.getResRes(0, undefined, result));
  });
});

// 根据包名获取图标链接（来源为酷安）（TODO 移除）
/*app.get('/nanoiconpack/iconurl/:pkg([A-Za-z\\d\._]+)', function(req, res) {
  var pkg = req.params.pkg;
  logger.info('GET /nanoiconpack/iconurl/' + pkg);
  var url = 'http://api.coolapk.com/market/v2/api.php'
    + '?apikey=5b90704e1db879af6f5ee08ec1e8f2a5&method=getApkMeta&qt=apkname&slm=1'
    + '&v=' + pkg;
  var option = {
    hostname: 'api.coolapk.com',
    path: '/market/v2/api.php?apikey=5b90704e1db879af6f5ee08ec1e8f2a5&method=getApkMeta&qt=apkname&slm=1&v=' + pkg,
    headers: {
      'User-Agent': 'Mozilla/5.1 (Linux; Android 5.1; Nexus 5 Build/LRX22C; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/53.0.2785.124 Mobile Safari/537.36 +CoolMarket/2.5.4',
      'Cookie': 'coolapk_did=d41d8cd98f00b204e9800998ecf8427e'
    },
    method: 'GET'
  };
  var req1 = http.request(option, function(res1) {
    var body = '';
    res1.on('data',function(buffer) {
      body += buffer;
    }).on('end', function() {
      try {
        var jsonData = JSON.parse(body);
        res.jsonp(utils.getResRes(0, undefined, jsonData.logo));
        return;
      } catch(err) {
        logger.warn('invalid json data: ' + body);
      }
      res.jsonp(utils.getResRes(6));
    });
  }).on('error', function(err) {
    logger.warn(err);
    res.jsonp(utils.getResRes(6));
  });
  req1.end();
});*/

// 接口：看门狗
app.get('/nanoiconpack/watchdog', function(req, res) {
  logger.info('GET /nanoiconpack/watchdog');
  
  res.jsonp(utils.getResRes(0, undefined, {
    port: serverPort,
    time: Date.now()
  }));
});

// 接口：错误
/*app.get('*', function(req, res) {
  res.status(404).send('404');
});*/


// ======================================= API BLOCK END ======================================== //


// ====================================== PAGE BLOCK START ====================================== //


app.get('/nanoiconpack', function(req, res) {
  res.redirect('/nanoiconpack/page/query');
});

// 页面：检索
app.get('/nanoiconpack/page/query', function(req, res) {
  logger.info('GET /nanoiconpack/page/query');
  res.sendFile(__dirname + '/pages/query.htm');
});

// 页面：常用APP一览
app.get('/nanoiconpack/page/base', function(req, res) {
  logger.info('GET /nanoiconpack/page/base');
  res.sendFile(__dirname + '/pages/base.htm');
});


// ======================================= PAGE BLOCK END ======================================= //


var server = app.listen(serverPort, function() {
  var host = server.address().address;
  var port = server.address().port;

  if (host == '::') {
    host = 'localhost';
  }
  
  logger.info('http://%s:%s/nanoiconpack/', host, port);
});

logger.info('NanoIconPackServer is running...');