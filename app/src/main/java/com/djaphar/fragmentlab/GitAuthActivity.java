package com.djaphar.fragmentlab;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class GitAuthActivity extends AppCompatActivity {

    Context context = this;
    Button button;
    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_git_auth);
        button = findViewById(R.id.button);
        editText = findViewById(R.id.editText2);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!editText.getText().toString().equals("")) {
                    new GitConnectionTask().execute("https://api.github.com/users/" +
                                                            editText.getText().toString() + "/repos");
                } else {
                    Toast.makeText(context, R.string.toast_username, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    class GitConnectionTask extends AsyncTask<String, Void, String[]> {

        String owner = "";

        @Override
        protected void onPreExecute() {
            button.setEnabled(false);
        }

        @Override
        protected String[] doInBackground(String... newUrl) {
            HttpsURLConnection connection;
            String resultJson = "";
            JSONArray jsonArray;
            String[] repositories = null;

            try {
                URL url = new URL(newUrl[0]);
                connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(10000);
                connection.connect();
                try {
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpsURLConnection.HTTP_OK) {
                        resultJson = streamConverse(connection.getInputStream());
                    }
                } finally {
                    connection.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (!resultJson.equals("")) {
                try {
                    jsonArray = new JSONArray(resultJson);
                    repositories = new String[jsonArray.length()];
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject objectRepositories = jsonArray.getJSONObject(i);
                        if (owner.equals("")) {
                            JSONObject objectOwner = objectRepositories.getJSONObject("owner");
                            owner = objectOwner.getString("login");
                        }
                        repositories[i] = objectRepositories.getString("name");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return repositories;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if (!(result == null)) {
                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("Repositories", result);
                intent.putExtra("Owner", owner);
                startActivity(intent);
                editText.setText("");
            } else {
                Toast.makeText(context, R.string.toast_connection, Toast.LENGTH_SHORT).show();
            }
            button.setEnabled(true);
        }
    }

    public String streamConverse(InputStream in) {
        BufferedReader reader = null;
        StringBuilder response = new StringBuilder();

        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return response.toString();
    }
}
