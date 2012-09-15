package sofit.sms_worker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

  private static final String TAG = "DatabaseHelper";
  private static final String DATABASE_NAME = "sms_worker.sqlite";
  private static final int DATABASE_VERSION = 1;
  public static final String TABLE_NAME = "send_queue";
  public static final String _ID = "_id";
  public static final String RECIPIENT = "recipient";
  public static final String BODY = "body";
  public static final String SEND_DATETIME = "send_datetime";

  public DatabaseHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    Log.i(TAG, "creating db...");
    db.execSQL("CREATE TABLE " + TABLE_NAME + " ( " +
        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        RECIPIENT + " TEXT, " +
        BODY + " TEXT, " +
        SEND_DATETIME + " DATETIME);");
  }


  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    Log.d(TAG, "upgrade db from " + oldVersion + " to " + newVersion);
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    onCreate(db);
  }

  public void addQueueElement(QueueElement queueElement) {
    SQLiteDatabase db = null;

    try {
      db = getWritableDatabase();
      ContentValues contentValues = new ContentValues();
      contentValues.put(BODY, queueElement.getBody());
      contentValues.put(RECIPIENT, queueElement.getRecipient());
      contentValues.put(SEND_DATETIME, queueElement.getSendDatetime());
      queueElement.setId(db.insert(TABLE_NAME, null, contentValues));
    }
    finally {
      if (db != null)
        db.close();
    }
  }

  public Cursor getAllQueueElements() {
    SQLiteDatabase db = null;

    try {
      db = getReadableDatabase();

      return db.query(TABLE_NAME, new String[] {_ID, SEND_DATETIME, RECIPIENT, BODY},
          null, null, null, null, DatabaseHelper.SEND_DATETIME);
    }
    finally {
      if (db != null)
        db.close();
    }
  }

  public void delete(QueueElement queueElement) {
    SQLiteDatabase db = null;

    try {
      db = getWritableDatabase();
      db.delete(TABLE_NAME, _ID + " = ?", new String[] {String.valueOf(queueElement.getId())});
    }
    finally {
      if (db != null)
        db.close();
    }
  }
}