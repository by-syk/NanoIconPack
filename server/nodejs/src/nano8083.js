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

var express = require('express'); // npm install express
var bodyParser = require('body-parser'); // npm install body-parser
var log4js = require('log4js'); // npm install log4js
var path = require('path');
var query = require('./utils/mysql');
var utils = require('./utils/utils');

// 服务运行目标端口
var serverPort = 8083;

var app = express();

// 解析 POST application/x-www-form-urlencoded
app.use(bodyParser.urlencoded({ extended: false }));
// 解析 POST JSON
//app.use(bodyParser.json({ limit: '1mb' }));

// 支持静态文件
app.use(express.static('../pages/public'));

// console log is loaded by default, so you won't normally need to do this
//log4js.loadAppender('console');
log4js.loadAppender('file');
//log4js.addAppender(log4js.appenders.console());
log4js.addAppender(log4js.appenders.file('../logs/nano' + serverPort + '.log'), 'nano' + serverPort);
var logger = log4js.getLogger('nano' + serverPort);
logger.setLevel('INFO'); // TRACE, DEBUG, INFO, WARN, ERROR, FATAL


// ====================================== API BLOCK START ======================================= //


// 接口：申请适配图标
app.post('/req/:iconpack([A-Za-z\\d\._]+)', function(req, res) {
  var iconPack = req.params.iconpack;
  logger.info('POST /req/' + iconPack);
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
  query(utils.sqlCmds.req, sqlOptions, function(err, rows) {
    if (err) {
      logger.warn(err);
      res.jsonp(utils.getResRes(3));
      return;
    }
    var sqlOptions1 = [iconPack, pkg];
    query(utils.sqlCmds.sumByIpP, sqlOptions1, function(err1, rows1) {
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
app.get('/reqnum/:iconpack([A-Za-z\\d\._]+)/:pkg([A-Za-z\\d\._]+)', function(req, res) {
  var iconPack = req.params.iconpack;
  var pkg = req.params.pkg;
  var deviceId = req.query.deviceid;
  if (!deviceId) {
    deviceId = null;
  }
  logger.info('GET /reqnum/' + iconPack + '/' + pkg + '?deviceid=' + deviceId);
  var sqlOptions = [iconPack, pkg, deviceId];
  query(utils.sqlCmds.sumByIpPDi, sqlOptions, function(err, rows) {
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

// TODO DEPRECATED
// 接口：查询请求数TOP的APP
app.get('/reqtop/:iconpack([A-Za-z\\d\._]+)/:user', function(req, res) {
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
  logger.info('GET /reqtop/' + iconPack + '/' + user + '?limit=' + limitNum + '&filter=' + filterMarked);
  var sqlOptions = [iconPack, user, limitNum];
  query(utils.sqlCmds.reqTopFilterMarked, sqlOptions, function(err, rows) {
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
    query(utils.sqlCmds.reqTop, sqlOptions1, function(err1, rows1) {
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

// 接口：查询请求数TOP的APP
app.get('/reqtop2/:iconpack([A-Za-z\\d\._]+)/:user', function(req, res) {
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
  logger.info('GET /reqtop2/' + iconPack + '/' + user + '?limit=' + limitNum + '&filter=' + filterMarked);
  // var sqlOptions = [iconPack, user, limitNum];
  var sqlOptions = [iconPack, iconPack, user, limitNum];
  query(utils.sqlCmds.reqTopFilterMarked2, sqlOptions, function(err, rows) {
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
    query(utils.sqlCmds.reqTop2, sqlOptions1, function(err1, rows1) {
      if (err1) {
        logger.warn(err1);
        res.jsonp(utils.getResRes(3));
        return;
      }
      var j = 0;
      for (var i in rows) {
        for (; j < rows1.length; ++j) {
          if (rows[i].pkg == rows1[j].pkg && rows[i].launcher == rows1[j].launcher) {
            rows1[j].filter = 0;
            break;
          }
        }
      }
      res.jsonp(utils.getResRes(0, undefined, rows1));
    });
  });
});

// TODO DEPRECATED
// 接口：在已标记的APP中查询请求数TOP的APP
app.get('/reqtopfiltered/:iconpack([A-Za-z\\d\._]+)/:user', function(req, res) {
  var iconPack = req.params.iconpack;
  var user = req.params.user;
  logger.info('GET /reqtopfiltered/' + iconPack + '/' + user);
  var sqlOptions = [iconPack, user];
  query(utils.sqlCmds.reqTopOnlyMarked, sqlOptions, function(err, rows) {
    if (err) {
      logger.warn(err);
      res.jsonp(utils.getResRes(3));
      return;
    }
    res.jsonp(utils.getResRes(0, undefined, rows));
  });
});

// 接口：在已标记的APP中查询请求数TOP的APP
app.get('/reqtopfiltered2/:iconpack([A-Za-z\\d\._]+)/:user', function(req, res) {
  var iconPack = req.params.iconpack;
  var user = req.params.user;
  logger.info('GET /reqtopfiltered2/' + iconPack + '/' + user);
  var sqlOptions = [iconPack, user];
  query(utils.sqlCmds.reqTopOnlyMarked2, sqlOptions, function(err, rows) {
    if (err) {
      logger.warn(err);
      res.jsonp(utils.getResRes(3));
      return;
    }
    res.jsonp(utils.getResRes(0, undefined, rows));
  });
});

// 接口：对申请适配的APP标记已处理
app.post('/reqfilter/:iconpack([A-Za-z\\d\._]+)/:user', function(req, res) {
  var iconPack = req.params.iconpack;
  var user = req.params.user;
  logger.info('POST /reqfilter/' + iconPack + '/' + user);
  var pkg = req.body.pkg;
  if (!pkg) {
    logger.warn('REJECT: No req.body.pkg');
    res.jsonp(utils.getResRes(2));
    return;
  }
  var launcher = req.body.launcher;
  var sqlOptions = [iconPack, user, pkg, launcher];
  query(utils.sqlCmds.reqFilter, sqlOptions, function(err, rows) {
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
app.delete('/reqfilter/:iconpack([A-Za-z\\d\._]+)/:user', function(req, res) {
  var iconPack = req.params.iconpack;
  var user = req.params.user;
  logger.info('DELEET /reqfilter/' + iconPack + '/' + user);
  var pkg = req.query.pkg;
  if (!pkg) {
    logger.warn('REJECT: No req.query.pkg');
    res.jsonp(utils.getResRes(2));
    return;
  }
  var launcher = req.query.launcher;
  if (!launcher) { // 兼容旧版本
    sqlCmd = utils.sqlCmds.reqUndoFilter;
    sqlOptions = [iconPack, user, pkg];
  } else {
    sqlCmd = utils.sqlCmds.reqUndoFilter2;
    sqlOptions = [iconPack, user, pkg, launcher];
  }
  query(sqlCmd, sqlOptions, function(err, rows) {
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
app.get('/code/:keyword', function(req, res) {
  var keyword = req.params.keyword;
  if (keyword.length == 1 && keyword.charCodeAt(0) < 128) {
    res.jsonp(utils.getResRes(2));
    return;
  }
  logger.info('GET /code/' + keyword);
  var sql;
  var sqlOptions;
  if ((new RegExp('^[a-zA-Z\\d_]+\\.[a-zA-Z\\d_\\.]+$')).test(keyword)) {
    sql = utils.sqlCmds.queryByPkg;
    sqlOptions = [keyword];
  } else {
    sql = utils.sqlCmds.queryByLabel;
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

// 接口：根据包名+启动项查询APP代码
app.get('/code/:pkg/:launcher', function(req, res) {
  var pkg = req.params.pkg;
  var launcher = req.params.launcher;
  logger.info('GET /code/' + pkg + '/' + launcher);
  var sqlOptions = [pkg, launcher];
  query(utils.sqlCmds.queryByPkgLauncher, sqlOptions, function(err, rows) {
    if (err) {
      logger.warn(err);
      res.jsonp(utils.getResRes(3));
      return;
    }
    res.jsonp(utils.getResRes(0, undefined, rows));
  });
});

// 接口：查询请求总数、APP总数和图标包总数
app.get('/sum', function(req, res) {
  logger.info('GET /sum');
  query(utils.sqlCmds.sumReqTimes, [], function(err, rows) {
    if (err) {
      logger.warn(err);
      res.jsonp(utils.getResRes(3));
      return;
    }
    query(utils.sqlCmds.sumApps, [], function(err1, rows1) {
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
      query(utils.sqlCmds.sumIconPacks, [], function(err2, rows2) {
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

// 接口：统计各图标包最近一月申请用户数和申请次数
app.get('/stats/month', function(req, res) {
  logger.info('GET /stats/month');
  query(utils.sqlCmds.statsReqTimesMonth, [], function(err, rows) {
    if (err) {
      logger.warn(err);
      res.jsonp(utils.getResRes(3));
      return;
    }
    query(utils.sqlCmds.statsUsersMonth, [], function(err1, rows1) {
      if (err1) {
        logger.warn(err1);
        res.jsonp(utils.getResRes(0, undefined, rows));
        return;
      }
      for (var i in rows) {
        for (var j in rows1) {
          if (rows1[j].pkg == rows[i].pkg) {
            rows[i].users = rows1[j].users;
            rows1.splice(j, 1);
            break;
          }
        }
      }
      res.jsonp(utils.getResRes(0, undefined, rows));
    });
  });
});

// 接口：统计目标图标包周申请趋势
app.get('/trend/week/:ip', function(req, res) {
  var iconPack = req.params.ip;
  logger.info('GET /trend/week/' + iconPack);
  if (!iconPack) {
    res.jsonp(utils.getResRes(2));
    return;
  }
  var sqlCmd;
  var sqlOptions;
  if (/^[A-Za-z\d_]+\.[A-Za-z\d\._]+$/.test(iconPack)) {
    sqlCmd = utils.sqlCmds.queryIpByIp;
    sqlOptions = [iconPack];
  } else {
    sqlCmd = utils.sqlCmds.queryIpByLabel;
    sqlOptions = ['%' + iconPack + '%', '%' + iconPack + '%']
  }
  query(sqlCmd, sqlOptions, function(err, rows) {
    if (err) {
      logger.warn(err);
      res.jsonp(utils.getResRes(3));
      return;
    }
    if (rows.length != 1) {
      res.jsonp(utils.getResRes(2));
      return;
    }
    var result = {
      label: rows[0].label,
      pkg: rows[0].pkg,
      weeks: []
    };
    var sqlOptions1 = [rows[0].pkg];
    query(utils.sqlCmds.trendReqTimesWeek, sqlOptions1, function(err1, rows1) {
      if (err1) {
        logger.warn(err1);
        res.jsonp(utils.getResRes(3));
        return;
      }
      for (var i in rows1) {
        if (rows1[i].week != '00') { // week 有效值 01-53
          result.weeks.push(rows1[i]);
        }
      }
      query(utils.sqlCmds.trendUsersWeek, sqlOptions1, function(err2, rows2) {
        if (err2) {
          logger.warn(err2);
          res.jsonp(utils.getResRes(0, undefined, result));
          return;
        }
        for (var i in result.weeks) {
          for (var j in rows2) {
            if (result.weeks[i].year == rows2[j].year && result.weeks[i].week == rows2[j].week) {
              result.weeks[i].users = rows2[j].users;
              rows2.splice(j, 1);
              break;
            }
          }
        }
        res.jsonp(utils.getResRes(0, undefined, result));
      });
    });
  });
});

// 接口：获取所有图标包
app.get('/iconpacks', function(req, res) {
  logger.info('GET /iconpacks');
  query(utils.sqlCmds.ip, [], function(err, rows) {
    if (err) {
      logger.warn(err);
      res.jsonp(utils.getResRes(3));
      return;
    }
    res.jsonp(utils.getResRes(0, undefined, rows));
  });
});

// 接口：获取各系统常用APP代码（如电话、信息、相机等）
app.get('/base', function(req, res) {
  logger.info('GET /base');
  query(utils.sqlCmds.baseApps, [], function(err, rows) {
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
        result.push({
          icon: row.name,
          label: row.label,
          labelEn: row.label_en,
          more: []
        });
      }
      if (row.pkg && row.launcher) {
        result[result.length - 1].more.push({
          pkg: row.pkg,
          launcher: row.launcher,
          brand: row.device_brand
        });
      }
    }
    res.jsonp(utils.getResRes(0, undefined, result));
  });
});

// 根据包名获取图标链接（来源为酷安）（TODO 移除）
/*app.get('/iconurl/:pkg([A-Za-z\\d\._]+)', function(req, res) {
  var pkg = req.params.pkg;
  logger.info('GET /iconurl/' + pkg);
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
app.get('/watchdog', function(req, res) {
  logger.info('GET /watchdog');

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


app.get('/', function(req, res) {
  res.redirect('/page/console');
});

// 页面：控制台主页
app.get('/page/console', function(req, res) {
  logger.info('GET /page/console');
  //res.sendFile(__dirname + '/pages/console.htm');
  res.sendFile(path.resolve('../pages/console.htm'));
});

// 页面：APP代码速查
app.get('/page/query', function(req, res) {
  logger.info('GET /page/query');
  res.sendFile(path.resolve('../pages/query.htm'));
});

// 页面：常用APP代码
app.get('/page/base', function(req, res) {
  logger.info('GET /page/base');
  res.sendFile(path.resolve('../pages/base.htm'));
});

// 页面：图标包统计
app.get('/page/stats', function(req, res) {
  logger.info('GET /page/stats');
  res.sendFile(path.resolve('../pages/stats.htm'));
});

// 页面：申请管理
app.get('/page/mark', function(req, res) {
  logger.info('GET /page/mark');
  res.sendFile(path.resolve('../pages/mark.htm'));
});


// ======================================= PAGE BLOCK END ======================================= //


var server = app.listen(serverPort, function() {
  var host = server.address().address;
  var port = server.address().port;

  if (host == '::') {
    host = 'localhost';
  }

  logger.info('http://%s:%s/', host, port);
});

logger.info('NanoIconPackServer is running...');
