package com.example.administrator.myphone.dao;

import com.example.administrator.myphone.a.a.a.a.a;

import java.util.List;

import gen.TbBuddy;
import gen.TbBuddyDao;

/**
 * Created by Administrator on 2016/3/2.
 */
public class mBuddyDaoImp {
    public static void addOrUpdate(TbBuddyDao mBuddyDao, TbBuddy buddy) {
        List<TbBuddy> list = mBuddyDao.queryBuilder().where(TbBuddyDao.Properties.Phone.eq(buddy.getPhone())).list();
        if (list.size()>0){
            list.get(0).setName(buddy.getName());
            mBuddyDao.update(list.get(0));
        }else{
            mBuddyDao.insert(buddy);
        }
    }
}
