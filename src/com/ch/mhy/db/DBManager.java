package com.ch.mhy.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

import com.ch.mhy.entity.ComicsDetail;
import com.ch.mhy.entity.Down;
import com.ch.mhy.entity.DownListData;

/**
 * 数据库管理器 2015年7月30日
 *
 */
public class DBManager {
	private DBHelper helper;
	private SQLiteDatabase db;

	public DBManager(Context context, String name, CursorFactory factory,
			int version) {
		helper = new DBHelper(context, name, factory, version);
		// 因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0,
		// mFactory);
		// 所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
		db = helper.getWritableDatabase();
	}

	public boolean isOpen() {
		return db.isOpen();
	}

	/**
	 * add persons
	 *
	 * @param persons
	 */
	public void addComicsDetail(List<ComicsDetail> cds) {
		db.beginTransaction(); // 开始事务
		try {
			for (ComicsDetail cd : cds) {
				db.execSQL(
						"INSERT INTO comic VALUES(?,?,?, ?, ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
						new Object[] {null, cd.mQid, cd.mId, cd.mName, cd.mUrl,
								cd.mNo, cd.CreateTime, cd.mTitle, cd.mDirector,
								cd.mPic, cd.mTotal, cd.mLianzai, cd.rIndex,
								cd.maxNo, cd.mContent, cd.mType1, cd.mFenAll,
								cd.localUrl, cd.port, cd.readTime, cd.minNo,cd.pmNo,cd.nmNo,cd.updateMessage });
			}
			db.setTransactionSuccessful(); // 设置事务成功完成
		} finally {
			db.endTransaction(); // 结束事务
		}
	}

	/**
	 * add Down
	 *
	 * @param persons
	 */
	public synchronized void addDown(List<Down> cds) {
		db.beginTransaction(); // 开始事务
		try {
			for (Down cd : cds) {
				db.execSQL(
						"INSERT INTO down VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
						new Object[] {null, cd.getCd().mQid, cd.getCd().mId,
								cd.getCd().mName, cd.getCd().mUrl,
								cd.getCd().mNo, cd.getCd().CreateTime,
								cd.getCd().mTitle, cd.getCd().mDirector,
								cd.getCd().mPic, cd.getCd().mTotal,
								cd.getCd().mLianzai, cd.getCd().mNo,
								cd.getCd().maxNo, cd.getIsdonw(),
								cd.getDowns(), cd.getCd().mType1,
								cd.getCd().mContent, cd.getCd().mFenAll,
								cd.getCd().localUrl, cd.getCd().port,
								new Date(), cd.getCd().minNo,cd.getCd().pmNo,cd.getCd().nmNo,cd.getCd().updateMessage });
			}
			db.setTransactionSuccessful(); // 设置事务成功完成
		} finally {
			db.endTransaction(); // 结束事务
		}
	}

	/**
	 * update Down
	 *
	 * @param persons
	 */
	public void updateDown(List<Down> cds) {
		db.beginTransaction(); // 开始事务
		try {
			for (Down cd : cds) {
				db.execSQL("update down set isdown=? where mQid=?",
						new Object[] { cd.getIsdonw(), cd.getCd().mQid });
			}
			db.setTransactionSuccessful(); // 设置事务成功完成
		} finally {
			db.endTransaction(); // 结束事务
		}

		// Log.d("tag", "批量修改完毕！");
	}

	/**
	 * add persons
	 *
	 * @param persons
	 */
	public void addOrUpdateComicDetail(ComicsDetail cd) {
		this.delete(cd);
		db.beginTransaction(); // 开始事务
		try {
			db.execSQL(
					"INSERT INTO comic VALUES(?,?, ?, ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
					new Object[] {null, cd.mQid, cd.mId, cd.mName, cd.mUrl, cd.mNo,
							cd.CreateTime, cd.mTitle, cd.mDirector, cd.mPic,
							cd.mTotal, cd.mLianzai, cd.rIndex, cd.maxNo,
							cd.mContent, cd.mType1, cd.mFenAll, cd.localUrl,
							cd.port, System.currentTimeMillis(), cd.minNo,cd.pmNo,cd.nmNo ,cd.updateMessage});
			db.setTransactionSuccessful(); // 设置事务成功完成
		} finally {
			db.endTransaction(); // 结束事务
		}
	}

