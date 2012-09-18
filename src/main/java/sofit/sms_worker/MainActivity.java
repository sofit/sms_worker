package sofit.sms_worker;

import java.util.Calendar;
import java.util.List;

import android.app.*;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ParseException;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

public class MainActivity extends Activity {

  private static final String TAG = "MainActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Intent intent = new Intent(this, QueueService.class);
    startService(intent);

    // setup action bar for tabs
    final ActionBar actionBar = getActionBar();
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    actionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);

    ActionBar.Tab tab1 = actionBar.newTab()
        .setText("Main")
        .setTabListener(new TabListener<MainActivity.MainFragment>(
            this, "main_tab", MainActivity.MainFragment.class));
    actionBar.addTab(tab1);

    ActionBar.Tab tab2 = actionBar.newTab()
        .setText("Inbox")
        .setTabListener(new TabListener<InboxActivity.InboxFragment>(
            this, "inbox_tab", InboxActivity.InboxFragment.class));
    actionBar.addTab(tab2);

    ActionBar.Tab tab3 = actionBar.newTab()
        .setText("Queue")
        .setTabListener(new TabListener<QueueActivity.QueueFragment>(
            this, "queue_tab", QueueActivity.QueueFragment.class));
    actionBar.addTab(tab3);
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

  public static class MainFragment extends Fragment implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {

    private Cursor peopleCursor;
    private DatabaseHelper dbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      dbHelper = ((MainApplication) getActivity().getApplication()).getDbHelper();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      return inflater.inflate(R.layout.main, container, false);
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
    }

    @Override
    public void onDestroy() {
      super.onDestroy();

      if (peopleCursor != null)
        peopleCursor.close();
    }

    public void queueSms(View view) throws ParseException {
      TextView addressView = (TextView) getActivity().findViewById(R.id.address);
      TextView bodyView = (TextView) getActivity().findViewById(R.id.body);
      TextView dateView = (TextView) getActivity().findViewById(R.id.date);
      TextView timeView = (TextView) getActivity().findViewById(R.id.time);

      String address = addressView.getText().toString();
      String body = bodyView.getText().toString();
      String sendDate = dateView.getText().toString() + " " + timeView.getText().toString();

      QueueElement queueElement = new QueueElement(address, body, sendDate);
      dbHelper.addQueueElement(queueElement);
    }

    public void onToggleClicked(View view) {
      boolean on = ((ToggleButton) view).isChecked();

      if (on) {
        Intent intent = new Intent(getActivity(), QueueService.class);
        getActivity().startService(intent);
      }
      else {
        Intent intent = new Intent(getActivity(), QueueService.class);
        getActivity().stopService(intent);
      }
    }

    public void showDatePickerDialog(View view) {
      String dateStr = String.valueOf(((TextView) getActivity().findViewById(R.id.date)).getText());
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
      String dateStr = String.valueOf(((TextView) getActivity().findViewById(R.id.time)).getText());
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

    public void clearQueue(View view) {
      List<QueueElement> queueElementList = dbHelper.getAllQueueElements();
      for (QueueElement queueElement : queueElementList)
        dbHelper.delete(queueElement);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
      String date = String.format("%02d.%02d.%04d", dayOfMonth, monthOfYear, year);
      ((TextView) getActivity().findViewById(R.id.date)).setText(date);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
      String time = String.format("%02d:%02d", hourOfDay, minute);
      ((TextView) getActivity().findViewById(R.id.time)).setText(time);
    }
  }
}