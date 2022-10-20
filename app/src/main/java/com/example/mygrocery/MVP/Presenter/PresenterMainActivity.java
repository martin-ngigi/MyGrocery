package com.example.mygrocery.MVP.Presenter;


import com.example.mygrocery.MVP.Model.ModelUser;

public class PresenterMainActivity {

    private ModelUser user;
    private View view;

    public PresenterMainActivity(View view) {
        this.user = new ModelUser();
        this.view = view;
    }

    public void updateFullName(String fullName){
        user.setFullName(fullName);
        view.updateUserInfoTextView(user.toString());

    }

    public void updateEmail(String email){
        user.setEmail(email);
        view.updateUserInfoTextView(user.toString());

    }

    public interface View{

        void updateUserInfoTextView(String info);
        void showProgressBar();
        void hideProgressBar();

    }
}
