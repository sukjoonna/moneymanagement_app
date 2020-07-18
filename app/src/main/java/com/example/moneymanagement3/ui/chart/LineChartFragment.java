package com.example.moneymanagement3.ui.chart;

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
import android.widget.Spinner;

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
import java.util.Collections;

public class LineChartFragment extends Fragment {
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
    LineChart lineChart;
    LineData lineData;
    LineDataSet lineDataSet;
    ArrayList lineEntries;
    ArrayList LineEntryLabels;
    /////

    @RequiresApi(api = Build.VERSION_CODES.O)
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_linechart, container, false);
        view.setBackgroundColor(Color.WHITE);
        btn1 = view.findViewById(R.id.gobackBtn);

        myDb = new DataBaseHelper(getActivity());

        //get database
        myDb = new DataBaseHelper(getActivity());

        //------------------------CYCLE CREATE AND UPDATER in DB (ALONG WITH SPINNER) -------------------------//                   *Make sure this is at top
        res3 = myDb.get_setting();
        res3.moveToFirst();

        //Cycle updater
        cycle_updater();

        spinner_cycles = view.findViewById(R.id.cycleSpn);

        //Create Cycle Spinner
        cycles = new ArrayList<String>();
        res4 = myDb.get_cycles();
        while (res4.moveToNext()) {
            String cyc_startdate = res4.getString(0);
            String cyc_enddate = res4.getString(1);
            LocalDate cyc_startdate_localdate = LocalDate.parse(cyc_startdate);
            LocalDate cyc_enddate_localdate = LocalDate.parse(cyc_enddate);

            //Formatting the localdate ==> custom string format (Month name dd, yyyy)
            DateTimeFormatter cyc_formatter = DateTimeFormatter.ofPattern("LLL dd, yy");
            String cyc_startdate_formatted = cyc_startdate_localdate.format(cyc_formatter);
            String cyc_enddate_formatted = cyc_enddate_localdate.format(cyc_formatter);

            String formatted_dates = cyc_startdate_formatted + " ~ " + cyc_enddate_formatted;
            cycles.add(formatted_dates);
        }
        Collections.reverse(cycles);
        ArrayAdapter<String> spn_cyc_adapter = new ArrayAdapter<String>(view.getContext(), R.layout.spinner_text, cycles);
        spinner_cycles.setAdapter(spn_cyc_adapter);
        //------------------------------------------------END-----------------------------------------------//

        //Line Chart
        lineChart = view.findViewById(R.id.lineChart);
        startdate = LocalDate.parse(res3.getString(0));
        enddate = LocalDate.parse(res3.getString(1));
        lineChartMaker(startdate,enddate);

        ////////////////////////
        //What the spinner does when item is selected / not selected
        spinner_cycles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View v, final int position, long id) {
                //this is important bc "cycles"/spinner shows new-->old, but in the database table4, it's indexed old--->new
                int inverted_pos = (cycles.size() - 1) - position;
                res4.moveToPosition(inverted_pos);
                startdate = LocalDate.parse(res4.getString(0));
                enddate = LocalDate.parse(res4.getString(1));
                lineChartMaker(startdate,enddate);
                Log.d("onselect", "onItemSelected: ");
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //Object item = adapterView.getItemAtPosition(0);
                res3.moveToFirst();
                startdate = LocalDate.parse(res3.getString(0));
                enddate = LocalDate.parse(res3.getString(1));
                lineChartMaker(startdate,enddate);
                Log.d("noneselect", "onNothingSelected: ");

            }
        });
        /////////////////////////////////
        onClick_GoBackBtn();

        return view;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////



    @RequiresApi(api = Build.VERSION_CODES.O)
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

    @RequiresApi(api = Build.VERSION_CODES.O)

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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getEntries(LocalDate startDate,LocalDate endDate) {
        Cursor table3Res = myDb.get_setting();
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
