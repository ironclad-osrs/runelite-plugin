package com.ironclad.clangoals.batches;

import com.ironclad.clangoals.service.ApiService;
import lombok.Setter;

@Setter
public class KillBatchQueue extends BatchQueue
{
    private ApiService api;
    private long account;

    @Override
    public void flush()
    {
        // Extra check to limit abnormal requests.
        if (items.isEmpty()) return;

        this.api.batchUpdateKills(this.account, items);

        this.resetQueue();
    }
}