	/**
	 * add persons
	 *
	 * @param persons
	 */
	public synchronized void  addOrUpdateDown(Down cd) {
		this.delete(cd);
		db.beginTransaction(); // 开始事务
		try {
			db.execSQL(
					"INSERT INTO down VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
					new Object[] {null, cd.getCd().mQid, cd.getCd().mId,
							cd.getCd().mName, cd.getCd().mUrl, cd.getCd().mNo,
							cd.getCd().CreateTime, cd.getCd().mTitle,
							cd.getCd().mDirector, cd.getCd().mPic,
							cd.getCd().mTotal, cd.getCd().mLianzai,
							cd.getCd().mNo, cd.getCd().maxNo, cd.getIsdonw(),
							cd.getDowns(), cd.getCd().mType1,
							cd.getCd().mContent, cd.getCd().mFenAll,
							cd.getCd().localUrl, cd.getCd().port, new Date(),
							cd.getCd().minNo ,cd.getCd().pmNo,cd.getCd().nmNo,cd.getCd().updateMessage});
			db.setTransactionSuccessful(); // 设置事务成功完成
		} finally {
			db.endTransaction(); // 结束事务
		}
	}

	/**
	 * 更新当前
	 *
	 * @param person
	 */
	public void updateDowns(Integer mQid, Integer downs) {
		ContentValues cv = new ContentValues();
		cv.put("downs", downs);

		db.beginTransaction(); // 开始事务
		try {
			db.update("down", cv, "mQid = ?", new String[] { mQid.toString() });
			db.setTransactionSuccessful(); // 设置事务成功完成
		} finally {
			db.endTransaction(); // 结束事务
		}
	}

	/**
	 * update person's age
	 *
	 * @param person
	 */
	public void updateDown(Integer mQid, Integer isDown) {
		ContentValues cv = new ContentValues();
		cv.put("isdown", isDown);

		db.beginTransaction(); // 开始事务

		try {
			db.update("down", cv, "mQid = ?", new String[] { mQid + "" });
			db.setTransactionSuccessful(); // 设置事务成功完成
		} finally {
			db.endTransaction(); // 结束事务
		}
	}

	int n = 0;

	public boolean isAdd(Integer mid) {
		Cursor c = db.rawQuery("SELECT * FROM comic where mId=" + mid, null);

		if (c.moveToNext()) {
			n++;
			return true;
		}
		return false;
	}

	public void delete(ComicsDetail cd) {
		db.delete("comic", "mId = ?", new String[] { String.valueOf(cd.mId) });
	}

	/**
	 * 批量删除
	 * 
	 * @param cds
	 *            书本集合
	 */
	public void delete(List<ComicsDetail> cds) {
		for (int i = 0; i < cds.size(); i++) {
			db.delete("comic", "mId = ?",
					new String[] { String.valueOf(cds.get(i).mId) });
		}
	}

	public void delete(Down cd) {
		db.delete("down", "mQid = ?",
				new String[] { String.valueOf(cd.getCd().getmQid()) });
	}

	public void deleteDowns(List<Down> downs) {
		for (int i = 0; i < downs.size(); i++) {
			db.delete(
					"down",
					"mQid = ?",
					new String[] { String.valueOf(downs.get(i).getCd()
							.getmQid()) });
		}
	}

	public void deleteByMid(int mid) {
		db.delete("down", "mId = ?", new String[] { mid + "" });
	}

