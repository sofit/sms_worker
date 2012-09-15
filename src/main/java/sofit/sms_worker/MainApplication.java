package sofit.sms_worker;

import android.app.Application;

/**
 * @author evgeny
 */
public class MainApplication extends Application {

  private static DatabaseHelper dbHelper;

  @Override
  public void onCreate() {
    super.onCreate();

    dbHelper = new DatabaseHelper(this);
  }

  @Override
  public void onTerminate() {
    super.onTerminate();

    dbHelper.close();
  }

  public DatabaseHelper getDbHelper() {
    return dbHelper;
  }
}
