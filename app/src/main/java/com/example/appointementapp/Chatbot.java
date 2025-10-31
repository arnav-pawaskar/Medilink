package com.example.appointementapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Chatbot Activity - AI-powered medical assistant
 * Integrates with Google Gemini API for medical consultations
 * Provides conversational interface with message history
 */
public class Chatbot extends AppCompatActivity {

    // UI Components
    private ScrollView svMessages;
    private LinearLayout llMessages;
    private EditText etMessage;
    private Button btnSend, btnBack, btnLogout;
    private ProgressBar progressBar;
    private TextView tvChatTitle;

    // API Key
    private String geminiApiKey;

    // Patient information
    private String appointmentId;
    private String patientName;
    private String patientEmail;
    private String bloodGroup;
    private String pastProblems;
    private String familyHistory;
    private String problemDescription;

    // API configuration
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    // Conversation history
    private List<Message> conversationHistory;

    /**
     * Message class for storing chat messages
     */
    private static class Message {
        String text;
        boolean isFromUser;

        Message(String text, boolean isFromUser) {
            this.text = text;
            this.isFromUser = isFromUser;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);


        // Initialize UI components
        initializeViews();

        // Get patient data from intent
        getPatientDataFromIntent();

        // Initialize conversation history
        conversationHistory = new ArrayList<>();

        // Setup click listeners
        setupClickListeners();

        // Load Gemini API key
        loadGeminiApiKey();

        // Display welcome message
        displayWelcomeMessage();
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews() {
        svMessages = findViewById(R.id.svMessages);
        llMessages = findViewById(R.id.llMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);
        btnLogout = findViewById(R.id.btnLogout);
        progressBar = findViewById(R.id.progressBar);
        tvChatTitle = findViewById(R.id.tvChatTitle);
    }

    /**
     * Get patient information from intent extras
     */
    private void getPatientDataFromIntent() {
        Intent intent = getIntent();
        appointmentId = intent.getStringExtra("appointmentId");
        patientName = intent.getStringExtra("patientName");
        patientEmail = intent.getStringExtra("patientEmail");
        bloodGroup = intent.getStringExtra("bloodGroup");
        pastProblems = intent.getStringExtra("pastProblems");
        familyHistory = intent.getStringExtra("familyHistory");
        problemDescription = intent.getStringExtra("problemDescription");

        // Set title with patient name
        if (patientName != null) {
            tvChatTitle.setText("Chat with AI - " + patientName);
        }
    }

    /**
     * Setup click listeners for buttons
     */
    private void setupClickListeners() {
        btnSend.setOnClickListener(v -> sendMessage());
        btnBack.setOnClickListener(v -> finish());
        btnLogout.setOnClickListener(v -> logout());
    }

