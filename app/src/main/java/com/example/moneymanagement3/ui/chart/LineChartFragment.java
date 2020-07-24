package com.example.moneymanagement3.ui.chart;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.moneymanagement3.DataBaseHelper;
import com.example.moneymanagement3.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

@RequiresApi(api = Build.VERSION_CODES.O)
public class LineChartFragment extends Fragment {
    View view;
    DataBaseHelper myDb;
    Button btn1;
    Button btn_selectDates;
    Cursor res3; Cursor res2; Cursor res4;
    //cycle updater variables
    LocalDate startdate;
    LocalDate enddate;
    LocalDate currentDate;
    String cycle_input;
    ArrayList<String> cycles;
    Spinner spinner_cycles;
    Spinner spinner_cycles1;
    Spinner spinner_cycles2;
    LineChart lineChart;
    LineData lineData;
    LineDataSet lineDataSet;
    ArrayList lineEntries;
    ArrayList LineEntryLabels;
    TextView tv_customDates;
    LocalDate startdate_this; LocalDate enddate_this;
    DateTimeFormatter formatter;
    Button btn_setStartDate;
    Button btn_setEndDate;
    DatePickerDialog.OnDateSetListener mDateSetListener_start; DatePickerDialog.OnDateSetListener mDateSetListener_end;
    LocalDate temp_startdate;
    LocalDate temp_enddate;
    /////

    @RequiresApi(api = Build.VERSION_CODES.O)
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //buttons and views initiating
        view = inflater.inflate(R.layout.fragment_linechart, container, false);
        view.setBackgroundColor(Color.WHITE);
        btn1 = view.findViewById(R.id.gobackBtn);
        btn_selectDates = view.findViewById(R.id.setDatesBtn2);
        tv_customDates = view.findViewById(R.id.customDatesTv2);

        formatter = DateTimeFormatter.ofPattern("LLL dd, yyyy");

        //get database
        myDb = new DataBaseHelper(getActivity());

        Cursor res_startup = myDb.get_setting();
        res_startup.moveToFirst();
        startdate_this = LocalDate.parse(res_startup.getString(0));
        enddate_this = LocalDate.parse(res_startup.getString(1));

        res3 = myDb.get_setting();
        res3.moveToFirst();
        cycle_updater();


        //Line Chart
        lineChart = view.findViewById(R.id.lineChart);
        startdate = LocalDate.parse(res3.getString(0));
        enddate = LocalDate.parse(res3.getString(1));
        lineChartMaker(startdate_this,enddate_this);

