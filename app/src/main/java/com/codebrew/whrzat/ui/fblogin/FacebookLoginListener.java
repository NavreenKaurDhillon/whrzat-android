package com.codebrew.whrzat.ui.fblogin;
//
//import com.facebook.FacebookException;
//import com.facebook.GraphResponse;

import org.json.JSONObject;

public interface FacebookLoginListener {

    void onFbLoginSuccess();

    void onFbLoginCancel();

//    void onFbLoginError(FacebookException exception);
//
//    void onGetProfileSuccess(JSONObject object, GraphResponse response);

   // void onGetFbFriends(List<FBFriendData> friendsIds);
}
