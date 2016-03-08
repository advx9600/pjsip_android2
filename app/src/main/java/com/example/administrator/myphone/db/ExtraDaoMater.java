package com.example.administrator.myphone.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import gen.DaoMaster;
import gen.DaoSession;
import gen.TbConfig;
import gen.TbConfigDao;

/**
 * Created by Administrator on 2016/2/24.
 */
public class ExtraDaoMater {
    public static void onCreate(SQLiteDatabase db,Context con) {
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        TbConfigDao dao = daoSession.getTbConfigDao();

        addSelf(con, dao);
    }

    private static void addSelf(Context con, TbConfigDao dao) {
//        TbConfig tb = new TbConfig();
//        tb.setVal("");
//        tb.setName("");
//        dao.insert(tb);
    }

}
