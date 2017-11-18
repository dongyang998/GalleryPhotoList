package com.example.dyang.galleryphotolist;

/**
 * Created by DYang on 9/7/2015.
 */
public class Photo {

    public enum PhotoType { IMAGE, VIDEO };
    private long mID;
    // x,y is the position of this photo in the two dimensions array
    // use to get the next or previous photo in PhotosContainer
    private int mX, mY;
    private long mDuration;
    private PhotoType mType;
    private int mOrientation;

    public Photo(long id, int x, int y) {
        this.mID = id;
        this.mX = x;
        this.mY = y;
        this.mOrientation = 0;

        this.mDuration = 0;
        this.mType = PhotoType.IMAGE;   // default is image
    }

    public void setDuration(long duration) {
        if (duration > 0) {
            this.mType = PhotoType.VIDEO;
            this.mDuration = duration;
        } else {
            this.mType = PhotoType.IMAGE;
            this.mDuration = 0;
        }
    }

    public void setOrientation(int orientation) {
        this.mOrientation = orientation;
    }

    public long getID() {
        return this.mID;
    }

    // the id use as a key in ImagesCache(LruCache)
    // video < 0, image > 0, in case has duplicated id
    public long getCatchID() {
        return this.mType == PhotoType.IMAGE ? this.mID : -1 * this.mID;
    }

    public PhotoType getType() {
        return this.mType;
    }

    public long getDuration() {
        return this.mDuration;
    }

    public int getX() {
        return this.mX;
    }

    public int getY() {
        return this.mY;
    }

    public int getOrientation() {
        return this.mOrientation;
    }
}
