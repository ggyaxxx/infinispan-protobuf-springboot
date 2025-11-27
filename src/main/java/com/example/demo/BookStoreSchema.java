package com.example.demo;

import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.protostream.annotations.ProtoSchema;

@ProtoSchema(
        includeClasses = {
                Book.class,
                Author.class,
                Language.class
        },
        schemaFileName = "books.proto",
        schemaFilePath = "proto/",
        schemaPackageName = "books"
)
public interface BookStoreSchema extends GeneratedSchema {}

