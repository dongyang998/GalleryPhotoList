package com.example.dyang.galleryphotolist;

import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ThumbnailUtils;
import android.provider.MediaStore.Video;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.graphics.Bitmap;
import android.widget.BaseAdapter;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * adapter of GridView
 */
public class GridViewAdapter extends BaseAdapter {

    private Context mContext;
    // ArrayList of Photo
    private ArrayList<Photo> mPhotos;
    BitmapFactory.Options mBitmapFactoryOptions;
    int mPhotoViewHeightPx, mPhotoViewWidthPx;
    int mDensityDpi;
    public final static int  TagKeyTask = 1;
    public final static int TagKeyPhoto = 2;

    public GridViewAdapter(Context context, ArrayList<Photo> photos) {
        mContext = context;
        mPhotos = photos;

        mBitmapFactoryOptions = new BitmapFactory.Options();
        mBitmapFactoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        float density = this.mContext.getResources().getDisplayMetrics().density;
        mDensityDpi = this.mContext.getResources().getDisplayMetrics().densityDpi;

        mPhotoViewHeightPx = (int)(GlobalSettings.PhotoHeight * density + 0.5f);
        mPhotoViewWidthPx = (int)(GlobalSettings.PhotoWidth * density + 0.5f);

        //Log.i("image height:", String.valueOf(mPhotoViewHeightPx));
        //Log.i("image mWidth:", String.valueOf(mPhotoViewHeightPx));
        //Log.i("densityDpi:", String.valueOf(mDensityDpi));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View gridViewItem;
        ImageView imageView;

        if (convertView != null) {
            gridViewItem = convertView;
        } else {
            Log.i("GridViewAdapter", "Create a new grid view item.");
            gridViewItem = LayoutInflater.from(mContext).inflate(R.layout.gridview_item, null);
        }

        imageView = (ImageView)gridViewItem.findViewById(R.id.gridView_item_photo);

        // if imageView has a older task, cancel it.
        if (imageView.getTag(TagKeyTask) != null) {
            AsyncLoadThumbnail olderTask;
            try {
                olderTask = (AsyncLoadThumbnail)imageView.getTag(TagKeyTask);
                olderTask.cancel(true);
                Log.i("GridViewAdapter", "Cancel old task.");
            }
            catch (Exception e) {
                if (GlobalSettings.Debug) {
                    Log.e("GridViewAdapter", "Can't convert the tag of the GridView item to an AsyncTask");
                }
            }
        }

        // release the older sync task and photo objects
        imageView.setTag(null);

        Photo photo = mPhotos.get(position);
        Bitmap bitmap = ImagesCache.get(String.valueOf(photo.getCatchID()));

        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageBitmap(null);

            // load thumbnail
            AsyncLoadThumbnail task = new AsyncLoadThumbnail(imageView);
            imageView.setTag(TagKeyTask, task);
            task.execute(photo);
        }

        // set the photo object to tag
        imageView.setTag(TagKeyPhoto, photo);

