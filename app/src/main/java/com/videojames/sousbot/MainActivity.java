package com.videojames.sousbot;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

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
import com.videojames.sousbot.data.ChatMessage;
import com.videojames.sousbot.data.MessageAdapter;
import com.videojames.sousbot.util.Constants;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView messagesView;
    private EditText editText;
    private ArrayList<ChatMessage> messages;
    private MessageAdapter messagesLog;
    private boolean firstMessage;

    /** Watson functionality, calls the service and stores the result in watsonSession
     * Assistant is the conversation service, STT and TTS might come later
     */
    private Assistant assistant;
    // private TextToSpeech textToSpeech;
    // private SpeechToText speechToText;
    private Response<SessionResponse> watsonSession;

    /** Loads the EditText and ImageButton on the layout to add functionality. Creates an
     * ArrayList of messages and pairs it with the MessageAdapter for view functionality. Sets
     * the layout manager to be the message window RecyclerView. Also initiates watsonServices()
     *
     * firstMessage is a boolean set to send an empty message to Watson to initiate the dialogue
     * using a lambda expression following a connection check.
     * @param savedInstanceState = The previous instance of the conversation
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.edit_text);
        ImageButton sendButton = findViewById(R.id.send_button);
        messages = new ArrayList<>();
        messagesLog = new MessageAdapter(messages);
        messagesView = findViewById(R.id.messages_view);
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


    /** Initiates the Watson services and autheticates them with the keys found in Constants.
     *
     */
    private void watsonServices() {
        assistant = new Assistant(Constants.ibmVersionDate, new IamAuthenticator(Constants.ibmApiKey));
        assistant.setServiceUrl(Constants.ibmUrl);

        /*       Text to speech credentials for later use
        textToSpeech = new TextToSpeech(new IamAuthenticator(Constants.ibmTtsApi));
        textToSpeech.setServiceUrl(Constants.ibmTtsUrl);
        */

        /*       Speech to text credentials for later use
        speechToText = new SpeechToText(new IamAuthenticator(Constants.ibmSttapi));
        speechToText.setServiceUrl(Constants.ibmStturl); */
    }


    /** Takes the message from the user in the editText bar, creates a ChatMessage of it to add
     * to the messages list and also creates a MessageInput to send to Watson. Since Watson
     * responses react to user input this also contains the logic to collect and display the bot
     * responses.
     */
    public void sendMessage() {
        String message = editText.getText().toString().trim();

        // Checks for first message. If true, it immediately sets to false with an empty payload.
        if (this.firstMessage) {
            //ChatMessage newMessage = new ChatMessage(message, "2");
            //messages.add(newMessage);
            this.firstMessage = false;
        /* As long as the message bar isn't empty, it creates a ChatMessage from the user
        containing the text.
         */
        } else {
            if (message.length() > 0) {
                ChatMessage newMessage = new ChatMessage(message, "1");
                messages.add(newMessage);

            }
        }
        /* Clears the EditText and alerts the action listener of new data in messagesLog to
        update the RecyclerView
         */
        editText.getText().clear();
        messagesLog.notifyDataSetChanged();

        // Creates a thread for sending and receiving messages from Watson
        Thread thread = new Thread(() -> {
            try {

                // If Watson is connected, create a new chat session with the assistant.
                if (watsonSession == null) {
                    ServiceCall<SessionResponse> call = assistant.createSession(
                            new CreateSessionOptions.Builder().assistantId(Constants.ibmId).build());
                    watsonSession = call.execute();
                }

                //Takes the string and puts it into an input message for Watson
                MessageInput input = new MessageInput.Builder()
                        .text(message)
                        .build();

                /* Takes the MessageInput as a payload and sends it to the Watson session ID
                as a user message.
                 */
                MessageOptions options = new MessageOptions.Builder()
                        .assistantId(Constants.ibmId)
                        .input(input)
                        .sessionId(watsonSession.getResult().getSessionId())
                        .build();

                // Gets the response from the Watson service.
                Response<MessageResponse> response = assistant.message(options).execute();

                /* Takes the response from Watson and turns it into a RuntimeResponseGeneric
                (a list of the responses from Watson) for MessageAdapter to handle.
                 */
                if (response != null &&
                        response.getResult().getOutput() != null &&
                        !response.getResult().getOutput().getGeneric().isEmpty()) {
                    List<RuntimeResponseGeneric> responses = response.getResult().getOutput().getGeneric();

                    /* Iterates through the list of responses from Watson. If it is a text message,
                    the message is extracted into a string and made into a ChatMessage tagged as
                    the bot ("2"). If it is an image, the title and URL are handled in the
                    MessageAdapter class. Default is to handle response errors. After each message,
                    there is a 1 second pause for user readability.
                     */
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

                    /* Notifies the system of new messages and scrolls down on bot response to
                    display the message.
                     */
                    runOnUiThread(() -> {
                        messagesLog.notifyDataSetChanged();
                        messagesView.scrollBy(0,1000);
                    });
                }
            } // Handles exceptions and prints the error message to console.
            catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();


    }


    /** The network connectivity checker. Is deprecated as I'm unsure how to do this otherwise.
     *
     * @return a boolean value of true or false for connectivity checks and returns a message for
     * failed connections to the user.
     */
    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
            return true;
        } else {
            Toast.makeText(this, "You are not connected to the Internet.",
                    Toast.LENGTH_LONG).show();
            return false;
        }
    }




}