package com.diegojesuscampos.remember.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.asyncclient.EvernoteCallback;
import com.evernote.client.android.asyncclient.EvernoteClientFactory;
import com.evernote.client.android.asyncclient.EvernoteHtmlHelper;
import com.evernote.client.android.asyncclient.EvernoteNoteStoreClient;
import com.evernote.client.android.type.NoteRef;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.type.LinkedNotebook;
import com.evernote.edam.type.Note;
import com.diegojesuscampos.remember.R;
import com.evernote.thrift.TException;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.List;


public class NoteDetailFragment extends Fragment {
    public static final String EXTRA_NOTE_GUID = "extra_note_guid";

//    private Note noteView;

    TextView titleText;
    TextView contentText;
    private String noteGuid;
    WebView wv;
    private EvernoteHtmlHelper mEvernoteHtmlHelper;

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
        View view = inflater.inflate(R.layout.fragment_note_detail, container, false);
        initUIReferences(view);
        loadNoteContent();
        return view;
    }

    private void initUIReferences(View view) {
//        titleText = (TextView) root.findViewById(R.id.title_detail_value);
//        contentText = (TextView) root.findViewById(R.id.content_detail_value);
         wv = (WebView) view.findViewById(R.id.webView);
    }

    private void loadNoteContent() {

        final EvernoteClientFactory clientFactory = EvernoteSession.getInstance().getEvernoteClientFactory();


        final EvernoteNoteStoreClient noteStoreClient = EvernoteSession.getInstance().getEvernoteClientFactory().getNoteStoreClient();

        noteStoreClient.getNoteAsync(noteGuid, true, false, false, false, new EvernoteCallback<Note>() {
            @Override
            public void onSuccess(final Note nota) {
                new GetHtmlNote(nota,clientFactory).execute();
            }

            @Override
            public void onException(Exception exception) {

            }
        });
    }

    // OBTENEMO EL HTML DE LA NOTA PARA VISUALIZAR
    class GetHtmlNote extends AsyncTask<Void, Void, EvernoteHtmlHelper> {

        private Note nota;
        private  EvernoteClientFactory clientFactory;

        public GetHtmlNote(Note note, EvernoteClientFactory clientFactory) {
            this.nota = note;
            this.clientFactory = clientFactory;
        }

        protected EvernoteHtmlHelper doInBackground(Void... urls) {
            EvernoteHtmlHelper htmlHelper;
            htmlHelper = this.clientFactory.getHtmlHelperDefault();

            return htmlHelper;
        }

        protected void onPostExecute(final EvernoteHtmlHelper htmlHelper) {

            try {
                htmlHelper.downloadNoteAsync(this.nota.getGuid(), new EvernoteCallback<Response>() {
                    @Override
                    public void onSuccess(Response response) {
                        // PARSEAMOS EL HTML OBTENIDO
                        new ParseBody(response,htmlHelper).execute();
                    }

                    @Override
                    public void onException(Exception exception) {

                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    // VAMOS A CARGAR EN EL WEBVIEW EL HATML PARSEADO Y LA IMAGEN DE LA NOTA SI LA TUVIERA
    class ParseBody extends AsyncTask<Void, Void, String> {

        private Response response;
        private  EvernoteHtmlHelper htmlHelper;

        public ParseBody(Response response, EvernoteHtmlHelper htmlHelper) {
            this.response = response;
            this.htmlHelper = htmlHelper;
        }

        protected String doInBackground(Void... urls) {
            String parsebody = null;
            try {
                parsebody = this.htmlHelper.parseBody(response);

            } catch (IOException e) {
                e.printStackTrace();
            }

            return parsebody;
        }

        protected void onPostExecute(String parsebody) {
            if(parsebody != null) {

                String data = "<html><head></head><body>" + parsebody + "</body></html>";

                // OBTENEMOS LA IMAGEN DE LA NOTA SI LA TUVIERA
                wv.setWebViewClient(new WebViewClient() {
                    @SuppressWarnings("deprecation")
                    @Override
                    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                        try {
                            Response response = getEvernoteHtmlHelper().fetchEvernoteUrl(url);
                            WebResourceResponse webResourceResponse = toWebResource(response);
                            if (webResourceResponse != null) {
                                return webResourceResponse;
                            }

                        } catch (Exception e) {

                        }
                        return super.shouldInterceptRequest(view, url);
                    }
                });

                // CARGAMOS EL HTM DE LA NOTA
                wv.loadDataWithBaseURL("", data, "text/html", "UTF-8", null);
            }
        }
        protected WebResourceResponse toWebResource(Response response) throws IOException {
            if (response == null || !response.isSuccessful()) {
                return null;
            }

            String mimeType = response.header("Content-Type");
            String charset = response.header("charset");
            return new WebResourceResponse(mimeType, charset, response.body().byteStream());
        }

        protected EvernoteHtmlHelper getEvernoteHtmlHelper() throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
            if (mEvernoteHtmlHelper == null) {
                EvernoteClientFactory clientFactory = EvernoteSession.getInstance().getEvernoteClientFactory();
                mEvernoteHtmlHelper = clientFactory.getHtmlHelperDefault();

            }

            return mEvernoteHtmlHelper;
        }

    }


}