	public ComicsDetail queryByMid(int mid) {
		Cursor c = db.rawQuery("SELECT * FROM comic where mId=" + mid, null);
		ComicsDetail cd = new ComicsDetail();
		while (c.moveToNext()) {
			cd.mId = c.getInt(c.getColumnIndex("mId"));
			cd.mTitle = c.getString(c.getColumnIndex("mTitle"));
			cd.mContent=c.getString(c.getColumnIndex("mContent"));
			cd.mQid = c.getInt(c.getColumnIndex("mQid"));
			cd.mUrl = c.getString(c.getColumnIndex("mUrl"));
			cd.mName = c.getString(c.getColumnIndex("mName"));
			cd.CreateTime = c.getString(c.getColumnIndex("CreateTime"));
			cd.mLianzai = c.getString(c.getColumnIndex("mLianzai"));
			cd.mDirector = c.getString(c.getColumnIndex("mDirector"));
			cd.mTotal = c.getInt(c.getColumnIndex("mTotal"));
			cd.mPic = c.getString(c.getColumnIndex("mPic"));
			cd.rIndex = c.getInt(c.getColumnIndex("rIndex"));
			cd.maxNo = c.getInt(c.getColumnIndex("maxNo"));
			cd.minNo = c.getInt(c.getColumnIndex("minNo"));
			cd.pmNo = c.getInt(c.getColumnIndex("pmNo"));
			cd.nmNo = c.getInt(c.getColumnIndex("nmNo"));
			cd.mNo = c.getInt(c.getColumnIndex("mNo"));
			cd.localUrl = c.getString(c.getColumnIndex("localUrl"));
			cd.port = c.getString(c.getColumnIndex("port"));
			cd.updateMessage = c.getString(c.getColumnIndex("updateMessage"));
		}
		c.close();

		if (cd.getmUrl() != null && cd.getLocalUrl() != null
				&& cd.getmPic() != null) {
			return cd;
		} else {
			return null;
		}
	}

	/**
	 * 查询最近阅读的漫画
	 * 
	 * @return
	 */
	public ComicsDetail queryMaxtReadTime() {
		Cursor c = db.rawQuery(
				"SELECT * FROM comic ORDER BY ReadTime DESC LIMIT 1", null);
		ComicsDetail cd = new ComicsDetail();
		while (c.moveToNext()) {
			cd.mId = c.getInt(c.getColumnIndex("mId"));
			cd.mTitle = c.getString(c.getColumnIndex("mTitle"));
			cd.mQid = c.getInt(c.getColumnIndex("mQid"));
			cd.mUrl = c.getString(c.getColumnIndex("mUrl"));
			cd.mName = c.getString(c.getColumnIndex("mName"));
			cd.CreateTime = c.getString(c.getColumnIndex("CreateTime"));
			cd.mLianzai = c.getString(c.getColumnIndex("mLianzai"));
			cd.mDirector = c.getString(c.getColumnIndex("mDirector"));
			cd.mTotal = c.getInt(c.getColumnIndex("mTotal"));
			cd.mPic = c.getString(c.getColumnIndex("mPic"));
			cd.rIndex = c.getInt(c.getColumnIndex("rIndex"));
			cd.maxNo = c.getInt(c.getColumnIndex("maxNo"));
			cd.minNo = c.getInt(c.getColumnIndex("minNo"));
			cd.pmNo = c.getInt(c.getColumnIndex("pmNo"));
			cd.nmNo = c.getInt(c.getColumnIndex("nmNo"));
			cd.mNo = c.getInt(c.getColumnIndex("mNo"));
			cd.localUrl = c.getString(c.getColumnIndex("localUrl"));
			cd.readTime = c.getString(c.getColumnIndex("ReadTime"));
			cd.port = c.getString(c.getColumnIndex("port"));
			cd.updateMessage = c.getString(c.getColumnIndex("updateMessage"));
		}
		c.close();

		return cd;
	}

