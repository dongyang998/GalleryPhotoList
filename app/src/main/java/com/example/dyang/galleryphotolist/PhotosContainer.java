package com.example.dyang.galleryphotolist;

import java.util.ArrayList;

/**
 * Photos container, save all the photos' id and taken date(yyyy-mm-dd)
 */
public class PhotosContainer {

    // Organize the photos' id by taken date
    // mPhotos is a two dimensions array
    private ArrayList<ArrayList<Photo>> mPhotos;
    private ArrayList<String> mDates;
    private int mTotalPhotos;

    public PhotosContainer() {
        mPhotos = new ArrayList<>();
        mDates = new ArrayList<>();
        mTotalPhotos = 0;
    }

    // append photo to the end of last array or create a new array
    // takenDate must sort
    // video:duration > 0 , image:duration <= 0
    public Photo append(long photoID, String dateTaken) {

        // one dimension array
        ArrayList<Photo> photos;
        Photo photo;

        // last element of mDates is the same, append to the last
        if (mDates.size() > 0 && mDates.get(mDates.size() - 1).equals(dateTaken)) {
            // get the last one
            photos = mPhotos.get(mPhotos.size() - 1);
            photo = new Photo(photoID, photos.size(), mPhotos.size() - 1);
            photos.add(photo);
        } else {
            // mDates doesn't have this takenDate, create a new array
            mDates.add(dateTaken);
            photos = new ArrayList<>();
            photo = new Photo(photoID, photos.size(), mPhotos.size() - 1);
            photos.add(photo);
            mPhotos.add(photos);
        }

        mTotalPhotos++;
        return photo;
    }

    // return the two dimensions ArrayList
    public ArrayList<ArrayList<Photo>> getPhotos() {
        return mPhotos;
    }

    public ArrayList<String> getDates() {
        return mDates;
    }

    public int getTotalPhotos() {
        return mTotalPhotos;
    }
}
