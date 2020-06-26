package com.example.moneymanagement3.ui.setting;

import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.moneymanagement3.DataBaseHelper;
import com.example.moneymanagement3.R;

import java.util.ArrayList;

public class ManageCycFragment extends Fragment {
    View view;
    DataBaseHelper myDb;
    Cursor res;
    Cursor res2;
    ListView lv;
    ArrayList<String> categories;
    String[] managecat_items;
    ArrayAdapter<String> adapter_managecat;
    Button btn1;
    CharSequence[] categories_list;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_managecat, container, false);
        view.setBackgroundColor(Color.WHITE);

        myDb = new DataBaseHelper(getActivity());
        //Cursor res = gets all data in the database table2
        res2 = myDb.getAllData_categories();

        btn1 = view.findViewById(R.id.gobackBtn);
        lv = view.findViewById(R.id.manageCatLv);

        managecat_items = new String[]{"Select Monthly Cycle Start Day", "Delete Previous Cycles", "Delete All Cycles"};
        adapter_managecat = new ArrayAdapter<String>(view.getContext(), R.layout.managecat_listview_text, R.id.managecat_item, managecat_items);
        lv.setAdapter(adapter_managecat);


//        onClick_itemselectedLv();
//        onClick_GoBackBtn ();


        return view;
    }

////////////////////////////////////////////////////////////////////////////////////////////////////

//    public void onClick_itemselectedLv() {
//        //Delete/edit selected items in the manageCat listview by selecting an item in the list view
//        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            public void onItemClick(AdapterView<?> a, View v, final int position, long id) {
//
//                //if "categories to delete" is selected
//                if (position == 0) {
//                    //creates the categories arraylist from database table2
//                    categories = new ArrayList<String>();
//                    while (res2.moveToNext()) {
//                        String category = res2.getString(1); //from database table2
//                        categories.add(category);
//                    }
//
//                    //creates boolean array of falses
//                    final boolean[] bool_list = new boolean[categories.size()];
//                    //converts categories arraylist to char sequence array
//                    categories_list = new CharSequence[categories.size()];
//                    for (int i = 0; i < categories.size(); i++) {
//                        categories_list[i] = categories.get(i);
//                    }
//
//                    //if there are no categories, show an alert saying "there are no categories"
//                    if (categories_list.length == 0) {
//                        AlertDialog.Builder builder0 = new AlertDialog.Builder(view.getContext());
//                        builder0.setTitle("Alert");
//                        builder0.setMessage("There are no categories");
//                        builder0.setPositiveButton("Okay", null);
//                        builder0.show();
//                    }
//
//                    //otherwise
//                    else {
//                        //create an alert dialog1 builder - "Manage Categories"
//                        AlertDialog.Builder builder1 = new AlertDialog.Builder(view.getContext());
//                        builder1.setTitle("Select Categories to Delete");
//                        builder1.setPositiveButton("Delete", null);
//                        builder1.setNeutralButton("Cancel", null);
//
//                        //set the checkbox listview
//                        builder1.setMultiChoiceItems(categories_list, bool_list,
//                                new DialogInterface.OnMultiChoiceClickListener() {
//                                    @Override
//                                    // indexSelected contains the index of item (of which checkbox checked)
//                                    //checks and unchecks the boxes when clicked
//                                    public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
//                                        bool_list[indexSelected] = isChecked;
//                                        String current_item = categories_list[indexSelected].toString();
//                                    }
//                                });
//                        //creates the alert dialog from the builder
//                        final AlertDialog alertDialog = builder1.create();
//
//                        //display the alert dialog with the buttons
//                        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                            @Override
//                            public void onShow(final DialogInterface dialog) {
//
//                                //delete button
//                                Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
//                                positiveButton.setOnClickListener(new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
//                                        //deletes the categories from database table2 if the boxes are checked
//                                        for (int i = 0; i < categories_list.length; i++) {
//                                            boolean checked = bool_list[i];
//                                            if (checked) {
//                                                res2.moveToPosition(i); //move to correct row in table2
//                                                String category_id = res2.getString(0); //get the ID of category
//                                                myDb.delete_categories(category_id);
//                                            }
//                                        }
//                                        //recreates SettingFragment so the checkbox list appears again after alertdialog closes
//                                        getFragmentManager()
//                                                .beginTransaction()
//                                                .detach(ManageCatFragment.this)
//                                                .attach(ManageCatFragment.this)
//                                                .commit();
//                                        dialog.dismiss();
//                                    }
//                                });
//
//                                //Cancel button
//                                Button neutralButton = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
//                                neutralButton.setOnClickListener(new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
//                                        //recreates SettingFragment so the checkbox list appears again after alertdialog closes
//                                        getFragmentManager()
//                                                .beginTransaction()
//                                                .detach(ManageCatFragment.this)
//                                                .attach(ManageCatFragment.this)
//                                                .commit();
//                                        dialog.dismiss();
//                                    }
//                                });
//                            }
//                        });
//                        alertDialog.show();
//                    }
//                }
//
//                //if "delete all categories" is selected
//                else {
//
//                    ArrayList<String> arr = new ArrayList<String>();
//                    while (res2.moveToNext()){
//                        arr.add(res2.getString(1));
//                    }
//                    //if there are no categories, show an alert saying "there are no categories"
//                    if (arr.isEmpty()) {
//                        AlertDialog.Builder builder0 = new AlertDialog.Builder(view.getContext());
//                        builder0.setTitle("Alert");
//                        builder0.setMessage("There are no categories");
//                        builder0.setPositiveButton("Okay", null);
//                        builder0.show();
//                    }
//                    //otherwise delete all
//                    else {
//                        AlertDialog.Builder adb = new AlertDialog.Builder(view.getContext());
//                        adb.setTitle("Alert");
//                        adb.setMessage("Are you sure you want to delete all categories?");
//                        adb.setNeutralButton("Cancel", new AlertDialog.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//
//                                //recreates SettingFragment so the checkbox list appears again after alertdialog closes
//                                getFragmentManager()
//                                        .beginTransaction()
//                                        .detach(ManageCatFragment.this)
//                                        .attach(ManageCatFragment.this)
//                                        .commit();
//
//                            }
//                        });
//                        adb.setPositiveButton("Yes", new AlertDialog.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//                                //deletes all categories in database table2 and restores to default
//                                int value = myDb.deleteAll_categories();
//                                if(value > 0)
//                                    Toast.makeText(view.getContext(),"Deleted all categories",Toast.LENGTH_SHORT).show();
//                                else
//                                    Toast.makeText(view.getContext(),"Data not Deleted",Toast.LENGTH_SHORT).show();
//
//                            }
//                        });
//                        adb.show();
//                    }
//
//                }
//            }
//        });
//    }
//
//
//    public void onClick_GoBackBtn () {
//        //Button to go back to settings
//        btn1.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                SettingFragment frag= new SettingFragment();
//                getActivity().getSupportFragmentManager().beginTransaction()
//                        .replace(R.id.fragment_container, frag, "settingFrag")
//                        .commit();
//            }
//        });
//    }
}
