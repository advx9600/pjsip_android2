package com.example.administrator.myphone.db;

/**
 * Created by Administrator on 2016/2/24.
 */
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import gen.DaoMaster;
import gen.DaoSession;

public class DB {
    public static SQLiteDatabase getWritableDb(Context act) {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(act,
                "/mnt/sdcard/my_phone.db", null);
        return helper.getWritableDatabase();
    }

    public static DaoSession getDaoSession(Context con) {
        return (new DaoMaster(getWritableDb(con))).newSession();
        // noteDao = daoSession.getNoteDao();
    }

    public static DaoSession getDaoSession(SQLiteDatabase db) {
        return (new DaoMaster(db)).newSession();
    }
}