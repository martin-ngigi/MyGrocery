package com.example.mygrocery.Tests.UnitTests.MockitoUnitTest.loginform.data;

public class LoginRepositoryImpl implements LoginRepository {

    @Override
    public void login(LoginCredentials credentials, LoginListener loginListener) {

        if (credentials.isValid()) {
            loginListener.onSuccess();
        } else {
            loginListener.onFailure("Invalid credentials");
        }

    }

}
