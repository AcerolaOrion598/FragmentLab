package com.djaphar.fragmentlab;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.djaphar.fragmentlab.SupportClasses.AccessToken;
import com.djaphar.fragmentlab.SupportClasses.GitHubClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;

import javax.net.ssl.HttpsURLConnection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GitAuthActivity extends AppCompatActivity {

    Context context = this;
    Button button;
    private String clientId = "YOUR_CLIENT_ID";
    private String clientSecret = "YOUR_CLIENT_SECRET";
    private String redirectUri = "myapp://callback";
    private String accessToken;
    String owner = "", avatarURL = "", email = "";
    boolean notAuthorized = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_git_auth);

        button = findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notAuthorized = true;
                button.setEnabled(false);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/login/oauth/authorize"
                        + "?client_id=" + clientId + "&scope=repo&redirect_uri=" + redirectUri));
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        Uri uri = getIntent().getData();

        if (notAuthorized && uri != null && uri.toString().startsWith(redirectUri)) {
            String code = uri.getQueryParameter("code");

            Retrofit.Builder builder = new Retrofit.Builder()
                    .baseUrl("https://github.com/")
                    .addConverterFactory(GsonConverterFactory.create());

            Retrofit retrofit = builder.build();
            GitHubClient client = retrofit.create(GitHubClient.class);
            Call<AccessToken> accessTokenCall = client.getToken(clientId, clientSecret, code);

            accessTokenCall.enqueue(new Callback<AccessToken>() {
                @Override
                public void onResponse(@NonNull Call<AccessToken> call, @NonNull Response<AccessToken> response) {
                    if (response.body() != null) {
                        accessToken = response.body().getToken();
                        new GitConnectionTask().execute("https://api.github.com/user?access_token=" + accessToken);
                        new GitConnectionTask().execute("https://api.github.com/user/repos?access_token=" + accessToken);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<AccessToken> call, @NonNull Throwable t) {
                    Toast.makeText(context, getString(R.string.toast_connection_failure), Toast.LENGTH_SHORT).show();
                    button.setEnabled(true);
                }
            });
        }
    }

    class GitConnectionTask extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... newUrl) {
            String resultJson = "";
            JSONArray jsonArray;
            String[] repositories = null;

                HttpsURLConnection connection;
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
                    if (Arrays.toString(newUrl).contains("repos")) {
                        jsonArray = new JSONArray(resultJson);
                        repositories = new String[jsonArray.length()];
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject objectRepositories = jsonArray.getJSONObject(i);
                            repositories[i] = objectRepositories.getString("name");
                        }
                    } else {
                        JSONObject objectUser = new JSONObject(resultJson);
                        avatarURL = objectUser.getString("avatar_url");
                        owner = objectUser.getString("login");
                        email = objectUser.getString("email");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return repositories;
        }

        @Override
        protected void onPostExecute(String[] result) {
            Toast.makeText(context, getString(R.string.toast_connection_success), Toast.LENGTH_SHORT).show();
            if (!(result == null)) {
                notAuthorized = false;
                Intent intentMainActivity = new Intent(context, MainActivity.class);
                intentMainActivity.putExtra("Repositories", result);
                intentMainActivity.putExtra("Owner", owner);
                intentMainActivity.putExtra("Avatar URL", avatarURL);
                intentMainActivity.putExtra("Email", email);
                startActivity(intentMainActivity);
            }
        }
    }

    public static String streamConverse(InputStream in) {
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
