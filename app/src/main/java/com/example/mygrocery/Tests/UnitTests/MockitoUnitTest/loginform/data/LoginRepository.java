package com.example.mygrocery.Tests.UnitTests.MockitoUnitTest.loginform.data;

public interface LoginRepository {

    interface LoginListener {

        void onSuccess();

        void onFailure(String message);

    }

    //abstract method be implemented in LoginRepository
    void login(LoginCredentials credentials, LoginListener loginListener);

}
