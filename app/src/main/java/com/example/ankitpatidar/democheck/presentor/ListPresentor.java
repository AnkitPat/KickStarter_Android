package com.example.ankitpatidar.democheck.presentor;

import android.util.Log;

import com.example.ankitpatidar.democheck.database.Project;
import com.example.ankitpatidar.democheck.interfaces.CallingInterface;
import com.example.ankitpatidar.democheck.model.ListItemHolder;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;


/**
 * Created by ankitpatidar on 28/10/17.
 * <p>
 * It is presentor class for project, it is using in dealing with view and network, database calling.
 * <p>
 * Flow can be as:
 * <p>
 * View -> Presentor -> through interface -> view
 * <p>
 * in this flow data is fetched and back to view
 */

public class ListPresentor {


    DataFetcher dataFetcher;

    public ListPresentor(DataFetcher dataFetcher) {
        this.dataFetcher = dataFetcher;
    }

    //Method for storing data come from server in database.
    public void moveToDatabase(ArrayList<ListItemHolder> listItemHolders) {

        int maxBacker = 0;
        for (ListItemHolder holder : listItemHolders) {
            Project project = new Project();
            project.project_sno = holder.getSNo();
            project.project_amt_pledged = holder.getAmtPledged();
            project.project_blurb = holder.getBlurb();
            project.project_by = holder.getBy();
            project.project_country = holder.getCountry();
            project.project_currency = holder.getCurrency();
            project.project_end_time = holder.getEndTime();
            project.project_location = holder.getLocation();
            project.project_num_backers = holder.getNumBackers();
            try {
                if (Integer.parseInt(project.project_num_backers) > maxBacker)
                    maxBacker = Integer.parseInt(project.project_num_backers);
            } catch (NumberFormatException e) {
                e.printStackTrace();

            }
            project.project_percentage = holder.getPercentageFunded();
            project.project_state = holder.getState();
            project.project_title = holder.getTitle();
            project.project_type = holder.getType();
            project.project_url = holder.getUrl();

            project.img_url = holder.getImg_url();

            project.save();

        }

        //interface that let max backer count to main activity
        dataFetcher.movedToLocalDatabase(maxBacker);
    }

    //Method for fetching All data from database.
    public ArrayList<ListItemHolder> fetchAllData() {
        List<Project> projectList = new Select().from(Project.class).queryList();

        return convertProjectToHolder(projectList);
    }


    //Method for calling database to return sorted data.
    public ArrayList<ListItemHolder> fetchSortedDataFromDatabase(String columnName, boolean order) {
        List<Project> projectList = new Select().from(Project.class).where().orderBy(order, columnName).queryList();

        return convertProjectToHolder(projectList);
    }


    //Method for calling database with where condition for taking data in form of range
    public ArrayList<ListItemHolder> fetchRangedDataFromDatabase(int minValue, int maxValur) {
        List<Project> projectList = new Select().from(Project.class).where("project_num_backers between " + minValue + " and " + maxValur).queryList();

        return convertProjectToHolder(projectList);
    }

    //Method fro calling database with where condition for fetching data in form of chunks of 20-20 from database.
    public ArrayList<ListItemHolder> fetchTwentyFromDatabase(int start, int end) {

        List<Project> projectList = new Select().from(Project.class).where("project_sno>=" + start + " and project_sno<=" + end).queryList();

        return convertProjectToHolder(projectList);

    }


    //Helping method for converting Project(Database holder) to ListItemHolder(List Holder)
    public ArrayList<ListItemHolder> convertProjectToHolder(List<Project> projects) {

        ArrayList<ListItemHolder> listItemHolders = new ArrayList<>();
        for (Project project : projects) {
            ListItemHolder listItemHolder = new ListItemHolder();
            listItemHolder.setTitle(project.project_title);
            listItemHolder.setAmtPledged(project.project_amt_pledged);
            listItemHolder.setBlurb(project.project_blurb);
            listItemHolder.setBy(project.project_by);
            listItemHolder.setCountry(project.project_country);
            listItemHolder.setCurrency(project.project_currency);
            listItemHolder.setEndTime(project.project_end_time);
            listItemHolder.setLocation(project.project_location);
            listItemHolder.setNumBackers(project.project_num_backers);
            listItemHolder.setPercentageFunded(project.project_percentage);
            listItemHolder.setSNo(project.project_sno);
            listItemHolder.setState(project.project_state);
            listItemHolder.setType(project.project_type);
            listItemHolder.setUrl(project.project_url);
            if (project.img_url == null) {
                listItemHolder.setImg_url("null");
            } else
                listItemHolder.setImg_url(project.img_url);
            listItemHolders.add(listItemHolder);
        }

        return listItemHolders;

    }

    /**
     * Actual fetching of data from server, i used Retrofit library here for making network calling.
     * and on success it return with data in form of List<ListItemHolder> and then interface let it
     * transfered to view activity.
     */

    public void fetchDataFromServer(String url) {
        
        RetrofitUtil retrofitUtil = new RetrofitUtil();
        CallingInterface service = retrofitUtil.getRetrofitService(url);
        
        //dataFetcher.fetchDataFromServer(service.getListItemModel());

        service.getListItemModel().enqueue(new Callback<List<ListItemHolder>>() {
            @Override
            public void onResponse(Response<List<ListItemHolder>> response, Retrofit retrofit) {
                ArrayList<ListItemHolder> listItemHolders = new ArrayList<>(response.body());
                dataFetcher.fetchDataFromServer(listItemHolders);


            }

            @Override
            public void onFailure(Throwable t) {
                Log.v("Exception", t.getMessage());
            }
        });

    }


    //Interface that connect view and presentor
    public interface DataFetcher {


        ArrayList<ListItemHolder> fetchDataFromServer(ArrayList<ListItemHolder> listItemHolders);

        void movedToLocalDatabase(int maxBacker);

    }
}