        return gridViewItem;
    }

    public int getCount() {
        return mPhotos != null ? mPhotos.size() : 0;
    }

    public Object getItem(int position) {
        return mPhotos != null ? mPhotos.get(position) : null;
    }

    public long getItemId(int position) {
        return position;
    }

    // asynchronous task load thumbnail
    public class AsyncLoadThumbnail extends AsyncTask <Photo, Void, Bitmap> {

        private Photo mPhoto;
        private ImageView mImageView;

        public AsyncLoadThumbnail(ImageView imageView) {
            this.mImageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(Photo... photos) {
            mPhoto = photos[0];
            Bitmap source, thumbnail;
            boolean detectSize;
            int targetHeight, targetWidth;

            if (this.isCancelled()) {
                Log.i("GridViewAdapter", "Task was cancelled.");
                return null;
            }

            // the GridView is scrolling
            if (mImageView.getTag(TagKeyTask) != this) {
                Log.i("GridViewAdapter", "2222 Task was cancelled.");
                return null;
            }

            if (mPhoto.getType() == Photo.PhotoType.IMAGE) {
                Cursor cursor = Images.Thumbnails.queryMiniThumbnail(mContext.getContentResolver(),
                                                                     mPhoto.getID(),
                                                                     Images.Thumbnails.MINI_KIND,
                                                                     new String[] {Images.Thumbnails.WIDTH,  Images.Thumbnails.HEIGHT});


                // if the thumbnail height > ImageView height, scale the thumbnail
                mBitmapFactoryOptions.inScaled = false;
                detectSize = false;
                if (cursor.moveToFirst()) {
                    if (cursor.getInt(1) > mPhotoViewHeightPx) {
                        mBitmapFactoryOptions.inScaled = true;
                        mBitmapFactoryOptions.inDensity = mDensityDpi * cursor.getInt(1) / mPhotoViewHeightPx;
                        mBitmapFactoryOptions.inTargetDensity = mDensityDpi;
                    }

                    Log.i("GridViewAdapter", "inDensity1:" + String.valueOf(mBitmapFactoryOptions.inDensity));
                    Log.i("GridViewAdapter", "Thumbnail width1:" + String.valueOf(cursor.getInt(0)));
                    Log.i("GridViewAdapter", "Thumbnail height1:" + String.valueOf(cursor.getInt(1)));
                } else {
                    detectSize = true;
                }

                cursor.close();

                if (this.isCancelled()) {
                    return null;
                }

                source = Images.Thumbnails.getThumbnail(mContext.getContentResolver(),
                                                        mPhoto.getID(),
                                                        Images.Thumbnails.MINI_KIND,
                                                        mBitmapFactoryOptions);

                if (source == null) {
                    Log.e("GridViewAdapter", "Can't get the thumbnail.");
                    return null;
                }

                Log.i("GridViewAdapter", "Thumbnail size2:" + String.valueOf(source.getByteCount()));
                Log.i("GridViewAdapter", "Thumbnail width2:" + String.valueOf(source.getWidth()));
                Log.i("GridViewAdapter", "Thumbnail height2:" + String.valueOf(source.getHeight()));
                Log.i("GridViewAdapter", "Thumbnail orientation:" + String.valueOf(mPhoto.getOrientation()));

                if (this.isCancelled()) {
                    source.recycle();
                    return null;
                }

                // the GridView is scrolling
                if (mImageView.getTag(TagKeyTask) != this) {
                    Log.i("GridViewAdapter", "5555 Task was cancelled.");
                    source.recycle();
                    return null;
                }

                if (detectSize && source.getHeight() > mPhotoViewHeightPx) {
                    source = Bitmap.createScaledBitmap(source, (int)(source.getWidth() * (double)mPhotoViewHeightPx / source.getHeight() + 0.5f), mPhotoViewHeightPx, true);
                    Log.i("GridViewAdapter", "Scale source");
                }

                if (this.isCancelled()) {
                    source.recycle();
                    Log.i("GridViewAdapter", "Task was cancelled.");
                    return null;
                }

                // If the thumbnail is vertical, rotate it.
                if (mPhoto.getOrientation() > 0) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(mPhoto.getOrientation());

                    //targetHeight = source.getHeight() >= mPhotoViewWidthPx ? mPhotoViewWidthPx : source.getHeight();
                    //targetWidth = source.getWidth() >= mPhotoViewHeightPx ? mPhotoViewHeightPx : (int) (mPhotoViewHeightPx * (double) targetHeight / mPhotoViewWidthPx + 0.5f);

                    if (source.getHeight() >= mPhotoViewWidthPx && source.getWidth() >= mPhotoViewHeightPx) {
                        targetHeight = mPhotoViewWidthPx;
                        targetWidth = mPhotoViewHeightPx;
                    } else if (source.getHeight() < mPhotoViewWidthPx && source.getWidth() >= mPhotoViewHeightPx) {
                        targetHeight = source.getHeight();
                        targetWidth = (int)(mPhotoViewHeightPx * (double)targetHeight / mPhotoViewWidthPx + 0.5f);
                    } else if (source.getHeight() >= mPhotoViewWidthPx && source.getWidth() < mPhotoViewHeightPx) {
                        targetWidth = source.getWidth();
                        targetHeight = (int)(mPhotoViewWidthPx * (double)targetWidth / mPhotoViewHeightPx + 0.5f);
                    } else if ((double)source.getHeight() / mPhotoViewWidthPx <= (double)source.getWidth() / mPhotoViewHeightPx) {
                        targetHeight = source.getHeight();
                        targetWidth = (int)(mPhotoViewHeightPx * (double)targetHeight / mPhotoViewWidthPx + 0.5f);
                    } else {
                        targetWidth = source.getWidth();
                        targetHeight = (int)(mPhotoViewWidthPx * (double)targetWidth / mPhotoViewHeightPx + 0.5f);
                    }

                    // Get subset of the source bitmap
                    thumbnail = Bitmap.createBitmap(source, 0, 0, targetWidth, targetHeight, matrix, true);
                    Log.i("GridViewAdapter", "rotate bitmap");
                } else {

                    //targetHeight = source.getHeight() >= mPhotoViewHeightPx ? mPhotoViewHeightPx : source.getHeight();
                    //targetWidth = source.getWidth() >= mPhotoViewWidthPx ? mPhotoViewWidthPx : (int)(mPhotoViewWidthPx * (double)targetHeight / mPhotoViewHeightPx + 0.5f);

                    if (source.getHeight() >= mPhotoViewHeightPx && source.getWidth() >= mPhotoViewWidthPx) {
                        targetHeight = mPhotoViewHeightPx;
                        targetWidth = mPhotoViewWidthPx;
                    } else if (source.getHeight() < mPhotoViewHeightPx && source.getWidth() >= mPhotoViewWidthPx) {
                        targetHeight = source.getHeight();
                        targetWidth = (int)(mPhotoViewWidthPx * (double)targetHeight / mPhotoViewHeightPx + 0.5f);
                    } else if (source.getHeight() >= mPhotoViewHeightPx && source.getWidth() < mPhotoViewWidthPx) {
                        targetWidth = source.getWidth();
                        targetHeight = (int)(mPhotoViewHeightPx * (double)targetWidth / mPhotoViewWidthPx + 0.5f);
                    } else if ((double)source.getHeight() / mPhotoViewHeightPx <= (double)source.getWidth() / mPhotoViewWidthPx) {
                        targetHeight = source.getHeight();
                        targetWidth = (int)(mPhotoViewWidthPx * (double)targetHeight / mPhotoViewHeightPx + 0.5f);
                    } else {
                        targetWidth = source.getWidth();
                        targetHeight = (int)(mPhotoViewHeightPx * (double)targetWidth / mPhotoViewWidthPx + 0.5f);
                    }

                    if (targetWidth != source.getWidth()) {
                        thumbnail = ThumbnailUtils.extractThumbnail(source, targetWidth, targetHeight);
                    } else {
                        thumbnail = source;
                    }
                }

                // recycle source bitmap if they are not the same object
                if (source != thumbnail) {
                    source.recycle();
                }

                if (this.isCancelled()) {
                    thumbnail.recycle();
                    Log.i("GridViewAdapter", "Task was cancelled.");
                    return null;
                }

                Log.i("GridViewAdapter", "Thumbnail size3:" + String.valueOf(thumbnail.getByteCount()));
                Log.i("GridViewAdapter", "Thumbnail width3:" + String.valueOf(thumbnail.getWidth()));
                Log.i("GridViewAdapter", "Thumbnail height3:" + String.valueOf(thumbnail.getHeight()));

            } else {
                // 512 x 384 thumbnail MICRO_KIND: 96 x 96 thumbnail
                mBitmapFactoryOptions.inScaled = false;
                source = Video.Thumbnails.getThumbnail(mContext.getContentResolver(),
                                                       mPhoto.getID(),
                                                       Video.Thumbnails.MINI_KIND,
                                                       mBitmapFactoryOptions);

                if (this.isCancelled()) {
                    source.recycle();
                    Log.i("GridViewAdapter", "Task was cancelled.");
                    return null;
                }

                if (source.getHeight() > mPhotoViewHeightPx) {
                    source = Bitmap.createScaledBitmap(source, (int)(source.getWidth() * (double)mPhotoViewHeightPx / source.getHeight() + 0.5f), mPhotoViewHeightPx, true);
                    targetHeight = source.getHeight();
                    targetWidth = mPhotoViewWidthPx;
                    Log.i("GridViewAdapter", "Scale source");
                } else {
                    targetHeight = source.getHeight();
                    targetWidth = (int)(mPhotoViewWidthPx * (double)targetHeight / mPhotoViewHeightPx + 0.5f);
                }

                if (this.isCancelled()) {
                    source.recycle();
                    return null;
                }

                if (targetWidth != source.getWidth()) {
                    thumbnail = ThumbnailUtils.extractThumbnail(source, targetWidth, targetHeight);
                } else {
                    thumbnail = source;
                }

                // recycle source bitmap if they are not the same object
                if (source != thumbnail) {
                    source.recycle();
                }

                if (this.isCancelled()) {
                    thumbnail.recycle();
                    Log.i("GridViewAdapter", "Task was cancelled.");
                    return null;
                }

                Log.i("GridViewAdapter", "Video Thumbnail size3:" + String.valueOf(thumbnail.getByteCount()));
                Log.i("GridViewAdapter", "Video Thumbnail width3:" + String.valueOf(thumbnail.getWidth()));
                Log.i("GridViewAdapter", "Video Thumbnail height3:" + String.valueOf(thumbnail.getHeight()));
            }

            if (this.mPhoto.getType() == Photo.PhotoType.IMAGE) {
                return thumbnail;
            }

            // video: draw duration
            long hours, minutes, seconds;
            long duration;

            duration = mPhoto.getDuration();
            hours = TimeUnit.MILLISECONDS.toHours(mPhoto.getDuration());
            duration = duration - TimeUnit.HOURS.toMillis(hours);
            minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
            duration = duration - TimeUnit.MINUTES.toMillis(minutes);
            seconds = TimeUnit.MILLISECONDS.toSeconds(duration);

            Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);

            Bitmap thumbnailVideo = Bitmap.createBitmap(thumbnail.getWidth(), thumbnail.getHeight(), thumbnail.getConfig());

            Canvas canvas = new Canvas(thumbnailVideo);
            canvas.drawBitmap(thumbnail, 0, 0, null);

            //paintText.setColor(Color.BLACK);
            //paintText.setAlpha(100);
            //canvas.drawRect(thumbnail.getWidth() - 30, thumbnail.getHeight() - 18, thumbnail.getWidth(), thumbnail.getHeight() - 3, paintText);

            paintText.setColor(Color.WHITE);
            paintText.setAlpha(255);
            paintText.setTextSize(15);
            paintText.setStyle(Paint.Style.FILL);
            paintText.setAntiAlias(true);//
            paintText.setShadowLayer(1f, 30f, 30f, Color.BLACK);

            // TODO....
            if (hours > 0) {
                canvas.drawText(String.format("%d:%02d:%02d", hours, minutes, seconds), thumbnailVideo.getWidth() - 40, thumbnailVideo.getHeight() - 5, paintText);
            } else {
                canvas.drawText(String.format("%2d:%02d", minutes, seconds), thumbnailVideo.getWidth() - 40, thumbnailVideo.getHeight() - 5, paintText);
            }

            thumbnail.recycle();
            return thumbnailVideo;
         }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap == null) {
                if (GlobalSettings.Debug) {
                    Log.i("AsyncTask", "Can't find the thumbnail. ImageID:" + mPhoto.getID() +
                                                                 "ImageType:" + (mPhoto.getType() == Photo.PhotoType.IMAGE ? "image" : "video"));
                }
                return;
            }

            // the GridView is scrolling
            if (mImageView.getTag(TagKeyTask) != this) {
                Log.i("GridViewAdapter", "3333 Task was cancelled.");
                bitmap.recycle();
                return;
            }

            mImageView.setImageBitmap(bitmap);
            // reset tag to null, identify the task is done
            mImageView.setTag(TagKeyTask, null);

            // add to cache
            ImagesCache.put(String.valueOf(mPhoto.getCatchID()), bitmap);
         }

        @Override
        protected void onCancelled(Bitmap bitmap) {
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
    }

}
