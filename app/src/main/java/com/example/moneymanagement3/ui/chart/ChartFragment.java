package com.example.moneymanagement3.ui.chart;

import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.moneymanagement3.DataBaseHelper;
import com.example.moneymanagement3.R;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;

public class ChartFragment extends Fragment {

    //cycle updater variables
    DataBaseHelper myDb;
    Cursor res3;
    Cursor res4; Cursor res2;
    LocalDate startdate;
    LocalDate enddate;
    LocalDate currentDate;
    String cycle_input;
    ArrayList<String> cycles;
    Spinner spinner_cycles;
    /////

    @RequiresApi(api = Build.VERSION_CODES.O)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chart, container, false);

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

            String formatted_dates = cyc_startdate_formatted + " - " + cyc_enddate_formatted;
            cycles.add(formatted_dates);
        }
        Collections.reverse(cycles);
        ArrayAdapter<String> spn_cyc_adapter = new ArrayAdapter<String>(view.getContext(), R.layout.spinner_text, cycles);
        spinner_cycles.setAdapter(spn_cyc_adapter);

        //------------------------------------------------END-----------------------------------------------//


        return view;
    }


//////////////////////////////////////////////////////////////////////////////////////////////////////


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
}