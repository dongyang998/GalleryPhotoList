package com.example.dyang.galleryphotolist;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * adapter of the ListView
 */
public class ListViewAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<ArrayList<Photo>> mPhotos;
    private ArrayList<String> mDates;

    public ListViewAdapter(Context context, PhotosContainer photosContainer) {
        mContext = context;
        if (photosContainer != null) {
            mPhotos = photosContainer.getPhotos();
            mDates = photosContainer.getDates();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listViewItem;

        if (mDates == null) {
            return null;
        }

        // Create a view for the ListView item
        if (convertView != null) {
            listViewItem = convertView;
        } else {
            listViewItem = LayoutInflater.from(mContext).inflate(R.layout.listview_item, null);
        }

        // set the date in the textView
        TextView textView = (TextView)listViewItem.findViewById(R.id.listView_item_date);
        textView.setText(mDates.get(position));

        // set the adapter of gridView
        GridView gridView = (GridView)listViewItem.findViewById(R.id.listView_item_gridView);
        // if the list view item is reused, cancel the old sync tasks,
        if (listViewItem.getTag() != null && (int)listViewItem.getTag() != position) {
            View      gridViewItem;
            ImageView imageView;
            for (int i = 0; i < gridView.getChildCount(); i++) {
                gridViewItem = gridView.getChildAt(i);
                imageView = (ImageView)gridViewItem.findViewById(R.id.gridView_item_photo);

                if (imageView.getTag(GridViewAdapter.TagKeyTask) != null) {
                    GridViewAdapter.AsyncLoadThumbnail thumbnailTask;
                    try {
                        thumbnailTask = (GridViewAdapter.AsyncLoadThumbnail)imageView.getTag(GridViewAdapter.TagKeyTask);
                        thumbnailTask.cancel(true);
                        Log.i("ListViewAdapter getView", "Cancel load thumbnail.");
                    } catch (Exception e) {
                        if (GlobalSettings.Debug) {
                            Log.e("ListViewAdapter getView", "Can't convert the tag of the GridView item to an AsyncTask");
                        }
                    }
                } else {
                    Log.i("ListViewAdapter getView", "GridView item's tag is null.");
                }

                // set tag to null to release syncTask and photo objects
                imageView.setTag(null);
                // set the image source to null to release the image
                imageView.setImageBitmap(null);
            }
        }

        // Set the tag to the list view position to identify it is reused or not.
        listViewItem.setTag(position);
        GridViewAdapter gridViewAdapter = new GridViewAdapter(mContext, mPhotos.get(position));

        // Release strong reference when a view is recycled
        gridView.setRecyclerListener(new AbsListView.RecyclerListener() {
            @Override
            public void onMovedToScrapHeap(View view) {
                if (GlobalSettings.Debug) {
                    Log.i("gridView recycler listener", "Release the image in the GridViw.");
                }

                // set the image source to null to release the image
                ImageView imageView = (ImageView)view.findViewById(R.id.gridView_item_photo);
                imageView.setImageBitmap(null);

                // cancel the sync task
                if (imageView.getTag(GridViewAdapter.TagKeyTask) != null) {
                    GridViewAdapter.AsyncLoadThumbnail thumbnailTask;
                    try {
                        thumbnailTask = (GridViewAdapter.AsyncLoadThumbnail)imageView.getTag(GridViewAdapter.TagKeyTask);
                        thumbnailTask.cancel(true);
                        Log.i("gridView recycler listener", "Cancel load thumbnail.");
                    } catch (Exception e) {
                        if (GlobalSettings.Debug) {
                            Log.e("gridView recycler listener", "Can't convert the tag of the GridView item to an AsyncTask");
                        }
                    }
                }

                // release the sync task and photo objects
                imageView.setTag(null);
                if (imageView.getTag(GridViewAdapter.TagKeyTask) != null || imageView.getTag(GridViewAdapter.TagKeyPhoto) != null) {
                    Log.i("gridView recycler listener", "Can't release the task and photo.");
                }
            }
        });

        //gridView.setOnItemClickListener();

        gridView.setAdapter(gridViewAdapter);

        return listViewItem;
    }

    public int getCount() {
        return mDates != null ? mDates.size() : 0;
    }

    public Object getItem(int position) {
        return mDates != null ? mDates.get(position) : null;
    }

    public long getItemId(int position) {
        return position;
    }

    public void updateResults(PhotosContainer photosContainer) {

        if (photosContainer != null) {
            mPhotos = photosContainer.getPhotos();
            mDates = photosContainer.getDates();
        } else {
            mPhotos = null;
            mDates = null;
        }

        // Triggers the list update
        notifyDataSetChanged();
    }
}
