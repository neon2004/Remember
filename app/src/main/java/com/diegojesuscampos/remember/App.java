package com.diegojesuscampos.remember;

import android.app.Application;

import com.evernote.client.android.EvernoteSession;


public class App extends Application {

    /*
     * Your Evernote API key. See http://dev.evernote.com/documentation/cloud/
     * Please obfuscate your code to help keep these values secret.
     */
    private static final String CONSUMER_KEY = "neon2004-1508";
    private static final String CONSUMER_SECRET = "6b773c114cc09ef9";
    public static final String FRAGMENT_LOAD_NOTES = "fragments_load_notes";
    public static final String FRAGMENT_ADD_NOTES = "fragments_add_notes";
    public static final String FRAGMENT_DETAIL_NOTES = "fragments_detail_notes";


    /*
     * Initial development is done on Evernote's testing service, the sandbox.
     *
     * Change to PRODUCTION to use the Evernote production service
     * once your code is complete.
     */
    private static final EvernoteSession.EvernoteService EVERNOTE_SERVICE = EvernoteSession.EvernoteService.SANDBOX;

    /*
     * Set this to true if you want to allow linked notebooks for accounts that
     * can only access a single notebook.
     */
    private static final boolean SUPPORT_APP_LINKED_NOTEBOOKS = true;

    @Override
    public void onCreate() {
        super.onCreate();

        //Set up the Evernote singleton session, use EvernoteSession.getInstance() later
        new EvernoteSession.Builder(this)
                .setEvernoteService(EVERNOTE_SERVICE)
                .setSupportAppLinkedNotebooks(SUPPORT_APP_LINKED_NOTEBOOKS)
                .setForceAuthenticationInThirdPartyApp(true)
//                .setLocale(Locale.SIMPLIFIED_CHINESE)
                .build(CONSUMER_KEY, CONSUMER_SECRET)
                .asSingleton();
    }
}
