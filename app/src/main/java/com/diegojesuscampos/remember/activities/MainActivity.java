package com.diegojesuscampos.remember.activities;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.diegojesuscampos.remember.App;
import com.diegojesuscampos.remember.R;
import com.diegojesuscampos.remember.fragments.AddNoteFragment;
import com.diegojesuscampos.remember.fragments.NoteDetailFragment;
import com.diegojesuscampos.remember.fragments.NoteListFragment;
import com.diegojesuscampos.remember.interfaces.ICargaFragment;
import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.login.EvernoteLoginFragment;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.NoteSortOrder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity implements EvernoteLoginFragment.ResultCallback,ICargaFragment{

    private NoteListFragment noteListFragment;
    private AddNoteFragment addNoteFragment;
    private NoteDetailFragment noteDetailFragment;
    private static Fragment FRAGMENT_ACT;
    private static final int SELECT_IMAGE = 1;
    //Instance of selected image
    public static ImageData mImageData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (!isTaskRoot()) {
            //noinspection ConstantConditions
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        autenticar();
    }

    // NOS AUTETICAMOS CON EVERNOTE O CARGAMOS LAS NOTAS
    private void autenticar() {
        if (!EvernoteSession.getInstance().isLoggedIn()) {
            EvernoteSession.getInstance().authenticate(MainActivity.this);
        }else{
            cargarFragment(App.FRAGMENT_LOAD_NOTES, null);
        }
    }

    // CUANDO SE TERMINA DE LOGEAR LANZAMOS LA CARGA DE FRAGMENT PARA IR A LA CARGA DE NOTAS
    @Override
    public void onLoginFinished(boolean successful) {
        cargarFragment(App.FRAGMENT_LOAD_NOTES, null);
    }

    // METODO DONDE SE CARGARA LOS DIFERENTES FRAGMENTS
    @Override
    public void cargarFragment(String fragment, Note note) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        switch (fragment){
            case App.FRAGMENT_LOAD_NOTES: {
                noteListFragment = NoteListFragment.newInstance();
                noteListFragment.callBack =  MainActivity.this;
                ft.replace(R.id.content_main, noteListFragment);
                FRAGMENT_ACT = noteListFragment;
                break;
            }
            case App.FRAGMENT_ADD_NOTES:{
                addNoteFragment = AddNoteFragment.newInstance();
                addNoteFragment.callBack = MainActivity.this;
                ft.replace(R.id.content_main, addNoteFragment);
                FRAGMENT_ACT = addNoteFragment;
                break;
            }
            case App.FRAGMENT_DETAIL_NOTES:{
                noteDetailFragment = NoteDetailFragment.newInstance(note.getGuid());
                ft.replace(R.id.content_main, noteDetailFragment);
                FRAGMENT_ACT = noteDetailFragment;
                break;
            }
        }
        ft.commit();
    }

    @Override
    public void onBackPressed() {
        if(FRAGMENT_ACT  instanceof NoteListFragment){
            finish();
        }else{
            cargarFragment(App.FRAGMENT_LOAD_NOTES, null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if (FRAGMENT_ACT instanceof NoteListFragment) {
            inflater.inflate(R.menu.menu_filter_logout, menu);
        } else {
            inflater.inflate(R.menu.menu_logout, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.filtra_titulo:
                NoteListFragment.loadNotesData(NoteSortOrder.TITLE);
                break;
            case R.id.filtra_fecha:
                NoteListFragment.loadNotesData(NoteSortOrder.CREATED);
                break;
            case R.id.salir:
                EvernoteSession.getInstance().logOut();
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Called when the user taps the "Select Image" button.
     * <p/>
     * Sends the user to the image gallery to choose an image to share.
     */
    public void startSelectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(intent, SELECT_IMAGE);
    }

    public class ImageData {
        public Bitmap imageBitmap;
        public String filePath;
        public String mimeType;
        public String fileName;
    }

    /**
     * Called when the control returns from an activity that we launched.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            //Grab image data when picker returns result
            case SELECT_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    new ImageSelector().execute(data);
                }
                break;
        }
    }


    private class ImageSelector extends AsyncTask<Intent, Void, ImageData> {

        // using showDialog, could use Fragments instead
        @SuppressWarnings("deprecation")
        @Override
        protected void onPreExecute() {
//            showDialog(DIALOG_PROGRESS);
        }

        protected ImageData doInBackground(Intent... intents) {
            if (intents == null || intents.length == 0) {
                return null;
            }

            Uri selectedImage = intents[0].getData();
            String[] queryColumns = {
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.MIME_TYPE,
                    MediaStore.Images.Media.DISPLAY_NAME};

            Cursor cursor = null;
            ImageData image = null;
            try {
                cursor = getContentResolver().query(selectedImage, queryColumns, null, null, null);
                if (cursor.moveToFirst()) {
                    image = new ImageData();

                    image.filePath = cursor.getString(cursor.getColumnIndex(queryColumns[1]));
                    image.mimeType = cursor.getString(cursor.getColumnIndex(queryColumns[2]));
                    image.fileName = cursor.getString(cursor.getColumnIndex(queryColumns[3]));

                    // First decode with inJustDecodeBounds=true to check dimensions
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;

                    Bitmap tempBitmap = BitmapFactory.decodeFile(image.filePath, options);

                    int dimen = 0;
                    int x = 0;
                    int y = 0;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                        Point size = new Point();
                        getWindowManager().getDefaultDisplay().getSize(size);

                        x = size.x;
                        y = size.y;
                    } else {
                        x = getWindowManager().getDefaultDisplay().getWidth();
                        y = getWindowManager().getDefaultDisplay().getHeight();
                    }

                    dimen = x < y ? x : y;

                    // Calculate inSampleSize
                    options.inSampleSize = calculateInSampleSize(options, dimen, dimen);

                    // Decode bitmap with inSampleSize set
                    options.inJustDecodeBounds = false;

                    tempBitmap = BitmapFactory.decodeFile(image.filePath, options);

                    image.imageBitmap = Bitmap.createScaledBitmap(tempBitmap, dimen, dimen, true);
                    tempBitmap.recycle();

                }
            } catch (Exception e) {
//                Log.e(LOGTAG, "Error retrieving image");
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return image;
        }

        /**
         * Calculates a sample size to be used when decoding a bitmap if you don't
         * require (or don't have enough memory) to load the full size bitmap.
         * <p/>
         * <p>This function has been taken form Android's training materials,
         * specifically the section about "Loading Large Bitmaps Efficiently".<p>
         *
         * @param options   a BitmapFactory.Options object, obtained from decoding only
         *                  the bitmap's bounds.
         * @param reqWidth  The required minimum width of the decoded bitmap.
         * @param reqHeight The required minimum height of the decoded bitmap.
         * @return the sample size needed to decode the bitmap to a size that meets
         *         the required width and height.
         * @see <a href="http://developer.android.com/training/displaying-bitmaps/load-bitmap.html#load-bitmap">Load a Scaled Down Version into Memory</a>
         */
        protected int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
            // Raw height and width of image
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {
                if (width > height) {
                    inSampleSize = Math.round((float) height / (float) reqHeight);
                } else {
                    inSampleSize = Math.round((float) width / (float) reqWidth);
                }
            }
            return inSampleSize;
        }

        /**
         * Sets the image to the background and enables saving it to evernote
         *
         * @param image
         */
        // using removeDialog, could use Fragments instead
        @SuppressWarnings("deprecation")
        @Override
        protected void onPostExecute(ImageData image) {
//            removeDialog(DIALOG_PROGRESS);

            if (image == null) {
                Toast.makeText(getApplicationContext(), "Error imagen", Toast.LENGTH_SHORT).show();
                return;
            }

            if (image.imageBitmap != null) {
                addNoteFragment.addImageView(image.imageBitmap);
            }

//            if (mEvernoteSession.isLoggedIn()) {
//                mBtnSave.setEnabled(true);
//            }

            mImageData = image;
//            updateSelectionUi();
        }
    }
}

