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

// 用到的SQL命令
exports.sqlCmds = {
  req: 'INSERT IGNORE INTO req(icon, label, label_en, pkg, launcher, sys_app, icon_pack, device_id, device_brand, device_model, device_sdk) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
  sumByIpP: 'SELECT COUNT(*) AS num FROM req WHERE icon_pack = ? AND pkg = ?',
  sumByIpPDi: 'SELECT device_id, COUNT(*) AS num FROM req WHERE icon_pack = ? AND pkg = ? GROUP BY device_id = ?',
  reqTopFilterMarked: 'SELECT label, pkg, COUNT(*) AS sum, 0 AS filter FROM req AS r WHERE icon_pack = ? AND pkg NOT IN (SELECT pkg FROM req_filter AS rf WHERE rf.icon_pack = r.icon_pack AND user = ?) GROUP BY pkg ORDER BY sum DESC, pkg ASC LIMIT ?',
  //reqTopFilterMarked2: 'SELECT label, pkg, launcher, COUNT(*) AS sum, 0 AS filter FROM req AS r WHERE icon_pack = ? AND CONCAT(pkg, \'/\', launcher) NOT IN (SELECT CONCAT(pkg, \'/\', launcher) FROM req_filter AS rf WHERE rf.icon_pack = r.icon_pack AND user = ?) GROUP BY pkg, launcher ORDER BY sum DESC, pkg ASC LIMIT ?',
  reqTopFilterMarked2: 'SELECT r.label, r.pkg, r.launcher, r.sum, r.filter FROM (SELECT label, pkg, launcher, COUNT(*) AS sum, 0 AS filter FROM req WHERE icon_pack = ? GROUP BY pkg, launcher) AS r LEFT JOIN (SELECT pkg, launcher FROM req_filter WHERE icon_pack = ? AND user = ? AND launcher <> \'\') AS rf ON r.pkg = rf.pkg AND r.launcher = rf.launcher WHERE rf.pkg IS NULL ORDER BY r.sum DESC, r.pkg ASC LIMIT ?',
  reqTopOnlyMarked: 'SELECT label, pkg, COUNT(*) AS sum, 1 AS filter FROM req AS r WHERE icon_pack = ? AND pkg IN (SELECT pkg FROM req_filter AS rf WHERE rf.icon_pack = r.icon_pack AND user = ?) GROUP BY pkg ORDER BY sum DESC, pkg ASC',
  //reqTopOnlyMarked2: 'SELECT label, pkg, launcher, COUNT(*) AS sum, 1 AS filter FROM req AS r WHERE icon_pack = ? AND CONCAT(pkg, \'/\', launcher) IN (SELECT CONCAT(pkg, \'/\', launcher) FROM req_filter AS rf WHERE rf.icon_pack = r.icon_pack AND user = ?) GROUP BY pkg, launcher ORDER BY sum DESC, pkg ASC',
  reqTopOnlyMarked2: 'SELECT r.label, r.pkg, r.launcher, COUNT(*) AS sum, 1 AS filter FROM req AS r INNER JOIN req_filter AS rf ON r.icon_pack = rf.icon_pack AND r.pkg = rf.pkg AND r.launcher = rf.launcher WHERE r.icon_pack = ? AND rf.user = ? GROUP BY r.pkg, r.launcher ORDER BY sum DESC, r.pkg ASC',
  reqTop: 'SELECT label, pkg, COUNT(*) AS sum, 1 AS filter FROM req WHERE icon_pack = ? GROUP BY pkg ORDER BY sum DESC, pkg ASC LIMIT ?',
  reqTop2: 'SELECT label, pkg, launcher, COUNT(*) AS sum, 1 AS filter FROM req WHERE icon_pack = ? GROUP BY pkg, launcher ORDER BY sum DESC, pkg ASC LIMIT ?',
  reqFilter: 'INSERT IGNORE INTO req_filter(icon_pack, user, pkg, launcher) VALUES(?, ?, ?, ?)',
  reqUndoFilter: 'DELETE FROM req_filter WHERE icon_pack = ? AND user = ? AND pkg = ?',
  reqUndoFilter2: 'DELETE FROM req_filter WHERE icon_pack = ? AND user = ? AND pkg = ? AND launcher = ?',
  queryByPkg: 'SELECT label, label_en AS labelEn, pkg, launcher, icon, COUNT(*) AS sum FROM req WHERE pkg = ? GROUP BY label, label_en, launcher',
  queryByLabel: 'SELECT label, label_en AS labelEn, pkg, launcher, icon, COUNT(*) AS sum FROM req WHERE label LIKE ? OR label_en LIKE ? GROUP BY label, label_en, launcher LIMIT 128',
  queryByPkgLauncher: 'SELECT label, label_en AS labelEn, pkg, launcher, icon FROM req WHERE pkg = ? AND launcher = ? GROUP BY label, label_en',
  sumReqTimes: 'SELECT COUNT(*) AS sum FROM req',
  sumApps: 'SELECT COUNT(*) AS sum FROM (SELECT pkg FROM req GROUP BY pkg, launcher) AS pkgs',
  sumIconPacks: 'SELECT COUNT(*) AS sum FROM (SELECT icon_pack FROM req GROUP BY icon_pack HAVING COUNT(icon_pack) > 128) AS iconPacks',
  statsReqTimesMonth: 'SELECT ips.label, pkgs.* FROM (SELECT icon_pack AS pkg, COUNT(*) AS reqs FROM req WHERE time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) GROUP BY icon_pack HAVING reqs > 32 ORDER BY reqs DESC) AS pkgs LEFT JOIN icon_pack AS ips ON pkgs.pkg = ips.pkg',
  statsUsersMonth: 'SELECT pkg, COUNT(*) AS users FROM (SELECT icon_pack AS pkg FROM req WHERE time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) GROUP BY icon_pack, device_id) AS t0 GROUP BY pkg HAVING users > 4 ORDER BY users DESC',
  trendReqTimesWeek: 'SELECT DATE_FORMAT(time, \'%x\') AS year, DATE_FORMAT(time, \'%u\') AS week, COUNT(*) AS reqs FROM req WHERE icon_pack = ? GROUP BY year, week ORDER BY year, week ASC',
  trendUsersWeek: 'SELECT year, week, COUNT(*) AS users FROM (SELECT DATE_FORMAT(time, \'%x\') AS year, DATE_FORMAT(time, \'%u\') AS week FROM req WHERE icon_pack = ? GROUP BY year, week, device_id) AS t0 GROUP BY year, week ORDER BY year, week ASC',
  baseApps: 'SELECT s.label, s.label_en, s.name, r.pkg, r.launcher, r.device_brand FROM series AS s LEFT JOIN req AS r ON s.name = r.series WHERE s.sys = 1 GROUP BY s.name, r.pkg, r.launcher ORDER BY s.name, r.pkg, r.launcher',
  queryIpByIp: 'SELECT label, pkg FROM icon_pack WHERE pkg = ?',
  queryIpByLabel: 'SELECT label, pkg FROM icon_pack WHERE label LIKE ? OR label_en LIKE ?',
  ip: 'SELECT ips.label, pkgs.pkg FROM (SELECT icon_pack AS pkg, COUNT(*) AS sum FROM req GROUP BY icon_pack HAVING sum > 128 ORDER BY sum DESC) AS pkgs LEFT JOIN icon_pack AS ips ON pkgs.pkg = ips.pkg'
};

