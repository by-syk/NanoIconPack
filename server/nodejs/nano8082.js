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
-- APP 代码表
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
  PRIMARY KEY(pkg, launcher, icon_pack, device_id)
);
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
 */

var http = require('http');
var express = require('express'); // npm install express
var bodyParser = require('body-parser'); // npm install body-parser
var log4js = require('log4js'); // npm install log4js
var query = require('./mysql');
var utils = require('./utils');

var app = express();

// 解析 POST application/x-www-form-urlencoded
app.use(bodyParser.urlencoded({ extended: false }));
// 解析 POST JSON
//app.use(bodyParser.json({ limit: '1mb' }));

// console log is loaded by default, so you won't normally need to do this
//log4js.loadAppender('console');
log4js.loadAppender('file');
//log4js.addAppender(log4js.appenders.console());
log4js.addAppender(log4js.appenders.file('logs/nano8082.log'), 'nano8082');
var logger = log4js.getLogger('nano8082');
logger.setLevel('INFO'); // TRACE, DEBUG, INFO, WARN, ERROR, FATAL


// ====================================== API BLOCK START ======================================= //


// 接口：主页
app.get('/nanoiconpack', function(req, res) {
  logger.info('GET /nanoiconpack');
  res.set('Content-Type', 'text/plain; charset=utf-8');
  res.send('APIs:'
    + '\n  // icon name - accurate search'
    + '\n  ' + req.originalUrl + '/icon/:icon'
    + '\n  // package name - accurate search'
    + '\n  ' + req.originalUrl + '/pkg/:pkg'
    + '\n  // app label - fuzzy search'
    + '\n  ' + req.originalUrl + '/label/:label'
    + '\n\nData is gathered from NanoIconPack, Sorcery, iFlat, IrideUI, Antimo, etc..'
    + '\n\nHope this helps.'
    + '\n\nCopyright © 2017 By_syk. All rights reserved.');
});

