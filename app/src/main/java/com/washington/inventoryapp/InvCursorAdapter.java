package com.washington.inventoryapp;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.washington.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * Created by Brent on 8/21/2017.
 */

public class InvCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link InvCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public InvCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
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
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the item data (in the current row pointed to by cursor) to the given
     * list item layout. Example: The item name binds to the name TextView.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(final View view, final Context context, Cursor cursor) {
        TextView item_name = (TextView) view.findViewById(R.id.name);
        TextView item_quantity = (TextView) view.findViewById(R.id.quantity);
        TextView item_price = (TextView) view.findViewById(R.id.price);
        Button sale_button = (Button) view.findViewById(R.id.sale_button);
        ImageView item_image = (ImageView) view.findViewById(R.id.item_image);

        final int inventoryId = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryEntry._ID));
        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_PRICE);
        final String itemImagePath = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_ITEM_IMAGE));

        int id = cursor.getInt(cursor.getColumnIndex(InventoryEntry._ID));
        final String itemName = cursor.getString(nameColumnIndex);
        final int itemQuantity = cursor.getInt(quantityColumnIndex);
        String itemPrice = cursor.getString(priceColumnIndex);

        // Uri for the item
        final Uri itemUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

        // Sets the image for the item
        if (itemImagePath != null) {
            item_image.setVisibility(View.VISIBLE);
            item_image.setImageURI(Uri.parse(itemImagePath));
        }

        item_name.setText(itemName);
        /*
        Display the quantity. Get a the value as a String so the program knows we want
        an integer.
         */
        item_quantity.setText(String.valueOf(itemQuantity));
        /*
        Display the price. Get a the value as a String so the program knows we want
        an integer.
         */
        item_price.setText(String.valueOf(itemPrice));

        // Set a click listener on the sale button
        sale_button.setOnClickListener(new View.OnClickListener() {
            // The sale method. Decreases the quantity by 1 when the user clicks the sale button.
            @Override
            public void onClick(View v) {
                ContentResolver contentResolver = view.getContext().getContentResolver();
                ContentValues values = new ContentValues();
                // If the item's quantity is larger than 0, decrease by 1.
                if (itemQuantity > 0) {
                    int currentItemQuantity = itemQuantity;
                    values.put(InventoryEntry.COLUMN_ITEM_QUANTITY,
                            --currentItemQuantity);

                    // Update the list view with the new quantity.
                    contentResolver.update(
                            itemUri,
                            values,
                            null,
                            null
                    );
                    context.getContentResolver().notifyChange(itemUri, null);

                    // Toast telling the user the amount they sold
                    String itemSaleToast = "Sold:(1) " + itemName;
                    Toast.makeText(context, itemSaleToast, Toast.LENGTH_SHORT).show();
                } else {
                    // If the item's quantity is 0, prompt the user to order more.
                    String itemOutOfStockToast = itemName + " is out of stock. Please order more.";
                    Toast.makeText(context, itemOutOfStockToast, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
