package com.washington.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import com.washington.inventoryapp.data.InventoryContract.InventoryEntry;

public class InventoryActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int INVENTORY_LOADER = 0;

    private InvCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // Sets up the image button to open the DetailsActivity.
        ImageButton imageButton = (ImageButton) findViewById(R.id.image_button);
        imageButton.setOnClickListener(new View.OnClickListener() {
            /*
            The onClick method to take the user to the DetailsActivity when the
            image button is clicked on.
             */
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InventoryActivity.this, DetailsActivity.class);
                startActivity(intent);
            }
        });

        // Finds the ListView that contains the item data.
        ListView inventoryListView = (ListView) findViewById(R.id.list);

        // Sets the empty view on the ListView, but only if the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        inventoryListView.setEmptyView(emptyView);

        /*
        Sets up an adapter for the ListView of items. Since there are no entries yet,
        remain as null to the Cursor until the Loader finishes.
         */
        mCursorAdapter = new InvCursorAdapter(this, null);
        inventoryListView.setAdapter(mCursorAdapter);

        // Sets up an item Click Listener
        inventoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(InventoryActivity.this, DetailsActivity.class);

               /*
               The content Uri which handles attaching an item to its ID.
               Example: "content://com.example.android.inventory/inventory/2" would be the URI
               for the item with an ID of 2 being clicked on.
                */
                Uri currentItemUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent.
                intent.setData(currentItemUri);

                // Launch the {@link DetailsActivity} to display the data for the current item.
                startActivity(intent);
            }
        });

        // Starts the loader
        getSupportLoaderManager().initLoader(INVENTORY_LOADER, null, this);
    }

    /**
     * Helper method that inserts hardcoded item data into the database. This is
     * only for debugging purposes.
     */
    private void insertItem() {
        /*
        Create a ContentValues object where column names are the keys and the item details
        are the values.
        */
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_ITEM_NAME, "Headphones");
        values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, "45");
        values.put(InventoryEntry.COLUMN_ITEM_PRICE, "5");

        /*
        Insert a new row for the item into the provider using the ContentResolver.
        The content Uri will indicate that a new row should be inserted into the table.
        A new content Uri will be given to access this item's data later.
        */
        Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
    }

    /**
     * Helper method to delete all items in the database.
     */
    private void deleteAllItems() {
        int rowsDeleted = getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);
        Log.v("InventoryActivity", rowsDeleted + " rows deleted from database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /*
        Inflate the menu options from the res/menu/inventory_menu.xml file.
        This adds menu items to the app bar.
        */
        getMenuInflater().inflate(R.menu.inventory_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Responds to the "Generate Item" menu option being clicked on
            case R.id.generate_item:
                insertItem();
                return true;
            // Responds to the "Delete all Items" menu option being clicked on.
            case R.id.action_delete_all_entries:
                deleteAllItems();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a column which specifies the column's row that we care about
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_ITEM_NAME,
                InventoryEntry.COLUMN_ITEM_QUANTITY,
                InventoryEntry.COLUMN_ITEM_PRICE,
                InventoryEntry.COLUMN_ITEM_IMAGE,
                InventoryEntry.COLUMN_ITEM_SUPPLIER};

        // This loader will execute the ContentProvider's query method on a background thread.
        return new CursorLoader(this,             // Parent activity context
                InventoryEntry.CONTENT_URI,       // Provider content with URI  to query
                projection,                       // Columns to include in the resulting Cursor
                null,                             // No selection clause
                null,                             // No selection arguments
                null);                            // Default sort order
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        // Swaps in the new cursor with the updated item data.
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        // Called when the last cursor from onLoadFinished() needs to be deleted.
        mCursorAdapter.swapCursor(null);
    }
}