// 接口：按图标名精确检索
app.get('/nanoiconpack/icon/:icon', function(req, res) {
  logger.info('GET /nanoiconpack/icon/' + req.params.icon);
  var cmd = 'SELECT series, label, label_en, pkg, launcher FROM code WHERE icon = \''
    + req.params.icon.replace('\'', '\\\'') + '\' LIMIT 128';
  query(cmd, function(err, rows) {
    if (err) {
      res.send(err);
      return;
    }
    rows.sort(utils.sortBy('icon', false, String)); // 按图标名排序
    var codes = '';
    for (var i in rows) {
      codes += utils.getCode(rows[i].label, rows[i].label_en, rows[i].pkg,
        rows[i].launcher, req.params.icon) + '\n\n';
    }
    if (rows.length >= 128) {
      codes += rows.length + ' in total and more is omitted.';
    } else {
      codes += rows.length + ' in total.';
    }

    if (rows.length > 0) {
      var cmd1 = 'SELECT icon, label, label_en, pkg, launcher FROM code WHERE series = \''
        + rows[0].series + '\'';
      query(cmd1, function(err1, rows1) {
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
});

// 接口：按包名精确检索
app.get('/nanoiconpack/pkg/:pkg', function(req, res) {
  logger.info('GET /nanoiconpack/pkg/' + req.params.pkg);
  var cmd = 'SELECT series, label, label_en, launcher, icon FROM code WHERE pkg = \''
    + req.params.pkg.replace('\'', '\\\'') + '\' LIMIT 128';
  query(cmd, function(err, rows) {
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
      var cmd1 = 'SELECT icon, label, label_en, pkg, launcher FROM code WHERE series = \''
        + rows[0].series + '\'';
      query(cmd1, function(err1, rows1) {
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
});

// 接口：按目标APP中文名、英文名模糊检索
app.get('/nanoiconpack/label/:label', function(req, res) {
  logger.info('GET /nanoiconpack/label/' + req.params.label);
  var cmd = 'SELECT label, label_en, pkg, launcher, icon FROM code WHERE label LIKE \'%'
    + req.params.label.replace('\'', '\\\'') + '%\' OR label_en LIKE \'%'
    + req.params.label.replace('\'', '\\\'') + '%\' LIMIT 128';
  query(cmd, function(err, rows) {
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
});

// 接口：申请重绘图标
app.post('/nanoiconpack/req/:iconpack([A-Za-z\\d\._]+)', function(req, res) {
  var iconPack = '\'' + req.params.iconpack + '\'';
  logger.info('POST /nanoiconpack/req/' + iconPack);
  var icon = req.body.icon;
  if (icon) {
    icon = '\'' + icon + '\'';
  } else {
    icon = 'null';
  }
  var label = req.body.label;
  if (label) {
    label = '\'' + label.replace('\'', '\\\'') + '\'';
  } else {
    label = 'null';
  }
  var labelEn = req.body.labelEn;
  if (labelEn) {
    labelEn = '\'' + labelEn.replace('\'', '\\\'') + '\'';
  } else {
    labelEn = 'null';
  }
  var pkg = req.body.pkg;
  if (pkg) {
    pkg = '\'' + pkg + '\'';
  } else {
    logger.warn('REJECT: No req.body.pkg');
    res.jsonp(utils.getResRes(2));
    return;
  }
  var launcher = req.body.launcher;
  if (launcher) {
    launcher = '\'' + launcher + '\'';
  } else {
    logger.warn('REJECT: No req.body.launcher');
    res.jsonp(utils.getResRes(2));
    return;
  }
  var sysApp = req.body.sysApp;
  if (sysApp == 1 || sysApp == 'true') {
    sysApp = 1;
  } else {
    sysApp = 0;
  }
  var deviceId = req.body.deviceId;
  if (deviceId) {
    deviceId = '\'' + deviceId + '\'';
  } else {
    logger.warn('REJECT: No req.body.deviceId');
    res.jsonp(utils.getResRes(2));
    return;
  }
  var deviceBrand = req.body.deviceBrand;
  if (deviceBrand) {
    deviceBrand = '\'' + deviceBrand + '\'';
  } else {
    deviceBrand = 'null';
  }
  var deviceModel = req.body.deviceModel;
  if (deviceModel) {
    deviceModel = '\'' + deviceModel + '\'';
  } else {
    deviceModel = 'null';
  }
  var deviceSdk = req.body.deviceSdk;
  if (!deviceSdk) {
    deviceSdk = 0;
  }
  var cmd = 'INSERT IGNORE INTO req(icon, label, label_en, pkg, launcher, sys_app,'
    + ' icon_pack, device_id, device_brand, device_model, device_sdk) VALUES('
    + icon + ', ' + label + ', ' + labelEn + ', ' + pkg + ', ' + launcher + ', ' + sysApp
    + ', ' + iconPack + ', ' + deviceId + ', ' + deviceBrand + ', ' + deviceModel + ', ' + deviceSdk + ')';
  query(cmd, function(err, rows) {
    if (err) {
      logger.warn(err);
      res.jsonp(utils.getResRes(3));
      return;
    }
    var cmd1 = 'SELECT COUNT(*) AS num FROM req WHERE icon_pack = ' + iconPack + ' AND pkg = ' + pkg;
    query(cmd1, function(err1, rows1) {
      if (err1) {
        logger.warn(err1);
        res.jsonp(utils.getResRes(rows.affectedRows > 0 ? 0 : 4));
        return;
      }
      res.jsonp(utils.getResRes(rows.affectedRows > 0 ? 0 : 4, undefined, rows1[0].num));
    });
    /*if (rows.affectedRows > 0) {
      res.jsonp(utils.getResRes(0));
    } else {
      res.jsonp(utils.getResRes(4));
    }*/
  });
});

// 接口：查询APP请求重绘图标次数
app.get('/nanoiconpack/reqnum/:iconpack([A-Za-z\\d\._]+)/:pkg([A-Za-z\\d\._]+)', function(req, res) {
  var iconPack = req.params.iconpack;
  var pkg = req.params.pkg;
  logger.info('GET /nanoiconpack/reqnum/' + iconPack + '/' + pkg);
  var deviceId = req.query.deviceid;
  var cmd = 'SELECT COUNT(*) AS num FROM req WHERE icon_pack = \'' + iconPack + '\' AND pkg = \'' + pkg + '\'';
  query(cmd, function(err, rows) {
    if (err) {
      logger.warn(err);
      res.jsonp(utils.getResRes(3));
      return;
    }
    if (!deviceId) {
      res.jsonp(utils.getResRes(0, undefined, { num: rows[0].num, reqed: 0 }));
      return;
    }
    var cmd1 = 'SELECT COUNT(*) AS num FROM req WHERE icon_pack = \'' + iconPack + '\' AND pkg = \'' + pkg
      + '\' AND device_id = \'' + deviceId + '\'';
    query(cmd1, function(err1, rows1) {
      var result = {
        num: rows[0].num,
        reqed: 0
      };
      if (err1) {
        logger.warn(err1);
      } else {
        result.reqed = rows1[0].num;
      }
      res.jsonp(utils.getResRes(0, undefined, result));
    });
  });
});

// 接口：查询请求数TOP的APP
app.get('/nanoiconpack/reqtop/:iconpack([A-Za-z\\d\._]+)/:user', function(req, res) {
  var iconPack = req.params.iconpack;
  var user = req.params.user;
  var limitNum = req.query.limit;
  if (!limitNum) {
    limitNum = 32;
  } else if (limitNum < 0) {
    limitNum = 0;
  } else if (limitNum > 128) {
    limitNum = 128;
  }
  var filter = req.query.filter;
  logger.info('GET /nanoiconpack/reqtop/' + iconPack + '/' + user + '?limit=' + limitNum + '&filter=' + filter);
  var cmd = 'SELECT label, pkg, COUNT(*) AS sum, 0 AS filter FROM req AS r WHERE icon_pack = \'' + iconPack
    + '\' AND pkg NOT IN (SELECT pkg FROM req_filter AS rf WHERE rf.icon_pack = r.icon_pack AND user = \'' + user + '\')'
    + ' GROUP BY pkg ORDER BY sum DESC, pkg ASC LIMIT ' + limitNum;
  query(cmd, function(err, rows) {
    if (err) {
      logger.warn(err);
      res.jsonp(utils.getResRes(3));
      return;
    }
    if (filter == 'true' || filter == 1) {
      res.jsonp(utils.getResRes(0, undefined, rows));
      return;
    }
    var cmd1 = 'SELECT label, pkg, COUNT(*) AS sum, 1 AS filter FROM req AS r WHERE icon_pack = \'' + iconPack
      + '\' GROUP BY pkg ORDER BY sum DESC, pkg ASC LIMIT ' + limitNum;
    query(cmd1, function(err1, rows1) {
      if (err1) {
        logger.warn(err);
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

// 接口：对申请重绘的APP标记已处理
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
  var cmd = 'INSERT IGNORE INTO req_filter(icon_pack, user, pkg) VALUES(\''
    + iconPack + '\', \'' + user.replace('\'', '\\\'') + '\', \'' + pkg + '\')';
  query(cmd, function(err, rows) {
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

// 接口：对申请重绘的APP标记未处理
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
  var cmd = 'DELETE FROM req_filter WHERE icon_pack = \'' + iconPack + '\' AND user = \''
    + user.replace('\'', '\\\'') + '\' AND pkg = \'' + pkg + '\'';
  query(cmd, function(err, rows) {
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

// 接口：根据包名查询APP代码
app.get('/nanoiconpack/code/:pkg([A-Za-z\\d\._]+)', function(req, res) {
  logger.info('GET /nanoiconpack/code/' + req.params.pkg);
  var cmd = 'SELECT label, label_en AS labelEn, pkg, launcher, icon FROM req WHERE pkg = \''
    + req.params.pkg + '\' GROUP BY label, label_en, launcher';
  query(cmd, function(err, rows) {
    if (err) {
      logger.warn(err);
      res.jsonp(utils.getResRes(3));
      return;
    }
    res.jsonp(utils.getResRes(0, undefined, rows));
  });
});

// 根据包名获取图标链接（来源为酷安）
app.get('/nanoiconpack/iconurl/:pkg([A-Za-z\\d\._]+)', function(req, res) {
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
});

// 接口：错误
/*app.get('*', function(req, res) {
  res.status(404).send('404');
});*/

// 接口：测试
app.get('/nanoiconpack/test', function(req, res) {
  res.jsonp(utils.getResRes(0));
});


// ======================================= API BLOCK END ======================================== //


var server = app.listen(8082, function() {
  var host = server.address().address;
  var port = server.address().port;

  if (host == '::') {
    host = 'localhost';
  }
  
  logger.info('http://%s:%s/nanoiconpack/', host, port);
});

logger.info('NanoIconPackServer is running...');