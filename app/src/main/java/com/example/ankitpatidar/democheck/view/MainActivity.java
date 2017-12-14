package com.example.ankitpatidar.democheck.view;

import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.example.ankitpatidar.democheck.adapter.CustomProjectListAdapter;
import com.example.ankitpatidar.democheck.model.ListItemHolder;

import com.example.ankitpatidar.democheck.R;
import com.example.ankitpatidar.democheck.presentor.ListPresentor;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.yahoo.mobile.client.android.util.rangeseekbar.RangeSeekBar;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * MainActivity i.e. view activity, that is used as main class for holding up view.
 *
 * I used recycler view for displaying list.
 *
 * Swipe refresh layout for fetching next 20 bunch data from database
 *
 * MainActivity holds only view related code, and for database and server related code,
 * it calls Presentor class. Which respond throung interface with data.
 *
 * I used DB Flow database for handling database in this application.
 *
 *
 */


public class MainActivity extends AppCompatActivity implements ListPresentor.DataFetcher {

    //Main Recycler List view variable
    RecyclerView listView;

    //AlertDialog for showing progress for data fetching from server and database
    AlertDialog alertDialog;

    //AlertDialog Builder
    AlertDialog.Builder dialogBuilder;

    //TextView of progress text
    TextView progressBarTextView;

    //Presentor Class Object
    private ListPresentor listPresentor;

    //Custom List Adapter for showing list item in list view.
    CustomProjectListAdapter customProjectListAdapter;

    //Main Item list holder
    ArrayList<ListItemHolder> mainListItemHolder;

    //Start point for list displaying by S.no
    int start_point = 0;

    //Capacity to display on single Scroll, it can be changeable
    int capacity = 19;

    //Swipe refresh layout for fetching next data set
    private SwipyRefreshLayout mSwipyRefreshLayout;

    //Scroll position is stored to reterive to position where is refresh to get next data set
    int scrollPosition = 0;

    //Extra functionality layout
    LinearLayout searchLayout, sortLayout, filterLayout;

    //Max backers value, used in filter
    private int maxBackers = 100;

    //Max and min range value for filter
    int minRangeValue, maxRangeValue;

    //Image for no data, when no data is avialable to display.
    ImageView noDataImage;

    //flags for sorting functionality
    boolean alphaUpSelect = false, alphaDownSelect = false, endUpSelect = false, endDownSelect = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (RecyclerView) findViewById(R.id.listView);
        listView.setLayoutManager(new LinearLayoutManager(this));

        mSwipyRefreshLayout = (SwipyRefreshLayout) findViewById(R.id.swipyrefreshlayout);


        searchLayout = (LinearLayout) findViewById(R.id.searchLinear);
        sortLayout = (LinearLayout) findViewById(R.id.sortingLinear);
        filterLayout = (LinearLayout) findViewById(R.id.filterLayout);


        noDataImage = (ImageView) findViewById(R.id.noData);


        showProgressDialog();

        mainListItemHolder = new ArrayList<>();
        listPresentor = new ListPresentor(this);


        FlowManager.init(this);


        //if network available fetch from server, else from database
        if (isNetworkAvailable(this)) {
            listPresentor.fetchDataFromServer("http://starlord.hackerearth.com/");

        } else {
            new DataPopulator().execute();
            hideProgressDialog();
        }


