package sofit.sms_worker;

import java.util.Calendar;
import java.util.List;

import android.app.*;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;

public class MainActivity extends Activity implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {

  private DatabaseHelper dbHelper;

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main, menu);
    menu.findItem(R.id.main_queue_service).setChecked(isQueueServiceRunning());
    DatabaseHelper dbHelper = ((MainApplication) getApplication()).getDbHelper();
    menu.findItem(R.id.main_clear).setEnabled(dbHelper.getQueueElementsCount() > 0);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
      case R.id.main_create:
        ActionBar actionBar = getActionBar();
        if (actionBar.getSelectedNavigationIndex() > 0)
          actionBar.setSelectedNavigationItem(0);
        return true;
      case R.id.main_clear:
        showClearQueueDialog();
        return true;
      case R.id.main_queue_service:
        item.setTitle(item.isChecked() ? R.string.service_toggle_off : R.string.service_toggle_on);
        item.setIcon(item.isChecked() ? R.drawable.av_play : R.drawable.av_stop);
        item.setChecked(!item.isChecked());
        Intent intent = new Intent(this, QueueService.class);
        if (item.isChecked())
          stopService(intent);
        else
          startService(intent);
        Toast.makeText(this, item.isChecked() ? R.string.queue_service_started : R.string.queue_service_stopped, Toast.LENGTH_SHORT).show();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    dbHelper = ((MainApplication) getApplication()).getDbHelper();

    // setup action bar for tabs
    final ActionBar actionBar = getActionBar();
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    actionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);

    ActionBar.Tab tab1 = actionBar.newTab()
        .setText(R.string.main_tab)
        .setTabListener(new TabListener<MainActivity.MainFragment>(
            this, "main_tab", MainActivity.MainFragment.class));
    actionBar.addTab(tab1);

    ActionBar.Tab tab2 = actionBar.newTab()
        .setText(R.string.inbox_tab)
        .setTabListener(new TabListener<InboxActivity.InboxFragment>(
            this, "inbox_tab", InboxActivity.InboxFragment.class));
    actionBar.addTab(tab2);

    ActionBar.Tab tab3 = actionBar.newTab()
        .setText(R.string.queue_tab)
        .setTabListener(new TabListener<QueueActivity.QueueFragment>(
            this, "queue_tab", QueueActivity.QueueFragment.class));
    actionBar.addTab(tab3);
  }

  public void showClearQueueDialog() {
    AlertDialog alertDialog = new AlertDialog.Builder(this)
        .setTitle(R.string.clear_queue_dialog_title)
        .setMessage(R.string.clear_queue_dialog_message)
        .setPositiveButton(R.string.alert_dialog_ok,
            new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int whichButton) {
                clearQueue();
              }
            }
        )
        .setNegativeButton(R.string.alert_dialog_cancel, null)
        .create();
    alertDialog.show();
  }

  private boolean isQueueServiceRunning() {
    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
    for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
      if (QueueService.class.getName().equals(service.service.getClassName()))
        return true;
    }
    return false;
  }

  private void clearQueue() {
    List<QueueElement> queueElementList = dbHelper.getAllQueueElements();
    for (QueueElement queueElement : queueElementList)
      dbHelper.delete(queueElement);
    findViewById(R.id.main_clear).setEnabled(false);
    Toast.makeText(this, R.string.queue_cleared, Toast.LENGTH_SHORT).show();
  }

  public void queueSms(View view) {
    TextView addressView = (TextView) findViewById(R.id.address);
    TextView bodyView = (TextView) findViewById(R.id.body);
    TextView dateView = (TextView) findViewById(R.id.date);
    TextView timeView = (TextView) findViewById(R.id.time);

    String address = addressView.getText().toString();
    String body = bodyView.getText().toString();
    String sendDate = dateView.getText().toString() + " " + timeView.getText().toString();

    QueueElement queueElement = new QueueElement(address, body, sendDate);
    dbHelper.addQueueElement(queueElement);
    addressView.setText("");
    bodyView.setText("");
    findViewById(R.id.main_clear).setEnabled(true);
    Toast.makeText(this, R.string.added_to_queue, Toast.LENGTH_SHORT).show();
  }

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
    sendDatePickerDialog.show(getFragmentManager(), "datePicker");
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
    sendTimePickerDialog.show(getFragmentManager(), "timePicker");
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

  public static class ContactListAdapter extends CursorAdapter implements Filterable {

    private ContentResolver content;
    private final LayoutInflater inflater;

    public ContactListAdapter(Context context, Cursor c) {
      super(context, c, FLAG_REGISTER_CONTENT_OBSERVER);
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

  public static class TabListener<T extends Fragment> implements ActionBar.TabListener {
    private Fragment fragment;
    private final Activity activity;
    private final String tag;
    private final Class<T> clazz;

    /**
     * Constructor used each time a new tab is created.
     *
     * @param activity The host Activity, used to instantiate the fragment
     * @param tag      The identifier tag for the fragment
     * @param clazz    The fragment's Class, used to instantiate the fragment
     */
    public TabListener(Activity activity, String tag, Class<T> clazz) {
      this.activity = activity;
      this.tag = tag;
      this.clazz = clazz;
    }

    /* The following are each of the ActionBar.TabListener callbacks */

    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
      // Check if the fragment is already initialized
      if (fragment == null) {
        // If not, instantiate and add it to the activity
        fragment = Fragment.instantiate(activity, clazz.getName());
        ft.add(android.R.id.content, fragment, tag);
      }
      else {
        // If it exists, simply attach it in order to show it
        ft.attach(fragment);
      }
    }

    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
      if (fragment != null) {
        // Detach the fragment, because another one is being attached
        ft.detach(fragment);
      }
    }

    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
      Toast.makeText(activity, "Reselected!", Toast.LENGTH_SHORT).show();
    }
  }

  public static class MainFragment extends Fragment {

    private enum WatcherType {ADDRESS, BODY}

    private Cursor peopleCursor;
    private boolean addressFilled = false;
    private boolean bodyFilled = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      return inflater.inflate(R.layout.main, container, false);
    }

    public void setQueueEnabled(WatcherType type, boolean empty) {
      switch (type) {
        case ADDRESS:
          addressFilled = !empty;
          break;
        case BODY:
          bodyFilled = !empty;
          break;
      }
      getActivity().findViewById(R.id.queue_button).setEnabled(addressFilled && bodyFilled);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
      super.onActivityCreated(savedInstanceState);

      peopleCursor = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PEOPLE_PROJECTION, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
      ContactListAdapter contactAdapter = new ContactListAdapter(getActivity(), peopleCursor);
      MultiAutoCompleteTextView addressView = (MultiAutoCompleteTextView) getActivity().findViewById(R.id.address);
      addressView.setAdapter(contactAdapter);
      addressView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

      final Calendar calendar = Calendar.getInstance();
      calendar.add(Calendar.MINUTE, 5);
      String date = String.format("%1$td.%1$tm.%1$tY", calendar);
      String time = String.format("%1$tH:%1$tM", calendar);
      ((TextView) getActivity().findViewById(R.id.date)).setText(date);
      ((TextView) getActivity().findViewById(R.id.time)).setText(time);

      ((TextView) getActivity().findViewById(R.id.address)).addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
          setQueueEnabled(WatcherType.ADDRESS, charSequence.length() == 0);
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
      });
      ((TextView) getActivity().findViewById(R.id.body)).addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
          setQueueEnabled(WatcherType.BODY, charSequence.length() == 0);
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
      });
    }

    @Override
    public void onDestroy() {
      super.onDestroy();

      if (peopleCursor != null)
        peopleCursor.close();
    }
  }
}