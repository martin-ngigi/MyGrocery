package com.example.mygrocery.Tests.UnitTests.MockitoUnitTest.loginform;

import com.example.mygrocery.Tests.UnitTests.MockitoUnitTest.loginform.data.LoginCredentials;

public interface Contract {

    interface LoginView {

        //abstract method be implemented
        void onSuccess();

        void onFailed(String message);

    }

    interface Presenter {

        void login(LoginCredentials loginCredentials);

    }

}
