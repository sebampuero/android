package com.example.tm18app.viewModels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CommentItemViewModel extends ViewModel {

    public MutableLiveData<String> name = new MutableLiveData<>();
    public MutableLiveData<String> lastname = new MutableLiveData<>();
    public MutableLiveData<String> content = new MutableLiveData<>();

    public CommentItemViewModel(String name, String lastname, String content) {
        this.name.setValue(name);
        this.lastname.setValue(lastname);
        this.content.setValue(content);
    }
}
