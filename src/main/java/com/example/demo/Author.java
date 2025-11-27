package com.example.demo;
//https://docs.redhat.com/en/documentation/red_hat_data_grid/8.3/html/cache_encoding_and_marshalling/marshalling_user_types

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

public class Author {
    @ProtoField(1)
    final String name;

    @ProtoField(2)
    final String surname;

    @ProtoFactory
    Author(String name, String surname) {
        this.name = name;
        this.surname = surname;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

}