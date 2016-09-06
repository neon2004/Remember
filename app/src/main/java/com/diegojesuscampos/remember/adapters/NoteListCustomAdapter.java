package com.diegojesuscampos.remember.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.diegojesuscampos.remember.R;
import com.evernote.edam.type.Note;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoteListCustomAdapter extends BaseAdapter {

    private static final String datePattern = "HH:mm dd/MM/yyyy";
    private List<Note> notes;
    private LayoutInflater mInflater;

    public NoteListCustomAdapter(Context context, List<Note> noteList) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        notes = noteList;
    }

    @Override
    public int getCount() {
        return notes.size();
    }

    @Override
    public Note getItem(int position) {
        return notes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Note note = getItem(position);
        if (note != null) {
            convertView = mInflater.inflate(R.layout.list_item, parent, false);

            TextView nameText = (TextView) convertView.findViewById(R.id.name);
            TextView updatedDateText = (TextView) convertView.findViewById(R.id.date);

            if (nameText != null) {
                nameText.setText(note.getTitle());
            }
            if (updatedDateText != null) {
                SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern, Locale.ENGLISH);
                updatedDateText.setText(dateFormatter.format(new Date(note.getUpdated())));
            }
        }

        return convertView;
    }

}