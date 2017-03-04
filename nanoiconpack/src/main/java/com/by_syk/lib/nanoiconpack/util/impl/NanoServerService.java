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
import com.by_syk.lib.nanoiconpack.bean.ResResBean;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by By_syk on 2017-02-24.
 */

public interface NanoServerService {
    /**
     * { "status": 0, "msg": "success" }
     */
    @GET("/")
    Call<ResResBean> testServer();

    /**
     * { "status": 0, "msg": "success", "result": 5 }
     */
    @FormUrlEncoded
    @POST("req/{iconpack}")
    Call<ResResBean<Integer>> reqRedraw(@Path("iconpack") String iconPack,
                               @FieldMap Map<String, String> fields);

    /**
     * { "status": 0, "msg": "success", "result": { "num": 5, "reqed": 1 } }
     */
    @GET("reqnum/{iconpack}/{pkg}")
    Call<ResResBean<JsonObject>> getReqNum(@Path("iconpack") String iconPack,
                                           @Path("pkg") String pkgName,
                                           @Query("deviceid") String deviceId);

    /**
     * { "status": 0, "msg": "success", "result": [
     *   { "label": "快图浏览", "pkg": "com.alensw.PicFolder", "sum": 2, "filter": 0 }
     * ]}
     */
    @GET("reqtop/{iconpack}/{user}")
    Call<ResResBean<JsonArray>> getReqTop(@Path("iconpack") String iconPack,
                                          @Path("user") String user,
                                          @Query("limit") int limitNum,
                                          @Query("filter") boolean filter);

    /**
     * { "status": 0, "msg": "success" }
     */
    @FormUrlEncoded
    @POST("reqfilter/{iconpack}/{user}")
    Call<ResResBean> filterPkg(@Path("iconpack") String iconPack,
                               @Path("user") String user,
                               @Field("pkg") String pkgName);

    /**
     * { "status": 0, "msg": "success" }
     */
    @DELETE("reqfilter/{iconpack}/{user}")
    Call<ResResBean> undoFilterPkg(@Path("iconpack") String iconPack,
                                   @Path("user") String user,
                                   @Query("pkg") String pkgName);

    /**
     * { "status": 0, "msg": "success", "result": [
     *   { "label": "快图浏览", "labelEn": "QuickPic", "pkg": "com.alensw.PicFolder",
     *     "launcher": "com.alensw.PicFolder.GalleryActivity", "icon": "quick_pic" }
     * ]}
     */
    @GET("code/{pkg}")
    Call<ResResBean<JsonArray>> getCode(@Path("pkg") String pkgName);

//    /**
//     * { "status": 0, "msg": "success",
//     *   "result": "http://image.coolapk.com/apk_logo/2016/0108/12202_1452248424_4592.png"
//     * }
//     */
//    @GET("iconurl/{pkg}")
//    Call<ResResBean<String>> getIconUrl(@Path("pkg") String pkgName);

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
