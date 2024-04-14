package com.ironclad.clangoals.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.http.HttpResponse;

public class ApiResponse
{
    /**
     * Check that a response has a successful
     * HTTP status code.
     */
    static public boolean isOk(HttpResponse<String> response)
    {
        int code = response.statusCode();

        return code >= 200 && code < 300;
    }

    /**
     * Parse response text into a JSON object.
     */
    static public JsonObject parse(HttpResponse<String> response)
    {
        return new JsonParser()
                .parse(response.body())
                .getAsJsonObject();
    }
}
