package com.example.administrator.myphone.dao;

import java.util.List;

import gen.TbUser;
import gen.TbUserDao;

/**
 * Created by Administrator on 2016/2/26.
 */
public class TbUserDaoImp {

    public static TbUser getUser(TbUserDao mUserDao) {
        List<TbUser> list = mUserDao.queryBuilder().list();
        if (list.size() == 0) return  null;
        return  list.get(0);
    }
}
