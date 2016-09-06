package com.diegojesuscampos.remember.fragments;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.diegojesuscampos.remember.App;
import com.diegojesuscampos.remember.R;
import com.diegojesuscampos.remember.activities.MainActivity;
import com.evernote.edam.notestore.NoteList;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.NoteSortOrder;

import com.diegojesuscampos.remember.adapters.NoteListCustomAdapter;

import java.util.ArrayList;
import java.util.List;

public class NoteListFragment extends Fragment {

    public static final int REQUEST_NOTE_DETAIL = 1;

    private ListView noteListView;
    private NoteListCustomAdapter noteListCustomAdapter;
    private FloatingActionButton addButton;
    private List<Note> notes;
    public static MainActivity callBack;

    public static NoteListFragment  NoteListFragment() {
        NoteListFragment f = new NoteListFragment();
        return f;
    }

    public static NoteListFragment newInstance() {
        return new NoteListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_note_list, container, false);
        //enable options menu
        setHasOptionsMenu(true);
        //init list data
        notes = new ArrayList<>();
        initUIReferences(view);
        initEvents();
        // load data
        loadNotesData();
        return view;
    }

    private void initUIReferences(View view) {
        noteListView = (ListView) view.findViewById(R.id.note_list);
        noteListCustomAdapter = new NoteListCustomAdapter(getContext(), notes);
        //setup adapter
        noteListView.setAdapter(noteListCustomAdapter);

        addButton = (FloatingActionButton) view.findViewById(R.id.add_button);
    }

    private void initEvents() {
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddScreen();
            }
        });

        noteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Note currentItem = noteListCustomAdapter.getItem(i);
                showDetailScreen(currentItem);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.action_settings:
//
//                break;
        }
        return super.onOptionsItemSelected(item);
    }



    private void showAddScreen() {
        callBack.cargarFragment(App.FRAGMENT_ADD_NOTES,null);
    }

    private void showDetailScreen(Note note) {
        callBack.cargarFragment(App.FRAGMENT_DETAIL_NOTES,note);
    }


    private void loadNotesData() {
        new NotesLoadTask().execute();
    }

    private class NotesLoadTask extends AsyncTask<Void, Void, NoteList> {

        @Override
        protected NoteList doInBackground(Void... voids) {
            return null;
        }

        @Override
        protected void onPostExecute(NoteList noteList) {
            if (noteList != null) {
                //clear data but not losing reference to keep adapter working
                notes.clear();
                notes.addAll(noteList.getNotes());
                //refresh data
                noteListCustomAdapter.notifyDataSetChanged();
            }
        }
    }
}