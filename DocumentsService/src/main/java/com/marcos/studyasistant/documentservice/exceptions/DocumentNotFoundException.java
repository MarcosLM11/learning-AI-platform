package com.marcos.studyasistant.documentservice.exceptions;

public class DocumentNotFoundException extends  RuntimeException {
    public DocumentNotFoundException(String documentNotFound) {
        super(documentNotFound);
    }
}
