package com.codebrew.whrzat.ui.fblogin;
//
//import android.app.Activity;
//import android.app.Fragment;
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.PackageInfo;
//import android.content.pm.PackageManager;
//import android.content.pm.Signature;
//import android.os.Bundle;
//import android.util.Base64;
//import android.util.Log;
//
//import com.facebook.AccessToken;
//import com.facebook.CallbackManager;
//import com.facebook.FacebookCallback;
//import com.facebook.FacebookException;
//import com.facebook.GraphRequest;
//import com.facebook.GraphRequestBatch;
//import com.facebook.GraphResponse;
//import com.facebook.login.LoginManager;
//import com.facebook.login.LoginResult;
//
//import org.json.JSONArray;
//import org.json.JSONObject;
//
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//import java.util.Arrays;
//
//
//public class FacebookLogin {
//    private static final String TAG = "FacebookLogin";
//
//    private Context mContext;
//    FacebookLoginListener facebookLoginListener;
// //   private CallbackManager mCallbackManager;
//    private Object mUiRef;
//    // OnGetFbFriendsListener onGetFbFriendsListener;
//    public String ID = "id", PROFILE_PIC = "picture.height(420).width(420)",
//            FIRST_NAME = "first_name", LAST_NAME = "last_name", FULL_NAME = "name", EMAIL = "email",
//            FIELDS = "fields", LOCATION = "location", GENDER = "gender";
//
//    public FacebookLogin(Context context, Object uiRef) {
//        mContext = context;
//        mUiRef = uiRef;
//        printHashKey();
//        mCallbackManager = CallbackManager.Factory.create();
//
//        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
//            @Override
//            public void onSuccess(LoginResult loginResult) {
//                facebookLoginListener.onFbLoginSuccess();
//            }
//
//            @Override
//            public void onCancel() {
//                facebookLoginListener.onFbLoginCancel();
//            }
//
//            @Override
//            public void onError(FacebookException exception) {
//                facebookLoginListener.onFbLoginError(exception);
//            }
//        });
//    }
//
//    public void performLogin() {
//        if (mUiRef instanceof Activity) {
//            LoginManager.getInstance().logInWithReadPermissions((Activity) mUiRef,
//                    Arrays.asList("email", "public_profile", "user_friends"));
//
//        } else if (mUiRef instanceof Fragment) {
//            LoginManager.getInstance().logInWithReadPermissions((Fragment) mUiRef,
//                    Arrays.asList("email", "public_profile", "user_friends"));
//        } else if (mUiRef instanceof androidx.fragment.app.Fragment) {
//            LoginManager.getInstance().logInWithReadPermissions((androidx.fragment.app.Fragment) mUiRef,
//                    Arrays.asList("email", "public_profile", "user_friends"));
//        }
//
//    }
//
//    public void getFriends() {
//    /*}
//    else {
//        showPop();
//    }*//* public void setOnGetFbFriendsListener(OnGetFbFriendsListener onGetFbFriendsListener) {
//         this.onGetFbFriendsListener = onGetFbFriendsListener;
//     }*/
//        if (!AccessToken.getCurrentAccessToken().isExpired())
//            if (AccessToken.getCurrentAccessToken() != null) {
//                GraphRequest graphRequest = GraphRequest.newMyFriendsRequest(
//                        AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONArrayCallback() {
//                            @Override
//                            public void onCompleted(
//                                    JSONArray jsonArray, GraphResponse response) {
//// Application code for users friends
//                /*JSONArray friendsIds = new JSONArray();*/
//                               /* List<FBFriendData> fbFriendDatas = new ArrayList<>();
//                                try {
//                                    for (int l = 0; l < jsonArray.length(); l++) {
//                                        FBFriendData fbFriendData = new FBFriendData();
//                                        fbFriendData.friendFbId = jsonArray.getJSONObject(l).getString("id");
//                                        // fbFriendData.name = jsonArray.getJSONObject(l).getString("name");
//                                        // fbFriendData.friendImageUrl =
//                                        // jsonArray.getJSONObject(l).getJSONObject("picture").
//                                        //       getJSONObject("data").getString("url");*//*"http://graph.facebook.com/"+jsonArray.getJSONObject(l).getString("id")
//                                        // +"/picture?type=square"*//*
//                                        fbFriendDatas.add(fbFriendData);
//                                    }
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                                facebookLoginListener.onGetFbFriends(fbFriendDatas);*/
////                                OnSuccess(jsonArray, response);
//                            }
//                        });
//
//                Bundle parameters = new Bundle();
//
//                parameters.putString(FIELDS, ID + "," + EMAIL + "," + FIRST_NAME + "," + LAST_NAME + "," + FULL_NAME + ","
//                        + PROFILE_PIC + "," + GENDER);
//                graphRequest.setParameters(parameters);
//                GraphRequestBatch batch = new GraphRequestBatch(graphRequest);
//                batch.addCallback(new GraphRequestBatch.Callback() {
//                    @Override
//                    public void onBatchCompleted(GraphRequestBatch graphRequests) {
//                        // Application code for when the batch finishes
//                    }
//                });
//                batch.executeAsync();
//            } else {
//               // Toast.makeText(mContext, R.string.message_try_again_later, Toast.LENGTH_SHORT).show();
//            }
//    }
//
//    public void getUserProfile() {
//        GraphRequest request = GraphRequest.newMeRequest(
//                AccessToken.getCurrentAccessToken(),
//                new GraphRequest.GraphJSONObjectCallback() {
//                    @Override
//                    public void onCompleted(
//                            JSONObject object,
//                            GraphResponse response) {
//                        facebookLoginListener.onGetProfileSuccess(object, response);
//                        //              OnSuccess(object, response);
//                        // Application code
//                    }
//                });
//
//        Bundle parameters = new Bundle();
//        parameters.putString(FIELDS, ID + "," + GENDER + "," + EMAIL + "," + FIRST_NAME +
//                "," + LAST_NAME + "," + FULL_NAME + "," + PROFILE_PIC);
//        request.setParameters(parameters);
//        request.executeAsync();
//    }
//
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        mCallbackManager.onActivityResult(requestCode, resultCode, data);
//    }
//
//    public void setFacebookLoginListener(FacebookLoginListener facebookLoginListener) {
//        this.facebookLoginListener = facebookLoginListener;
//    }
//
//    void printHashKey() {
//        try {
//            PackageInfo info = mContext.getPackageManager().getPackageInfo(
//                    "com.codebrew.whrzat",
//                    PackageManager.GET_SIGNATURES);
//            for (Signature signature : info.signatures) {
//                MessageDigest md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                Log.e("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
//            }
//        } catch (PackageManager.NameNotFoundException e) {
//
//        } catch (NoSuchAlgorithmException e) {
//        }
//    }
//
//}

