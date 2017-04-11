package com.example.android.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.inventoryapp.data.InventoryContract;

/**
 * {@link InventoryCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of item data as its data source. This adapter knows
 * how to create list items for each row of item data in the {@link Cursor}.
 */

 public class InventoryCursorAdapter extends CursorAdapter {

    private String mCursorPhotoPath;
    /**
     * ImageView field to add an image
     */
    private ImageView pictureImageView;

     /**
       * Constructs a new {@link InventoryCursorAdapter}.
       *
       * @param context The context
       * @param c       The cursor from which to get the data.
       */
     public InventoryCursorAdapter(Context context, Cursor c) {
           super(context, c, 0 /* flags */);
           }

     /**
       * Makes a new blank list item view. No data is set (or bound) to the views yet.
       *
       * @param context app context
       * @param cursor  The cursor from which to get the data. The cursor is already
       *                moved to the correct position.
       * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        //Fill out this method and return the list item view (instead of null)
        return LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
    }

    /**
     * This method binds the item data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current item can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        /// Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView producerTextView = (TextView) view.findViewById(R.id.producer);
        ImageView pictureImageView = (ImageView) view.findViewById(R.id.thumbnail);

        // Find the columns of item attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(InventoryContract.ItemEntry.COLUMN_ITEM_NAME);
        int producerColumnIndex = cursor.getColumnIndex(InventoryContract.ItemEntry.COLUMN_ITEM_PRODUCER);
        int pictureColumnIndex = cursor.getColumnIndex(InventoryContract.ItemEntry.COLUMN_ITEM_PICTURE);


        // Read the item attributes from the Cursor for the current item
        String itemName = cursor.getString(nameColumnIndex);
        String itemProducer = cursor.getString(producerColumnIndex);
        String itemPicture = cursor.getString(pictureColumnIndex);


        // If the item producer is empty string or null, then use some default text
        // that says "Unknown producer", so the TextView isn't blank.
       if (TextUtils.isEmpty(itemProducer)) {
           itemProducer = context.getString(R.string.unknown_producer);
       }

        // Update the TextViews with the attributes for the current item
        nameTextView.setText(itemName);
        producerTextView.setText(itemProducer);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(itemPicture, options);
        int photoW = options.outWidth;
        int photoH = options.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/72, photoH/72);

        // Decode the image file into a Bitmap sized to fill the View
        options.inJustDecodeBounds = false;
        options.inSampleSize = scaleFactor;
        options.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(itemPicture, options);
        pictureImageView.setImageBitmap(bitmap);

    }

}