        //Swipe refresh layout to fetch next data set
        mSwipyRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {

                if (!expandedSearch && !expandedFilter && !expandedSort) {
                    scrollPosition = start_point;
                    new DataPopulator().execute();

                    if (mSwipyRefreshLayout.isRefreshing())
                        mSwipyRefreshLayout.setRefreshing(false);
                }
            }
        });


        //Exit transition animation, going from list to detail view
        TransitionInflater inflater = TransitionInflater.from(this);
        Transition transition = inflater.inflateTransition(R.transition.transition);
        getWindow().setSharedElementExitTransition(transition);


        filterFunctioning();


    }

    //Method for network connection checking
    public boolean isNetworkAvailable(Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }


    //Filter functionality method
    private void filterFunctioning() {

        //Min and mav text view
        final TextView minValueText = filterLayout.findViewById(R.id.minValue);
        final TextView maxValueText = filterLayout.findViewById(R.id.maxValue);

        //Seekbar initialisation
        RangeSeekBar<Integer> rangeSeekBar = filterLayout.findViewById(R.id.rangeSeekbar);

        rangeSeekBar.setRangeValues(0, maxBackers);
        rangeSeekBar.setNotifyWhileDragging(true);


        rangeSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {


                String minValueString = "Min Value: " + minValue;
                minValueText.setText(minValueString);
                String maxValueString = "Max Value: " + maxValue;
                maxValueText.setText(maxValueString);


                minRangeValue = minValue;

                maxRangeValue = maxValue;


                System.out.println(minValue + " " + maxValue);
            }
        });


        Button applybutton = filterLayout.findViewById(R.id.applyButton);
        applybutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                //Populate list view according to value selected in seekbar
                new FilterPopulator(minRangeValue, maxRangeValue).execute();
            }
        });
    }




    //Method for sort Functionality
    private void sortFunctioning() {

        final ImageView aplphaUp = sortLayout.findViewById(R.id.alphabeticallyUp);
        final ImageView aplphaDown = sortLayout.findViewById(R.id.alphabeticallyDown);

        final ImageView endUp = sortLayout.findViewById(R.id.timeUp);
        final ImageView endDown = sortLayout.findViewById(R.id.timeDown);


        //Sort click listener
        aplphaUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (alphaUpSelect) {
                    aplphaUp.setImageDrawable(getResources().getDrawable(R.drawable.black_up));

                    alphaDownSelect = false;

                } else {
                    aplphaUp.setImageDrawable(getResources().getDrawable(R.drawable.blue_up));
                    alphaUpSelect = true;

                    //code for displaying other sort,and maintain one sort function at a time.
                    if (alphaDownSelect) {
                        aplphaDown.setImageDrawable(getResources().getDrawable(R.drawable.black_down));
                        alphaDownSelect = false;
                    }
                    if (endUpSelect) {
                        endUp.setImageDrawable(getResources().getDrawable(R.drawable.black_up));
                        endUpSelect = false;
                    }
                    if (endDownSelect) {
                        endDown.setImageDrawable(getResources().getDrawable(R.drawable.black_down));
                        endDownSelect = false;
                    }

                    //Populate list according to sort input
                    new SortPopulator("project_title", true).execute();


                }
            }
        });

        aplphaDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (alphaDownSelect) {
                    aplphaDown.setImageDrawable(getResources().getDrawable(R.drawable.black_down));

                    alphaDownSelect = false;

                } else {
                    aplphaDown.setImageDrawable(getResources().getDrawable(R.drawable.blue_down));

                    alphaDownSelect = true;

                    //code for displaying other sort,and maintain one sort function at a time.
                    if (alphaUpSelect) {
                        aplphaUp.setImageDrawable(getResources().getDrawable(R.drawable.black_up));
                        alphaUpSelect = false;
                    }
                    if (endUpSelect) {
                        endUp.setImageDrawable(getResources().getDrawable(R.drawable.black_up));
                        endUpSelect = false;
                    }
                    if (endDownSelect) {
                        endDown.setImageDrawable(getResources().getDrawable(R.drawable.black_down));
                        endDownSelect = false;
                    }


                    //Populate list according to sort input
                    new SortPopulator("project_title", false).execute();
                }
            }
        });


        endUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (endUpSelect) {
                    endUp.setImageDrawable(getResources().getDrawable(R.drawable.black_up));

                    endUpSelect = false;
                } else {
                    endUp.setImageDrawable(getResources().getDrawable(R.drawable.blue_up));

                    endUpSelect = true;

                    //code for displaying other sort,and maintain one sort function at a time.
                    if (alphaDownSelect) {
                        aplphaDown.setImageDrawable(getResources().getDrawable(R.drawable.black_down));
                        alphaDownSelect = false;
                    }
                    if (alphaUpSelect) {
                        aplphaUp.setImageDrawable(getResources().getDrawable(R.drawable.black_up));
                        alphaUpSelect = false;
                    }
                    if (endDownSelect) {
                        endDown.setImageDrawable(getResources().getDrawable(R.drawable.black_down));
                        endDownSelect = false;
                    }


                    //Populate list according to sort input
                    new SortPopulator("project_end_time", true).execute();
                }
            }
        });

        endDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (endDownSelect) {
                    endDown.setImageDrawable(getResources().getDrawable(R.drawable.black_down));

                    endDownSelect = false;
                } else {
                    endDown.setImageDrawable(getResources().getDrawable(R.drawable.blue_down));

                    endDownSelect = true;

                    //code for displaying other sort,and maintain one sort function at a time.
                    if (alphaDownSelect) {
                        aplphaDown.setImageDrawable(getResources().getDrawable(R.drawable.black_down));
                        alphaDownSelect = false;
                    }
                    if (endUpSelect) {
                        endUp.setImageDrawable(getResources().getDrawable(R.drawable.black_up));
                        endUpSelect = false;
                    }
                    if (alphaUpSelect) {
                        aplphaUp.setImageDrawable(getResources().getDrawable(R.drawable.black_up));
                        alphaUpSelect = false;
                    }


                    //Populate list according to sort input
                    new SortPopulator("project_end_time", false).execute();
                }
            }
        });
    }


    ArrayList<ListItemHolder> dataList;


    /**
     * Search functionality,
     *
     * here i fetched whole data and then compare project title with char entered in edit text.
     */
    private void searchFunctioning() {
        dataList = new ArrayList<>();

        showProgressDialog();
        progressBarTextView.setText(R.string.fetch_search_list);

        new Thread(new Runnable() {
            @Override
            public void run() {
                dataList = listPresentor.fetchAllData();
                hideProgressDialog();

            }
        }).start();

        final EditText searchEdit = searchLayout.findViewById(R.id.searchEditText);
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                ArrayList<ListItemHolder> searched = new ArrayList<>();                  //It new arraylist contains element which come in search crietaria.

                Pattern p = Pattern.compile(charSequence + "\\w*$");                           //Pattern for searching.

                for (int j = 0; j < dataList.size(); j++) {
                    String text = dataList.get(j).getTitle();
                    Matcher m;
                    Matcher m1;
                    if (text.contains(" ")) {
                        m = p.matcher(text.substring(0, text.indexOf(" ")).toLowerCase());
                        m1 = p.matcher(text.substring(0, text.indexOf(" ")));
                    } else {
                        m = p.matcher(text.toLowerCase());
                        m1 = p.matcher(text);
                    }

                    if (m.matches() || m1.matches()) {                          //when match found in that element of arraylist.
                        searched.add(dataList.get(j));
                    }

                    //Showing searched Result
                    customProjectListAdapter = new CustomProjectListAdapter(MainActivity.this, searched);
                    listView.setAdapter(customProjectListAdapter);

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        searchEdit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                final int DRAWABLE_RIGHT = 2;


                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (searchEdit.getRight() - searchEdit.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // your action here

                        collapseSearch();
                        expandedSearch = false;
                        start_point = 0;
                        new DataPopulator().execute();

                        return true;
                    }
                }
                return false;
            }
        });

    }

    //Method for showing generalised progress dialog.
    public void showProgressDialog() {
        dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final ViewGroup viewGroup = null;
        View dialogView = inflater.inflate(R.layout.progress_dialog_layout, viewGroup, false);
        progressBarTextView = dialogView.findViewById(R.id.textViewProgress);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    //Method for hiding progress dialog
    public void hideProgressDialog() {

        alertDialog.dismiss();
    }


    //Override method that return ListItemHolders from Presentor class

    @Override
    public ArrayList<ListItemHolder> fetchDataFromServer(final ArrayList<ListItemHolder> listItemHolders) {
      //  progressBarTextView.setText(R.string.move_local_database);


        //Again calling presentor class method for moving data in database.
        new Thread(new Runnable() {
            @Override
            public void run() {
                listPresentor.moveToDatabase(listItemHolders);
            }
        }).start();
        
        return listItemHolders;

    }

    //Override method that return from Presentor class when data is stored in database
    @Override
    public void movedToLocalDatabase(int maxBacker) {

        maxBackers = maxBacker;

        new DataPopulator().execute();

        hideProgressDialog();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    boolean expandedSearch = false;
    boolean expandedSort = false;
    boolean expandedFilter = false;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                if (!expandedSearch) {
                    expandSearch();
                    searchFunctioning();
                    expandedSearch = true;
                } else {


                    collapseSearch();
                    expandedSearch = false;
                    start_point = 0;
                    new DataPopulator().execute();
                }

                break;
            case R.id.sort:
                if (!expandedSort) {
                    expandSort();
                    sortFunctioning();
                    expandedSort = true;
                } else {
                    collapseSort();
                    expandedSort = false;
                    start_point = 0;
                    new DataPopulator().execute();
                }
                break;
            case R.id.filter:
                if (!expandedFilter) {
                    expandFilter();
                    filterFunctioning();
                    expandedFilter = true;
                } else {
                    collapseFilter();
                    expandedFilter = false;
                    start_point = 0;
                    new DataPopulator().execute();
                }
                break;
        }
        return true;
    }

    //Hide search bar
    private void collapseSearch() {


        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 0, -100);
        translateAnimation.setDuration(800);


        searchLayout.setAnimation(translateAnimation);
        searchLayout.setVisibility(View.GONE);
    }

    //Show search bar
    private void expandSearch() {

        //Code for showing one bar at a time.
        if (expandedFilter) {
            collapseFilter();
            expandedFilter = false;
        }
        if (expandedSort) {
            collapseSort();
            expandedSort = false;
        }
        searchLayout.setVisibility(View.INVISIBLE);
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, -100, 0);
        translateAnimation.setDuration(800);
        translateAnimation.setFillAfter(true);

        searchLayout.setAnimation(translateAnimation);


    }

    //Hide Sort functionality bar
    private void collapseSort() {


        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 0, -100);
        translateAnimation.setDuration(800);


        sortLayout.setAnimation(translateAnimation);
        sortLayout.setVisibility(View.GONE);
    }


    //Show Sort funcionality bar
    private void expandSort() {


        //Code for showing one bar at a time.
        if (expandedFilter) {
            collapseFilter();
            expandedFilter = false;
        }
        if (expandedSearch) {
            collapseSearch();
            expandedSearch = false;
        }
        sortLayout.setVisibility(View.INVISIBLE);
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, -100, 0);
        translateAnimation.setDuration(800);
        translateAnimation.setFillAfter(true);

        sortLayout.setAnimation(translateAnimation);


    }

    //Hide Filter bar
    private void collapseFilter() {


        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 0, -100);
        translateAnimation.setDuration(800);


        filterLayout.setAnimation(translateAnimation);
        filterLayout.setVisibility(View.GONE);
    }

    //Show Filter bar
    private void expandFilter() {

        //Code for showing one bar at a time.
        if (expandedSearch) {
            collapseSearch();
            expandedSearch = false;
        }
        if (expandedSort) {
            collapseSort();
            expandedSort = false;
        }
        filterLayout.setVisibility(View.INVISIBLE);
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, -100, 0);
        translateAnimation.setDuration(800);
        translateAnimation.setFillAfter(true);

        filterLayout.setAnimation(translateAnimation);


    }



    //Asynch task to populate sorted data list
    private class SortPopulator extends AsyncTask<Void, Void, CustomProjectListAdapter> {
        String columnName;
        boolean order;

        SortPopulator(String columnName, boolean order) {
            this.columnName = columnName;
            this.order = order;
        }

        @Override
        protected void onPostExecute(CustomProjectListAdapter customProjectListAdapter) {
            super.onPostExecute(customProjectListAdapter);

            if (customProjectListAdapter.getItemCount() == 0) {
                noDataImage.setImageDrawable(getResources().getDrawable(R.drawable.nodata));
                noDataImage.setVisibility(View.VISIBLE);
                mSwipyRefreshLayout.setVisibility(View.GONE);

                hideProgressDialog();

            } else {
                noDataImage.setVisibility(View.GONE);
                mSwipyRefreshLayout.setVisibility(View.VISIBLE);
                listView.setAdapter(customProjectListAdapter);

            }
        }

        @Override
        protected CustomProjectListAdapter doInBackground(Void... voids) {

            ArrayList<ListItemHolder> listItemHolders = listPresentor.fetchSortedDataFromDatabase(columnName, order);
            customProjectListAdapter = new CustomProjectListAdapter(MainActivity.this, listItemHolders);


            return customProjectListAdapter;
        }
    }


    //Asynch task for populating Filtered data
    private class FilterPopulator extends AsyncTask<Void, Void, CustomProjectListAdapter> {
        int minValue;
        int maxValue;

        FilterPopulator(int minValue, int maxValue) {
            this.minValue = minValue;
            this.maxValue = maxValue;
        }

        @Override
        protected void onPostExecute(CustomProjectListAdapter customProjectListAdapter) {
            super.onPostExecute(customProjectListAdapter);
            if (customProjectListAdapter.getItemCount() == 0) {
                noDataImage.setImageDrawable(getResources().getDrawable(R.drawable.nodata));
                noDataImage.setVisibility(View.VISIBLE);
                mSwipyRefreshLayout.setVisibility(View.GONE);
                hideProgressDialog();
            } else {
                noDataImage.setVisibility(View.GONE);
                mSwipyRefreshLayout.setVisibility(View.VISIBLE);
                listView.setAdapter(customProjectListAdapter);

            }
        }

        @Override
        protected CustomProjectListAdapter doInBackground(Void... voids) {
            ArrayList<ListItemHolder> listItemHolders = listPresentor.fetchRangedDataFromDatabase(minValue, maxValue);
            customProjectListAdapter = new CustomProjectListAdapter(MainActivity.this, listItemHolders);


            return customProjectListAdapter;
        }
    }


    //Asynch task for populating data  in form of chuncks
    private class DataPopulator extends AsyncTask<Void, Void, CustomProjectListAdapter> {
        @Override
        protected CustomProjectListAdapter doInBackground(Void... voids) {
            ArrayList<ListItemHolder> listItemHolders = listPresentor.fetchTwentyFromDatabase(start_point, start_point + capacity);


            mainListItemHolder.addAll(listItemHolders);
            start_point = start_point + capacity + 1;
            customProjectListAdapter = new CustomProjectListAdapter(MainActivity.this, mainListItemHolder);

            return customProjectListAdapter;
        }

        @Override
        protected void onPostExecute(CustomProjectListAdapter customProjectListAdapter) {
            super.onPostExecute(customProjectListAdapter);
            if (customProjectListAdapter.getItemCount() == 0) {
                noDataImage.setImageDrawable(getResources().getDrawable(R.drawable.nodata));
                noDataImage.setVisibility(View.VISIBLE);
                mSwipyRefreshLayout.setVisibility(View.GONE);
                hideProgressDialog();
            } else {
                noDataImage.setVisibility(View.GONE);
                mSwipyRefreshLayout.setVisibility(View.VISIBLE);
                listView.setAdapter(customProjectListAdapter);

            }
            listView.scrollToPosition(scrollPosition - 1);
        }
    }
}
