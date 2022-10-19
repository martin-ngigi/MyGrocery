package com.example.mygrocery.Activities;

import static org.junit.Assert.*;

import org.junit.Test;

public class RegisterUserActivityTest {
    private String fullName, phoneNumber, country, state, city, address, email, password, confirmPassword;
    RegisterUserActivity registerUserActivity = new RegisterUserActivity();


    @Test
    public void AssertNotNullFullName() {
        fullName = registerUserActivity.getFullName();
        assertNotNull(fullName);
    }

    @Test
    public void AssertNotNullPhoneNumber() {
        phoneNumber = registerUserActivity.getPhoneNumber();
        assertNotNull(phoneNumber);
    }

    @Test
    public void AssertNotNullCountry() {
        country = registerUserActivity.getCountry();
        assertNotNull(country);
    }

    @Test
    public void AssertNotNullState() {
        state = registerUserActivity.getState();
        assertNotNull(state);
    }

    @Test
    public void AssertNotNullCity() {
        city = registerUserActivity.getCity();
        assertNotNull(city);
    }

    @Test
    public void AssertNotNullAddress() {
        address = registerUserActivity.getAddress();
        assertNotNull(address);
    }

    @Test
    public void AssertNotNullEmail() {
        email = registerUserActivity.getEmail();
        assertNotNull(email);
    }

    @Test
    public void AssertNotNullPassword() {
        password = registerUserActivity.getPassword();
        assertNotNull(password);
    }

    @Test
    public void AssertNotNullConfirmPassword() {
        confirmPassword = registerUserActivity.getConfirmPassword();
        assertNotNull(confirmPassword);
    }
}