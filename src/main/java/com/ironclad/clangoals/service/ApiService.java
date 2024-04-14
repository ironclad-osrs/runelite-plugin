package com.ironclad.clangoals.service;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.runelite.api.Player;
import net.runelite.api.Skill;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class ApiService
{
    private static final Logger log = LoggerFactory.getLogger(ApiService.class);
    private final String apiKey;

    public boolean verified = false;

    public ApiService (String apiKey)
    {
        this.apiKey = apiKey;

        verified = ApiResponse.isOk(me());
    }

    /**
     * Check that the current API key is valid.
     */
    public HttpResponse<String> me ()
    {
        try {
            HttpRequest req = sharedRequest(makeUri("/me"))
                .GET()
                .build();

            log.info("[ironclad-clan-goals] send get self request");

            return sharedClient()
                .send(req, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Persist the account has with the current player name
     * against the authenticated API key.
     */
    public HttpResponse<Void> updatePlayer (long account, Player player)
    {
        try {
            JsonObject data = new JsonObject();

            data.addProperty("account_hash", account);
            data.addProperty("character_name", player.getName());

            HttpRequest req = sharedRequest(makeUri("/characters"))
                .PUT(HttpRequest.BodyPublishers.ofString(data.toString()))
                .build();

            log.info("[ironclad-clan-goals] send update character request");

            return sharedClient()
                .send(req, HttpResponse.BodyHandlers.discarding());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Persist the xp against an account and skill for the
     * authenticated API key.
     */
    public CompletableFuture
            <HttpResponse<Void>> updateXp (long account, Skill skill, int xp)
    {
        JsonObject data = new JsonObject();

        data.addProperty("account_hash", account);
        data.addProperty("skill", skill.getName().toLowerCase());
        data.addProperty("xp", xp);

        HttpRequest req = sharedRequest(makeUri("/xp"))
            .PUT(HttpRequest.BodyPublishers.ofString(data.toString()))
            .build();

        log.info("[ironclad-clan-goals] send update xp request");

        return sharedClient()
            .sendAsync(req, HttpResponse.BodyHandlers.discarding());
    }

    /**
     * Shared request headers for all requests.
     */
    private HttpRequest.Builder sharedRequest(URI uri)
    {
        return HttpRequest
            .newBuilder(uri)
            .header("accept", "application/json")
            .header("content-type", "application/json")
            .header("authorization", apiKey);
    }

    /**
     * Shared response parse for all requests.
     */
    private HttpClient sharedClient()
    {
        return HttpClient
            .newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();
    }

    /**
     * Shared base URI for all requests.
     */
    private URI makeUri (String path)
    {
        return URI.create("https://progress.quest/api/runelite"+path);
    }
}
