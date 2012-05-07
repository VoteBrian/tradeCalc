package net.votebrian.tradecalc;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class DbAdapter {
  private DatabaseHelper mDbHelper;
  private static SQLiteDatabase mDb;

  private static final int DB_VERSION = 2;

  private static final String DB_NAME = "data.db";
  private static final String DB_PATH = "/data/data/net.votebrian.tradecalc/databases/";

  // table names
  private static final String DB_TABLE_PICKS = "picks";

  // column names in database
  public static final String KEY_ROW_ID   = "_id";
  public static final String KEY_PICK     = "pick";
  public static final String KEY_ROUND    = "round";
  public static final String KEY_SUB_PICK = "sub_pick";
  public static final String KEY_BLOCKED  = "blocked";
  public static final String KEY_VALUE    = "value";
  public static final String KEY_TEAM     = "team";
  public static final String KEY_SEL_A    = "selA";
  public static final String KEY_SEL_B    = "selB";

  private static Context mCtx;

  // Much of this section was stolen from:
  // http://www.reigndesign.com/blog/using-your-own-sqlite-database-in-android-applications/
  private static class DatabaseHelper extends SQLiteOpenHelper {

    DatabaseHelper(Context context) {
      super(context, DB_NAME, null, DB_VERSION);
      mCtx = context;
    }

    public void createDatabase() throws IOException {
      boolean dBExists = checkDb();
      String fullPath = DB_PATH + DB_NAME;

      if(dBExists) {
        mCtx.deleteDatabase(fullPath);
      }

      this.getReadableDatabase();  // creates a database in the default directory
      try {
        copyDb();
      } catch (IOException e) {
        throw new Error("Error!  Could not copy database");
      }
    }

    // Check if the database already exists to avoid re-copying the asset file each time.
    public boolean checkDb() {
      SQLiteDatabase check = null;

      try {
        String fullPath = DB_PATH + DB_NAME;
        check = SQLiteDatabase.openDatabase(fullPath, null, SQLiteDatabase.OPEN_READWRITE);
      } catch (SQLiteException e)  {
        // database does not exist
      }
      if(check != null) {
        check.close();
        return true;
      } else {
        return false;
      }
    }

    public void copyDb() throws IOException {
      InputStream myInput = mCtx.getAssets().open(DB_NAME);

      String fullFile = DB_PATH + DB_NAME;
      OutputStream myOutput = new FileOutputStream(fullFile);

      // copy stuff
      byte[] buffer = new byte[1024];
      int length;
      while ((length = myInput.read(buffer)) > 0) {
        myOutput.write(buffer, 0, length);
      }

      // close streams
      myOutput.flush();
      myOutput.close();
      myInput.close();
    }

    public void openDatabase() throws SQLException {
      String fullPath = DB_PATH + DB_NAME;
      mDb = SQLiteDatabase.openDatabase(fullPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    @Override
    public synchronized void close() {
      if(mDb != null) {
        mDb.close();
      }

      super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      // do nothing here
    }

    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
      String fullFile = DB_PATH + DB_NAME;
      mCtx.deleteDatabase(fullFile);
      this.getReadableDatabase();  // creates a database in the default directory
      try {
        copyDb();
      } catch (IOException e) {
        throw new Error("Error!  Could not copy database");
      }
    }
  }


  public DbAdapter(Context ctx){
    mCtx = ctx;
  }


  public DbAdapter open() throws SQLException {
    mDbHelper = new DatabaseHelper(mCtx);

    try {
      mDbHelper.createDatabase();
    } catch (IOException e){
      throw new Error("Unable to create database... :(");
    }

    try {
      mDbHelper.openDatabase();
    } catch (SQLiteException e) {
      throw e;
    }

    return this;
  }


  public void close() {
    mDbHelper.close();
  }


  public Cursor fetchTeamPicks(int team) {
    Cursor mCursor = mDb.query(
        true,
        DB_TABLE_PICKS,
        new String[] {KEY_ROW_ID, KEY_PICK, KEY_ROUND, KEY_SUB_PICK, KEY_BLOCKED, KEY_VALUE, KEY_TEAM, KEY_SEL_A, KEY_SEL_B},
        KEY_TEAM + "=" + team,
        null,
        null,
        null,
        KEY_PICK,
        null);
    if( mCursor != null) {
      mCursor.moveToFirst();
    }
    return mCursor;
  }



  public boolean emptyCheck() {
    Cursor mCursor = mDb.rawQuery("SELECT " + KEY_PICK + " FROM " + DB_TABLE_PICKS, null);
    if(mCursor.getCount() > 0) {
      return false;
    } else {
      return true;
    }
  }


  // Returns a Cursor to the "selA" or "selB" columns
  public Cursor fetchSelections(int sel_team) {
    Cursor mCursor;
    if(sel_team == 1) {
      mCursor = mDb.query(
          true,
          DB_TABLE_PICKS,
          new String[] {KEY_ROW_ID, KEY_PICK, KEY_VALUE, KEY_SEL_A},
          null,
          null,
          null,
          null,
          KEY_ROW_ID,
          null);
    } else {
      mCursor = mDb.query(
          true,
          DB_TABLE_PICKS,
          new String[] {KEY_ROW_ID, KEY_PICK, KEY_VALUE, KEY_SEL_B},
          null,
          null,
          null,
          null,
          KEY_ROW_ID,
          null);
    }
    return mCursor;
  }

  public boolean checkSelection(int page, long sel) {
    if(page == 1) {
      Cursor mCursor = mDb.query(
          true,
          DB_TABLE_PICKS,
          new String[] {KEY_ROW_ID, KEY_SEL_A},
          KEY_PICK + "=" + sel,
          null,
          null,
          null,
          null,
          null);
      mCursor.moveToFirst();
      int columnIndex = mCursor.getColumnIndex(KEY_SEL_A);
      return mCursor.getInt(columnIndex) > 0 ? true: false;
    } else {
      Cursor mCursor = mDb.query(
          true,
          DB_TABLE_PICKS,
          new String[] {KEY_ROW_ID, KEY_SEL_B},
          KEY_PICK + "=" + sel,
          null,
          null,
          null,
          null,
          null);
      mCursor.moveToFirst();
      int columnIndex = mCursor.getColumnIndex(KEY_SEL_B);
      return mCursor.getInt(columnIndex) > 0 ? true: false;
    }
  }

  public void makeSelection(int page, long sel) {
    String sql;
    if(page == 1) {
      sql = "UPDATE " + DB_TABLE_PICKS + " SET " + KEY_SEL_A + "= '1' WHERE " + KEY_PICK + " = " + sel;
    } else {
      sql = "UPDATE " + DB_TABLE_PICKS + " SET " + KEY_SEL_B + "= '1' WHERE " + KEY_PICK + " = " + sel;
    }

    mDb.execSQL(sql);
  }

  public void removeSelection(int page, long sel) {
    String sql;
    if(page == 1) {
      sql = "UPDATE " + DB_TABLE_PICKS + " SET " + KEY_SEL_A + "= '0' WHERE " + KEY_PICK + " = " + sel;
    } else {
      sql = "UPDATE " + DB_TABLE_PICKS + " SET " + KEY_SEL_B + "= '0' WHERE " + KEY_PICK + " = " + sel;
    }

    mDb.execSQL(sql);
  }

  public void resetSelections(int page) {
    String sql;
    if(page == 1) {
      sql = "UPDATE " + DB_TABLE_PICKS + " SET " + KEY_SEL_A + "= 0";
    } else {
      sql = "UPDATE " + DB_TABLE_PICKS + " SET " + KEY_SEL_B + "= 0";
    }

    mDb.execSQL(sql);
  }

  public Cursor pullASelections() {
    Cursor cursor = mDb.query(
        true,
        DB_TABLE_PICKS,
        new String[] {KEY_ROW_ID, KEY_ROUND, KEY_SUB_PICK, KEY_VALUE, KEY_TEAM},
        KEY_SEL_A + "=" + 1,
        null,
        null,
        null,
        null,
        null);
    cursor.moveToFirst();
    return cursor;
  }

  public Cursor pullBSelections() {
    Cursor cursor = mDb.query(
        true,
        DB_TABLE_PICKS,
        new String[] {KEY_ROW_ID, KEY_ROUND, KEY_SUB_PICK, KEY_VALUE, KEY_TEAM},
        KEY_SEL_B + "=" + 1,
        null,
        null,
        null,
        null,
        null);
    cursor.moveToFirst();
    return cursor;
  }

  public Cursor fetchAllPicks() {
    Cursor cursor = mDb.query (
        true,
        DB_TABLE_PICKS,
        new String[] {KEY_ROW_ID, KEY_ROUND, KEY_SUB_PICK, KEY_PICK, KEY_VALUE, KEY_TEAM},
        null,
        null,
        null,
        null,
        null,
        null);
    cursor.moveToFirst();
    return cursor;
  }
}
