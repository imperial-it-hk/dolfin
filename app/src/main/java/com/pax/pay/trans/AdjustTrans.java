/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-12-27
 * Module Author: caowb
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.trans;

import android.content.Context;
import android.content.DialogInterface;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.abl.utils.EncUtils;
import com.pax.edc.R;
import com.pax.edc.opensdk.TransResult;
import com.pax.pay.ECR.EcrData;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.Constants;
import com.pax.pay.trans.action.ActionInputPassword;
import com.pax.pay.trans.action.ActionInputTransData;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.Utils;
import com.pax.settings.SysParam;
import com.pax.view.dialog.CustomAlertDialog;

import java.util.LinkedHashMap;

/*
 * according to the EDC Prolin, Tip of Sale,
 * Offline sale can be adjusted times before settlement.
 */

public class AdjustTrans extends BaseTrans {
    private String origTransNo;

    // the adjustment is just a state for a transaction,
    // so it cannot use the logic of base transaction which uses transData from the BaseTrans,
    // and in the db, each record has its id,
    // which means we cannot call transData.save() cuz it will create an excess record,
    // and we cannot call the transData.updateTrans() either, cuz the record with new id is not existed.
    // So we have to use the origTransData instead of transData.
    private TransData origTransData;
    /**
     * is need read transaction record
     */
    private boolean isNeedFindOrigTrans = true;
    /**
     * is need input transaction NO
     */
    private boolean isNeedInputTransNo = true;

    public AdjustTrans(Context context, TransEndListener transListener) {
        super(context, ETransType.ADJUST, transListener);
        isNeedFindOrigTrans = true;
        isNeedInputTransNo = true;
    }

    public AdjustTrans(Context context, TransData origTransData, TransEndListener transListener) {
        super(context, ETransType.ADJUST, transListener); // ignore the type, cuz we are using the origTransData
        this.origTransData = origTransData;
        isNeedFindOrigTrans = false;
        isNeedInputTransNo = false;
    }

    public AdjustTrans(Context context, String origTransNo, TransEndListener transListener) {
        super(context, ETransType.ADJUST, transListener);
        this.origTransNo = origTransNo;
        isNeedFindOrigTrans = true;
        isNeedInputTransNo = false;
    }

