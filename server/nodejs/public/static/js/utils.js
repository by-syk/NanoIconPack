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

// 复制文本内容到剪切板
function copy2ClipboardT(text) {
  //var $temp = $("<input>"); // 不支持换行符
  var $temp = $("<textarea>");
  $("body").append($temp);
  $temp.val(text).select();
  document.execCommand("copy");
  $temp.remove();
}

// 从目标组件读取文本内容并复制到剪切板
function copy2ClipboardE(element) {
  var text = $(element).text();
  if (!text) { // <textarea />
    text = $(element).val();
  }
  copy2ClipboardT(text);
}

// APP英文名代码化
function codeAppName(name) {
  if (!name) {
    return "";
  }
  name = name.trim();
  if (name.length == 0) {
    return "";
  }
  // 注意不是 /^[A-Za-z][A-Za-z\d'\+-\. _]*$/
  if (/^[A-Za-z][A-Za-z\d'\+\-\. _]*$/.test(name)) {
    var res;
    while ((res = /([a-z][A-Z])|([A-Za-z]\d)|(\d[A-Za-z])/.exec(name)) != null) {
      name = name.replace(res[0], res[0].charAt(0) + "_" + res[0].charAt(1));
    }
    return name.toLowerCase()
      .replace(/'/g, "")
      .replace(/\+/g, "_plus")
      .replace(/-|\.| /g, "_")
      .replace(/_{2,}/g, '_');
  }
  return "";
}

function shrinkPkg(pkg) {
  if (pkg.lastIndexOf("iconpack") == pkg.length - 8) {
    if (pkg.length < 16) {
      pkg = pkg.substring(0, pkg.length - 8) + "...";
    } else {
      pkg = "..." + pkg.substring(pkg.length - 16, pkg.length - 8) + "...";
    }
  } else {
    pkg = "..." + pkg.substring(pkg.length - 8, pkg.length);
  }
  return pkg;
}

function getQuery(name) {
   var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
   var r = window.location.search.substr(1).match(reg);
   if (r != null) {
     return decodeURIComponent(r[2]);
   }
   return undefined;
}

function getWeekOrder() {
  var time, week, checkDate = new Date(new Date());
  checkDate.setDate(checkDate.getDate() + 4 - (checkDate.getDay() || 7));
  time = checkDate.getTime();
  checkDate.setMonth(0);
  checkDate.setDate(1);
  return Math.floor(Math.round((time - checkDate) / 86400000) / 7) + 1;
}