        ////////////////////////
        //What the spinner does when item is selected / not selected
//        spinner_cycles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @RequiresApi(api = Build.VERSION_CODES.O)
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View v, final int position, long id) {
//                //this is important bc "cycles"/spinner shows new-->old, but in the database table4, it's indexed old--->new
//                int inverted_pos = (cycles.size() - 1) - position;
//                res4.moveToPosition(inverted_pos);
//                startdate = LocalDate.parse(res4.getString(0));
//                enddate = LocalDate.parse(res4.getString(1));
//                lineChartMaker(startdate,enddate);
//                Log.d("onselect", "onItemSelected: ");
//            }
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//                //Object item = adapterView.getItemAtPosition(0);
//                res3.moveToFirst();
//                startdate = LocalDate.parse(res3.getString(0));
//                enddate = LocalDate.parse(res3.getString(1));
//                lineChartMaker(startdate,enddate);
//                Log.d("noneselect", "onNothingSelected: ");
//
//            }
//        });
        /////////////////////////////////
        onClick_GoBackBtn();
        onClick_selectDates();
        return view;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////



    //updates the start and end date of the cycle
    public void cycle_updater() {

        cycle_input = "01"; //sets the default cycle input as the first of the month
        currentDate = LocalDate.now();

        if (res3 != null && res3.moveToFirst()) {  //makes sure table3 is not null
            cycle_input = res3.getString(2);
        } else {
            myDb.create_filler_setting_onStartup(cycle_input);
        }

        String currentDate_string = String.valueOf(currentDate);
        String currentMonth_string = "" + currentDate_string.substring(5, 7); //"MM" -- [start ind,end ind)

        String var_string = "" + currentDate_string.substring(0, 5) + currentMonth_string + "-" + cycle_input; //variable to compare current date with
        LocalDate var = LocalDate.parse(var_string);    //convert var into a localdate

        //determine and sets the start and end dates of the cycle
        if (currentDate.isBefore(var)) {
            LocalDate var_new = var.plusMonths(-1);
            startdate = var_new;
            enddate = var.minusDays(1);
            //update database table3
            myDb.update_cycle_setting(String.valueOf(startdate), String.valueOf(enddate), cycle_input);
        } else {
            LocalDate var_new = var.plusMonths(1);
            startdate = var;
            enddate = var_new.minusDays(1);
            //update database table3
            myDb.update_cycle_setting(String.valueOf(startdate), String.valueOf(enddate), cycle_input);
        }



        //******************************************************************************new added


        //dealing with table4 (cycle table) ---- for cycle spinner
        res4 = myDb.get_cycles();
        if (res4!=null && res4.moveToLast()) { //if table4 is not null on startup (run basically every time this fragment is selected)
            String past_startdate = res4.getString(0);
            String past_enddate = res4.getString(1);

            if (!past_startdate.equals(String.valueOf(startdate)) && !past_enddate.equals(String.valueOf(enddate))) { //if a new cycle started (new month)
                res4.moveToLast();
                String cycle_budget = res4.getString(2);
                String categories_list_as_string = res4.getString(3);
                String categories_budget_list_as_string = res4.getString(4);
                //inserts the start and end date of the cycle only if the dates changed
                myDb.insert_new_cycle(String.valueOf(startdate), String.valueOf(enddate), cycle_budget,
                        categories_list_as_string, categories_budget_list_as_string);
            }
        }

        else { //if table4 null (only when first run)

            StringBuilder categories_budget_list_as_string = new StringBuilder();
            StringBuilder categories_list_as_string = new StringBuilder();
            res2 = myDb.getAllData_categories();
            if (res2 != null) { // if categories table3 is not empty
                while (res2.moveToNext()) {
                    String category = res2.getString(1);
                    categories_list_as_string.append(category).append(";");
                    categories_budget_list_as_string.append("0.00").append(";");
                }
            }
            //inserts the start and end date of the cycle only if the dates changed
            myDb.insert_new_cycle(String.valueOf(startdate), String.valueOf(enddate), "0.00",
                    categories_list_as_string.toString(), categories_budget_list_as_string.toString());


        }

        //******************************************************************************new added

    }

    public void setDatesTv(){
        //Formatting the localdate ==> custom string format (Month name dd, yyyy)
        String startdate_formatted = startdate_this.format(formatter);
        String enddate_formatted = enddate_this.format(formatter);
        tv_customDates.setText(startdate_formatted + " ~ " + enddate_formatted);
    }

//    public void onClick_selectDates() {
//        //Button to go back to settings
//        btn_selectDates.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                //creates a second alert dialog
//                AlertDialog.Builder adb2 = new AlertDialog.Builder(view.getContext());
//                //creates edit texts in the second alert dialog
//                final LayoutInflater inflater = getLayoutInflater();
//                final View view_alertButtons = inflater.inflate(R.layout.alert_buttons_layout, null);
//                final Button btn_currentCycle = view_alertButtons.findViewById(R.id.btn1);
//                final Button btn_cycles = view_alertButtons.findViewById(R.id.btn2);
//                final Button btn_customDates = view_alertButtons.findViewById(R.id.btn3);
//
//                btn_cycles.setText("Cycles");
//                btn_customDates.setText("Custom Dates");
//                btn_currentCycle.setText("Current Cycle");
//
//                adb2.setTitle("Select Date Range:");
//
//                adb2.setView(view_alertButtons); //shows the edit texts from the xml file in the alert dialog
//                adb2.setNeutralButton("Back", null);
//
//                //creates the onShow function that keeps the alert dialog open unless specifically called to close
//                final AlertDialog alertDialog = adb2.create();
//
//                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                    @Override
//                    public void onShow(final DialogInterface dialog) {
//
//                        //current cycle button
//                        btn_currentCycle.setOnClickListener(new View.OnClickListener() {
//                            @RequiresApi(api = Build.VERSION_CODES.O)
//                            public void onClick(View v) {
//                                res3 = myDb.get_setting();
//                                res3.moveToFirst();
//                                startdate_this = LocalDate.parse(res3.getString(0));
//                                enddate_this = LocalDate.parse(res3.getString(1));
//                                lineChartMaker(startdate_this,enddate_this);
//                                setDatesTv();
//                                //catTotal.setText("Select a Category");
//                                //catTotal.setTextSize(25);
//                                alertDialog.dismiss();
//                            }
//                        });
//
////                        //cycles button
//                        btn_cycles.setOnClickListener(new View.OnClickListener() {
//                            @RequiresApi(api = Build.VERSION_CODES.O)
//                            public void onClick(View v) {
//                                final ArrayList<String> cycles_startToEnd = new ArrayList<>();
//
//                                LayoutInflater inflater = getLayoutInflater();
//                                final View view_alertSpinner = inflater.inflate(R.layout.alert_spinner, null);
//                                spinner_cycles = view_alertSpinner.findViewById(R.id.the_spinner);
//                                //Create Cycle Spinner --- from table4
//                                res4 = myDb.get_cycles();
//                                while(res4.moveToNext()){
//                                    String cyc_startdate = res4.getString(0);
//                                    String cyc_enddate = res4.getString(1);
//                                    LocalDate cyc_startdate_localdate = LocalDate.parse(cyc_startdate);
//                                    LocalDate cyc_enddate_localdate = LocalDate.parse(cyc_enddate);
//
//                                    //Formatting the localdate ==> custom string format (Month name dd, yyyy)
//                                    DateTimeFormatter cyc_formatter = DateTimeFormatter.ofPattern("LLL dd, yy");
//                                    String cyc_startdate_formatted = cyc_startdate_localdate.format(cyc_formatter);
//                                    String cyc_enddate_formatted = cyc_enddate_localdate.format(cyc_formatter);
//
//                                    String formatted_dates = cyc_startdate_formatted + " ~ " + cyc_enddate_formatted;
//                                    cycles_startToEnd.add(formatted_dates);
//                                }
//                                Collections.reverse(cycles_startToEnd);
//                                ArrayAdapter<String> spn_cyc_adapter = new ArrayAdapter<String>(view.getContext(), R.layout.spinner_text2,cycles_startToEnd);
//                                spinner_cycles.setAdapter(spn_cyc_adapter);
//
//
//                                //What the spinner does when item is selected / not selected
//                                spinner_cycles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//                                    @RequiresApi(api = Build.VERSION_CODES.O)
//                                    @Override
//                                    public void onItemSelected(AdapterView<?> adapterView, View v, final int position, long id) {
//                                        //this is important bc "cycles"/spinner shows new-->old, but in the database table4, it's indexed old--->new
//                                        int inverted_pos = (cycles_startToEnd.size() - 1) - position;
//                                        res4.moveToPosition(inverted_pos);
//                                        startdate_this = LocalDate.parse(res4.getString(0));
//                                        enddate_this = LocalDate.parse(res4.getString(1));
//                                    }
//                                    @Override
//                                    public void onNothingSelected(AdapterView<?> adapterView) {
//                                        Object item = adapterView.getItemAtPosition(0);
//                                    }
//                                });
//
//
//                                //create the alertdialog
//                                AlertDialog.Builder adb = new AlertDialog.Builder(view.getContext());
//                                adb.setView(view_alertSpinner);
//                                adb.setTitle("Select cycle:");
//                                adb.setPositiveButton("Select",null);
//                                adb.setNeutralButton("Back",null);
//
//                                final AlertDialog alertDialog2 = adb.create();
//                                alertDialog2.setOnShowListener(new DialogInterface.OnShowListener() {
//                                    @Override
//                                    public void onShow(final DialogInterface dialog) {
//                                        //Back button (first alertdialog)
//                                        Button positiveButton = alertDialog2.getButton(AlertDialog.BUTTON_POSITIVE);
//                                        positiveButton.setOnClickListener(new View.OnClickListener() {
//                                            @RequiresApi(api = Build.VERSION_CODES.O)
//                                            @Override
//                                            public void onClick(View v) {
//
//                                                lineChartMaker(startdate_this,enddate_this);
//                                                setDatesTv();
//                                                //catTotal.setText("Select a Category");
//                                                //catTotal.setTextSize(25);
//
//                                                dialog.dismiss();
//                                                alertDialog.dismiss();
//
//                                            }
//                                        });
//
//                                        //Back button (first alertdialog)
//                                        Button neutralButton = alertDialog2.getButton(AlertDialog.BUTTON_NEUTRAL);
//                                        neutralButton.setOnClickListener(new View.OnClickListener() {
//                                            @RequiresApi(api = Build.VERSION_CODES.O)
//                                            @Override
//                                            public void onClick(View v) {
//                                                dialog.dismiss();
//                                            }
//                                        });
//                                    }
//                                });
//
//                                alertDialog2.show();
//
//                            }
//                        });
//
////
//
//                        //customdates button
//                        btn_customDates.setOnClickListener(new View.OnClickListener() {
//                            @RequiresApi(api = Build.VERSION_CODES.O)
//                            public void onClick(View v) {
//
//                                AlertDialog.Builder adb = new AlertDialog.Builder(view.getContext());
//                                LayoutInflater inflater = getLayoutInflater();
//                                final View view_setCustomDateBtns = inflater.inflate(R.layout.set_custom_dates, null);
//                                adb.setView(view_setCustomDateBtns);
//                                adb.setNeutralButton("Back",null);
//                                adb.setPositiveButton("Set",null);
//
//                                final AlertDialog alertDialog3 = adb.create();
//
//                                alertDialog3.setOnShowListener(new DialogInterface.OnShowListener() {
//                                    @Override
//                                    public void onShow(final DialogInterface dialog) {
//                                        btn_setStartDate = view_setCustomDateBtns.findViewById(R.id.startBtn);
//                                        btn_setEndDate = view_setCustomDateBtns.findViewById(R.id.endBtn);
//
//                                        btn_setStartDate.setOnClickListener(new View.OnClickListener() {
//                                            public void onClick(View v) {
//                                                DatePickerDialog datePickerDialog = new DatePickerDialog(view.getContext(), mDateSetListener_start,
//                                                        Calendar.getInstance().get(Calendar.YEAR),
//                                                        Calendar.getInstance().get(Calendar.MONTH),
//                                                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
//                                                datePickerDialog.show();
//                                            }
//                                        });
//
//                                        btn_setEndDate.setOnClickListener(new View.OnClickListener() {
//                                            public void onClick(View v) {
//                                                if(temp_startdate!=null){
//                                                    DatePickerDialog datePickerDialog = new DatePickerDialog(view.getContext(), mDateSetListener_end,
//                                                            temp_startdate.getYear(),
//                                                            temp_startdate.getMonthValue()-1,
//                                                            temp_startdate.getDayOfMonth());
//                                                    datePickerDialog.show();
//                                                }
//                                                else{
//                                                    DatePickerDialog datePickerDialog = new DatePickerDialog(view.getContext(), mDateSetListener_end,
//                                                            Calendar.getInstance().get(Calendar.YEAR),
//                                                            Calendar.getInstance().get(Calendar.MONTH),
//                                                            Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
//                                                    datePickerDialog.show();
//                                                }
//                                            }
//                                        });
//
//                                        mDateSetListener_start = new DatePickerDialog.OnDateSetListener() {
//                                            @RequiresApi(api = Build.VERSION_CODES.O)
//                                            @Override
//                                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
//                                                month = month + 1;
//
//                                                String month_string = String.valueOf(month);
//                                                String day_string = String.valueOf(day);
//                                                if (month_string.length() < 2){
//                                                    month_string = "0" + month_string;
//                                                }
//                                                if (day_string.length() < 2){
//                                                    day_string = "0" + day_string;
//                                                }
//                                                String date = year + "-" + month_string + "-" + day_string;
//                                                temp_startdate = LocalDate.parse(date);
//                                                btn_setStartDate.setText(temp_startdate.format(formatter));
//
//                                            }
//                                        };
//
//                                        mDateSetListener_end = new DatePickerDialog.OnDateSetListener() {
//                                            @RequiresApi(api = Build.VERSION_CODES.O)
//                                            @Override
//                                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
//                                                month = month + 1;
//
//                                                String month_string = String.valueOf(month);
//                                                String day_string = String.valueOf(day);
//                                                if (month_string.length() < 2){
//                                                    month_string = "0" + month_string;
//                                                }
//                                                if (day_string.length() < 2){
//                                                    day_string = "0" + day_string;
//                                                }
//                                                String date = year + "-" + month_string + "-" + day_string;
//                                                temp_enddate = LocalDate.parse(date);
//                                                btn_setEndDate.setText(temp_enddate.format(formatter));
//                                            }
//                                        };
//
//                                        //set button (first alertdialog)
//                                        Button positiveButton = alertDialog3.getButton(AlertDialog.BUTTON_POSITIVE);
//                                        positiveButton.setOnClickListener(new View.OnClickListener() {
//                                            @RequiresApi(api = Build.VERSION_CODES.O)
//                                            @Override
//                                            public void onClick(View v) {
//
//                                                if (temp_startdate.isAfter(temp_enddate)){
//                                                    AlertDialog.Builder adb = new AlertDialog.Builder(view.getContext());
//                                                    adb.setTitle("The start date is after the end date");
//                                                    btn_setEndDate.setText("End Date");
//                                                    adb.setPositiveButton("Okay", null);
//                                                    adb.show();
//                                                }
//                                                else{
//                                                    startdate_this = temp_startdate;
//                                                    enddate_this = temp_enddate;
//
//                                                    lineChartMaker(startdate_this,enddate_this);
//                                                    setDatesTv();
//                                                   // catTotal.setText("Select a Category");
//                                                   // catTotal.setTextSize(25);
//
//                                                    dialog.dismiss();
//                                                    alertDialog.dismiss();
//
//                                                }
//
//                                            }
//                                        });
//
//                                    }
//                                });
//                                alertDialog3.show();
//
//                            }
//
//                        });
//
//
//                        //Back button (first alertdialog)
//                        Button neutralButton = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
//                        neutralButton.setOnClickListener(new View.OnClickListener() {
//                            @RequiresApi(api = Build.VERSION_CODES.O)
//                            @Override
//                            public void onClick(View v) {
//
//                                dialog.dismiss();
//
//                            }
//                        });
//                    }
//                });
//
//                alertDialog.show();
//
//            }
//        });
//
//    }
public void onClick_selectDates() {
    //Button to go back to settings
    btn_selectDates.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            //creates a second alert dialog
            AlertDialog.Builder adb2 = new AlertDialog.Builder(view.getContext());
            //creates edit texts in the second alert dialog
            final LayoutInflater inflater = getLayoutInflater();
            final View view_alertButtons = inflater.inflate(R.layout.alert_buttons_layout, null);
            final Button btn_currentCycle = view_alertButtons.findViewById(R.id.btn1);
            final Button btn_cycles = view_alertButtons.findViewById(R.id.btn2);
            final Button btn_customDates = view_alertButtons.findViewById(R.id.btn3);

            btn_cycles.setText("Cycles");
            btn_customDates.setText("Custom Dates");
            btn_currentCycle.setText("Current Cycle");

            adb2.setTitle("Select Date Range:");

            adb2.setView(view_alertButtons); //shows the edit texts from the xml file in the alert dialog
            adb2.setNeutralButton("Back", null);

            //creates the onShow function that keeps the alert dialog open unless specifically called to close
            final AlertDialog alertDialog = adb2.create();

            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(final DialogInterface dialog) {

                    //current cycle button
                    btn_currentCycle.setOnClickListener(new View.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        public void onClick(View v) {
                            res3 = myDb.get_setting();
                            res3.moveToFirst();
                            startdate_this = LocalDate.parse(res3.getString(0));
                            enddate_this = LocalDate.parse(res3.getString(1));
                            lineChartMaker(startdate_this,enddate_this);
                            setDatesTv();
                            //catTotal.setText("Select a Category");
                            //catTotal.setTextSize(25);
                            alertDialog.dismiss();
                        }
                    });

//                        //cycles button
                    btn_cycles.setOnClickListener(new View.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        public void onClick(View v) {
                            final ArrayList<String> cycles_startToEnd = new ArrayList<>();

                            LayoutInflater inflater = getLayoutInflater();
                            final View view_alertSpinner = inflater.inflate(R.layout.alert_spinner, null);
                            spinner_cycles = view_alertSpinner.findViewById(R.id.the_spinner);
                            //Create Cycle Spinner --- from table4
                            res4 = myDb.get_cycles();
                            while(res4.moveToNext()){
                                String cyc_startdate = res4.getString(0);
                                String cyc_enddate = res4.getString(1);
                                LocalDate cyc_startdate_localdate = LocalDate.parse(cyc_startdate);
                                LocalDate cyc_enddate_localdate = LocalDate.parse(cyc_enddate);

                                //Formatting the localdate ==> custom string format (Month name dd, yyyy)
                                DateTimeFormatter cyc_formatter = DateTimeFormatter.ofPattern("LLL dd, yy");
                                String cyc_startdate_formatted = cyc_startdate_localdate.format(cyc_formatter);
                                String cyc_enddate_formatted = cyc_enddate_localdate.format(cyc_formatter);

                                String formatted_dates = cyc_startdate_formatted + " ~ " + cyc_enddate_formatted;
                                cycles_startToEnd.add(formatted_dates);
                            }
                            Collections.reverse(cycles_startToEnd);
                            ArrayAdapter<String> spn_cyc_adapter = new ArrayAdapter<String>(view.getContext(), R.layout.spinner_text2,cycles_startToEnd);
                            spinner_cycles.setAdapter(spn_cyc_adapter);


                            //What the spinner does when item is selected / not selected
                            spinner_cycles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @RequiresApi(api = Build.VERSION_CODES.O)
                                @Override
                                public void onItemSelected(AdapterView<?> adapterView, View v, final int position, long id) {
                                    //this is important bc "cycles"/spinner shows new-->old, but in the database table4, it's indexed old--->new
                                    int inverted_pos = (cycles_startToEnd.size() - 1) - position;
                                    res4.moveToPosition(inverted_pos);
                                    startdate_this = LocalDate.parse(res4.getString(0));
                                    enddate_this = LocalDate.parse(res4.getString(1));
                                }
                                @Override
                                public void onNothingSelected(AdapterView<?> adapterView) {
                                    Object item = adapterView.getItemAtPosition(0);
                                }
                            });


                            //create the alertdialog
                            AlertDialog.Builder adb = new AlertDialog.Builder(view.getContext());
                            adb.setView(view_alertSpinner);
                            adb.setTitle("Select cycle:");
                            adb.setPositiveButton("Select",null);
                            adb.setNeutralButton("Back",null);

                            final AlertDialog alertDialog2 = adb.create();
                            alertDialog2.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(final DialogInterface dialog) {
                                    //Back button (first alertdialog)
                                    Button positiveButton = alertDialog2.getButton(AlertDialog.BUTTON_POSITIVE);
                                    positiveButton.setOnClickListener(new View.OnClickListener() {
                                        @RequiresApi(api = Build.VERSION_CODES.O)
                                        @Override
                                        public void onClick(View v) {

                                            lineChartMaker(startdate_this,enddate_this);
                                            setDatesTv();
                                            //catTotal.setText("Select a Category");
                                            //catTotal.setTextSize(25);

                                            dialog.dismiss();
                                            alertDialog.dismiss();

                                        }
                                    });

                                    //Back button (first alertdialog)
                                    Button neutralButton = alertDialog2.getButton(AlertDialog.BUTTON_NEUTRAL);
                                    neutralButton.setOnClickListener(new View.OnClickListener() {
                                        @RequiresApi(api = Build.VERSION_CODES.O)
                                        @Override
                                        public void onClick(View v) {
                                            dialog.dismiss();
                                        }
                                    });
                                }
                            });

                            alertDialog2.show();

                        }
                    });

