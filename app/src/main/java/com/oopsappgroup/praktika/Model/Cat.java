package com.oopsappgroup.praktika.Model;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Cat {
    public String uid;
    public String name;
    public String photo;

    public Cat(){

    }

    public Cat(String uid, String name, String photo){
        this.uid = uid;
        this.name = name;
        this.photo = photo;
    }

    @Exclude
    public Map<String, String> toMap() {
        HashMap<String, String> result = new HashMap<>();

        result.put("uid", uid);
        result.put("name", name);
        result.put("photo", photo);

        return result;
    }
}
