package com.djaphar.fragmentlab;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

public class GitAuth extends Fragment {

    MainActivity mainActivity;
    TextView textView;
    Button button;
    Fragment repoFragment;
    EditText editText;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_git_auth, container, false);

        mainActivity = (MainActivity)getActivity();
        editText = rootView.findViewById(R.id.editText2);
        textView = rootView.findViewById(R.id.textView3);
        button = rootView.findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!editText.getText().toString().equals("")) {
                    new GitConnectionTask().execute("https://api.github.com/users/" +
                                                            editText.getText().toString() + "/repos");
                } else {
                    Toast.makeText(getActivity(), "Введите имя пользователя!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return rootView;
    }

    class GitConnectionTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            button.setEnabled(false);
        }

        @Override
        protected String doInBackground(String... newUrl) {
            HttpsURLConnection connection = null;
            String resultJson = "";

            try {
                URL url = new URL(newUrl[0]);
                connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(10000);
                connection.connect();
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                resultJson = streamConverse(connection.getInputStream());
                }
            } catch (IOException e) {
                Toast.makeText(getActivity(), "Ошибка соединения!", Toast.LENGTH_SHORT).show();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

            return resultJson;
        }

        @Override
        protected void onPostExecute(String result) {
            JSONArray jsonArray;
            StringBuilder repositories = new StringBuilder();

            try {
                jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    repositories.append(object.getString("name"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            repoFragment = new GitRepoFragment();
            ((GitRepoFragment) repoFragment).getTextForTV(repositories.toString());
            mainActivity.gitRepoFragment = repoFragment;
            Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment, repoFragment).commit();

            editText.setText("");
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