    /**
     * Load Gemini API key from strings.xml
     */
    private void loadGeminiApiKey() {
        // Get API key from strings.xml
        geminiApiKey = getString(R.string.gemini_api_key);

        if (geminiApiKey == null || geminiApiKey.isEmpty()) {
            Toast.makeText(Chatbot.this, "Error: API key not configured", Toast.LENGTH_SHORT).show();
            btnSend.setEnabled(false);
        } else {
            btnSend.setEnabled(true);
            Toast.makeText(Chatbot.this, "Chatbot ready!", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Display welcome message and patient context
     */
    private void displayWelcomeMessage() {
        String welcomeText = "Hello! I'm MediLink's AI Healthcare Assistant.\n\n" +
                "I've reviewed your information:\n" +
                "• Name: " + (patientName != null ? patientName : "N/A") + "\n" +
                "• Blood Group: " + (bloodGroup != null ? bloodGroup : "N/A") + "\n" +
                "• Current Problem: " + (problemDescription != null ? problemDescription : "N/A") + "\n\n" +
                "How can I help you today with your health concerns?";

        addMessageToUI(welcomeText, false);
    }

    /**
     * Send user message and get AI response
     */
    private void sendMessage() {
        String userMessage = etMessage.getText().toString().trim();

        if (TextUtils.isEmpty(userMessage)) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        if (geminiApiKey == null || geminiApiKey.isEmpty()) {
            Toast.makeText(this, "API key not configured", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add user message to UI (but NOT to history yet - we'll add after API call)
        addMessageToUI(userMessage, true);

        // Clear input field
        etMessage.setText("");

        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);
        btnSend.setEnabled(false);

        // Get AI response in background thread
        new Thread(() -> {
            try {
                String botResponse = callGeminiAPI(userMessage);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnSend.setEnabled(true);

                    if (botResponse != null && !botResponse.isEmpty()) {
                        // Now add both user message and bot response to history
                        conversationHistory.add(new Message(userMessage, true));
                        conversationHistory.add(new Message(botResponse, false));
                        addMessageToUI(botResponse, false);
                    } else {
                        Toast.makeText(Chatbot.this, "Error getting response", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnSend.setEnabled(true);
                    Toast.makeText(Chatbot.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    /**
     * Call Google Gemini API with medical context
     */
    private String callGeminiAPI(String userMessage) throws IOException, JSONException {
        OkHttpClient client = new OkHttpClient();

        // Build system prompt with patient context
        String systemPrompt = "You are an AI healthcare assistant for an online diagnostic platform. " +
                "Patient Information:\n" +
                "- Name: " + patientName + "\n" +
                "- Blood Group: " + bloodGroup + "\n" +
                "- Past Medical Problems: " + (pastProblems != null ? pastProblems : "None reported") + "\n" +
                "- Family Medical History: " + (familyHistory != null ? familyHistory : "None reported") + "\n" +
                "- Current Problem: " + problemDescription + "\n\n" +
                "Provide professional medical guidance based on this patient data. " +
                "Format responses with sections: Summary of Findings, Possible Conditions, Urgency Level, " +
                "Recommended Specialist, and Next Steps. " +
                "Only answer medical questions. For off-topic queries, politely redirect to medical topics.";

        // Build request body
        JSONObject requestBody = new JSONObject();
        JSONArray contentsArray = new JSONArray();

        // If this is the first message, prepend system context to user message
        String messageToSend = userMessage;
        if (conversationHistory.isEmpty()) {
            messageToSend = systemPrompt + "\n\nUser question: " + userMessage;
        }

        // Add conversation history (previous user and model messages only)
        for (Message msg : conversationHistory) {
            JSONObject msgObj = new JSONObject();
            msgObj.put("role", msg.isFromUser ? "user" : "model");
            JSONArray parts = new JSONArray();
            JSONObject part = new JSONObject();
            part.put("text", msg.text);
            parts.put(part);
            msgObj.put("parts", parts);
            contentsArray.put(msgObj);
        }

        // Add current user message
        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        JSONArray userParts = new JSONArray();
        JSONObject userPart = new JSONObject();
        userPart.put("text", messageToSend);
        userParts.put(userPart);
        userMsg.put("parts", userParts);
        contentsArray.put(userMsg);

        requestBody.put("contents", contentsArray);

        // Add generation configuration
        JSONObject generationConfig = new JSONObject();
        generationConfig.put("temperature", 0.7);
        generationConfig.put("topP", 0.95);
        generationConfig.put("topK", 40);
        generationConfig.put("maxOutputTokens", 1024);
        requestBody.put("generationConfig", generationConfig);


        // Create request with proper headers
        String url = GEMINI_API_URL + "?key=" + geminiApiKey;
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toString(), JSON))
                .build();

        // Execute request
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = "";
                if (response.body() != null) {
                    errorBody = response.body().string();
                }
                return "Error: Unable to get response from AI (HTTP " + response.code() + ") - " + errorBody;
            }

            String responseBody = response.body().string();

            JSONObject jsonResponse = new JSONObject(responseBody);

            // Extract text from response
            if (jsonResponse.has("candidates")) {
                JSONArray candidates = jsonResponse.getJSONArray("candidates");
                if (candidates.length() > 0) {
                    JSONObject firstCandidate = candidates.getJSONObject(0);
                    if (firstCandidate.has("content")) {
                        JSONObject content = firstCandidate.getJSONObject("content");
                        if (content.has("parts")) {
                            JSONArray parts = content.getJSONArray("parts");
                            if (parts.length() > 0) {
                                JSONObject part = parts.getJSONObject(0);
                                if (part.has("text")) {
                                    return part.getString("text");
                                }
                            }
                        }
                    }
                }
            }

            return "Sorry, I couldn't process your request. Please try again.";
        }
    }

    /**
     * Add message to UI with proper chat bubble styling
     */
    private void addMessageToUI(String message, boolean isFromUser) {
        LinearLayout messageContainer = new LinearLayout(this);
        messageContainer.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        containerParams.setMargins(0, 6, 0, 6);
        messageContainer.setLayoutParams(containerParams);
        messageContainer.setGravity(isFromUser ? Gravity.END : Gravity.START);
        messageContainer.setPadding(8, 4, 8, 4);

        // Create the message bubble
        androidx.cardview.widget.CardView cardView = new androidx.cardview.widget.CardView(this);
        cardView.setRadius(12);
        cardView.setCardElevation(2);

        // Calculate max width (75% of screen width)
        int maxWidth = (int) (getResources().getDisplayMetrics().widthPixels * 0.75);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.weight = 0;
        cardView.setLayoutParams(cardParams);

        // Set background color based on sender
        if (isFromUser) {
            cardView.setCardBackgroundColor(0xFF1E3A8A); // Dark blue for user
        } else {
            cardView.setCardBackgroundColor(0xFFE8F0FE); // Light blue for bot
        }

        // Create text view for message
        TextView messageView = new TextView(this);
        messageView.setText(message);
        messageView.setTextSize(14);
        messageView.setLineSpacing(1.2f, 1.0f);
        messageView.setPadding(12, 8, 12, 8);
        messageView.setMaxWidth(maxWidth);

        if (isFromUser) {
            messageView.setTextColor(0xFFFFFFFF); // White text for user
        } else {
            messageView.setTextColor(0xFF1F2937); // Dark gray text for bot
        }

        cardView.addView(messageView);
        messageContainer.addView(cardView);

        llMessages.addView(messageContainer);

        // Auto-scroll to bottom
        svMessages.post(() -> svMessages.fullScroll(View.FOCUS_DOWN));
    }

    /**
     * Go back to appointment confirmation screen
     */
    private void goBackToConfirmation() {
        Intent intent = new Intent(Chatbot.this, AppointmentConfirm.class);
        intent.putExtra("appointmentId", appointmentId);
        startActivity(intent);
        finish();
    }

    /**
     * Handle logout
     */
    private void logout() {
        Intent intent = new Intent(Chatbot.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Handle back button press
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goBackToConfirmation();
    }
}