//

                    //customdates button
                    btn_customDates.setOnClickListener(new View.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        public void onClick(View v) {
                            final ArrayList<String> cycles_startToEnd = new ArrayList<>();
                            AlertDialog.Builder adb = new AlertDialog.Builder(view.getContext());
                            LayoutInflater inflater = getLayoutInflater();
                            final View view_setCustomDateBtns = inflater.inflate(R.layout.set_custom_dates_spinner, null);
                            adb.setView(view_setCustomDateBtns);
                            adb.setNeutralButton("Back",null);
                            adb.setPositiveButton("Set",null);

                           // spinner_cycles1 = view.findViewById(R.id.spinner);
                           // spinner_cycles2 = view.findViewById(R.id.spinner2);
                            spinner_cycles1 = view_setCustomDateBtns.findViewById(R.id.spinner);
                            spinner_cycles2 = view_setCustomDateBtns.findViewById(R.id.spinner2);

                            final AlertDialog alertDialog3 = adb.create();
                            //Create Cycle Spinner --- from table4
                            res4 = myDb.get_cycles();
                            while(res4.moveToNext()){
                                String cyc_startdate = res4.getString(0);
                                String cyc_enddate = res4.getString(1);
                                LocalDate cyc_startdate_localdate = LocalDate.parse(cyc_startdate);
                                LocalDate cyc_enddate_localdate = LocalDate.parse(cyc_enddate);

                                //Formatting the localdate ==> custom string format (Month name dd, yyyy)
                                DateTimeFormatter cyc_formatter = DateTimeFormatter.ofPattern("LLL dd, yy");
                                String cyc_startdate_formatted = cyc_startdate_localdate.format(cyc_formatter);
                                String cyc_enddate_formatted = cyc_enddate_localdate.format(cyc_formatter);

                                String formatted_dates = cyc_startdate_formatted + " ~ " + cyc_enddate_formatted;
                                cycles_startToEnd.add(formatted_dates);
                            }
                            Collections.reverse(cycles_startToEnd);
                            ArrayAdapter<String> spn_cyc_adapter = new ArrayAdapter<String>(view.getContext(), R.layout.spinner_text2,cycles_startToEnd);
                            spinner_cycles1.setAdapter(spn_cyc_adapter);
                            spinner_cycles2.setAdapter(spn_cyc_adapter);

                            alertDialog3.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(final DialogInterface dialog) {


//                                    btn_setStartDate.setOnClickListener(new View.OnClickListener() {
//                                        public void onClick(View v) {
//                                            DatePickerDialog datePickerDialog = new DatePickerDialog(view.getContext(), mDateSetListener_start,
//                                                    Calendar.getInstance().get(Calendar.YEAR),
//                                                    Calendar.getInstance().get(Calendar.MONTH),
//                                                    Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
//                                            datePickerDialog.show();
//                                        }
//                                    });

//                                    btn_setEndDate.setOnClickListener(new View.OnClickListener() {
//                                        public void onClick(View v) {
//                                            if(temp_startdate!=null){
//                                                DatePickerDialog datePickerDialog = new DatePickerDialog(view.getContext(), mDateSetListener_end,
//                                                        temp_startdate.getYear(),
//                                                        temp_startdate.getMonthValue()-1,
//                                                        temp_startdate.getDayOfMonth());
//                                                datePickerDialog.show();
//                                            }
//                                            else{
//                                                DatePickerDialog datePickerDialog = new DatePickerDialog(view.getContext(), mDateSetListener_end,
//                                                        Calendar.getInstance().get(Calendar.YEAR),
//                                                        Calendar.getInstance().get(Calendar.MONTH),
//                                                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
//                                                datePickerDialog.show();
//                                            }
//                                        }
//                                    });

//                                    mDateSetListener_start = new DatePickerDialog.OnDateSetListener() {
//                                        @RequiresApi(api = Build.VERSION_CODES.O)
//                                        @Override
//                                        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
//                                            month = month + 1;
//
//                                            String month_string = String.valueOf(month);
//                                            String day_string = String.valueOf(day);
//                                            if (month_string.length() < 2){
//                                                month_string = "0" + month_string;
//                                            }
//                                            if (day_string.length() < 2){
//                                                day_string = "0" + day_string;
//                                            }
//                                            String date = year + "-" + month_string + "-" + day_string;
//                                            temp_startdate = LocalDate.parse(date);
//                                            btn_setStartDate.setText(temp_startdate.format(formatter));
//
//                                        }
//                                    };
//
//                                    mDateSetListener_end = new DatePickerDialog.OnDateSetListener() {
//                                        @RequiresApi(api = Build.VERSION_CODES.O)
//                                        @Override
//                                        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
//                                            month = month + 1;
//
//                                            String month_string = String.valueOf(month);
//                                            String day_string = String.valueOf(day);
//                                            if (month_string.length() < 2){
//                                                month_string = "0" + month_string;
//                                            }
//                                            if (day_string.length() < 2){
//                                                day_string = "0" + day_string;
//                                            }
//                                            String date = year + "-" + month_string + "-" + day_string;
//                                            temp_enddate = LocalDate.parse(date);
//                                            btn_setEndDate.setText(temp_enddate.format(formatter));
//                                        }
//                                    };

                                    //set button (first alertdialog)
                                    Button positiveButton = alertDialog3.getButton(AlertDialog.BUTTON_POSITIVE);
                                    positiveButton.setOnClickListener(new View.OnClickListener() {
                                        @RequiresApi(api = Build.VERSION_CODES.O)
                                        @Override
                                        public void onClick(View v) {

                                            if (temp_startdate.isAfter(temp_enddate)){
                                                AlertDialog.Builder adb = new AlertDialog.Builder(view.getContext());
                                                adb.setTitle("The start date is after the end date");
                                                btn_setEndDate.setText("End Date");
                                                adb.setPositiveButton("Okay", null);
                                                adb.show();
                                            }
                                            else{
                                                startdate_this = temp_startdate;
                                                enddate_this = temp_enddate;

                                                lineChartMaker(startdate_this,enddate_this);
                                                setDatesTv();
                                                // catTotal.setText("Select a Category");
                                                // catTotal.setTextSize(25);

                                                dialog.dismiss();
                                                alertDialog.dismiss();

                                            }

                                        }
                                    });

                                }
                            });
                            alertDialog3.show();

                        }

                    });

                    //Back button (first alertdialog)
                    Button neutralButton = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                    neutralButton.setOnClickListener(new View.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onClick(View v) {

                            dialog.dismiss();

                        }
                    });
                }
            });

            alertDialog.show();

        }
    });

}
    public void lineChartMaker(LocalDate startDate,LocalDate endDate){
        lineChart.invalidate();////
        getEntries(startDate,endDate);
        lineDataSet = new LineDataSet(lineEntries, "");
        lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);
        // lineDataSet.setColors(ColorTemplate.JOYFUL_COLORS);
        lineDataSet.setValueTextColor(Color.BLACK);
        lineDataSet.setValueTextColor(Color.WHITE);
        lineDataSet.setValueTextSize(18f);
        lineChart.getLegend().setEnabled(false);
        lineData.notifyDataChanged();////
        lineChart.setTouchEnabled(true);
        //////////////////////
        lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener()
        {
            @Override
            public void onValueSelected(Entry e, Highlight h)
            {
                float x=e.getX();
                Log.d("mytag", String.valueOf(x));
                float y=e.getY();
                Log.d("mytag", String.valueOf(y));
            }

            @Override
            public void onNothingSelected()
            {

            }
        });
        ////////////////////////////////////////////////

    }

    private void getEntries(LocalDate startDate,LocalDate endDate) {
        Cursor dataInRangeRes;
        Float monthlyTotal = (float) 0;
        Float currentMonthAmount;
        lineEntries = new ArrayList<>();
        int x = 0;

        dataInRangeRes = myDb.getLineChartMonthly(startDate.minusDays(1),endDate);

        while(dataInRangeRes.moveToNext()){
            currentMonthAmount = Float.parseFloat(dataInRangeRes.getString(0));
            monthlyTotal = monthlyTotal + currentMonthAmount;
            lineEntries.add(new Entry(x, monthlyTotal));
            x++;
        }


    }

    private void getEntries2(LocalDate startDate,LocalDate endDate) {
        Cursor dataInRangeRes;
        Float monthlyTotal = (float) 0;
        Float currentMonthAmount;
        lineEntries = new ArrayList<>();
        int x = 0;

        dataInRangeRes = myDb.getLineChartMonthly(startDate.minusDays(1),endDate);

        while(dataInRangeRes.moveToNext()){
            currentMonthAmount = Float.parseFloat(dataInRangeRes.getString(0));
            monthlyTotal = monthlyTotal + currentMonthAmount;
            lineEntries.add(new Entry(x, monthlyTotal));
            x++;
        }

    }

    public void onClick_GoBackBtn () {
        //Button to go back to settings
        btn1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ChartFragment frag= new ChartFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, frag, "linechartFrag")
                        .commit();
            }
        });
    }

}
