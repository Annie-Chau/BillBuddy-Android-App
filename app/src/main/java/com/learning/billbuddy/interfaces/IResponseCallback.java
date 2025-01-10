package com.learning.billbuddy.interfaces;

public interface IResponseCallback {
    void onSuccess();
    void onFailure(Exception e);
}
