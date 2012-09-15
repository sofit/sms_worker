package sofit.sms_worker;

import java.util.Calendar;
import java.util.List;

import android.app.IntentService;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;

/**
 * @author evgeny
 */
public class QueueService extends IntentService {

  private DatabaseHelper dbHelper;
  private static final String TAG = "QueueService";
  private final SmsManager smsManager = SmsManager.getDefault();

  public QueueService() {
    super("QueueService");
  }

  @Override
  public void onCreate() {
    super.onCreate();

    dbHelper = ((MainApplication) getApplication()).getDbHelper();
  }

  @Override
  protected void onHandleIntent(Intent intent) {

    while (true) {
      synchronized (this) {
        Calendar calendar = Calendar.getInstance();
        String datetime = String.format("%1$td.%1$tm.%1$tY %1$tH:%1$tM", calendar);

        List<QueueElement> queueElementList = dbHelper.getQueueElements(DatabaseHelper.SEND_DATETIME + " <= ?", new String[] {datetime});

        for (QueueElement queueElement : queueElementList) {

          smsManager.sendTextMessage(queueElement.getRecipient(), null, queueElement.getBody(), null, null);
          dbHelper.delete(queueElement);
        }
      }

      try {
        Thread.sleep(1000 * 60 * 5);
      }
      catch (InterruptedException ex) {
        Log.e(TAG, ex.toString());
      }
    }
  }
}
