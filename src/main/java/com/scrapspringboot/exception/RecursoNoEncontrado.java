package com.scrapspringboot.exception;

public class RecursoNoEncontrado extends RuntimeException {
    public RecursoNoEncontrado(String message) {
        super(message);
    }
}