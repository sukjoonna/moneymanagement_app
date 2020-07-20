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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.moneymanagement3.DataBaseHelper;
import com.example.moneymanagement3.R;
import com.example.moneymanagement3.ui.budget.BudgetFragment;
import com.example.moneymanagement3.ui.budget.SetBudgetCatFragment;
import com.example.moneymanagement3.ui.setting.ManageCatFragment;
import com.example.moneymanagement3.ui.setting.SettingFragment;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class BarChartFragment extends Fragment {
    View view;
    DataBaseHelper myDb;
    Button btn1;
    Cursor res3; Cursor res2; Cursor res4;
    //cycle updater variables
    LocalDate startdate;
    LocalDate enddate;
    LocalDate currentDate;
    String cycle_input;
    ArrayList<String> cycles;
    Spinner spinner_cycles;
    /////
    TextView tv_debit; TextView tv_credit; TextView tv_cash; TextView tv_check;
    ArrayList<String> paymentTypes_arraylist; ArrayList<String> sums_arraylist;
    //
    Button btn_selectDates; TextView tv_customDates;
    DateTimeFormatter formatter;
    LocalDate startdate_this; LocalDate enddate_this;
    Cursor res;
    Button btn_setStartDate;
    Button btn_setEndDate;
    DatePickerDialog.OnDateSetListener mDateSetListener_start;     DatePickerDialog.OnDateSetListener mDateSetListener_end;
    LocalDate temp_startdate;
    LocalDate temp_enddate;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_barchart, container, false);
        view.setBackgroundColor(Color.WHITE);
        btn1 = view.findViewById(R.id.gobackBtn);

        tv_debit = view.findViewById(R.id.debitTv);
        tv_credit = view.findViewById(R.id.creditTv);
        tv_cash = view.findViewById(R.id.cashTv);
        tv_check = view.findViewById(R.id.checkTv);

        //get database
        myDb = new DataBaseHelper(getActivity());

        btn_selectDates = view.findViewById(R.id.setDatesBtn);
        tv_customDates = view.findViewById(R.id.customDatesTv);

        formatter = DateTimeFormatter.ofPattern("LLL dd, yyyy");

        Cursor res_startup = myDb.get_setting();
        res_startup.moveToFirst();
        startdate_this = LocalDate.parse(res_startup.getString(0));
        enddate_this = LocalDate.parse(res_startup.getString(1));

        setPaymentTypeText(startdate_this,enddate_this);
        setDatesTv();


        onClick_selectDates();

        onClick_GoBackBtn();

        return view;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setDatesTv(){
        //Formatting the localdate ==> custom string format (Month name dd, yyyy)
        String startdate_formatted = startdate_this.format(formatter);
        String enddate_formatted = enddate_this.format(formatter);
        tv_customDates.setText(startdate_formatted + " ~ " + enddate_formatted);
    }


    public void setPaymentTypeText (LocalDate startdate, LocalDate enddate){
        Cursor res_paymentTypeSums = myDb.getPaymentTypesDateRange(startdate,enddate); //first col = payment types, second col = sums

        String payment_type = "";
        double payment_type_sum = 0;

        tv_check.setText("-$0.00");
        tv_credit.setText("-$0.00");
        tv_debit.setText("-$0.00");
        tv_cash.setText("-$0.00");

        while (res_paymentTypeSums.moveToNext()){
            payment_type = res_paymentTypeSums.getString(0);
            payment_type_sum = Double.parseDouble(res_paymentTypeSums.getString(1));

            switch (payment_type){
                case "Credit":
                    tv_credit.setText("-$" + String.format("%.2f",payment_type_sum));
                    break;
                case "Debit":
                    tv_debit.setText("-$" + String.format("%.2f",payment_type_sum));
                    break;
                case "Check":
                    tv_check.setText("-$" + String.format("%.2f",payment_type_sum));
                    break;
                case "Cash":
                    tv_cash.setText("-$" + String.format("%.2f",payment_type_sum));
                    break;
            }

        }

    }


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
                                setPaymentTypeText(startdate_this,enddate_this);
                                setDatesTv();
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

                                                setPaymentTypeText(startdate_this,enddate_this);
                                                setDatesTv();

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

                                AlertDialog.Builder adb = new AlertDialog.Builder(view.getContext());
                                LayoutInflater inflater = getLayoutInflater();
                                final View view_setCustomDateBtns = inflater.inflate(R.layout.set_custom_dates, null);
                                adb.setView(view_setCustomDateBtns);
                                adb.setNeutralButton("Back",null);
                                adb.setPositiveButton("Set",null);

                                final AlertDialog alertDialog3 = adb.create();

                                alertDialog3.setOnShowListener(new DialogInterface.OnShowListener() {
                                    @Override
                                    public void onShow(final DialogInterface dialog) {
                                        btn_setStartDate = view_setCustomDateBtns.findViewById(R.id.startBtn);
                                        btn_setEndDate = view_setCustomDateBtns.findViewById(R.id.endBtn);

                                        btn_setStartDate.setOnClickListener(new View.OnClickListener() {
                                            public void onClick(View v) {
                                                DatePickerDialog datePickerDialog = new DatePickerDialog(view.getContext(), mDateSetListener_start,
                                                        Calendar.getInstance().get(Calendar.YEAR),
                                                        Calendar.getInstance().get(Calendar.MONTH),
                                                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
                                                datePickerDialog.show();
                                            }
                                        });

                                        btn_setEndDate.setOnClickListener(new View.OnClickListener() {
                                            public void onClick(View v) {
                                                if(temp_startdate!=null){
                                                    DatePickerDialog datePickerDialog = new DatePickerDialog(view.getContext(), mDateSetListener_end,
                                                            temp_startdate.getYear(),
                                                            temp_startdate.getMonthValue()-1,
                                                            temp_startdate.getDayOfMonth());
                                                    datePickerDialog.show();
                                                }
                                                else{
                                                    DatePickerDialog datePickerDialog = new DatePickerDialog(view.getContext(), mDateSetListener_end,
                                                            Calendar.getInstance().get(Calendar.YEAR),
                                                            Calendar.getInstance().get(Calendar.MONTH),
                                                            Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
                                                    datePickerDialog.show();
                                                }
                                            }
                                        });

                                        mDateSetListener_start = new DatePickerDialog.OnDateSetListener() {
                                            @RequiresApi(api = Build.VERSION_CODES.O)
                                            @Override
                                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                                month = month + 1;

                                                String month_string = String.valueOf(month);
                                                String day_string = String.valueOf(day);
                                                if (month_string.length() < 2){
                                                    month_string = "0" + month_string;
                                                }
                                                if (day_string.length() < 2){
                                                    day_string = "0" + day_string;
                                                }
                                                String date = year + "-" + month_string + "-" + day_string;
                                                temp_startdate = LocalDate.parse(date);
                                                btn_setStartDate.setText(temp_startdate.format(formatter));

                                            }
                                        };

                                        mDateSetListener_end = new DatePickerDialog.OnDateSetListener() {
                                            @RequiresApi(api = Build.VERSION_CODES.O)
                                            @Override
                                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                                month = month + 1;

                                                String month_string = String.valueOf(month);
                                                String day_string = String.valueOf(day);
                                                if (month_string.length() < 2){
                                                    month_string = "0" + month_string;
                                                }
                                                if (day_string.length() < 2){
                                                    day_string = "0" + day_string;
                                                }
                                                String date = year + "-" + month_string + "-" + day_string;
                                                temp_enddate = LocalDate.parse(date);
                                                btn_setEndDate.setText(temp_enddate.format(formatter));
                                            }
                                        };

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

                                                    setPaymentTypeText(startdate_this,enddate_this);
                                                    setDatesTv();

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



    public void onClick_GoBackBtn () {
        //Button to go back to settings
        btn1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ChartFragment frag= new ChartFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, frag, "piechartFrag")
                        .commit();
            }
        });
    }


}
