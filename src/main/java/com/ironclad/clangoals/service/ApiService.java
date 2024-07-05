package com.ironclad.clangoals.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ironclad.clangoals.PluginItem;
import com.ironclad.clangoals.PluginNPC;
import com.ironclad.clangoals.batches.QueueItem;
import lombok.NonNull;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.runelite.api.NPC;
import net.runelite.api.events.StatChanged;
import net.runelite.api.Player;

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

        sharedClient().newCall(req).enqueue(sharedCallback(
                "character updated",
                "error updating character"
        ));
    }

    /**
     * Persist a batch of xp drops.
     */
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

        sharedClient().newCall(req).enqueue(sharedCallback(
                "batch xp updated",
                "error updating xp"
        ));
    }

    /**
     * Persist a batch of loot drops.
     */
    public void batchUpdateLoot(long account, ArrayList<QueueItem> batch)
    {
        JsonArray loot = new JsonArray();

        batch.forEach(item -> {
            PluginItem event = (PluginItem) item.getData();

            JsonObject tmp = new JsonObject();
            tmp.addProperty("item_id", event.getId());
            tmp.addProperty("quantity", event.getQuantity());
            tmp.addProperty("name", event.getName());

            loot.add(tmp);
        });

        JsonObject data = new JsonObject();

        data.addProperty("account_hash", account);
        data.add("batch", loot);

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), data.toString()
        );

        Request req = sharedRequest(makeUri("/batch/loot"))
                .put(body)
                .build();

        log.debug("[ironclad-clan-goals] send batch update loot request");

        sharedClient().newCall(req).enqueue(sharedCallback(
                "batch loot updated",
                "error updating loot"
        ));
    }

    /**
     * Persist a batch of kill records.
     */
    public void batchUpdateKills(long account, ArrayList<QueueItem> batch)
    {
        JsonArray kills = new JsonArray();

        batch.forEach(item -> {
            PluginNPC event = (PluginNPC) item.getData();

            JsonObject tmp = new JsonObject();
            tmp.addProperty("npc_id", event.getId());
            tmp.addProperty("name", event.getName());

            kills.add(tmp);
        });

        JsonObject data = new JsonObject();

        data.addProperty("account_hash", account);
        data.add("batch", kills);

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), data.toString()
        );

        Request req = sharedRequest(makeUri("/batch/kills"))
                .put(body)
                .build();

        log.debug("[ironclad-clan-goals] send batch update kills request");

        sharedClient().newCall(req).enqueue(sharedCallback(
                "batch kills updated",
                "error updating kills"
        ));
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
     * Shared client callback.
     */
    private Callback sharedCallback(String successMessage, String errorMessage)
    {
        return new Callback() {
            @Override
            public void onFailure(
                    @NonNull
                    Call call,
                    @NonNull
                    IOException e
            ) {
                log.warn("[ironclad-clan-goals] "+errorMessage);
            }

            @Override
            public void onResponse(
                    @NonNull
                    Call call,
                    @NonNull
                    Response response
            ) throws IOException {
                response.close();

                log.debug("[ironclad-clan-goals] "+successMessage);
            }
        };
    }

    /**
     * Shared base URI for all requests.
     */
    private URI makeUri (String path)
    {
        return URI.create("https://progress.quest/api/runelite"+path);
    }
}
