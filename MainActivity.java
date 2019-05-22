package com.example.socialmedia;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    LoginButton loginbutton;
    TwitterLoginButton loginButton2;
    TextView textViewEmail2;
    ImageView imageView2;
    private CircleImageView imageView;
    private TextView txtName, txtEmail;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                // .twitterAuthConfig(new TwitterAuthConfig(getString(R.string.CONSUMER_KEY), getString(R.string.twitter_secret)))
                .debug(true)
                .build();
        Twitter.initialize(config);
        setContentView(R.layout.activity_main);


        loginButton2 = (TwitterLoginButton) findViewById(R.id.login_button2);
        textViewEmail2 = findViewById(R.id.profile_name2);
        imageView2 = findViewById(R.id.imageView);
        txtName = (TextView) findViewById(R.id.profile_name);
        txtEmail = (TextView) findViewById(R.id.profile_email);
        imageView = (CircleImageView) findViewById(R.id.profile_pic);
        loginbutton=(LoginButton)findViewById(R.id.login_button);

        callbackManager= CallbackManager.Factory.create();
        loginbutton.setReadPermissions(Arrays.asList("email","public_profile"));
        checkLoginStatus();



        loginbutton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {


            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });



        loginButton2.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                // Do something with result, which provides a TwitterSession for making API calls
                Log.i("Session Username", String.valueOf(result.data.getUserId()));

                login(result);

            }

            @Override
            public void failure(TwitterException exception) {
                // Do something on failure
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        callbackManager.onActivityResult(requestCode,resultCode,data);
        super.onActivityResult(requestCode, resultCode, data);
        // Pass the activity result to the login button.
        loginButton2.onActivityResult(requestCode, resultCode, data);

    }




    //The login function accepting the result object
    public void login(final Result<TwitterSession> result) {

        //Creating a twitter session with result's data
        TwitterSession session = result.data;

        //This code will fetch the profile image URL
        //Getting the account service of the user logged in
        TwitterCore.getInstance().getApiClient(session).getAccountService().verifyCredentials(true, false, true).enqueue(new retrofit2.Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()){
                    //If it succeeds creating a User object from userResult.data
                    User user = response.body();

                    //Getting the profile image url
                    String profileImage = user.profileImageUrl.replace("_normal", "");
                    Glide.with(getApplicationContext()).load(profileImage).into(imageView2);

                    textViewEmail2.setText("Welcome " + user.name);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });
    }





    AccessTokenTracker tokenTracker =new AccessTokenTracker() {
        @Override
        protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken)
        {
            if(currentAccessToken==null)
            {
                txtName.setText("");
                txtEmail.setText("");
                imageView.setImageResource(0);
                Toast.makeText(MainActivity.this,"USER LOGGED OUT",Toast.LENGTH_SHORT).show();
            }
            else
                loaduserprofile(currentAccessToken);

        }
    };

    private  void loaduserprofile(AccessToken newAccessToken)
    {
        GraphRequest request=GraphRequest.newMeRequest(newAccessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    String first_name = object.getString("first_name");
                    //String last_name = object.getString("last_name");
                    String email=object.getString("email");
                    String id=object.getString("id");
                    String image_url="https://graph.facebook.com/"+id+"/picture?type=normal";

                    txtEmail.setText(email);
                    txtName.setText(first_name);
                    RequestOptions requestOPtions=new RequestOptions();
                    requestOPtions.dontAnimate();

                    Glide.with(MainActivity.this).load(image_url).into(imageView);


                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }


            }
        });
        Bundle parameters=new Bundle();
        parameters.putString("fields","first_name,last_name,email,id");
        request.setParameters(parameters);
        request.executeAsync();

    }

    private void checkLoginStatus()
    {
        if(AccessToken.getCurrentAccessToken()!=null){
            loaduserprofile(AccessToken.getCurrentAccessToken());
        }
    }



}
