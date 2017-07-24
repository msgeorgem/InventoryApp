/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract;

import static com.example.android.inventoryapp.data.InventoryContract.ItemEntry._ID;

/**
 * Displays list of items that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int ITEM_LOADER = 0;
    InventoryCursorAdapter mCursorAdapter;
    View emptyView;
    SQLiteDatabase mDb;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, DetailAcitvity.class);
                startActivity(intent);
            }
        });
        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        emptyView = findViewById(R.id.empty_view);

        mRecyclerView = (RecyclerView) findViewById(R.id.list_view);
        mLayoutManager = new LinearLayoutManager(CatalogActivity.this);
        mRecyclerView.setLayoutManager(mLayoutManager);


        mCursorAdapter = new InventoryCursorAdapter(this, null);
        mRecyclerView.setAdapter(mCursorAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        //kick off the loader
        getSupportLoaderManager().initLoader(ITEM_LOADER, null, this);
//        InventoryDbHelper dbHelper = new InventoryDbHelper(this);
//        mDb = dbHelper.getReadableDatabase();

        // Create a new ItemTouchHelper with a SimpleCallback that handles both LEFT  swipe directions
        // Create an item touch helper to handle swiping items off the list
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            // Override onMove and simply return false inside
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                //do nothing, we only care about swiping
                return false;
            }
            // Override onSwiped
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                showDeleteConfirmationDialogOneItem(viewHolder);
            }
            //attach the ItemTouchHelper to the waitlistRecyclerView
        }).attachToRecyclerView(mRecyclerView);

    }

    public void onItemClick(long id) {
        Intent intent = new Intent(CatalogActivity.this, DetailAcitvity.class);

        Uri currentProductUri = ContentUris.withAppendedId(InventoryContract.ItemEntry.CONTENT_URI, id);
        intent.setData(currentProductUri);

        startActivity(intent);
    }

    public void onLongItemClick() {
        showConfirmationDialog();
    }

    public void onSellClick(long id, int quantity, String mName) {
        Uri currentProductUri = ContentUris.withAppendedId(InventoryContract.ItemEntry.CONTENT_URI, id);

        if (quantity > 0) {
            quantity--;
            ContentValues values = new ContentValues();
            values.put(InventoryContract.ItemEntry.COLUMN_ITEM_QUANTITY, quantity);
            getContentResolver().update(currentProductUri, values, null, null);
            Toast.makeText(this, getString(R.string.sold) + " " + mName, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.empty) + " " + mName, Toast.LENGTH_SHORT).show();
        }
    }

    private void insertItem() {
        // Create a ContentValues object, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(InventoryContract.ItemEntry.COLUMN_ITEM_NAME, "Kitten");
        values.put(InventoryContract.ItemEntry.COLUMN_ITEM_DESCRIPTION, "Confused Kitten");
        values.put(InventoryContract.ItemEntry.COLUMN_ITEM_EMAIL, "john.supplier@jjj.com");
        values.put(InventoryContract.ItemEntry.COLUMN_ITEM_PRICE, 100.23);
        values.put(InventoryContract.ItemEntry.COLUMN_ITEM_QUANTITY, 1);
        values.put(InventoryContract.ItemEntry.COLUMN_ITEM_PICTURE, getString(R.string.dummy_pictureUri));
        // Insert the new row, returning the primary key value of the new row
        getContentResolver().insert(InventoryContract.ItemEntry.CONTENT_URI, values);
    }

    private void deleteAllItems() {
        int rowsDeleted = getContentResolver().delete(InventoryContract.ItemEntry.CONTENT_URI, null, null);
        Toast.makeText(this, rowsDeleted + " " + getString(R.string.delete_all_items), Toast.LENGTH_SHORT).show();
    }

    private void deleteOneItem(long id) {
        int rowDeleted = getContentResolver().delete(InventoryContract.ItemEntry.CONTENT_URI, InventoryContract.ItemEntry._ID + "=" + id, null);
        Toast.makeText(this, rowDeleted + " " + getString(R.string.delete_one_item), Toast.LENGTH_SHORT).show();
    }

    //  working method but need to getReadable Database using db Helper(few lines earlier)
//  private Cursor query() {
//        return mDb.query(
//                InventoryContract.ItemEntry.TABLE_NAME,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null
//        );
//    }
    private Cursor querY() {
        return getContentResolver().query(InventoryContract.ItemEntry.CONTENT_URI, null, null, null, null);

    }


    private void showDeleteConfirmationDialogAllItems() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_allitems_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the item.
                deleteAllItems();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.what_to_do_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the item.
//                deleteAllItems();
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialogOneItem(final RecyclerView.ViewHolder viewHolder) {
        //Inside, get the viewHolder's itemView's tag and store in a long variable id
        //get the iD of the item being swiped
        final long iD = (long) viewHolder.itemView.getTag();

        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_oneitem_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the item.
                //remove from DB
                deleteOneItem(iD);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
                //call swapCursor on mAdapter passing in null as the argument
                //update the list
                mCursorAdapter.swapCursor(querY());
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertItem();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                showDeleteConfirmationDialogAllItems();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                _ID,
                InventoryContract.ItemEntry.COLUMN_ITEM_NAME,
                InventoryContract.ItemEntry.COLUMN_ITEM_DESCRIPTION,
                InventoryContract.ItemEntry.COLUMN_ITEM_PRICE,
                InventoryContract.ItemEntry.COLUMN_ITEM_QUANTITY,
                InventoryContract.ItemEntry.COLUMN_ITEM_PICTURE,
        };

        // Perform a query using CursorLoader
        return new CursorLoader(this,    // Parent activity context
                InventoryContract.ItemEntry.CONTENT_URI, // Provider content URI to query
                projection,            // The columns to include in the resulting Cursor
                null,                  // The values for the WHERE clause
                null,                  // No selection arguments
                null);                 // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link ItemCursor Adapter with this new cursor containing updated item data
        if (!data.moveToFirst()) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }
        mCursorAdapter.swapCursor(data);

    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
