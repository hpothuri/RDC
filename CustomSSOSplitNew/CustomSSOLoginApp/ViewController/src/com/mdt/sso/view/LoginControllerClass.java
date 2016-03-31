package com.mdt.sso.view;

import oracle.adf.controller.v2.context.PageLifecycleContext;
import oracle.adf.controller.v2.lifecycle.Lifecycle;
import oracle.adf.controller.v2.lifecycle.PagePhaseEvent;
import oracle.adf.controller.v2.lifecycle.PagePhaseListener;

public class LoginControllerClass implements PagePhaseListener {
    public LoginControllerClass() {
        super();
    }

    @Override
    public void afterPhase(PagePhaseEvent pagePhaseEvent) {
        PageLifecycleContext ctx = (PageLifecycleContext)pagePhaseEvent.getLifecycleContext();
        if (pagePhaseEvent.getPhaseId() == Lifecycle.PREPARE_RENDER_ID) {
            onPagePreRender();
        }
    }

    @Override
    public void beforePhase(PagePhaseEvent pagePhaseEvent) {
        PageLifecycleContext ctx = (PageLifecycleContext)pagePhaseEvent.getLifecycleContext();
        if (pagePhaseEvent.getPhaseId() == Lifecycle.PREPARE_MODEL_ID) {
            onPageLoad();
        }
    }

    public void onPageLoad() {
        // Subclasses can override this.
        if (!ADFUtils.isIEOrMozillaBrowser()) {
            ADFUtils.navigateToControlFlowCase("browserCheck");
        }
    }

    public void onPagePreRender() {
        // Subclasses can override this.
    }
}
