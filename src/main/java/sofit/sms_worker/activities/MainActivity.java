package sofit.sms_worker.activities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import sofit.sms_worker.R;

public class MainActivity extends FragmentActivity implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {

  private static final String TAG = "MainActivity";
  public static final Uri SMS_DRAFT_CONTENT_URI = Uri.parse("content://sms/draft");
  private final SmsWorkerOpenHelper smsWorkerOpenHelper = new SmsWorkerOpenHelper(this);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    Cursor cursor = getContentResolver().query(
        SMS_DRAFT_CONTENT_URI,
        new String[] {"_id", "date", "person", "address", "body"},
        null,
        null,
        "date DESC");

    Cursor peopleCursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PEOPLE_PROJECTION, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
    ContactListAdapter contactAdapter = new ContactListAdapter(this, peopleCursor);
    MultiAutoCompleteTextView addressView = (MultiAutoCompleteTextView) findViewById(R.id.address);
    addressView.setAdapter(contactAdapter);
    addressView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

    startManagingCursor(cursor);
    // the desired columns to be bound
    String[] columns = new String[] {"date", /*"person", */"address", "body"};
    // the XML defined views which the data will be bound to
    int[] to = new int[] {R.id.entry_date, R.id.entry_address, /*R.id.person_entry, */R.id.entry_body};
    SimpleCursorAdapter listAdapter = new SimpleCursorAdapter(this, R.layout.sms_list_entry, cursor, columns, to);
    ListView listView = (ListView) findViewById(R.id.listView);
    listView.setAdapter(listAdapter);
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Cursor data = (Cursor) adapterView.getItemAtPosition(i);
        String address = data.getString(3);
        String body = data.getString(4);
        TextView addressView = (TextView) findViewById(R.id.address);
        addressView.setText(address);
        TextView bodyView = (TextView) findViewById(R.id.body);
        bodyView.setText(body);
      }
    });

    final Calendar calendar = Calendar.getInstance();
    String date = String.format("%1$td.%1$tm.%1$tY", calendar);
    String time = String.format("%1$tH:%1$tM", calendar);
    ((TextView) findViewById(R.id.date)).setText(date);
    ((TextView) findViewById(R.id.time)).setText(time);
  }

  public void sendSms(View view) throws ParseException {
    TextView addressView = (TextView) findViewById(R.id.address);
    TextView bodyView = (TextView) findViewById(R.id.body);
    TextView dateView = (TextView) findViewById(R.id.date);
    TextView timeView = (TextView) findViewById(R.id.time);

    String address = addressView.getText().toString();
    String body = bodyView.getText().toString();
    String sendDate = dateView.getText().toString() + timeView.getText().toString();

    SQLiteDatabase sqLiteDatabase = smsWorkerOpenHelper.getWritableDatabase();
    ContentValues contentValues = new ContentValues();
    contentValues.put(SmsWorkerOpenHelper.BODY_COLUMN, body);
    contentValues.put(SmsWorkerOpenHelper.RECIPIENT_COLUMN, address);
    contentValues.put(SmsWorkerOpenHelper.SEND_DATE_COLUMN, sendDate);
    sqLiteDatabase.insert(SmsWorkerOpenHelper.TABLE_NAME, null, contentValues);
    /*
    Timer timer = new Timer();
    timer.schedule(new TimerTask() {

      @Override
      public void run() {
        //TODO: implement this method
      }
    }, sendDate);
    SmsManager smsManager = SmsManager.getDefault();
    smsManager.sendTextMessage(addressView.getText().toString(), null, bodyView.getText().toString(), null, null);
    addressView.setText("");
    bodyView.setText("");*/
  }

  public static class ContactListAdapter extends CursorAdapter implements Filterable {

    private ContentResolver content;
    private final LayoutInflater inflater;


    public ContactListAdapter(Context context, Cursor c) {
      super(context, c);
      content = context.getContentResolver();
      inflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
      return inflater.inflate(R.layout.people_list_entry, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
      int nameIdx = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
      int numberIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

      String name = cursor.getString(nameIdx);
      String number = cursor.getString(numberIdx);
      TextView nameView = (TextView) view.findViewById(R.id.entry_name);
      TextView numberView = (TextView) view.findViewById(R.id.entry_number);
      nameView.setText(name);
      numberView.setText(number);
    }

    @Override
    public String convertToString(Cursor cursor) {
      int numberIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
      return cursor.getString(numberIdx);
    }

    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
      if (getFilterQueryProvider() != null) {
        return getFilterQueryProvider().runQuery(constraint);
      }

      StringBuilder buffer = null;
      String[] args = null;
      if (constraint != null) {
        buffer = new StringBuilder();
//        buffer.append("UPPER("); TODO: upper with unicode does not work
        buffer.append(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
//        buffer.append(") LIKE ?");
        buffer.append(" LIKE ?");
        args = new String[] {"%" + constraint.toString() + "%"};
      }

      return content.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PEOPLE_PROJECTION,
          buffer == null ? null : buffer.toString(), args,
          ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
    }
  }

  private static final String[] PEOPLE_PROJECTION = new String[] {
      ContactsContract.CommonDataKinds.Phone._ID,
      ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
      ContactsContract.CommonDataKinds.Phone.NUMBER
  };

  public void showDatePickerDialog(View view) {
    String dateStr = String.valueOf(((TextView) findViewById(R.id.date)).getText());
    int year = 0, month = 0, day = 0;

    if (dateStr.isEmpty()) {
      Calendar calendar = Calendar.getInstance();
      year = calendar.get(Calendar.YEAR);
      month = calendar.get(Calendar.MONTH);
      day = calendar.get(Calendar.DAY_OF_MONTH);
    }
    else {
      int i = 1;
      for (String str : dateStr.split("\\.")) {
        switch (i) {
          case 1:
            day = Integer.valueOf(str);
            break;
          case 2:
            month = Integer.valueOf(str);
            break;
          case 3:
            year = Integer.valueOf(str);
            break;
        }
        i++;
      }
    }

    DialogFragment sendDatePickerDialog = new SendDatePickerDialog(this, year, month, day);
    sendDatePickerDialog.show(getSupportFragmentManager(), "datePicker");
  }

  public void showTimePickerDialog(View view) {
    String dateStr = String.valueOf(((TextView) findViewById(R.id.time)).getText());
    int hour = 0, minute = 0;

    if (dateStr.isEmpty()) {
      Calendar calendar = Calendar.getInstance();
      hour = calendar.get(Calendar.HOUR);
      minute = calendar.get(Calendar.MINUTE);
    }
    else {
      int i = 1;
      for (String str : dateStr.split(":")) {
        switch (i) {
          case 1:
            hour = Integer.valueOf(str);
            break;
          case 2:
            minute = Integer.valueOf(str);
            break;
        }
        i++;
      }
    }
    DialogFragment sendTimePickerDialog = new SendTimePickerDialog(this, hour, minute);
    sendTimePickerDialog.show(getSupportFragmentManager(), "timePicker");
  }

  @Override
  public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
    String date = String.format("%02d.%02d.%04d", dayOfMonth, monthOfYear, year);
    ((TextView) findViewById(R.id.date)).setText(date);
  }

  @Override
  public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
    String time = String.format("%02d:%02d", hourOfDay, minute);
    ((TextView) findViewById(R.id.time)).setText(time);
  }
}