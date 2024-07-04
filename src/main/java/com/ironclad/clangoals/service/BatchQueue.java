package com.ironclad.clangoals.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public abstract class BatchQueue
{
    protected static final Logger log = LoggerFactory.getLogger(ApiService.class);

    // How many items can be batched
    // together before flushing.
    private final int MAX_ITEMS = 10;

    // How many ticks (0.6s) can a batch
    // have between each item being added.
    //
    // If cooldown is reached, items are
    // flushed.
    //
    // 15*0.6=9
    private final int TICK_COOLDOWN = 9;

    // Array of items to be batched together.
    protected final ArrayList<QueueItem> items;

    // How many ticks since last item.
    private int sinceLastItem = 0;

    public BatchQueue ()
    {
        this.items = new ArrayList<>();
    }

    /**
     * Flush any items by processing them.
     */
    public abstract void flush();

    /**
     * Clear any items in the queue
     * and reset since last item
     * cooldown.
     */
    protected void resetQueue()
    {
        log.debug("[ironclad-clan-goals] Resetting batch queue.");

        this.items.clear();
        this.sinceLastItem = 0;
    }

    /**
     * Pushes an item to the queue.
     *
     * Once the MAX_ITEMS is hit then
     * automatically flush items.
     *
     * @param item QueueItem to push to the queue.
     */
    public void addItem (QueueItem item)
    {
        log.debug("[ironclad-clan-goals] Adding item to batch queue");

        items.add(item);
        sinceLastItem = 0;

        if (items.size() >= MAX_ITEMS) {
            log.debug("[ironclad-clan-goals] Batch queue max items reached. Flushing.");

            this.flush();
        }
    }

    /**
     * Increment the cooldown since the
     * last item was pushed into the queue.
     *
     * Once the TICK_COOLDOWN is reached then
     * automatically flush items.
     */
    public void incrementCooldown()
    {
        sinceLastItem += 1;

        if (sinceLastItem >= TICK_COOLDOWN && !items.isEmpty()) {
            log.debug("[ironclad-clan-goals] Batch queue cooldown reached. Flushing.");

            this.flush();
        }
    }
}
