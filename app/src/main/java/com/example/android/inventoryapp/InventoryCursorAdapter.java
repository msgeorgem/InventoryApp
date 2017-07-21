package com.example.android.inventoryapp;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.inventoryapp.data.InventoryContract;

/**
 * {@link InventoryCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of item data as its data source. This adapter knows
 * how to create list items for each row of item data in the {@link Cursor}.
 */

public class InventoryCursorAdapter extends CursorRecyclerAdapter<InventoryCursorAdapter.ViewHolder> {

    private CatalogActivity activity = new CatalogActivity();

    public InventoryCursorAdapter(CatalogActivity context, Cursor c) {
        super(context, c);
        this.activity = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        ViewHolder vh = new ViewHolder(itemView);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, Cursor cursor) {

        final long id;
        final int mQuantity;

        // Find the columns of item attributes that we're interested in
        id = cursor.getLong(cursor.getColumnIndex(InventoryContract.ItemEntry._ID));
        int nameColumnIndex = cursor.getColumnIndex(InventoryContract.ItemEntry.COLUMN_ITEM_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryContract.ItemEntry.COLUMN_ITEM_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.ItemEntry.COLUMN_ITEM_QUANTITY);
        int pictureColumnIndex = cursor.getColumnIndex(InventoryContract.ItemEntry.COLUMN_ITEM_PICTURE);


        // Read the item attributes from the Cursor for the current item
        final String itemName = cursor.getString(nameColumnIndex);
        String itemPrice = cursor.getString(priceColumnIndex);
        int itemQuantity = cursor.getInt(quantityColumnIndex);
        String itemPicture = cursor.getString(pictureColumnIndex);


        mQuantity = itemQuantity;
//        Context context = viewHolder.itemView.getContext();
//        Picasso.with(context).load(uri).into(viewHolder.pictureImageView);
        BitmapFactory.Options options = new BitmapFactory.Options();

        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(itemPicture, options);
        int photoW = options.outWidth;
        int photoH = options.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / 88, photoH / 88);

        // Decode the image file into a Bitmap sized to fill the View
        options.inJustDecodeBounds = false;
        options.inSampleSize = scaleFactor;
        options.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(itemPicture, options);
        viewHolder.pictureImageView.setImageBitmap(bitmap);
        
        // Update the TextViews with the attributes for the current item
        viewHolder.nameTextView.setText(itemName);
        viewHolder.priceTextView.setText(itemPrice);
        viewHolder.sellItemTextView.setText(Integer.toString(itemQuantity));
        viewHolder.itemView.setTag(id);

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.onItemClick(id);
            }
        });

        viewHolder.sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.onSellClick(id, mQuantity, itemName);
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView nameTextView;
        public TextView priceTextView;
        public TextView sellItemTextView;
        public ImageView pictureImageView;
        public Button sellButton;


        public ViewHolder(View view) {
            super(view);
            nameTextView = (TextView) view.findViewById(R.id.name);
            priceTextView = (TextView) view.findViewById(R.id.price);
            sellItemTextView = (TextView) view.findViewById(R.id.sell_item);
            pictureImageView = (ImageView) view.findViewById(R.id.thumbnail);
            sellButton = (Button) view.findViewById(R.id.sell_instant);
        }
    }
}