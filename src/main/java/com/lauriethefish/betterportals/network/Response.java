package com.lauriethefish.betterportals.network;

import java.io.Serializable;

import lombok.Getter;

// Allows you to send back an error or success from a request
public class Response implements Serializable   {
    private static final long serialVersionUID = -2447012242238557293L;

    private Object result;
    @Getter private RequestException error; // This is null if the result was a success

    // Private constructor so that we use the statis methods below
    private Response(Object result, RequestException error)  {
        this.result = result;
        this.error = error;
    }

    // Methods for easily creating an error or success result
    public static Response error(RequestException error) {
        return new Response(null, error);
    }

    public static Response success(Object result)   {
        return new Response(result, null);
    }

    // Throw the exception if one was found, otherwise return the result
    public Object getResult() throws RequestException {
        if(error != null)   {throw error;}
        return result;
    }

    // Returns the result if there is one, or null if there isn't.
    public Object tryGetResult()   {
        return result;
    }

    // Simple marker for all exceptions that are returned in a response
    public static class RequestException extends Exception {
        private static final long serialVersionUID = -276936249697279234L;

        public RequestException(String message) {
            super(message);
        }
    }
}
