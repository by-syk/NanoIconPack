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

import com.by_syk.lib.nanoiconpack.bean.ResResBean;
import com.by_syk.lib.nanoiconpack.util.impl.NanoServerService;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by By_syk on 2017-02-25.
 */

public class RetrofitHelper {
    private Retrofit retrofit;

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

    public Retrofit getRetrofit() {
        return retrofit;
    }

    /**
     * Cannot run in UiThread
     */
    public boolean testServer() {
        NanoServerService nanoServerService = retrofit.create(NanoServerService.class);
        Call<ResResBean> call = nanoServerService.testServer();
        try {
            ResResBean resResBean = call.execute().body();
            return resResBean != null && resResBean.isStatusSuccess();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static RetrofitHelper getInstance() {
        if (retrofitHelper == null) {
            retrofitHelper = new RetrofitHelper();
        }
        return retrofitHelper;
    }
}