// 拼装APP代码
exports.getCode = function(label, labelEn, pkg, launcher, icon) {
  if (!label) {
    label = '';
  }
  if (!labelEn) {
    labelEn = '';
  }
  if (!pkg) {
    pkg = '';
  }
  if (!launcher) {
    launcher = '';
  }
  if (!icon) {
    icon = '';
  }
  var code = '<!-- ' + label + ' / ' + labelEn + ' -->';
  code += '\n<item component="ComponentInfo{' + pkg + '/' + launcher + '}" drawable="' + icon + '" />';
  return code;
};

// 拼装APP代码
exports.getCodeLite = function(pkg, launcher, icon) {
  if (!pkg) {
    pkg = '';
  }
  if (!launcher) {
    launcher = '';
  }
  if (!icon) {
    icon = '';
  }
  return '<item component="ComponentInfo{' + pkg + '/' + launcher + '}" drawable="' + icon + '" />';
};

// JSON 排序
exports.sortBy = function(filed, rev, primer) {
  rev = (rev) ? -1 : 1;
  return function (a, b) {
    a = a[filed];
    b = b[filed];
    if (typeof (primer) != 'undefined') {
      a = primer(a);
      b = primer(b);
    }
    if (a < b) { return rev * -1; }
    if (a > b) { return rev * 1; }
    return 1;
  }
};

// 解析客户端IP
/*exports.getClientIp = function(req) {
  return req.headers['x-forwarded-for']
    || req.connection.remoteAddress
    || req.socket.remoteAddress
    || req.connection.socket.remoteAddress;
};*/

// 拼装返回JSON数据
exports.getResRes = function(status, msg, result) {
  if (msg == undefined) {
    if (status == 0) {
      msg = 'success';
    } else if (status == 1) {
      msg = 'error';
    } else if (status == 2) {
      msg = 'invalid_req';
    } else if (status == 3) {
      msg = 'sql_error';
    } else if (status == 4) {
      msg = 'existed';
    } else if (status == 5) {
      msg = 'no_such';
    } else if (status == 6) {
      msg = 'http_error';
    }
  }
  var resJson = {
    status: status,
    msg: msg
  };
  if (result != undefined) {
    resJson.result = result;
  }
  return resJson;
}
