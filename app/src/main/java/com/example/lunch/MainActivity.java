package com.example.lunch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

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
    TextView textView;
    TableLayout tableLayout;
    DBHelper dbHelper;
    private DatabaseReference myDataBase;
    ArrayList<String> arrayList_Name = null;
    ArrayList<String> arrayList_Phone = null;
    ArrayList<String> arrayList_Portion = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init_view();
        update_list();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = db.query("MYTABLE1_1", null, null, null, null, null, null);
        if (!c.moveToFirst()) { //проверим зарегистрирован пользователь или нет
            Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
            startActivity(intent);
        }
        c.close();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    block_button();
                }
            }
        });
        thread.start();
        Thread block_reset = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean state = false;
                while (!state) {
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    Cursor cursor = db.query("MYTABLE1_1", null, null, null, null, null, null);
                    if (cursor.getCount() != 0) {
                        state = true;
                    }
                    if (state) {
                        cursor.moveToFirst();
                        int nameColIndex = cursor.getColumnIndex("name");
                        if (cursor.getString(nameColIndex).equals("admin")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    btn_yes.setVisibility(View.INVISIBLE);
                                    btn_no.setVisibility(View.INVISIBLE);
                                    btn_reset.setVisibility(View.VISIBLE);
                                    editText_number_portion.setVisibility(View.INVISIBLE);
                                    textView.setVisibility(View.INVISIBLE);
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    btn_yes.setVisibility(View.VISIBLE);
                                    btn_no.setVisibility(View.VISIBLE);
                                    btn_reset.setVisibility(View.INVISIBLE);
                                    editText_number_portion.setVisibility(View.VISIBLE);
                                    textView.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    }
                    cursor.close();
                }
            }
        });
        block_reset.start();
    }

    void init_view() {
        arrayList_Name = new ArrayList<>();
        arrayList_Phone = new ArrayList<>();
        arrayList_Portion = new ArrayList<>();

        btn_yes = (Button) findViewById(R.id.btn_yes);
        btn_no = (Button) findViewById(R.id.btn_no);
        btn_reset = (Button) findViewById(R.id.btn_reset);

        editText_number_portion = (EditText) findViewById(R.id.editText_number_portion);

        textView = (TextView) findViewById(R.id.textView2);

        tableLayout = (TableLayout) findViewById(R.id.table);

        btn_yes.setOnClickListener(this);
        btn_no.setOnClickListener(this);
        btn_reset.setOnClickListener(this);

        dbHelper = new DBHelper(this);
    }

    void update_list() {
        myDataBase = FirebaseDatabase.getInstance().getReference();

        ValueEventListener valueEventListener = new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i=0;
                arrayList_Portion.clear();
                arrayList_Name.clear();
                arrayList_Phone.clear();
                tableLayout.removeAllViews();

                TableRow tableRow1 = new TableRow(MainActivity.this);
                tableRow1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                TextView tv6 = new TextView(MainActivity.this);
                TextView tv7 = new TextView(MainActivity.this);
                TextView tv8 = new TextView(MainActivity.this);

                tv6.setTextColor(Color.WHITE);
                tv7.setTextColor(Color.WHITE);
                tv8.setTextColor(Color.WHITE);

                tv6.setBackgroundResource(R.drawable.myshape);
                tv7.setBackgroundResource(R.drawable.myshape);
                tv8.setBackgroundResource(R.drawable.myshape);

                tv6.setText(" ФИО ");
                tv7.setText(" Моб. номер ");
                tv8.setText(" Количество порций ");

                tableRow1.addView(tv6);
                tableRow1.addView(tv7);
                tableRow1.addView(tv8);

                tableLayout.addView(tableRow1);
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Person person = ds.getValue(Person.class);
                    if (!person.name.equals("admin")) {
                        TableRow tableRow = new TableRow(MainActivity.this);
                        tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                        arrayList_Name.add(person.name);
                        arrayList_Phone.add(person.phone);
                        arrayList_Portion.add(person.number_portion);

                        TextView tv2 = new TextView(MainActivity.this);
                        TextView tv3 = new TextView(MainActivity.this);
                        TextView tv4 = new TextView(MainActivity.this);

                        tv2.setTextColor(Color.WHITE);
                        tv3.setTextColor(Color.WHITE);
                        tv4.setTextColor(Color.WHITE);

                        tv2.setBackgroundResource(R.drawable.myshape);
                        tv3.setBackgroundResource(R.drawable.myshape);
                        tv4.setBackgroundResource(R.drawable.myshape);

                        tv2.setText(" "+arrayList_Name.get(i)+" ");
                        tv3.setText(" "+arrayList_Phone.get(i)+" ");
                        tv4.setText(" "+arrayList_Portion.get(i)+" ");

                        i++;

                        tableRow.addView(tv2);
                        tableRow.addView(tv3);
                        tableRow.addView(tv4);

                        tableLayout.addView(tableRow);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        myDataBase.addValueEventListener(valueEventListener);
    }

    void block_button() {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor = database.query("MYTABLE1_1", null, null, null, null, null, null);
        if(cursor.moveToFirst()){
            int nameColIndex = cursor.getColumnIndex("name");
            if (cursor.getString(nameColIndex).equals("admin")){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setVisibility(View.INVISIBLE);
                    }
                });
            }
        }
        cursor.close();

        Calendar calendar = Calendar.getInstance();
        String day_of_week = Calendar.MONDAY + ", " + Calendar.TUESDAY + ", " + Calendar.WEDNESDAY + ", " + Calendar.THURSDAY + ", " + Calendar.FRIDAY;
        if (day_of_week.contains(String.valueOf(calendar.get(Calendar.DAY_OF_WEEK)))) {
            if (calendar.get(Calendar.HOUR_OF_DAY) >= 11) {
                if (calendar.get(Calendar.HOUR_OF_DAY) == 11 & calendar.get(Calendar.MINUTE) > 39) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btn_yes.setVisibility(View.INVISIBLE);
                            btn_no.setVisibility(View.INVISIBLE);
                            editText_number_portion.setVisibility(View.INVISIBLE);
                            textView.setVisibility(View.INVISIBLE);
                        }
                    });
                } else if (calendar.get(Calendar.HOUR_OF_DAY) > 11) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btn_yes.setVisibility(View.INVISIBLE);
                            btn_no.setVisibility(View.INVISIBLE);
                            editText_number_portion.setVisibility(View.INVISIBLE);
                            textView.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            } else if (calendar.get(Calendar.HOUR_OF_DAY) == 0 & calendar.get(Calendar.MINUTE) == 0) {
                choice_person("-", "0");
            } else if (calendar.get(Calendar.HOUR_OF_DAY) < 11) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btn_yes.setVisibility(View.VISIBLE);
                        btn_no.setVisibility(View.VISIBLE);
                        textView.setVisibility(View.VISIBLE);
                        editText_number_portion.setVisibility(View.VISIBLE);
                    }
                });
            }
        } else {
            choice_person("-", "0");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btn_yes.setVisibility(View.VISIBLE);
                    btn_no.setVisibility(View.VISIBLE);
                    textView.setVisibility(View.VISIBLE);
                    editText_number_portion.setVisibility(View.VISIBLE);
                }
            });
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
                reset_database();
                break;
        }
    }

    void reset_database() {
        for (int i = 0; i < arrayList_Name.size(); i++) {
            myDataBase = FirebaseDatabase.getInstance().getReference();
            myDataBase.child(arrayList_Name.get(i)).child("choice").setValue("-");
            myDataBase.child(arrayList_Name.get(i)).child("name").setValue(arrayList_Name.get(i));
            myDataBase.child(arrayList_Name.get(i)).child("number_portion").setValue("0");
            myDataBase.child(arrayList_Name.get(i)).child("phone").setValue(arrayList_Phone.get(i));
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