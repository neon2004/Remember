package com.diegojesuscampos.remember.activities;


import android.app.Fragment;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

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

public class MainActivity extends AppCompatActivity implements EvernoteLoginFragment.ResultCallback,ICargaFragment{

    private NoteListFragment noteListFragment;
    private AddNoteFragment addNoteFragment;
    private NoteDetailFragment noteDetailFragment;
    private static Fragment FRAGMENT_ACT;

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

                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
