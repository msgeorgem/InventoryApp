package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;


/**
 * Created by Marcin on 2017-03-30.
 */



public final class InventoryContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private InventoryContract() {}

    /**
     *
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.inventoryapp/items/ is a valid path for
     * looking at pet data. content://com.example.android.inventoryapp/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_ITEMS = "items";

    /**
     * Inner class that defines constant values for the items database table.
     * Each entry in the table represents a single item.
     */

    public static abstract class ItemEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ITEMS);
        public static final String TABLE_NAME = "items";
        public static final String TEMP_BACKUP_TABLE_NAME = "items_backup";
        public static final String UPLOAD_TABLE_NAME = "items_upload";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_ITEM_NAME = "name";
        public static final String COLUMN_ITEM_TYPE = "type";
        public static final String COLUMN_ITEM_PRODUCER = "producer";
        public static final String COLUMN_ITEM_WEIGHT = "weight";
        public static final String COLUMN_ITEM_PICTURE = "producer";

        /**
         * Possible values for the COLUMN_TYPEtype.
         */

        public static final int TYPE_UNKNOWN = 0;
        public static final int TYPE_AAA = 1;
        public static final int TYPE_BBB = 2;


        public static boolean isValidType(int item) {
            return item == TYPE_UNKNOWN || item == TYPE_AAA || item == TYPE_BBB;
        }
        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of items.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single item.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;
    }

}
