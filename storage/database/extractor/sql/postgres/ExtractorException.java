package com.boostasoft.goaway.tilingDB.storage.database.extractor.sql.postgres;

 
public class ExtractorException extends RuntimeException {
    private static final long serialVersionUID = 1L;


  /** Construit une exception avec les details spécifiés du message*/
    public ExtractorException(String message) {
            super(message);
    }

   
}