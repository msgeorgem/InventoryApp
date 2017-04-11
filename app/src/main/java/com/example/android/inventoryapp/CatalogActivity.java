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
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int ITEM_LOADER = 0;
    InventoryCursorAdapter mCursorAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorAcitvity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the pet data
        ListView itemListView = (ListView) findViewById(R.id.list_view);
        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        itemListView.setEmptyView(emptyView);

        mCursorAdapter = new InventoryCursorAdapter(this, null);
        itemListView.setAdapter(mCursorAdapter);

        //Setup item click listener
        itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CatalogActivity.this, EditorAcitvity.class);
                //From the content URI that represents the specific pet that was clicked on,
                //by appending the "id" (passed as input to this method)onto the
                //{@link ItemEntry#CONTENT_URI}.
                Uri currentItemUri = ContentUris.withAppendedId(InventoryContract.ItemEntry.CONTENT_URI, id);

                //Set the URI on the data field of the intent
                intent.setData(currentItemUri);
                //Launch the {@link EditorActivity} to display the data fot the current item.
                startActivity(intent);
            }
        });

        //kick off the loader
        getSupportLoaderManager().initLoader(ITEM_LOADER, null,this);
    }



    private void insertItem(){

        // Create a ContentValues object, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(InventoryContract.ItemEntry.COLUMN_ITEM_NAME, "MPhone123");
        values.put(InventoryContract.ItemEntry.COLUMN_ITEM_DESCRIPTION, "Description");
        values.put(InventoryContract.ItemEntry.COLUMN_ITEM_PRODUCER, "MasterProducer");
        values.put(InventoryContract.ItemEntry.COLUMN_ITEM_STOCK, 0);
//        values.put(InventoryContract.ItemEntry.COLUMN_ITEM_PICTURE, );

        // Insert the new row, returning the primary key value of the new row
        Uri newUri = getContentResolver().insert(InventoryContract.ItemEntry.CONTENT_URI, values);
    }
    private void deleteAllItems(){
        int rowsDeleted = getContentResolver().delete(InventoryContract.ItemEntry.CONTENT_URI, null, null);
        Toast.makeText(this, rowsDeleted +" "+ getString(R.string.delete_all_items), Toast.LENGTH_SHORT).show();
    }
    private void showDeleteConfirmationDialogAllItems() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_allitems_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteAllItems();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
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
                InventoryContract.ItemEntry._ID,
                InventoryContract.ItemEntry.COLUMN_ITEM_NAME,
                InventoryContract.ItemEntry.COLUMN_ITEM_DESCRIPTION,
                InventoryContract.ItemEntry.COLUMN_ITEM_PRODUCER,
//                InventoryContract.ItemEntry.COLUMN_ITEM_STOCK
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
        // Update {@link PetCursor Adapter with this new cursor containing updated pet data
        mCursorAdapter.swapCursor(data);


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);

    }
}
