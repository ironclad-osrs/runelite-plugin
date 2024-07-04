package com.ironclad.clangoals.service;

import lombok.Setter;

@Setter
public class XpBatchQueue extends BatchQueue
{
    private ApiService api;
    private long account;

    @Override
    public void flush()
    {
        // Extra check to limit abnormal requests.
        if (items.isEmpty()) return;

        // Pass items onto API service to deal with
        // data transformation and request handling.
        this.api.batchUpdateXp(
                this.account,
                items
        );

        this.resetQueue();
    }
}
