package com.alibaba.csp.sentinel;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlot;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;

/**
 * The entry for asynchronous resources.
 *
 * @author Eric Zhao
 */
public class AsyncEntry extends CtEntry {

    private Context asyncContext;

    AsyncEntry(ResourceWrapper resourceWrapper, ProcessorSlot<Object> chain, Context context) {
        super(resourceWrapper, chain, context);
    }

    /**
     * Remove current entry from local context, but does not exit.
     */
    void cleanCurrentEntryInLocal() {
        Context originalContext = context;
        if (originalContext != null) {
            Entry curEntry = originalContext.getCurEntry();
            if (curEntry == this) {
                Entry parent = this.parent;
                originalContext.setCurEntry(parent);
                if (parent != null) {
                    ((CtEntry)parent).child = null;
                }
            } else {
                throw new IllegalStateException("Bad async context state");
            }
        }
    }

    public Context getAsyncContext() {
        return asyncContext;
    }

    /**
     * The async context should not be initialized until the node for current resource has been set to current entry.
     */
    void setUpAsyncContext() {
        if (asyncContext == null) {
            this.asyncContext = Context.newAsyncContext(context.getEntranceNode(), context.getName())
                .setOrigin(context.getOrigin())
                .setCurEntry(this);
        } else {
            throw new IllegalStateException("Duplicate initialize of async context");
        }
    }

    @Override
    protected void clearEntryContext() {
        super.clearEntryContext();
        this.asyncContext = null;
    }

    @Override
    protected Entry trueExit(int count, Object... args) throws ErrorEntryFreeException {
        System.out.println("Exit for async invocation: " + resourceWrapper.getName());
        exitForContext(asyncContext, count, args);

        return parent;
    }
}