	/**
	 * query all persons, return list
	 *
	 * @return List<Person>
	 */
	public List<ComicsDetail> query() {
		ArrayList<ComicsDetail> cds = new ArrayList<ComicsDetail>();
		Cursor c = queryTheCursor();
		while (c.moveToNext()) {
			ComicsDetail cd = new ComicsDetail();
			cd.mId = c.getInt(c.getColumnIndex("mId"));
			cd.mTitle = c.getString(c.getColumnIndex("mTitle"));
			cd.mContent = c.getString(c.getColumnIndex("mContent"));
			cd.mQid = c.getInt(c.getColumnIndex("mQid"));
			cd.mType1 = c.getInt(c.getColumnIndex("mType1"));
			cd.mUrl = c.getString(c.getColumnIndex("mUrl"));
			cd.mName = c.getString(c.getColumnIndex("mName"));
			cd.mFenAll = c.getInt(c.getColumnIndex("mFenAll"));
			cd.CreateTime = c.getString(c.getColumnIndex("CreateTime"));
			cd.mLianzai = c.getString(c.getColumnIndex("mLianzai"));
			cd.mDirector = c.getString(c.getColumnIndex("mDirector"));
			cd.mTotal = c.getInt(c.getColumnIndex("mTotal"));
			cd.mPic = c.getString(c.getColumnIndex("mPic"));
			cd.rIndex = c.getInt(c.getColumnIndex("rIndex"));
			cd.localUrl = c.getString(c.getColumnIndex("localUrl"));
			cd.port = c.getString(c.getColumnIndex("port"));
			cd.minNo = c.getInt(c.getColumnIndex("minNo"));
			cd.pmNo = c.getInt(c.getColumnIndex("pmNo"));
			cd.nmNo = c.getInt(c.getColumnIndex("nmNo"));
			cd.setmNo(c.getInt(c.getColumnIndex("mNo")));
			cd.setMaxNo(c.getInt(c.getColumnIndex("maxNo")));
			cd.updateMessage = c.getString(c.getColumnIndex("updateMessage"));

			// Log.d("tag",
			// "ReadTime   "+c.getString(c.getColumnIndex("ReadTime")));
			cds.add(cd);
		}
		c.close();
		return cds;
	}

	/**
	 * query all persons, return list
	 *
	 * @return List<Person>
	 */
	public ComicsDetail query(String sql, String[] str) {
		ArrayList<ComicsDetail> cds = new ArrayList<ComicsDetail>();
		Cursor c = db
				.rawQuery(sql + "order by cast(ReadTime as int) desc", str);
		while (c.moveToNext()) {
			ComicsDetail cd = new ComicsDetail();
			cd.mId = c.getInt(c.getColumnIndex("mId"));
			cd.mTitle = c.getString(c.getColumnIndex("mTitle"));
			cd.mContent = c.getString(c.getColumnIndex("mContent"));
			cd.mQid = c.getInt(c.getColumnIndex("mQid"));
			cd.mType1 = c.getInt(c.getColumnIndex("mType1"));
			cd.mUrl = c.getString(c.getColumnIndex("mUrl"));
			cd.mName = c.getString(c.getColumnIndex("mName"));
			cd.mFenAll = c.getInt(c.getColumnIndex("mFenAll"));
			cd.CreateTime = c.getString(c.getColumnIndex("CreateTime"));
			cd.mLianzai = c.getString(c.getColumnIndex("mLianzai"));
			cd.mDirector = c.getString(c.getColumnIndex("mDirector"));
			cd.mTotal = c.getInt(c.getColumnIndex("mTotal"));
			cd.mPic = c.getString(c.getColumnIndex("mPic"));
			cd.rIndex = c.getInt(c.getColumnIndex("rIndex"));
			cd.localUrl = c.getString(c.getColumnIndex("localUrl"));
			cd.port = c.getString(c.getColumnIndex("port"));
			cd.minNo = c.getInt(c.getColumnIndex("minNo"));
			cd.pmNo = c.getInt(c.getColumnIndex("pmNo"));
			cd.nmNo = c.getInt(c.getColumnIndex("nmNo"));
			cd.setmNo(c.getInt(c.getColumnIndex("mNo")));
			cd.setMaxNo(c.getInt(c.getColumnIndex("maxNo")));
			cd.updateMessage = c.getString(c.getColumnIndex("updateMessage"));
			cds.add(cd);
		}
		c.close();
		if (cds.size() > 0) {
			return cds.get(0);
		} else {

			return null;
		}
	}

