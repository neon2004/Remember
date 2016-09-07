package com.diegojesuscampos.remember.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
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
    private final Context ctx;
    private List<Note> notes;
    private LayoutInflater mInflater;

    public NoteListCustomAdapter(Context context, List<Note> noteList) {
        notes = noteList;
        ctx = context;
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
        ViewHolder holder = null;
        View item = convertView;
        Note note = getItem(position);

        if(item == null || !( item.getTag() instanceof ViewHolder)) {
                LayoutInflater mInflater = LayoutInflater.from(ctx);
                item = mInflater.inflate( R.layout.list_item, null);
                holder = new ViewHolder();

                holder.titulo = (TextView) item.findViewById(R.id.name);
                holder.fecha = (TextView) item.findViewById(R.id.date);

                item.setTag(holder);
        }else{
            holder = (ViewHolder) item.getTag();
        }

        if (note != null) {
            if (holder.titulo != null) {
                holder.titulo.setText(note.getTitle());
            }
            if (holder.fecha != null) {
                SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern, Locale.ENGLISH);
                holder.fecha.setText(dateFormatter.format(new Date(note.getUpdated())));
            }
        }
        return item;
    }

    static class ViewHolder{
        TextView titulo,fecha;
    }

}