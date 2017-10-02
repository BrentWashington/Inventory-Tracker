package com.washington.inventoryapp.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Brent on 8/21/2017.
 */

public class InventoryContract {

    // Made private so the contract class is not accidentally instantiated.
    private InventoryContract() {
    }

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.inventory";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_INVENTORY = "inventory";

    public static class InventoryEntry implements BaseColumns {

        /**
         * The content URI to access the inventory data in the provider.
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI,
                PATH_INVENTORY);

        public static Uri inventoryUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        // Name of the table
        public static final String TABLE_NAME = "Inventory";

        // The _ID column
        public static final String _ID = BaseColumns._ID;

        /**
         * The name of the item
         * <p>
         * Type: Text
         */
        public static final String COLUMN_ITEM_NAME = "name";

        /**
         * The quantity of the item in stock
         * <p>
         * Type: Integer
         */
        public static final String COLUMN_ITEM_QUANTITY = "quantity";

        /**
         * The price of the item
         * <p>
         * Type: Integer
         */
        public static final String COLUMN_ITEM_PRICE = "price";

        // The supplier that the user can contact for an item
        public static final String COLUMN_ITEM_SUPPLIER = "supplier";

        // An image of the item
        public static final String COLUMN_ITEM_IMAGE = "image";

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of items.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/"
                        + PATH_INVENTORY;

        /**
         * The MIME type of the {@link #CONTENT_URI} for one item.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/"
                        + PATH_INVENTORY;
    }
}