	/**
	 * query all persons, return list
	 *
	 * @return List<Person>
	 */
	public ComicsDetail queryByMno(String sql, String[] str) {
		ArrayList<ComicsDetail> cds = new ArrayList<ComicsDetail>();
		Cursor c = db.rawQuery(sql
				+ "order by cast(ReadTime as int) desc limit 1 ", str);
		while (c.moveToNext()) {
			ComicsDetail cd = new ComicsDetail();
			cd.mId = c.getInt(c.getColumnIndex("mId"));
			cd.mTitle = c.getString(c.getColumnIndex("mTitle"));
			cd.mContent = c.getString(c.getColumnIndex("mContent"));
			cd.mQid = c.getInt(c.getColumnIndex("mQid"));
			cd.mType1 = c.getInt(c.getColumnIndex("mType1"));
			cd.mUrl = c.getString(c.getColumnIndex("mUrl"));
			cd.mName = c.getString(c.getColumnIndex("mName"));
			cd.mFenAll = c.getInt(c.getColumnIndex("mFenAll"));
			cd.CreateTime = c.getString(c.getColumnIndex("CreateTime"));
			cd.mLianzai = c.getString(c.getColumnIndex("mLianzai"));
			cd.mDirector = c.getString(c.getColumnIndex("mDirector"));
			cd.mTotal = c.getInt(c.getColumnIndex("mTotal"));
			cd.mPic = c.getString(c.getColumnIndex("mPic"));
			cd.rIndex = c.getInt(c.getColumnIndex("rIndex"));
			cd.localUrl = c.getString(c.getColumnIndex("localUrl"));
			cd.port = c.getString(c.getColumnIndex("port"));
			cd.minNo = c.getInt(c.getColumnIndex("minNo"));
			cd.nmNo = c.getInt(c.getColumnIndex("nmNo"));
			cd.pmNo = c.getInt(c.getColumnIndex("pmNo"));
			cd.setmNo(c.getInt(c.getColumnIndex("mNo")));
			cd.setMaxNo(c.getInt(c.getColumnIndex("maxNo")));
			cd.updateMessage = c.getString(c.getColumnIndex("updateMessage"));
			cds.add(cd);
		}
		c.close();
		if (cds.size() > 0) {
			return cds.get(0);
		} else {

			return null;
		}
	}

	/**
	 * query all persons, return cursor
	 *
	 * @return Cursor
	 */
	public Cursor queryTheCursor() {
		Cursor c = db
				.rawQuery(
						"SELECT * FROM comic order by cast(ReadTime as int) desc",
						null);
		return c;
	}

	/**
	 * close database
	 */
	public void closeDB() {
		db.close();
	}

	public long queryReadedCount() {
		Cursor cursor = db.rawQuery("select count(*) from comic", null);
		// 游标移到第一条记录准备获取数据
		cursor.moveToFirst();
		// 获取数据中的LONG类型数据
		Long count = cursor.getLong(0);
		return count;
	}

	public long queryCollectCount() {
		Cursor cursor = db.rawQuery("select count(*) from comic", null);
		// 游标移到第一条记录准备获取数据
		cursor.moveToFirst();
		// 获取数据中的LONG类型数据
		Long count = cursor.getLong(0);
		return count;
	}

