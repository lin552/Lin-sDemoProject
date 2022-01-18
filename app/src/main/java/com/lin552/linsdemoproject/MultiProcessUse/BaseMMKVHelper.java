package com.lin552.linsdemoproject.MultiProcessUse;

import com.lin552.linsdemoproject.MainApplication;
import com.tencent.mmkv.MMKV;

public class BaseMMKVHelper {

    private MMKV mmkv;

    protected BaseMMKVHelper() {
        MMKV.initialize(MainApplication.Companion.instance());
        mmkv = MMKV.mmkvWithID(getMMKVName(), MMKV.SINGLE_PROCESS_MODE);
    }

    interface BaseMMKVHelperHolder {
        BaseMMKVHelper INSTANCE = new BaseMMKVHelper();
    }

    private static BaseMMKVHelper getDefault() {
        return BaseMMKVHelperHolder.INSTANCE;
    }

    public String getMMKVName() {
        return BaseMMKVHelper.class.getSimpleName();
    }

    /**
     * //5.2.1 版本 注释掉
     *
     * @return
     */

    public static MMKV mmkv() {
        return getDefault().mmkv;
    }

    protected MMKV getMMKVInstance() {
        return mmkv;
    }

}
