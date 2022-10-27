package com.dev.cyclo;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

public class MenuActivityViewModel extends ViewModel {

    public MutableLiveData<String> listen = new MutableLiveData<>();

    public MutableLiveData<String> getListen() {
        return listen;
    }
    public void setListen(String s){
        listen.setValue(s);
    }
}

