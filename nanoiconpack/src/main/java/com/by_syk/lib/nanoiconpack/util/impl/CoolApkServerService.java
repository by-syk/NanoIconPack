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

package com.by_syk.lib.nanoiconpack.util.impl;

import com.by_syk.lib.nanoiconpack.bean.CoolApkApkDetailBean;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Query;

/**
 * Created by By_syk on 2017-02-24.
 */

public interface CoolApkServerService {
    /**
     * { "data" { "logo": "http://image.coolapk.com/apk_logo/2016/0108/12202_1452248424_4592.png" } }
     */
    @Headers({
            "User-Agent: Dalvik/2.1.0 (Linux; U; Android 5.1.1; Nexus 4 Build/LMY48T) (#Build; google; Nexus 4; LMY48T; 5.1.1) +CoolMarket/7.3",
            "X-Requested-With: XMLHttpRequest",
            "X-Sdk-Int: 22",
            "X-Sdk-Locale: zh-CN",
            "X-App-Id: coolmarket",
//            "X-App-Token: ",
            "X-App-Version: 7.4",
            "X-App-Code: 1702202",
            "X-Api-Version: 7"
    })
    @GET("apk/detail")
    Call<CoolApkApkDetailBean> getCoolApkApkDetail(@Header("X-App-Token") String token, @Query("id") String pkgName);
}
