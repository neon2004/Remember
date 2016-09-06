package com.diegojesuscampos.remember.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.evernote.edam.type.Note;
import com.diegojesuscampos.remember.R;


public class NoteDetailFragment extends Fragment {
    public static final String EXTRA_NOTE_GUID = "extra_note_guid";

    private String noteGuid;

    TextView titleText;
    TextView contentText;

    public NoteDetailFragment() {
    }

    public static NoteDetailFragment newInstance(String noteGuid) {
        NoteDetailFragment fragment = new NoteDetailFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_NOTE_GUID, noteGuid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            noteGuid = getArguments().getString(EXTRA_NOTE_GUID);
        }
        getActivity().setTitle(getActivity().getString(R.string.note_detail_title));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_note_detail, container, false);
        initUIReferences(root);
        loadNoteContent();
        return root;
    }

    private void initUIReferences(View root) {
        titleText = (TextView) root.findViewById(R.id.title_detail_value);
        contentText = (TextView) root.findViewById(R.id.content_detail_value);
    }

    private void loadNoteContent() {
        new GetNoteTask(noteGuid).execute();
    }

    class GetNoteTask extends AsyncTask<Void, Void, Note> {

        private String noteGuid;

        public GetNoteTask(String noteGuid) {
            this.noteGuid = noteGuid;
        }

        protected Note doInBackground(Void... urls) {
            return new Note();
        }

        protected void onPostExecute(Note note) {
//            titleText.setText(note.getTitle());
//            contentText.setText(note.getContent());
        }
    }


}
