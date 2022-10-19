package com.example.mygrocery.Activities;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LoginActivityTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void emailNotNull(){
        LoginActivity loginActivity = new LoginActivity();
        String email = loginActivity.getEmail();
        assertNotNull(email);
    }

    @Test
    public void PasswordNotNull(){
        LoginActivity loginActivity = new LoginActivity();
        String password = loginActivity.getPassword();
        assertNotNull(password);
    }

}