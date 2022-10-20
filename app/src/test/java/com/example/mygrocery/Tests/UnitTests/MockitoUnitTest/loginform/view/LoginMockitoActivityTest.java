package com.example.mygrocery.Tests.UnitTests.MockitoUnitTest.loginform.view;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import com.example.mygrocery.Tests.UnitTests.MockitoUnitTest.loginform.Contract;
import com.example.mygrocery.Tests.UnitTests.MockitoUnitTest.loginform.LoginPresenter;
import com.example.mygrocery.Tests.UnitTests.MockitoUnitTest.loginform.data.LoginCredentials;
import com.example.mygrocery.Tests.UnitTests.MockitoUnitTest.loginform.data.LoginRepository;
import com.example.mygrocery.Tests.UnitTests.MockitoUnitTest.loginform.data.LoginRepositoryImpl;


public class LoginMockitoActivityTest {

    //Youtube link https://www.youtube.com/watch?v=MnKXpI846Es

    private LoginPresenter loginPresenter;

    private LoginCredentials loginCredentials = new LoginCredentials(
            "martin@gmail.com",
            "12345");

    @Mock
    private LoginRepositoryImpl loginRepository;

    @Mock
    private Contract.LoginView loginView;

    @Captor
    private ArgumentCaptor<LoginRepository.LoginListener> loginListenerArgumentCaptor;

    @Before
    public void setUpLoginPresenter() {
        MockitoAnnotations.initMocks(this);
        loginPresenter = new LoginPresenter(loginRepository, loginView);
    }

    @Test
    public void login() {
        loginPresenter.login(loginCredentials);
        verify(loginRepository).login(eq(loginCredentials), loginListenerArgumentCaptor.capture());

        loginListenerArgumentCaptor.getValue().onSuccess();
        verify(loginView).onSuccess();

        loginListenerArgumentCaptor.getValue().onFailure("Invalid Credentials");
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(loginView).onFailed(argumentCaptor.capture());

        assertEquals("Invalid Credentials", argumentCaptor.getValue());
    }

}
