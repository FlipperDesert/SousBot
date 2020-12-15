package com.videojames.watsontest;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.sql.Time;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ibm.cloud.sdk.core.http.Response;
import com.ibm.cloud.sdk.core.http.ServiceCall;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.assistant.v2.Assistant;

import com.ibm.watson.assistant.v2.model.CreateSessionOptions;
import com.ibm.watson.assistant.v2.model.MessageInput;
import com.ibm.watson.assistant.v2.model.MessageOptions;
import com.ibm.watson.assistant.v2.model.MessageResponse;
import com.ibm.watson.assistant.v2.model.RuntimeResponseGeneric;
import com.ibm.watson.assistant.v2.model.SessionResponse;
import com.videojames.watsontest.data.ChatMessage;
import com.videojames.watsontest.data.MessageAdapter;
import com.videojames.watsontest.util.Constants;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView messagesView;
    private EditText editText;
    private ImageButton sendButton;
    private ArrayList messages;
    private MessageAdapter messagesLog;
    private boolean firstMessage;

    // Watson
    private Assistant assistant;
    // private TextToSpeech textToSpeech;
    // private SpeechToText speechToText;
    private Response<SessionResponse> watsonSession;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText) findViewById(R.id.edit_text);
        sendButton = (ImageButton) findViewById(R.id.send_button);
        messages = new ArrayList<>();
        messagesLog = new MessageAdapter(messages);
        messagesView = (RecyclerView) findViewById(R.id.messages_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        messagesView.setLayoutManager(layoutManager);
        messagesView.setItemAnimator(new DefaultItemAnimator());
        messagesView.setAdapter(messagesLog);

        this.editText.setText("");
        this.firstMessage = true;

        sendButton.setOnClickListener(v -> {
            if (isOnline()) {
                sendMessage();
            }
        });

        watsonServices();
        sendMessage();
    }


    // Watson. If it breaks, it's this
    private void watsonServices() {
        assistant = new Assistant(Constants.ibmVersionDate, new IamAuthenticator(Constants.ibmApiKey));
        assistant.setServiceUrl(Constants.ibmUrl);

        /*       Text to speech credentials
        textToSpeech = new TextToSpeech(new IamAuthenticator(Constants.ibmTtsApi));
        textToSpeech.setServiceUrl(Constants.ibmTtsUrl);
        */

        /*       Speech to text credentials
        speechToText = new SpeechToText(new IamAuthenticator(Constants.ibmSttapi));
        speechToText.setServiceUrl(Constants.ibmStturl); */
    }




    public void sendMessage() {
        String message = editText.getText().toString().trim();

        if (this.firstMessage) {
            //ChatMessage newMessage = new ChatMessage(message, "2");
            //messages.add(newMessage);
            this.firstMessage = false;

        } else {
            if (message.length() > 0) {
                ChatMessage newMessage = new ChatMessage(message, "1");
                messages.add(newMessage);

            }
        }
        editText.getText().clear();
        messagesLog.notifyDataSetChanged();

        Thread thread = new Thread(() -> {
            try {
                if (watsonSession == null) {
                    ServiceCall<SessionResponse> call = assistant.createSession(
                            new CreateSessionOptions.Builder().assistantId(Constants.ibmId).build());
                    watsonSession = call.execute();
                }

                MessageInput input = new MessageInput.Builder()
                        .text(message)
                        .build();

                MessageOptions options = new MessageOptions.Builder()
                        .assistantId(Constants.ibmId)
                        .input(input)
                        .sessionId(watsonSession.getResult().getSessionId())
                        .build();

                Response<MessageResponse> response = assistant.message(options).execute();

                if (response != null &&
                        response.getResult().getOutput() != null &&
                        !response.getResult().getOutput().getGeneric().isEmpty()) {
                    List<RuntimeResponseGeneric> responses = response.getResult().getOutput().getGeneric();


                    for (RuntimeResponseGeneric run : responses) {
                        ChatMessage output;
                        Thread.sleep(1000);
                        switch (run.responseType()) {
                            case "text":
                                output = new ChatMessage(run.text(), "2");
                                messages.add(output);
                                break;

                            case "image":
                                output = new ChatMessage(run);
                                messages.add(output);
                                break;

                            default:
                                Log.e("Error", "Unhandled message type");
                        }

                    }

                    runOnUiThread(new Runnable() {
                        public void run() {
                            messagesLog.notifyDataSetChanged();
                            messagesView.scrollBy(0,1000);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();


    }


    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
            return true;
        } else {
            Toast.makeText(this, "You are not connected to the Internet.", Toast.LENGTH_LONG).show();
            return false;
        }
    }




}