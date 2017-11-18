package com.example.dyang.galleryphotolist;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.content.Loader;
import android.app.LoaderManager;
import android.view.View;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;

public class MainActivity extends ActionBarActivity {

    // adapter for ListView
    private ListViewAdapter mListViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ListView photoWall;
        photoWall = (ListView)this.findViewById(R.id.photoWall);

        /*
        photoWall.setRecyclerListener(new AbsListView.RecyclerListener() {
            @Override
            public void onMovedToScrapHeap(View view) {
                if (GlobalSettings.Debug) {
                    Log.i("mainActivity", "Release the image in the ListView.");
                }

                GridView gridView = (GridView)view.findViewById(R.id.listView_item_gridView);

                for (int i = 0; i < gridView.getChildCount(); i++) {
                    ImageView imageView = (ImageView)gridView.getChildAt(i);

                    if (imageView.getTag() == null) {
                        Log.i("mainActivity", "GridView item's tag is null.");
                        continue;
                    }

                    if (imageView.getTag() != null) {
                        GridViewAdapter.AsyncLoadThumbnail thumbnailTask;
                        try {
                            thumbnailTask = (GridViewAdapter.AsyncLoadThumbnail)imageView.getTag();
                            thumbnailTask.cancel(true);
                            Log.i("mainActivity", "Cancel load thumbnail.");
                        } catch (Exception e) {
                            if (GlobalSettings.Debug) {
                                Log.e("mainActivity", "Can't convert the tag of the GridView item to an AsyncTask");
                            }
                        }
                        imageView.setTag(null);
                    }

                    // set the image source to null to release the image
                    //ImageView imageView = (ImageView) view.findViewById(R.id.photo);
                    imageView.setImageBitmap(null);
                }

            }

        });
        */

        mListViewAdapter = new ListViewAdapter(this, null);
        photoWall.setAdapter(mListViewAdapter);

        // trigger to load photos
        PhotosLoaderCallback photosLoaderCallback = new PhotosLoaderCallback();
        this.getLoaderManager().initLoader(1, null, photosLoaderCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);

        // Memory we can release here will help overall system performance, and
        // make us a smaller target as the system looks for memory
        if (level >= TRIM_MEMORY_MODERATE) { // 60
            // Nearing middle of list of cached background apps; evict our
            // entire thumbnail cache
            ImagesCache.evilAll();

        } else if (level >= TRIM_MEMORY_BACKGROUND) { // 40
            // Entering list of cached background apps; evict oldest half of our
            // thumbnail cache
            ImagesCache.trimHalfCache();
        }
    }

    /**
     * Implement the loader callback interface
     * Load camera photos
     */
    private class PhotosLoaderCallback implements LoaderManager.LoaderCallbacks<PhotosContainer> {

        @Override
        public AsyncPhotosLoader onCreateLoader(int loaderID, Bundle bundle) {

            if (GlobalSettings.Debug) {
                Log.i("PhotosLoaderCallback", "Start to create a new AsyncPhotosLoader.");
            }

            try {
                return new AsyncPhotosLoader(MainActivity.this);
            } catch (Exception e) {
                if (GlobalSettings.Debug) {
                    Log.e("PhotosLoaderCallback error:", e.getMessage());
                }
            }

            return null;
        }

        @Override
        public void onLoadFinished(Loader<PhotosContainer> loader, PhotosContainer photosContainer) {
            if (GlobalSettings.Debug) {
                Log.i("Total number of photos:", String.valueOf(photosContainer.getTotalPhotos()));
            }
            mListViewAdapter.updateResults(photosContainer);
        }

        @Override
        public void onLoaderReset(Loader<PhotosContainer> loader) {
            mListViewAdapter.updateResults(null);
        }
    }
}
