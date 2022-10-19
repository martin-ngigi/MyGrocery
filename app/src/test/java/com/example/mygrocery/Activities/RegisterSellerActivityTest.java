package com.example.mygrocery.Activities;

import static org.junit.Assert.*;

import org.junit.Test;

public class RegisterSellerActivityTest {
    private String fullName, shopName, phoneNumber, deliveryFee, country, state, city, address, email, password, confirmPassword;
    RegisterSellerActivity registerSellerActivity = new RegisterSellerActivity();

    @Test
    public void assertNotNullGetName() {
        fullName = registerSellerActivity.getFullName();
        assertNotNull(fullName);
    }

    @Test
    public void assertNotNullGetShopName() {
        shopName = registerSellerActivity.getShopName();
        assertNotNull(shopName);
    }

    @Test
    public void assertNotNullGetPhoneEtRS() {
        phoneNumber = registerSellerActivity.getPhoneNumber();
        assertNotNull(phoneNumber);
    }

    @Test
    public void assertNotNullGetDeliveryFee() {
        deliveryFee = registerSellerActivity.getDeliveryFee();
        assertNotNull(deliveryFee);
    }

    @Test
    public void assertNotNullGetCountry() {
        country = registerSellerActivity.getCountry();
        assertNotNull(country);
    }

    @Test
    public void assertNotNullGetState() {
        state = registerSellerActivity.getState();
        assertNotNull(state);
    }

    @Test
    public void assertNotNullGetCity() {
        city = registerSellerActivity.getCity();
        assertNotNull(city);
    }

    @Test
    public void assertNotNullGetAddress() {
        address = registerSellerActivity.getCity();
        assertNotNull(address);
    }

    @Test
    public void assertNotNullGetEmailEtRS() {
        email = registerSellerActivity.getEmail();
        assertNotNull(email);
    }

    @Test
    public void getPasswordETRS() {
        password = registerSellerActivity.getPassword();
        assertNotNull(password);
    }

    @Test
    public void getConfirmPasswordETRS() {
        confirmPassword = registerSellerActivity.getConfirmPassword();
        assertNotNull(confirmPassword);
    }
}