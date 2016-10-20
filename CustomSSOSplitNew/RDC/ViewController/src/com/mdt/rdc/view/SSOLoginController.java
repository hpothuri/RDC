package com.mdt.rdc.view;

import com.mdt.rdc.view.UserStudyDetailsBean;

import com.mdt.sso.view.ADFUtils;

import java.util.Map;

import javax.faces.context.FacesContext;

import javax.faces.event.PhaseId;

import oracle.adf.model.RegionContext;
import oracle.adf.model.RegionController;

public class SSOLoginController implements RegionController {
    public SSOLoginController() {
        super();
    }

    @Override
    public boolean refreshRegion(RegionContext regionContext) {
        int refreshFlag = regionContext.getRefreshFlag();
        FacesContext fctx = FacesContext.getCurrentInstance();
        //check internal request parameter
        //Map requestMap = fctx.getExternalContext().getRequestMap();
        UserStudyDetailsBean sutdyBean = (UserStudyDetailsBean)ADFUtils.evaluateEL("#{pageFlowScope.UserStudyBean}");
        // System.out.println("IN refresh Region ...");
        if (null != sutdyBean && sutdyBean.isSingleStudy()) {
            System.out.println("IN refresh Region ...Single study case.refreshFlag...." + refreshFlag);
//            sutdyBean.invokeRDCLogin();
            regionContext.getRegionBinding().refresh(refreshFlag);
        }
        return false;
    }

    @Override
    public boolean validateRegion(RegionContext regionContext) {
        regionContext.getRegionBinding().validate();
        return false;
    }

    @Override
    public boolean isRegionViewable(RegionContext regionContext) {
        return false;
    }

    @Override
    public String getName() {
        return null;
    }
}
