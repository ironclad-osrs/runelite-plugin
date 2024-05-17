package com.ironclad.clangoals.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.Response;

public class ApiResponse
{
    /**
     * Parse response text into a JSON object.
     */
    static public JsonObject parse(Response response)
    {
        return new JsonParser()
                .parse(response.body().toString())
                .getAsJsonObject();
    }
}
