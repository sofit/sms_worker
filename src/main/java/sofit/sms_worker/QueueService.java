package sofit.sms_worker;

import android.app.IntentService;
import android.content.Intent;

/**
 * @author evgeny
 */
public class QueueService extends IntentService {

  public QueueService() {
    super("QueueService");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    //TODO: implement this method
  }
}
