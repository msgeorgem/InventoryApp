package com.example.android.inventoryapp;

/**
 * Created by Marcin on 2017-04-06.
 */

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.ItemEntry;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.android.inventoryapp.data.InventoryContract.ItemEntry.COLUMN_ITEM_DESCRIPTION;
import static com.example.android.inventoryapp.data.InventoryContract.ItemEntry.COLUMN_ITEM_EMAIL;
import static com.example.android.inventoryapp.data.InventoryContract.ItemEntry.COLUMN_ITEM_NAME;
import static com.example.android.inventoryapp.data.InventoryContract.ItemEntry.COLUMN_ITEM_PICTURE;
import static com.example.android.inventoryapp.data.InventoryContract.ItemEntry.COLUMN_ITEM_PRICE;
import static com.example.android.inventoryapp.data.InventoryContract.ItemEntry.COLUMN_ITEM_QUANTITY;
import static com.example.android.inventoryapp.data.InventoryContract.ItemEntry.CONTENT_URI;
import static com.example.android.inventoryapp.data.InventoryContract.ItemEntry._ID;

/**
 * Allows user to create a new item or edit an existing one.
 */
public class DetailAcitvity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the item data loader
     */
    private static final int EXISTING_ITEM_LOADER = 0;
    private static int REQUEST_TAKE_PHOTO = 1;
    public int id;
    public int mQuantity;
    public String mEmail;
    public String mName;

    private Uri mCurrentItemUri;
    private EditText mNameEditText;
    private EditText mDescriptionEditText;
    private EditText mEmailEditText;
    private EditText mPriceEditText;
    private TextView mPriceText;
    private TextView mQuantityText;
    private EditText mQuantityEditText;
    private ImageView mImageView;
    private String mCurrentPhotoPath;
    // restore the info about image from external
    private byte[] mImageByteArray;
    private boolean mItemHasChanged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    public static boolean isValidEmail(CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new item or editing an existing one.
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();
        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_items_name);
        mDescriptionEditText = (EditText) findViewById(R.id.edit_description);
        mEmailEditText = (EditText) findViewById(R.id.edit_email);
        mEmailEditText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        mPriceEditText = (EditText) findViewById(R.id.edit_price);
        mPriceText = (TextView) findViewById(R.id.text_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_quantity);
        mQuantityText = (TextView) findViewById(R.id.text_quantity);
        mImageView = (ImageView) findViewById(R.id.inserted_image);


        Button mSellButton = (Button) findViewById(R.id.sell);
        mSellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sellItem(id, mQuantity, mEmail, mName);
            }
        });

        Button mAddButton = (Button) findViewById(R.id.add);
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItemToStock(id, mQuantity);
            }
        });

        Button mOrderButton = (Button) findViewById(R.id.order);
        mOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeAnOrder(id, mEmail, mName);
            }
        });

        Button mImageButton = (Button) findViewById(R.id.insert_image);
        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
        // If the intent DOES NOT contain a item content URI, then we know that we are
        // creating a new item.
        if (mCurrentItemUri == null) {
            // This is a new item, so change the app bar to say "Add an Item"
            setTitle(getString(R.string.editor_activity_title_new_item));
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a item that hasn't been created yet.)
            mAddButton.setVisibility(View.GONE);
            mSellButton.setVisibility(View.GONE);
            mOrderButton.setVisibility(View.GONE);
            mQuantityEditText.setVisibility(View.VISIBLE);
            mQuantityText.setVisibility(View.GONE);
            mPriceEditText.setVisibility(View.VISIBLE);
            mPriceText.setVisibility(View.GONE);
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing item, so change app bar to say "Edit Item"
            setTitle(getString(R.string.editor_activity_title_edit_item));
            mAddButton.setVisibility(View.VISIBLE);
            mSellButton.setVisibility(View.VISIBLE);
            mOrderButton.setVisibility(View.VISIBLE);
            mQuantityEditText.setVisibility(View.GONE);
            mQuantityText.setVisibility(View.VISIBLE);
            mPriceEditText.setVisibility(View.GONE);
            mPriceText.setVisibility(View.VISIBLE);
            // Initialize a loader to read the item data from the database
            // and display the current values in the editor
            getSupportLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }


        mNameEditText.setOnTouchListener(mTouchListener);
        mDescriptionEditText.setOnTouchListener(mTouchListener);
        mEmailEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mImageView.setOnTouchListener(mTouchListener);

    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
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

    @Override
    public void onBackPressed() {
        // If the item hasn't changed, continue with handling back button press
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    // Get user input from editor and save item into database.
    private void saveItem() throws IOException {

        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String descriptionString = mDescriptionEditText.getText().toString().trim();
        String emailString = mEmailEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();

        if (mCurrentItemUri == null) {
            String quantityString = mQuantityEditText.getText().toString().trim();
            // Check if this is supposed to be a new item
            // and check if all the fields in the editor are blank
            if (mCurrentItemUri == null &&
                    TextUtils.isEmpty(nameString) &&
                    TextUtils.isEmpty(descriptionString) &&
                    TextUtils.isEmpty(emailString) &&
                    TextUtils.isEmpty(priceString) &&
                    TextUtils.isEmpty(quantityString) &&
                    mImageByteArray == null) {

                Toast.makeText(this, R.string.warning_invalid_input, Toast.LENGTH_SHORT).show();
                // Since no fields were modified, we can return early without creating a new item.
                // No need to create ContentValues and no need to do any ContentProvider operations.

                return;
            }
            if (TextUtils.isEmpty(nameString)) {
                Toast.makeText(this, getString(R.string.product_required), Toast.LENGTH_SHORT).show();
                return;
            }
            ContentValues values = new ContentValues();
            values.put(COLUMN_ITEM_NAME, nameString);

            if (TextUtils.isEmpty(descriptionString)) {
                Toast.makeText(this, getString(R.string.description_required), Toast.LENGTH_SHORT).show();
                return;
            }
            values.put(COLUMN_ITEM_DESCRIPTION, descriptionString);

            if (TextUtils.isEmpty(emailString)) {
                Toast.makeText(this, getString(R.string.email_required), Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isValidEmail(emailString)) {
                Toast.makeText(this, getString(R.string.email_wrong), Toast.LENGTH_SHORT).show();
                return;
            }
            values.put(COLUMN_ITEM_EMAIL, emailString);

            if (TextUtils.isEmpty(priceString) || TextUtils.equals("0", priceString) || TextUtils.equals("0.0", priceString)) {
                Toast.makeText(this, getString(R.string.price_required), Toast.LENGTH_SHORT).show();
                return;
            }
            values.put(COLUMN_ITEM_PRICE, priceString);

            if (TextUtils.isEmpty(quantityString)) {
                Toast.makeText(this, getString(R.string.quantity_required), Toast.LENGTH_SHORT).show();
                return;
            }
            values.put(COLUMN_ITEM_QUANTITY, quantityString);
            // Create a ContentValues object where column names are the keys,
            // and item attributes from the editor are the values.

            if (TextUtils.isEmpty(mCurrentPhotoPath)) {
                Toast.makeText(this, getString(R.string.picture_required), Toast.LENGTH_SHORT).show();
                return;
            }
            values.put(COLUMN_ITEM_PICTURE, mCurrentPhotoPath);

            // This is a NEW item, so insert a new item into the provider,
            // returning the content URI for the item item.
            Uri newUri = getContentResolver().insert(CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_item_failed), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_item_success), Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            String quantityString = mQuantityText.getText().toString().trim();
            // Check if this is supposed to be a new item
            // and check if all the fields in the editor are blank
            if (mCurrentItemUri == null &&
                    TextUtils.isEmpty(nameString) &&
                    TextUtils.isEmpty(descriptionString) &&
                    TextUtils.isEmpty(emailString) &&
                    TextUtils.isEmpty(priceString) &&
                    TextUtils.isEmpty(quantityString) &&
                    mImageByteArray == null) {

                Toast.makeText(this, R.string.warning_invalid_input, Toast.LENGTH_SHORT).show();
                // Since no fields were modified, we can return early without creating a new item.
                // No need to create ContentValues and no need to do any ContentProvider operations.
                return;
            }
            if (TextUtils.isEmpty(nameString)) {
                Toast.makeText(this, getString(R.string.product_required), Toast.LENGTH_SHORT).show();
                return;
            }
            ContentValues values = new ContentValues();
            values.put(COLUMN_ITEM_NAME, nameString);

            if (TextUtils.isEmpty(descriptionString)) {
                Toast.makeText(this, getString(R.string.description_required), Toast.LENGTH_SHORT).show();
                return;
            }
            values.put(COLUMN_ITEM_DESCRIPTION, descriptionString);

            if (TextUtils.isEmpty(emailString)) {
                Toast.makeText(this, getString(R.string.email_required), Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isValidEmail(emailString)) {
                Toast.makeText(this, getString(R.string.email_wrong), Toast.LENGTH_SHORT).show();
                return;
            }
            values.put(COLUMN_ITEM_EMAIL, emailString);

            if (TextUtils.isEmpty(priceString)) {
                Toast.makeText(this, getString(R.string.price_required), Toast.LENGTH_SHORT).show();
                return;
            }
            values.put(COLUMN_ITEM_PRICE, priceString);
            // Create a ContentValues object where column names are the keys,
            // and item attributes from the editor are the values.
            values.put(COLUMN_ITEM_QUANTITY, quantityString);
            values.put(COLUMN_ITEM_PICTURE, mCurrentPhotoPath);
            // Otherwise this is an EXISTING item, so update the itme with content URI: mCurrentItemUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentItemUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_item_failed), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_item_success), Toast.LENGTH_SHORT).show();
                finish();
            }
        }

    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the item.
                deleteItem();
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

    /**
     * Perform the deletion of the item in the database.
     */
    private void deleteItem() {
        // Only perform the delete if this is an existing item.
        if (mCurrentItemUri != null) {
            // Call the ContentResolver to delete the item at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentItemUri
            // content URI already identifies the item that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_item_failed), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_item_successful), Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new item, hide the "Delete" menu item.
        if (mCurrentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save item in database
                try {
                    saveItem();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the item hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(DetailAcitvity.this);
                    return true;
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(DetailAcitvity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the editor shows all item attributes, define a projection that contains
        // all columns from the items table
        String[] projection = {
                ItemEntry._ID,
                COLUMN_ITEM_NAME,
                COLUMN_ITEM_DESCRIPTION,
                COLUMN_ITEM_EMAIL,
                COLUMN_ITEM_PRICE,
                COLUMN_ITEM_QUANTITY,
                COLUMN_ITEM_PICTURE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentItemUri,         // Query the content URI for the current item
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of item attributes that we're interested in
            id = cursor.getInt(cursor.getColumnIndex(_ID));
            int nameColumnIndex = cursor.getColumnIndex(COLUMN_ITEM_NAME);
            int descriptionColumnIndex = cursor.getColumnIndex(COLUMN_ITEM_DESCRIPTION);
            int emailColumnIndex = cursor.getColumnIndex(COLUMN_ITEM_EMAIL);
            int priceColumnIndex = cursor.getColumnIndex(COLUMN_ITEM_PRICE);
            int stockColumnIndex = cursor.getColumnIndex(COLUMN_ITEM_QUANTITY);
            int pictureColumnIndex = cursor.getColumnIndex(COLUMN_ITEM_PICTURE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String description = cursor.getString(descriptionColumnIndex);
            String email = cursor.getString(emailColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);
            int quantity = cursor.getInt(stockColumnIndex);
            String picture = cursor.getString(pictureColumnIndex);

            mQuantity = quantity;
            mEmail = email;
            mName = name;

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mDescriptionEditText.setText(description);
            mEmailEditText.setText(email);
            mPriceEditText.setText(Double.toString(price));
            mQuantityText.setText(Integer.toString(quantity));
            mPriceText.setText(Double.toString(price));
            mCurrentPhotoPath = picture;
            loadImage();
            cursor.close();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mDescriptionEditText.setText("");
        mEmailEditText.setText("");
        mPriceEditText.setText("");
        mQuantityText.setText("");

    }

    //This is the beginning of the logic having to do with taking and storing pictures
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            loadImage();
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e("MainActivity", "Error occurred while creating image file");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void loadImage() {

        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        Log.e("targetW", Integer.toString(targetW));
        Log.e("targetH", Integer.toString(targetH));

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;

        bmOptions.inPurgeable = true;
//            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cat);
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        mImageView.setImageBitmap(bitmap);
    }

    public void sellItem(final long id, int quantity, String email, String name) {
        ContentUris.withAppendedId(CONTENT_URI, id);
        mQuantity = quantity;
        mEmail = email;
        mName = name;


        if (mQuantity > 0) {
            mQuantity--;
            ContentValues values = new ContentValues();
            values.put(COLUMN_ITEM_QUANTITY, mQuantity);
            getContentResolver().update(mCurrentItemUri, values, null, null);
            Toast toast;
            Toast.makeText(this, getString(R.string.sold) + " " + mName, Toast.LENGTH_SHORT).show();

        } else {

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("You have no items on stock!");
            alert.setMessage("Would you like to place an order?");

            alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    String subject = "XYZ order for: 5 pieces of " + mName;
                    String orderSummary = "We would like to order 5 pieces of " + mName;
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto: " + mEmail));
                    intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                    intent.putExtra(Intent.EXTRA_TEXT, orderSummary);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            });

            alert.setNegativeButton("NO. Will do later", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                }
            });

            alert.show();

        }
        mQuantityText.setText(Integer.toString(mQuantity));
    }

    private void addItemToStock(long id, int quantity) {
        ContentUris.withAppendedId(CONTENT_URI, id);
        mQuantity = quantity;

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Add quantity to Stock ");

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setRawInputType(Configuration.KEYBOARD_12KEY);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                // Do something with value!
                String mInput = input.getText().toString().trim();
                int finalValue = Integer.parseInt(mInput);

                mQuantity = mQuantity + finalValue;
                if (mQuantity > 999) {
                    Toast.makeText(getApplicationContext(), finalValue + " " + getString(R.string.tobig), Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_ITEM_QUANTITY, mQuantity);
                    getContentResolver().update(mCurrentItemUri, values, null, null);


                    mQuantityText.setText(String.valueOf(mQuantity));
                    Toast.makeText(getApplicationContext(), finalValue + " " + getString(R.string.added), Toast.LENGTH_SHORT).show();
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();

    }

    private void makeAnOrder(long id, final String email, final String name) {
        ContentUris.withAppendedId(CONTENT_URI, id);

        mEmail = email;
        mName = name;

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("How many items would you like to order?");
        alert.setMessage("Your requested number of items is going " +
                "to be passed to suppliers email which you can still edit");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setRawInputType(Configuration.KEYBOARD_12KEY);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                String mInput = input.getText().toString();
//                int finalValue = Integer.parseInt(mInput);

                String subject = "XYZ order for: " + mInput + " pieces of " + mName;
                String orderSummary = "We would like to order " + mInput + " pieces of " + mName;
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto: " + mEmail));
                intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                intent.putExtra(Intent.EXTRA_TEXT, orderSummary);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

}

