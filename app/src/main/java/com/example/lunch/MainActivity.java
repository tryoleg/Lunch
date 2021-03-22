package com.example.lunch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button btn_yes, btn_no, btn_reset;
    EditText editText_number_portion;
    ListView listView;
    DBHelper dbHelper;
    private DatabaseReference myDataBase;
    ArrayList<String> arrayList = new ArrayList<>();
    boolean reset = false;

//    public void createChannelIfNeeded() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            CharSequence name = "RemindChannel";
//            String description = "Channel";
//            int importance = NotificationManager.IMPORTANCE_DEFAULT;
//            NotificationChannel channel = new NotificationChannel("notifyReminder",name,importance);
//            channel.setDescription(description);
//            NotificationManager notificationManager = getSystemService(NotificationManager.class);
//        }
//    }
//
//    private void set_notification() {
//        Intent intent = new Intent(MainActivity.this,MyNotification.class);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this,0,intent,0);
//
//        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//
//        long timeA = System.currentTimeMillis();
//        long timeB = 1000*10;
//
//        alarmManager.set(AlarmManager.RTC_WAKEUP,timeA+timeB,pendingIntent);
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        update_list();
//        createChannelIfNeeded();
//        set_notification();

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("MYTABLE1_1", null, null);
        Cursor c = db.query("MYTABLE1_1", null, null, null, null, null, null);
        if (!c.moveToFirst()) { //проверим зарегистрирован пользователь или нет
            Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
            startActivity(intent);
        }
        c.close();
        dbHelper.close();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    block_button();
                }
            }
        });
//        thread.start();

        Thread block_reset = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean state = false;
                while (!state) {
                    SQLiteDatabase database = dbHelper.getWritableDatabase();
                    Cursor cursor = database.query("MYTABLE1_1", null, null, null, null, null, null);
                    if (cursor.getCount() != 0) {
                        state = true;
                    }
                    if (state) {
                        cursor.moveToFirst();
                        int nameColIndex = cursor.getColumnIndex("name");
                        if (cursor.getString(nameColIndex).equals("admin"))
                            btn_reset.setVisibility(View.VISIBLE);
                        else
                            btn_reset.setVisibility(View.INVISIBLE);
                    }
                    cursor.close();
                    database.close();
                }
            }
        });
        block_reset.start();
    }

    void init() {
        btn_yes = (Button) findViewById(R.id.btn_yes);
        btn_no = (Button) findViewById(R.id.btn_no);
        btn_reset = (Button) findViewById(R.id.btn_reset);

        editText_number_portion = (EditText) findViewById(R.id.editText_number_portion);

        listView = (ListView) findViewById(R.id.list_order);

        btn_yes.setOnClickListener(this);
        btn_no.setOnClickListener(this);
        btn_reset.setOnClickListener(this);

        dbHelper = new DBHelper(this);
    }

    void block_button() {
        Calendar calendar = Calendar.getInstance();
        String day_of_week = Calendar.MONDAY + ", " + Calendar.TUESDAY + ", " + Calendar.WEDNESDAY + ", " + Calendar.THURSDAY + ", " + Calendar.FRIDAY;
        if (day_of_week.contains(String.valueOf(calendar.get(Calendar.DAY_OF_WEEK)))) {
            if (calendar.get(Calendar.HOUR_OF_DAY) >= 11) {
                if (calendar.get(Calendar.HOUR_OF_DAY) == 11 & calendar.get(Calendar.MINUTE) > 39) {
                    btn_yes.setClickable(false);
                    btn_no.setClickable(false);
                } else if (calendar.get(Calendar.HOUR_OF_DAY) > 11) {
                    btn_yes.setClickable(false);
                    btn_no.setClickable(false);
                }
            } else if (calendar.get(Calendar.HOUR_OF_DAY) == 0 & calendar.get(Calendar.MINUTE) == 0) {
                choice_person("-", "0");
            } else if (calendar.get(Calendar.HOUR_OF_DAY) < 11) {
                btn_yes.setClickable(true);
                btn_no.setClickable(true);
            }
        } else {
            choice_person("-", "0");
            btn_yes.setClickable(false);
            btn_no.setClickable(false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_yes:
                choice_person("yes", editText_number_portion.getText().toString());
                break;
            case R.id.btn_no:
                choice_person("no", "0");
                break;
            case R.id.btn_reset:
                reset = true;
                break;
        }
    }

    void choice_person(String str, String number_portion) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor = database.query("MYTABLE1_1", null, null, null, null, null, null);

        int nameColIndex = cursor.getColumnIndex("name");
        int phoneColIndex = cursor.getColumnIndex("phone");

        cursor.moveToFirst();

        Person person = new Person(cursor.getString(nameColIndex), cursor.getString(phoneColIndex), str, number_portion);

        ContentValues cv = new ContentValues();
        cv.put("name", cursor.getString(nameColIndex));
        cv.put("phone", cursor.getString(phoneColIndex));
        cv.put("choice", str);
        cv.put("number_portion", number_portion);
        database.insert("MYTABLE1_1", null, cv);

        myDataBase = FirebaseDatabase.getInstance().getReference(cursor.getString(nameColIndex));
        myDataBase.setValue(person);

        cursor.close();
        database.close();
    }

    void update_list() {
        myDataBase = FirebaseDatabase.getInstance().getReference().getRef();
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.custom_listview_item, arrayList);
        listView.setAdapter(adapter);

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Person person = ds.getValue(Person.class);

                    if (person.choice == "no")
                        arrayList.add(person.name + ", \t" + person.phone + ", \t" + person.choice);
                    else
                        arrayList.add(person.name + ", \t" + person.phone + ", \t" + "порций: " + person.number_portion);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        myDataBase.addValueEventListener(valueEventListener);
    }

    static class DBHelper extends SQLiteOpenHelper {
        public DBHelper(Context context) {
            super(context, "mytable1_1", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table MYTABLE1_1 (" + "name text," + "phone text," + "choice text," + "number_portion text" + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}