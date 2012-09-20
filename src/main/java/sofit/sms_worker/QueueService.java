package sofit.sms_worker;

import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;

/**
 * @author evgeny
 */
public class QueueService extends Service {

  private DatabaseHelper dbHelper;
  private static final String TAG = "QueueService";
  private final SmsManager smsManager = SmsManager.getDefault();
  private static long UPDATE_INTERVAL = 5 * 60 * 1000;
  private static Timer timer;

  public QueueService() {
  }

  @Override
  public void onCreate() {
    super.onCreate();

    dbHelper = ((MainApplication) getApplication()).getDbHelper();
    timer = new Timer();
    timer.scheduleAtFixedRate(
        new TimerTask() {
          public void run() {
            doWork();
          }
        }, 1000, UPDATE_INTERVAL);
    Log.i(getClass().getSimpleName(), "FileScannerService Timer started....");
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;  //TODO: implement this method
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (timer != null)
      timer.cancel();
    Log.i(getClass().getSimpleName(), "Timer stopped...");
  }

  private void doWork() {
    synchronized (this) {
      Calendar calendar = Calendar.getInstance();
      String datetime = String.format("%1$td.%1$tm.%1$tY %1$tH:%1$tM", calendar);

      List<QueueElement> queueElementList = dbHelper.getQueueElements(DatabaseHelper.SEND_DATETIME + " <= ?", new String[] {datetime});

      for (QueueElement queueElement : queueElementList) {

        smsManager.sendTextMessage(queueElement.getRecipient(), null, queueElement.getBody(), null, null);
        dbHelper.delete(queueElement);
      }
    }
  }
}