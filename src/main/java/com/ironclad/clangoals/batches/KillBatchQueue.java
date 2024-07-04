package com.ironclad.clangoals.batches;

import com.ironclad.clangoals.service.ApiService;
import lombok.Setter;
import net.runelite.api.NPC;
import net.runelite.client.events.NpcLootReceived;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Setter
public class KillBatchQueue extends BatchQueue
{
    private ApiService api;
    private long account;

    @Override
    public void flush()
    {
        log.info("Flushing kill batch");

        // Extra check to limit abnormal requests.
        if (items.isEmpty()) return;

        ArrayList<QueueItem> kills = items.stream().map(l -> {
            NpcLootReceived npcLootReceived = (NpcLootReceived) l.getData();
            NPC npc = npcLootReceived.getNpc();

            return new QueueItem(npc);
        }).collect(Collectors.toCollection(ArrayList::new));

        this.api.batchUpdateKills(this.account, kills);

        this.resetQueue();
    }
}