    @Override
    protected void bindStateOnAction() {
        //input manager password
        ActionInputPassword inputPasswordAction = new ActionInputPassword(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionInputPassword) action).setParam(getCurrentContext(), 6,
                        getString(R.string.prompt_adjust_pwd), null);
            }
        });
        bind(State.INPUT_PWD.toString(), inputPasswordAction);

        //input original trance no
        ActionInputTransData enterTransNoAction = new ActionInputTransData(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                Context context = getCurrentContext();
                ((ActionInputTransData) action).setParam(context, getString(R.string.menu_adjust))
                        .setInputLine(getString(R.string.prompt_input_transno), ActionInputTransData.EInputType.NUM, 6, true);
            }
        });
        bind(AdjustTrans.State.ENTER_TRANSNO.toString(), enterTransNoAction, true);

        // input new tips
        ActionInputTransData newTipsAction = new ActionInputTransData(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                String title = getString(R.string.menu_adjust);
                String transAmount = origTransData.getAmount();
                String transTips = origTransData.getTipAmount();
                float adjustPercent = origTransData.getIssuer().getAdjustPercent();

                LinkedHashMap<String, String> map = new LinkedHashMap<>();
                map.put(getString(R.string.prompt_total_amount), transAmount);
                map.put(getString(R.string.prompt_ori_tips), transTips);
                map.put(getString(R.string.prompt_adjust_percent), Float.toString(adjustPercent));

                ((ActionInputTransData) action).setParam(getCurrentContext(), title, map).setInputLine(
                        getString(R.string.prompt_new_tips), ActionInputTransData.EInputType.AMOUNT, Constants.AMOUNT_DIGIT, false);
            }
        });
        bind(AdjustTrans.State.ENTER_AMOUNT.toString(), newTipsAction, true);

        // if need pwd for adjust
        /*if (FinancialApplication.getSysParam().get(SysParam.BooleanParam.OTHTC_VERIFY)) {
            gotoState(AdjustTrans.State.INPUT_PWD.toString());
        } else if (isNeedInputTransNo) {// need input trans NO
            gotoState(AdjustTrans.State.ENTER_TRANSNO.toString());
        } else {// not need input trans NO
            if (isNeedFindOrigTrans) {
                validateOrigTransData(Utils.parseLongSafe(origTransNo, -1));
            } else {
                updateAcqIssuer();
            }
        }*/
        showMsgNotAllowed();

    }

    enum State {
        INPUT_PWD,
        ENTER_TRANSNO,
        ENTER_AMOUNT
    }

    @Override
    public void gotoState(String state) {
        if (state.equals(State.INPUT_PWD.toString())) {
            EcrData.instance.isOnHomeScreen = false;
        }
        super.gotoState(state);
    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {
        State state = State.valueOf(currentState);
        switch (state) {
            case INPUT_PWD:
                if (result.getRet() != TransResult.SUCC) {
                    EcrData.instance.isOnHomeScreen = true;
                    transEnd(result);
                }
                else {
                    onInputPwd(result);
                }
                break;
            case ENTER_TRANSNO:
                onEnterTraceNo(result);
                break;
            case ENTER_AMOUNT:
                onEnterAmount(result);
                break;
            default:
                break;
        }
    }

    private void onInputPwd(ActionResult result) {
        String data = EncUtils.sha1((String) result.getData());
        if (!data.equals(FinancialApplication.getSysParam().get(SysParam.StringParam.SEC_ADJUST_PWD))) {
            EcrData.instance.isOnHomeScreen = true;
            transEnd(new ActionResult(TransResult.ERR_PASSWORD, null));
            return;
        }
        if (isNeedInputTransNo) {// need input trans NO
            gotoState(AdjustTrans.State.ENTER_TRANSNO.toString());
        } else {// not need input trans NO
            if (isNeedFindOrigTrans) {
                validateOrigTransData(Utils.parseLongSafe(origTransNo, -1));
            } else {
                updateAcqIssuer();
                gotoState(State.ENTER_AMOUNT.toString());
            }
        }
    }

    private void onEnterTraceNo(ActionResult result) {
        String content = (String) result.getData();
        long transNo;
        if (content == null) {
            TransData tempTransData = FinancialApplication.getTransDataDbHelper().findLastTransData();
            if (tempTransData == null) {
                transEnd(new ActionResult(TransResult.ERR_NO_TRANS, null));
                return;
            }
            transNo = tempTransData.getTraceNo();
        } else {
            transNo = Utils.parseLongSafe(content, -1);
        }
        validateOrigTransData(transNo);
    }

    private void onEnterAmount(ActionResult result) {
        long newTotalAmount = (long) result.getData();
        long newTipAmount = (long) result.getData1();

        //base amount and tip
        origTransData.setAmount(Long.toString(newTotalAmount) + "");
        //set tip
        origTransData.setTipAmount(Long.toString(newTipAmount) + "");
        // update original transaction record
        //set status as adjusted
        origTransData.setTransState(TransData.ETransStatus.ADJUSTED);
        origTransData.setOfflineSendState(TransData.OfflineStatus.OFFLINE_NOT_SENT);
        FinancialApplication.getTransDataDbHelper().updateTransData(origTransData);
        transEnd(result);
    }

    // check original transaction information
    private void validateOrigTransData(long origTransNo) {
        origTransData = FinancialApplication.getTransDataDbHelper().findTransDataByTraceNo(origTransNo, true);
        if (origTransData == null) {
            // no original transaction
            transEnd(new ActionResult(TransResult.ERR_NO_ORIG_TRANS, null));
            return;
        }

        if (Component.chkSettlementStatus(origTransData.getAcquirer().getName())) {
            // Last settlement not success, need to settle firstly
            transEnd(new ActionResult(TransResult.ERR_SETTLE_NOT_COMPLETED,  null));
            return;
        }

        // unsale or offline sale can't adjust
        ETransType trType = origTransData.getTransType();
        if (!trType.isAdjustAllowed()) {
            transEnd(new ActionResult(TransResult.ERR_ADJUST_UNSUPPORTED, null));
            return;
        }

        // tip not open
        if (!FinancialApplication.getSysParam().get(SysParam.BooleanParam.EDC_SUPPORT_TIP)) {
            transEnd(new ActionResult(TransResult.ERR_ADJUST_UNSUPPORTED, null));
            return;
        }

        //  has voided/adjust transaction can not adjust
        TransData.ETransStatus trStatus = origTransData.getTransState();
        if (trStatus.equals(TransData.ETransStatus.VOIDED)) {
            transEnd(new ActionResult(TransResult.ERR_HAS_VOIDED, null));
            return;
        }

        gotoState(State.ENTER_AMOUNT.toString());
    }

    // set original trans data
    private void updateAcqIssuer() {
        transData.setIssuer(origTransData.getIssuer());
    }

    private void showMsgNotAllowed(){
        FinancialApplication.getApp().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final CustomAlertDialog dialog = new CustomAlertDialog(context, CustomAlertDialog.NORMAL_TYPE);
                dialog.setConfirmClickListener(new CustomAlertDialog.OnCustomClickListener() {
                    @Override
                    public void onClick(CustomAlertDialog alertDialog) {
                        dialog.dismiss();
                    }
                });
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
                    }
                });
                dialog.setTimeout(3);
                dialog.show();
                dialog.setNormalText(getString(R.string.err_not_allowed));
                dialog.showCancelButton(false);
                dialog.showConfirmButton(true);
            }
        });
    }

}