	public long queryDownsCount() {
		Cursor cursor = db.rawQuery("select count(distinct(mId)) from down",
				null);
		// 游标移到第一条记录准备获取数据
		cursor.moveToFirst();
		// 获取数据中的LONG类型数据
		Long count = cursor.getLong(0);
		return count;
	}

	/**
	 * 查询一本漫画书有多少话在下载例表
	 * 
	 * @return
	 */
	public long queryComicDownCount(String mId) {
		Cursor cursor = db.rawQuery("select count(*) from down where mId = ?",
				new String[] { mId });
		// 游标移到第一条记录准备获取数据
		cursor.moveToFirst();
		// 获取数据中的LONG类型数据
		Long count = cursor.getLong(0);
		return count;
	}

	/**
	 * 查找下载列表显示项
	 * 
	 * @return
	 */
	public List<DownListData> queryDownDataList(String sql, String[] ss) {
		List<DownListData> downs = new ArrayList<DownListData>();
		Cursor c = db.rawQuery(sql, ss);
		while (c.moveToNext()) {
			DownListData downListData = new DownListData();
			downListData
					.setmDirector(c.getString(c.getColumnIndex("mDirector")));
			downListData.setmId(c.getInt(c.getColumnIndex("mId")));
			downListData.setmTitle(c.getString(c.getColumnIndex("mTitle")));
			downListData.setIsdown(c.getInt(c.getColumnIndex("isdown")));
			downListData.setmPic(c.getString(c.getColumnIndex("mPic")));
			downListData.setFlag(true);
			downs.add(downListData);
		}
		c.close();
		return downs;
	}

	/**
	 * 查找下载列表
	 * 
	 * @return
	 */
	public synchronized List<Down> queryDown(String sql, String[] ss) {
		List<Down> downs = new ArrayList<Down>();
		Cursor c = db.rawQuery(sql, ss);
		while (c.moveToNext()) {
			Down down = new Down();
			ComicsDetail cd = new ComicsDetail();
			cd.mId = c.getInt(c.getColumnIndex("mId"));
			cd.mTitle = c.getString(c.getColumnIndex("mTitle"));
			cd.mQid = c.getInt(c.getColumnIndex("mQid"));
			cd.mUrl = c.getString(c.getColumnIndex("mUrl"));
			cd.mName = c.getString(c.getColumnIndex("mName"));
			cd.CreateTime = c.getString(c.getColumnIndex("CreateTime"));
			cd.mLianzai = c.getString(c.getColumnIndex("mLianzai"));
			cd.mDirector = c.getString(c.getColumnIndex("mDirector"));
			cd.mTotal = c.getInt(c.getColumnIndex("mTotal"));
			cd.mPic = c.getString(c.getColumnIndex("mPic"));
			cd.mType1 = c.getInt(c.getColumnIndex("mType1"));
			cd.mContent = c.getString(c.getColumnIndex("mContent"));
			cd.mFenAll = c.getInt(c.getColumnIndex("mFenAll"));
			cd.mNo = c.getInt(c.getColumnIndex("mNo"));
			cd.maxNo = c.getInt(c.getColumnIndex("maxNo"));
			cd.minNo = c.getInt(c.getColumnIndex("minNo"));
			cd.nmNo = c.getInt(c.getColumnIndex("nmNo"));
			cd.pmNo = c.getInt(c.getColumnIndex("pmNo"));
			cd.localUrl = c.getString(c.getColumnIndex("localUrl"));
			cd.port = c.getString(c.getColumnIndex("port"));
			cd.updateMessage = c.getString(c.getColumnIndex("updateMessage"));
			down.setIsdonw(c.getInt(c.getColumnIndex("isdown")));
			down.setDowns(c.getInt(c.getColumnIndex("downs")));
			down.setCd(cd);
			downs.add(down);
		}
		c.close();
		return downs;
	}
}