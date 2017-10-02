package com.washington.inventoryapp.data;

/**
 * Created by Brent on 8/21/2017.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.washington.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * The database helper for the app. It manages database creation and the database version.
 */
public class InventoryDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = InventoryDbHelper.class.getSimpleName();

    /**
     * Name of database file
     */
    private static final String DATABASE_NAME = "inventory.db";

    /**
     * Database version. If the database schema is changed, the version must be incremented.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link InventoryDbHelper}
     *
     * @param context of the app
     */
    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database has been created for the first time.
     *
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Creates a String for the inventory table.
        String SQL_CREATE_INVENTORY_TABLE = "CREATE TABLE " + InventoryEntry.TABLE_NAME + " ("
                // ID is set to increase with the number of entries in the table.
                + InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                // The item must have a name and cannot be null.
                + InventoryEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL, "
                /*
                The item must have a current quantity. If the user doesn't enter a quantity,
                then it will be 0.
                 */
                + InventoryEntry.COLUMN_ITEM_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                // The item must have a price and cannot be null.
                + InventoryEntry.COLUMN_ITEM_PRICE + " TEXT NOT NULL, "
                // The image of the item
                + InventoryEntry.COLUMN_ITEM_IMAGE + " TEXT, "
                // The item's supplier
                + InventoryEntry.COLUMN_ITEM_SUPPLIER + " TEXT " + ");";

        db.execSQL(SQL_CREATE_INVENTORY_TABLE);

        Log.v(LOG_TAG, SQL_CREATE_INVENTORY_TABLE);
    }

    /**
     * This method will be called when the database needs to upgraded.
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(" DROP TABLE IF EXISTS " + InventoryEntry.TABLE_NAME);
        onCreate(db);
    }
}

