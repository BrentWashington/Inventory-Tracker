package com.washington.inventoryapp;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static com.washington.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * Created by Brent on 8/21/2017.
 */

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // Identifier for the data item loader
    private static final int EXISTING_ITEM_LOADER = 0;

    // The photo request code for the details editor
    public static final int PHOTO_REQUEST = 20;

    // The external storage request permission code
    public static final int EXTERNAL_STORAGE_REQUEST_PERMISSION = 21;

    // The content Uri for the current item
    private Uri mCurrentItemUri;

    // The image Uri
    private Uri mCurrentImageUri;

    // The image of the item
    private ImageView mItemImage;

    // The EditText field for the name of the item
    private EditText mItemName;

    // The EditText field for the quantity of the item
    private EditText mItemQuantity;

    // The EditText field for the price of the item
    private EditText mItemPrice;

    // The EditText field for the supplier
    private EditText mItemSupplier;

    // The supplier's email
    private String mSupplierEmail;

    // Which item the user wants to order
    private String mOrderWhichItem;

    // The quantity of the item the user wants to order (e.g. 10)
    private int mOrderQuantity = 10;

    // Button that deletes the item
    private Button deleteItem;

    // Button that orders more of an item
    private Button orderItem;

    // Button that updates an item's details
    private Button updateItem;

    // Button that increases the quantity of the item
    private Button mQuantityIncrement;

    // Button that decreases the quantity of the item
    private Button mQuantityDecrement;

    /**
     * Boolean that tells us if the item has been edited or not
     * <p>
     * Yes (true)
     * No (false)
     */
    private boolean mItemHasChanged = false;

    /**
     * OnTouchListener makes note if any of the views have been touched, which will turn
     * the mItemHasChanged boolean to true.
     */
    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // The name of the item in the editor
        mItemName = (EditText) findViewById(R.id.input_name);
        mItemName.setOnTouchListener(touchListener);

        // The quantity of the item in the editor
        mItemQuantity = (EditText) findViewById(R.id.input_quantity);
        mItemQuantity.setOnTouchListener(touchListener);

        // The price of the item in the editor
        mItemPrice = (EditText) findViewById(R.id.input_price);
        mItemPrice.setOnTouchListener(touchListener);

        // The item supplier in the editor
        mItemSupplier = (EditText) findViewById(R.id.detail_supplier_edit);
        mItemSupplier.setOnTouchListener(touchListener);

        // The image of the item in the editor
        mItemImage = (ImageView) findViewById(R.id.details_image);
        mItemImage.setOnTouchListener(touchListener);
        // Set a click listener on the item image
        mItemImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setItemImageUpdate(v);
            }
        });

        // The increment button for the item's quantity
        mQuantityIncrement = (Button) findViewById(R.id.increment);
        // Set a click listener on the increment button in the editor
        mQuantityIncrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                increment();
                mItemHasChanged = true;
            }
        });

        // The decrement button for the item's quantity
        mQuantityDecrement = (Button) findViewById(R.id.decrement);
        // Set a click listener on the decrement button in the editor
        mQuantityDecrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decrement();
                mItemHasChanged = true;
            }
        });

        // Button that orders more of an item
        orderItem = (Button) findViewById(R.id.order_button);
        orderItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemOrder();
            }
        });

        // Button that deletes the current item
        deleteItem = (Button) findViewById(R.id.delete_button);
        /*
        Set a click listener on the delete button. If the user clicks the
        delete button with unsaved changes, show a confirmation dialog.
         */
        deleteItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });

        // Button that saves/updates an item
        updateItem = (Button) findViewById(R.id.save_button);
        // Set a click listener on the save button
        updateItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveItem();
            }
        });

        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        // Statement if the user is adding a new item
        if (mCurrentItemUri == null) {
            // If the item is not in the database, set the title to "Add Item"
            setTitle(getString(R.string.detail_activity_title_new_item));
        } else {
            // If the item already exists, set the title to "Edit Item"
            setTitle(getString(R.string.detail_activity_title_edit_item));
            /*
            Initializes a loader to read the item data from the database and display
            the current values in the editor.
             */
            getSupportLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }
    }

    /**
     * Method for the image selector after the user accepts permissions request
     */
    private void getItemImage() {
        // Send an intent that takes the user to their pictures
        Intent galleryIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        // Get the data from the external storage directory
        File gallery = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        String path = gallery.getPath();
        Uri imageDataUri = Uri.parse(path);
        galleryIntent.setDataAndType(imageDataUri, "image/*");
        startActivityForResult(galleryIntent, PHOTO_REQUEST);
    }

    /**
     * Handles the permissions for accessing the user's images
     */
    public void setItemImageUpdate(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Asks the user for permission
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                // Run the method if the user gives permission
                getItemImage();
            } else {
                /*
                If permissions have not already been given, make a request to the
                user for permissions.
                 */
                String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                // Use the external storage request permissions code to request permissions
                requestPermissions(permissions, EXTERNAL_STORAGE_REQUEST_PERMISSION);
            }
        } else {
            getItemImage();
        }
    }

    /*
    Method for when the user accepts the permissions request. Kept getting a "method does not overwrite
    method from its superclass error," so I used code from a stackoverflow.com post.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // Call the parent constructor with arguments
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // If the user accepts, access their stored photos
        if (requestCode == EXTERNAL_STORAGE_REQUEST_PERMISSION && grantResults[0]
                == PackageManager.PERMISSION_GRANTED) {
            getItemImage();
        } else {
            // Do nothing
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == PHOTO_REQUEST && resultCode == RESULT_OK) {
            if (resultData != null) {
                mCurrentImageUri = resultData.getData();
                mItemImage.setImageURI(mCurrentImageUri);
                mItemImage.invalidate();
            }
        }
    }

    /**
     * Method that decreases the item quantity
     */
    private void decrement() {
        String currentQuantity = mItemQuantity.getText().toString();
        int lastQuantity;
        /*
        If the quantity input is empty or equal to 0, then don't decrease.
        The user will be unable to decrease to a negative quantity.
         */
        if (currentQuantity.isEmpty() || currentQuantity.equals("0")) {
            return;
            // If not, subtract 1 from the previous quantity.
        } else {
            lastQuantity = Integer.parseInt(currentQuantity);
            mItemQuantity.setText(String.valueOf(lastQuantity - 1));
        }
    }

    /**
     * Method that increases the item quantity
     */
    private void increment() {
        String currentQuantity = mItemQuantity.getText().toString();
        int lastQuantity;
        /*
        If the quantity input is empty, then don't increase because there is
        no entered quantity to increase.
         */
        if (currentQuantity.isEmpty()) {
            return;
            // If not, add 1 to the previous quantity.
        } else {
            lastQuantity = Integer.parseInt(currentQuantity);
            mItemQuantity.setText(String.valueOf(lastQuantity + 1));
        }
    }

    /**
     * Gets user input and inserts a new item into the database.
     */
    private void saveItem() {
        // Read from the input fields and use trim to eliminate trailing white space.
        String nameString = mItemName.getText().toString().trim();
        String quantityString = mItemQuantity.getText().toString().trim();
        String priceString = mItemPrice.getText().toString().trim();
        String imageString;
        if (mCurrentImageUri != null) {
            imageString = mCurrentImageUri.toString();
        } else {
            imageString = null;
            mItemImage.setImageResource(R.mipmap.ic_launcher_round);
        }
        String supplierString = mItemSupplier.getText().toString().trim();

        //Checks if this is a new item and if every editor field is blank.
        if (mCurrentItemUri == null &&
                TextUtils.isEmpty(nameString) &&
                TextUtils.isEmpty(quantityString) &&
                TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(imageString) &&
                TextUtils.isEmpty(supplierString)) {
            /*
            Since no fields were modified, we can return early without creating a new item.
            No need to create ContentValues and no need to do any ContentProvider operations.
             */
            return;
        }

        /*
        Create a ContentValues object where column names are the keys,
        and item details from the editor are the values.
        */
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_ITEM_NAME, nameString);
        values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, quantityString);
        values.put(InventoryEntry.COLUMN_ITEM_PRICE, priceString);
        values.put(InventoryEntry.COLUMN_ITEM_IMAGE, imageString);
        values.put(InventoryEntry.COLUMN_ITEM_SUPPLIER, supplierString);

        // Determines if this is a new or existing item and if mCurrentItemUri is null.
        if (mCurrentItemUri == null) {
            /*
            Sanity Check: check that the item's name is not null. If it is, show a Toast
            telling the user that they need to enter a name.
             */
            if (nameString.isEmpty() || nameString.equals("")) {
                Toast.makeText(this, "Please enter a valid name", Toast.LENGTH_SHORT).show();
                return;
            }

            // Sanity Check: check that the item's current quantity is not null.
            if (quantityString.isEmpty()) {
                Toast.makeText(this, "Please enter a valid quantity", Toast.LENGTH_SHORT).show();
                return;
            }

            // Sanity Check: check that the item's price is valid and not null.
            if (priceString.isEmpty()) {
                Toast.makeText(this, "Please enter a valid price", Toast.LENGTH_SHORT).show();
                return;
            }

            // Sanity Check: check that the user added a photo
            if (mCurrentImageUri == null) {
                Toast.makeText(this, "Please add a photo", Toast.LENGTH_SHORT).show();
                return;
            }

           /*
           If this is a new item, insert a new item into the provider, returning the
           content URI for the new item.
            */
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

            // Show a toast based on the results of the insertion.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.details_insert_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // If not, then the insertion was successful
                Toast.makeText(this, getString(R.string.details_insert_item_successful),
                        Toast.LENGTH_SHORT).show();
                // Launch the InventoryActivity class after a successful insertion
                Intent successfulIntent = new Intent(this, InventoryActivity.class);
                startActivity(successfulIntent);
            }
        } else {
             /*
             Otherwise this is an existing item, so update the item with content URI
           and pass in the new ContentValues. Pass in null for the selection and selection args
           because mCurrentItemUri will already identify the row in the database that is
           being modified.
            */
            int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);

            if (nameString.isEmpty() || nameString.equals("")) {
                Toast.makeText(this, "Please enter a valid name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (quantityString.isEmpty()) {
                Toast.makeText(this, "Please enter a valid quantity", Toast.LENGTH_SHORT).show();
                return;
            }

            if (priceString.isEmpty()) {
                Toast.makeText(this, "Please enter a valid price", Toast.LENGTH_SHORT).show();
                return;
            }

            /*
            Image Sanity Check is unnecessary because the image is already saved to
            the database.
             */

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.details_update_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // If rows WERE affected, then the update was successful
                Toast.makeText(this, getString(R.string.details_item_updated),
                        Toast.LENGTH_SHORT).show();
                // Launch the InventoryActivity class after a successful update
                Intent successfulIntent = new Intent(this, InventoryActivity.class);
                startActivity(successfulIntent);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem mi) {
        switch (mi.getItemId()) {
            case android.R.id.home:
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(DetailsActivity.this);
                    return true;
                }
                /*
                If the user has made some changes but decides to discard it, show
                an unsaved changes dialog.
                 */
                DialogInterface.OnClickListener discardClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                NavUtils.navigateUpFromSameTask(DetailsActivity.this);
                            }
                        };
                // The unsaved changes dialog
                showUnsavedChangesDialog(discardClickListener);
                return true;
        }

        return super.onOptionsItemSelected(mi);
    }

    // Method for the user to order an item from their supplier
    private void itemOrder() {
        String[] supplier = {mSupplierEmail};
        Intent supplierIntent = new Intent(Intent.ACTION_SEND);
        supplierIntent.setData(Uri.parse("mailto: "));
        supplierIntent.setType("text/plain");
        // The email for the supplier
        supplierIntent.putExtra(Intent.EXTRA_EMAIL, supplier);
        // Sets "Order Shipment" as the subject in the email
        supplierIntent.putExtra(Intent.EXTRA_SUBJECT, "Order Shipment: " + mOrderWhichItem);
        // Preset text for the email order
        supplierIntent.putExtra(Intent.EXTRA_TEXT, "Item: " + mOrderWhichItem
                + " \nQuantity: " + mOrderQuantity);
        try {
            startActivity(Intent.createChooser(supplierIntent, "Send "));
        } catch (android.content.ActivityNotFoundException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the item hasn't been changed then proceed with the back button press.
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        /*
        Otherwise if there are unsaved changes, setup a dialog to warn the user. Create
        a click listener to handle the user confirming that changes should be discarded.
         */
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost if
     * they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when the user
     *                                   confirms they want to discard their changes.
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        /*
        Create an AlertDialog.Builder and set the message and click listeners for
        the positive and negative buttons on the dialog.
         */
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int id) {
                /*
                User clicked on "Keep editing" button, so dismiss the dialog and
                continue editing the item.
                 */
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });

        // Create and show the AlertDialog.
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        /*
        Create an AlertDialog.Builder and set the message and click listeners
        for the positive and negative buttons on the dialog.
        */
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // The user clicked the "Delete" button, so delete the item.
                deleteItem();
            }
        });
        // The user clicked "cancel," so dismiss the dialog.
        builder.setNegativeButton(R.string.cancel, null);

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Performs the deletion of the item in the database.
     */
    private void deleteItem() {
        // Only perform the delete if this is an existing item.
        if (mCurrentItemUri != null) {
            /*
            Call the ContentResolver to delete the item at the given content URI.
            Pass in null for the selection and selection args because the mCurrentItemUri
            content URI already identifies the item that is wanted.
             */
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);

            // Show a toast message depending on the results of the deletion.
            if (rowsDeleted == 0) {
                // If now rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.details_delete_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful.
                Toast.makeText(this, getString(R.string.details_delete_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle args) {
        // The query projection
        String[] projection = {
             /*
             Since the editor shows all item details, define a projection that contains
             all the columns in the item table.
              */
                InventoryEntry._ID,
                InventoryEntry.COLUMN_ITEM_NAME,
                InventoryEntry.COLUMN_ITEM_QUANTITY,
                InventoryEntry.COLUMN_ITEM_PRICE,
                InventoryEntry.COLUMN_ITEM_IMAGE,
                InventoryEntry.COLUMN_ITEM_SUPPLIER
        };

        // This loader will execute the ContentProvider's query method on a background thread.
        return new CursorLoader(this,
                mCurrentItemUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor.
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
            /*
            Continue moving to the first row of the cursor and reading data from it.
            (This should be the only row in the cursor)
             */
        if (cursor.moveToFirst()) {
            // The order in which the contents in the data rows are arranged
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_PRICE);
            int imageColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_IMAGE);
            int supplierColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_SUPPLIER);

            // Extract out the value from the Cursor for the given column
            String itemName = cursor.getString(nameColumnIndex);
            int itemQuantity = cursor.getInt(quantityColumnIndex);
            int itemPrice = cursor.getInt(priceColumnIndex);
            String itemImage = cursor.getString(imageColumnIndex);
            String itemSupplier = cursor.getString(supplierColumnIndex);
            /*
            Use the name of the item to tell their supplier what they want
            to order more of.
             */
            mOrderWhichItem = itemName;
            /*
            The supplier's email
             */
            mSupplierEmail = "udasupply@nano" + itemSupplier + ".net";

            mItemName.setText(itemName);
            mItemQuantity.setText(String.valueOf(itemQuantity));
            mItemPrice.setText(String.valueOf(itemPrice));
            if (itemImage != null) {
                mCurrentImageUri = Uri.parse(itemImage);
                mItemImage.setImageURI(mCurrentImageUri);
            } else {
                mItemImage.setImageResource(R.mipmap.ic_launcher_round);
            }
            mItemSupplier.setText(itemSupplier);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mItemName.setText("");
        mItemQuantity.setText("");
        mItemPrice.setText("");
        mItemSupplier.setText("");
    }
}

