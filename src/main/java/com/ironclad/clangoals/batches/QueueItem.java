package com.ironclad.clangoals.batches;

import lombok.Getter;

@Getter
public class QueueItem
{
    private final Object data;

    public QueueItem(Object data)
    {
        this.data = data;
    }
}
