package com.diegojesuscampos.remember.fragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.diegojesuscampos.remember.App;
import com.diegojesuscampos.remember.activities.MainActivity;
import com.evernote.client.android.EvernoteUtil;
import com.evernote.edam.type.Note;
import com.diegojesuscampos.remember.R;


public class AddNoteFragment extends DialogFragment {
    private TextInputEditText titleField;
    private TextInputEditText contentField;

    private TextInputLayout titleLabel;
    private TextInputLayout contentLabel;
    public static MainActivity callBack;
    private FloatingActionButton saveButton;

    public static AddNoteFragment AddNoteFragment() {
        AddNoteFragment f = new AddNoteFragment();
        return f;
    }

    public static AddNoteFragment newInstance() {
        return new AddNoteFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_note, container, false);
        initUIReferences(view);
        initEvents(view);
        return view;
    }

    private void initEvents(View view) {
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNote();
            }
        });
    }

    private void initUIReferences(View view) {
        //UI References
        titleField = (TextInputEditText) view.findViewById(R.id.title_value);
        contentField = (TextInputEditText) view.findViewById(R.id.content_value);

        titleLabel = (TextInputLayout) view.findViewById(R.id.title_label);
        contentLabel = (TextInputLayout) view.findViewById(R.id.content_label);

        saveButton = (FloatingActionButton) view.findViewById(R.id.save_edit_button);
    }

    private void addNote() {
        if (areMandatoryFieldsFilled()) {
            Note note = getNoteContent();
            new AddNoteTask(note).execute();
        }
    }


    private Note getNoteContent() {
        Note note = new Note();
        note.setTitle(titleField.getText().toString());
        note.setContent(EvernoteUtil.NOTE_PREFIX + contentField.getText().toString() + EvernoteUtil.NOTE_SUFFIX);
        return note;
    }

    private boolean areMandatoryFieldsFilled() {
        boolean correct = true;
        String title = titleField.getText().toString();
        String content = contentField.getText().toString();

        if (TextUtils.isEmpty(title)) {
            titleLabel.setError(getString(R.string.campo_error));
            correct = false;
        } else {
            titleLabel.setError(null);
        }

        if (TextUtils.isEmpty(content)) {
            contentLabel.setError(getString(R.string.campo_error));
            correct = false;
        } else {
            contentLabel.setError(null);
        }

        return correct;
    }


    private void showAddNoteError() {
        Toast.makeText(getActivity(), getActivity().getString(R.string.add_note_error), Toast.LENGTH_LONG).show();
    }

    private void showSuccessUpdate() {
        Toast.makeText(getActivity(), getActivity().getString(R.string.note_save_success), Toast.LENGTH_LONG).show();
    }

    private void showNotesScreen(Boolean success) {
        if (!success) {
            showAddNoteError();
        } else {
            showSuccessUpdate();
            callBack.cargarFragment(App.FRAGMENT_LOAD_NOTES,null);
        }
    }

    private class AddNoteTask extends AsyncTask<Void, Void, Boolean> {

        private Note note;

        public AddNoteTask(Note note) {
            this.note = note;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            showNotesScreen(result);
        }
    }
}
