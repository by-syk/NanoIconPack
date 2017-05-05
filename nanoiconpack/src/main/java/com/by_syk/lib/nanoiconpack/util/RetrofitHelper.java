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

package com.by_syk.lib.nanoiconpack.util;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by By_syk on 2017-02-25.
 */

public class RetrofitHelper {
    private Retrofit retrofit;
    private Retrofit retrofit4Coolapk;

    private static RetrofitHelper retrofitHelper;

    private RetrofitHelper() {
        init();
    }

    private void init() {
        retrofit = new Retrofit.Builder()
                .baseUrl(C.URL_NANO_SERVER) // baseUrl must end in /
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public RetrofitHelper init4Coolapk() {
        if (retrofit4Coolapk != null) {
            return retrofitHelper;
        }

        retrofit4Coolapk = new Retrofit.Builder()
                .baseUrl(C.URL_COOLAPK_API)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofitHelper;
    }

//    public Retrofit getRetrofit() {
//        return retrofit;
//    }
//
//    public Retrofit getRetrofit4Coolapk() {
//        return retrofit4Coolapk;
//    }

    public <T> T getService(Class<T> service) {
        return retrofit.create(service);
    }

    public <T> T getService4Coolapk(Class<T> service) {
        return retrofit4Coolapk.create(service);
    }

    public static RetrofitHelper getInstance() {
        if (retrofitHelper == null) {
            synchronized(RetrofitHelper.class) {
                if (retrofitHelper == null) {
                    retrofitHelper = new RetrofitHelper();
                }
            }
        }
        return retrofitHelper;
    }
}
