package com.example.dyang.galleryphotolist;

import android.content.AsyncTaskLoader;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * A synchronous task to load the photos
 */
public class AsyncPhotosLoader extends AsyncTaskLoader<PhotosContainer> {

    boolean mDescend = true;

    public AsyncPhotosLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        this.forceLoad();   // enforce to load photos
    }

    public void setDescend(boolean descend) {
        this.mDescend = descend;
    }

    @Override
    public PhotosContainer loadInBackground() {

        PhotosContainer photosContainer = new PhotosContainer();

        ContentResolver contentResolver = this.getContext().getContentResolver();
        // get photo's ID and DATE_TAKEN

         Date bDate;
         try {
             bDate = new SimpleDateFormat("yyyy-MM-dd").parse("2015-06-14");
         } catch (Exception e) {
             bDate = Calendar.getInstance().getTime();
         }

        Cursor photosCursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                                    new String[] { MediaStore.Images.Media._ID,
                                                                   MediaStore.Images.Media.DATE_TAKEN,
                                                                   MediaStore.Images.Media.ORIENTATION },
                                                    MediaStore.Images.Media.DATA + " LIKE ?",//and " + MediaStore.Images.Media.DATE_TAKEN + ">?",
                                                    new String[] { "%/DCIM/Camera/%"/*, String.valueOf(bDate.getTime()) */},
                                                    MediaStore.Images.Media.DATE_TAKEN + (this.mDescend ? " DESC" : " ASC"));

        //get video's ID, DATE_TAKEN and DURATION
        Cursor videoCursor = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                                   new String[] { MediaStore.Video.Media._ID,
                                                                  MediaStore.Video.Media.DATE_TAKEN,
                                                                  MediaStore.Video.Media.DURATION },
                                                   MediaStore.Audio.Media.DATA + " LIKE ?",//and " + MediaStore.Video.Media.DATE_TAKEN + ">?",
                                                   new String[] { "%/DCIM/Camera/%"/*, String.valueOf(bDate.getTime())*/ },
                                                   MediaStore.Audio.Media.DATE_ADDED + (this.mDescend ? " DESC" : " ASC"));

        if (photosCursor == null || videoCursor == null) {
            if (GlobalSettings.Debug) {
                Log.e("AsyncPhotosLoader", "Can't load the camera photos and video.");
            }

            return photosContainer;  // return a empty list
        }

        if (GlobalSettings.Debug) {
            Log.i("AsyncPhotosLoader", "Total photos: " + photosCursor.getCount());
            Log.i("AsyncPhotosLoader", "Total video: " + videoCursor.getCount());
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();

        // photoDateTaken = -1 end of the photosCursor
        // videoDateTaken = -1 end of the videoCursor
        long photoDateTaken = 0, videoDateTaken = 0, dateTaken;
        int  pointer = 0;   // 1:photos 2:video
        Photo photo;

        while (true) {
            if ((pointer == 0 || pointer == 1) && photoDateTaken != -1) {
                if (photosCursor.moveToNext()) {
                    photoDateTaken = photosCursor.getLong(1);
                } else {
                    photoDateTaken = -1;
                }
            }

            if ((pointer == 0 || pointer == 2) && videoDateTaken != -1) {
                if (videoCursor.moveToNext()) {
                    videoDateTaken = videoCursor.getLong(1);
                } else {
                    videoDateTaken = -1;
                }
            }

            if (photoDateTaken == -1 && videoDateTaken == -1) {
                break;
            }

            if ((this.mDescend && photoDateTaken > videoDateTaken) ||
                (this.mDescend == false && photoDateTaken <= videoDateTaken)) {
                pointer = 1;
                dateTaken = photoDateTaken;
            } else {
                pointer = 2;
                dateTaken = videoDateTaken;
            }

            // convert the milliseconds to date type
            calendar.setTimeInMillis(dateTaken);

            // remove time from the taken date
            // sort by the dateTaken, append the photoID to the last
            if (pointer == 1) {
                // add a photo
                photo = photosContainer.append(photosCursor.getLong(0), dateFormat.format(calendar.getTime()));
                photo.setOrientation(photosCursor.getInt((2))); // set image orientation
            } else {
                // add a video
                photo = photosContainer.append(videoCursor.getLong(0), dateFormat.format(calendar.getTime()));
                photo.setDuration(videoCursor.getLong(2));      // set video duration
            }
        }

        photosCursor.close();
        videoCursor.close();

        return photosContainer;
    }

}
