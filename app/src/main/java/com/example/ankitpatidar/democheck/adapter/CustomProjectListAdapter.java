package com.example.ankitpatidar.democheck.adapter;

import android.app.Activity;

import android.content.Intent;

import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;

import com.example.ankitpatidar.democheck.R;
import com.example.ankitpatidar.democheck.database.Project;
import com.example.ankitpatidar.democheck.model.ListItemHolder;
import com.example.ankitpatidar.democheck.view.DetailActivity;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


import java.io.IOException;

import java.util.ArrayList;

/**
 * Created by ankitpatidar on 29/10/17.
 *
 * CustomProjectListAdapter is an Recycler Adapter with RecyclerViewHolder inside in it only.
 * It is use to have Dynamic allocation of memory at run time when that cell is see on screen.
 *
 * I have implemented WebCrawling code in adapter only,
 * so that calling to web crawler is at runtime when that cell is on screen because of recycler view property.
 */

public class CustomProjectListAdapter extends RecyclerView.Adapter<CustomProjectListAdapter.ViewHolder> {

    private Activity activity;
    private ArrayList<ListItemHolder> listItemHolders;

    public CustomProjectListAdapter(Activity activity, ArrayList<ListItemHolder> listItemHolders) {
        this.activity = activity;
        this.listItemHolders = listItemHolders;


    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_list_item, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        //Setting up the data in list items
        holder.titleText.setText(listItemHolders.get(position).getTitle());
        String ownedByString = "Owned By: " + listItemHolders.get(position).getBy();
        holder.ownedByText.setText(ownedByString);

        String endTimeString = "End: " + listItemHolders.get(position).getEndTime().substring(0, listItemHolders.get(position).getEndTime().indexOf('T'));
        holder.endTimeText.setText(endTimeString);

        String amountTextString = "Amount: " + String.valueOf(listItemHolders.get(position).getAmtPledged()) + " " + listItemHolders.get(position).getCurrency();
        holder.amountText.setText(amountTextString);


        //check for thumbnail image url, if exist or not in arraylist coming from database
        //if image url already there in database, then it directly displayed it through picasso library.
        if (listItemHolders.get(position).getImg_url() != null && listItemHolders.get(position).getImg_url().equalsIgnoreCase("null")) {

            //if image url not exist in database, then background thread call for web crawling.
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String url = "https://www.kickstarter.com" + listItemHolders.get(position).getUrl();
                        Document document = Jsoup.connect(url).get();

                        Element img = document.getElementsByClass("js-feature-image").first();

                        if (img != null) {
                            final String imgUrl = img.attr("src");

                            listItemHolders.get(position).setImg_url(imgUrl);


                            //Update project data with image url in database when crawled from html page
                            Project project = new Project();
                            project.project_sno = listItemHolders.get(position).getSNo();
                            project.project_amt_pledged = listItemHolders.get(position).getAmtPledged();
                            project.project_blurb = listItemHolders.get(position).getBlurb();
                            project.project_by = listItemHolders.get(position).getBy();
                            project.project_country = listItemHolders.get(position).getCountry();
                            project.project_currency = listItemHolders.get(position).getCurrency();
                            project.project_end_time = listItemHolders.get(position).getEndTime();
                            project.project_location = listItemHolders.get(position).getLocation();
                            project.project_num_backers = listItemHolders.get(position).getNumBackers();
                            project.project_percentage = listItemHolders.get(position).getPercentageFunded();
                            project.project_state = listItemHolders.get(position).getState();
                            project.project_title = listItemHolders.get(position).getTitle();
                            project.project_type = listItemHolders.get(position).getType();
                            project.project_url = listItemHolders.get(position).getUrl();

                            project.update();

                            System.out.println("Image Url" + imgUrl);

                            //UI thread to show image in imageview throung picasso library
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Picasso.with(activity).load(imgUrl).error(R.drawable.placeholder_thumbnail).placeholder(R.drawable.placeholder_thumbnail).into(holder.thumbnailImage);

                                }
                            });
                        } else
                            //UI thread to show image in imageview throung picasso library

                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Picasso.with(activity).load("jkadsjf.com").error(R.drawable.placeholder_thumbnail).placeholder(R.drawable.placeholder_thumbnail).into(holder.thumbnailImage);

                                }
                            });

                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }
            }).start();
        } else {
            Picasso.with(activity).load(listItemHolders.get(position).getImg_url()).error(R.drawable.placeholder_thumbnail).placeholder(R.drawable.placeholder_thumbnail).into(holder.thumbnailImage);


        }


        //Item click listener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //For making animation on thumbnail image
                ActivityOptionsCompat compat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, holder.thumbnailImage, "shared");

                Intent intent = new Intent(activity, DetailActivity.class);
                intent.putExtra("Data", listItemHolders.get(position));

                activity.startActivity(intent, compat.toBundle());
            }
        });


    }

    @Override
    public int getItemCount() {
        return listItemHolders.size();
    }


    //ViewHolder for storing views, so that it can be used in next items view.
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView ownedByText;
        TextView amountText;
        TextView endTimeText;

        ImageView thumbnailImage;

        ViewHolder(View view) {
            super(view);
            titleText = view.findViewById(R.id.titleTextView);
            ownedByText = view.findViewById(R.id.ownedByTextView);
            amountText = view.findViewById(R.id.amountSpended);
            endTimeText = view.findViewById(R.id.endTime);
            thumbnailImage = view.findViewById(R.id.projectThumbnail);
        }
    }
}
