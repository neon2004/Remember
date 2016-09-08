package com.diegojesuscampos.remember.fragments;


import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.diegojesuscampos.remember.App;
import com.diegojesuscampos.remember.activities.MainActivity;
import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.EvernoteUtil;
import com.evernote.client.android.asyncclient.EvernoteCallback;
import com.evernote.client.android.asyncclient.EvernoteNoteStoreClient;
import com.evernote.client.conn.mobile.FileData;
import com.evernote.edam.type.Note;
import com.diegojesuscampos.remember.R;
import com.evernote.edam.type.Resource;
import com.evernote.edam.type.ResourceAttributes;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class AddNoteFragment extends DialogFragment {
    private static ImageView imageView;
    private TextInputEditText titleField;
    private TextInputEditText contentField;

    private TextInputLayout titleLabel;
    private TextInputLayout contentLabel;
    public static MainActivity callBack;
    private FloatingActionButton saveButton;
    private FloatingActionButton camButton;


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
        getActivity().setTitle(getActivity().getString(R.string.add_nota));
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

        camButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callBack.startSelectImage();
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
        camButton = (FloatingActionButton) view.findViewById(R.id.get_image);

        imageView = (ImageView) view.findViewById(R.id.ivImage);
    }

    private void addNote() {
        if (areMandatoryFieldsFilled()) {
            final ProgressDialog dialog = new ProgressDialog(getActivity());

            dialog.setMessage(getActivity().getResources().getString(R.string.guardando));
           dialog.show();
            saveButton.setEnabled(false);
            camButton.setEnabled(false);
            Note note = getNoteContent();


            String f = callBack.mImageData != null ? callBack.mImageData.filePath : "";

            try {
                if(f != null && !f.equals("")){

                // Hash the data in the image file. The hash is used to reference the
                // file in the ENML note content.
                InputStream in = new BufferedInputStream(new FileInputStream(f));
                FileData data = new FileData(EvernoteUtil.hash(in), new File(f));
                in.close();

                ArrayList<Resource> listResources = new ArrayList<Resource>();

                // Create a new Resource
                Resource resource = new Resource();
                resource.setData(data);
                resource.setMime(callBack.mImageData.mimeType);
                ResourceAttributes attributes = new ResourceAttributes();
                attributes.setFileName(callBack.mImageData.fileName);
                resource.setAttributes(attributes);

                listResources.add(resource);

                String nBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
                nBody += "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">";
                nBody += "<en-note>"+contentField.getText().toString();

                if (resource != null) {
                    // Add Resource objects to note body
                    nBody += "<br /><br />";
                    note.setResources(listResources);
                    for (Resource item_resource : listResources) {
                        StringBuilder sb = new StringBuilder();
                        for (byte hashByte : item_resource.getData().getBodyHash()) {
                            int intVal = 0xff & hashByte;
                            if (intVal < 0x10) {
                                sb.append('0');
                            }
                            sb.append(Integer.toHexString(intVal));
                        }
                        String hexhash = sb.toString();
                        nBody += " <br /><en-media type=\"" + item_resource.getMime() + "\" hash=\"" + hexhash + "\" /><br />";
                    }
                }
                nBody += "</en-note>";

                note.setContent(nBody);
            }
                EvernoteNoteStoreClient noteStoreClient = EvernoteSession.getInstance().getEvernoteClientFactory().getNoteStoreClient();
                noteStoreClient.createNoteAsync(note, new EvernoteCallback<Note>() {
                    @Override
                    public void onSuccess(Note note) {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                            saveButton.setEnabled(true);
                            camButton.setEnabled(true);
                        }

                        showNotesScreen(true);
                        callBack.cargarFragment(App.FRAGMENT_DETAIL_NOTES,note);
                    }

                    @Override
                    public void onException(Exception exception) {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                            saveButton.setEnabled(true);
                            camButton.setEnabled(true);
                        }
                        showNotesScreen(false);
                    }
                });

            } catch (Exception ex) {
                Toast.makeText(getActivity(), "ERROR", Toast.LENGTH_LONG).show();
            }
        }
    }

    // OBTENEMOS LOS DATOS INTRODUCIDOS
    private Note getNoteContent() {
        Note note = new Note();
        note.setTitle(titleField.getText().toString());
        note.setContent(EvernoteUtil.NOTE_PREFIX + contentField.getText().toString() + EvernoteUtil.NOTE_SUFFIX);
        return note;
    }

    // VALIDAMOS LOS CAMPOS NECESARIOS
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

    // AÑADIMOS LA IMAGEN PARA MOSTRARLA SI ES NECESARIO
    public static void addImageView(Bitmap takenPictureData){
        imageView.setImageBitmap(takenPictureData);
    }
}
