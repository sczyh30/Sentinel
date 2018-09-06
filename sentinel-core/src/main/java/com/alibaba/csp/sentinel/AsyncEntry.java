package com.alibaba.csp.sentinel;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlot;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;

/**
 * @author Eric Zhao
 */
public class AsyncEntry extends CtEntry {

    private Context asyncContext;

    AsyncEntry(ResourceWrapper resourceWrapper, ProcessorSlot<Object> chain, Context context) {
        super(resourceWrapper, chain, context);
    }

    public Context getAsyncContext() {
        return asyncContext;
    }

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
