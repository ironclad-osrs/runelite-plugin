package com.ironclad.clangoals.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.runelite.api.events.StatChanged;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.runelite.api.Player;
import net.runelite.api.Skill;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

public class ApiService
{
    private static final Logger log = LoggerFactory.getLogger(ApiService.class);

    private final OkHttpClient httpClient;
    private final String apiKey;

    public boolean verified = false;

    public ApiService (OkHttpClient httpClient, String apiKey)
    {
        this.httpClient = httpClient;
        this.apiKey = apiKey;

        Response tmp = me();
        verified = tmp.isSuccessful();
        tmp.close();
    }

    /**
     * Check that the current API key is valid.
     */
    public Response me ()
    {
        try {
            Request req = sharedRequest(makeUri("/me"))
                .get()
                .build();

            log.debug("[ironclad-clan-goals] send get self request");

            return httpClient.newCall(req).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Persist the account has with the current player name
     * against the authenticated API key.
     */
    public void updatePlayer (long account, Player player)
    {
        JsonObject data = new JsonObject();

        data.addProperty("account_hash", account);
        data.addProperty("character_name", player.getName());

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), data.toString()
        );

        Request req = sharedRequest(makeUri("/characters"))
                .put(body)
                .build();

        log.debug("[ironclad-clan-goals] send update character request");

        sharedClient().newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log.warn(
                    "[ironclad-clan-goals] error updating character {}:{}",
                    account, player.getName(), e
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                response.close();

                log.debug(
                    "[ironclad-clan-goals] character updated {}:{}",
                    account, player.getName()
                );
            }
        });
    }

    /**
     * Persist the xp against an account and skill for the
     * authenticated API key.
     */
    public void updateXp (long account, Skill skill, int xp)
    {
        JsonObject data = new JsonObject();

        data.addProperty("account_hash", account);
        data.addProperty("skill", skill.getName().toLowerCase());
        data.addProperty("xp", xp);

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), data.toString()
        );

        Request req = sharedRequest(makeUri("/xp"))
                .put(body)
                .build();

        log.debug("[ironclad-clan-goals] send update xp request");

        sharedClient().newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log.warn(
                    "[ironclad-clan-goals] error updating xp {}",
                    account, e
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                response.close();

                log.debug("[ironclad-clan-goals] xp updated {}", account);
            }
        });
    }

    public void batchUpdateXp(long account, ArrayList<QueueItem> batch)
    {
        JsonArray skills = new JsonArray();

        batch.forEach(item -> {
            StatChanged event = (StatChanged) item.getData();

            JsonObject tmp = new JsonObject();
            tmp.addProperty("skill", event.getSkill().getName().toLowerCase());
            tmp.addProperty("xp", event.getXp());

            skills.add(tmp);
        });

        JsonObject data = new JsonObject();

        data.addProperty("account_hash", account);
        data.add("batch", skills);

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), data.toString()
        );

        Request req = sharedRequest(makeUri("/batch/xp"))
                .put(body)
                .build();

        log.debug("[ironclad-clan-goals] send batch update xp request");

        sharedClient().newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log.warn(
                        "[ironclad-clan-goals] error updating xp {}",
                        account, e
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                response.close();

                log.debug("[ironclad-clan-goals] batch xp updated {}", account);
            }
        });
    }

    /**
     * Shared request headers for all requests.
     */
    private Request.Builder sharedRequest(URI uri)
    {
        return new Request
            .Builder()
            .url(uri.toString())
            .header("accept", "application/json")
            .header("content-type", "application/json")
            .header("authorization", apiKey);
    }

    /**
     * Shared http client for all requests.
     */
    private OkHttpClient sharedClient()
    {
        return httpClient
            .newBuilder()
            .build();
    }

    /**
     * Shared base URI for all requests.
     */
    private URI makeUri (String path)
    {
        return URI.create("http://localhost:3000/api/runelite"+path);
//        return URI.create("https://progress.quest/api/runelite"+path);
    }
}
