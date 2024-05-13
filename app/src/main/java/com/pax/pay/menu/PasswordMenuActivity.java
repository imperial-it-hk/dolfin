/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-11-25
 * Module Author: Steven.W
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.menu;

import com.pax.edc.R;
import com.pax.pay.password.ChangeAdjustPwdActivity;
import com.pax.pay.password.ChangeCancelPwdActivity;
import com.pax.pay.password.ChangeMerchantPwdActivity;
import com.pax.pay.password.ChangeRefundPwdActivity;
import com.pax.pay.password.ChangeSettlePwdActivity;
import com.pax.pay.password.ChangeVoidPwdActivity;
import com.pax.pay.password.ChangeTlePwdActivity;
import com.pax.view.MenuPage;

public class PasswordMenuActivity extends BaseMenuActivity {

    /**
     * Change password
     */
    public MenuPage createMenuPage() {

        MenuPage.Builder builder = new MenuPage.Builder(PasswordMenuActivity.this, 9, 3)
                .addMenuItem(getString(R.string.pwd_merchant), R.drawable.app_opermag,
                        ChangeMerchantPwdActivity.class)
                //.addMenuItem(getString(R.string.pwd_terminal), R.drawable.modify_mag_passwd, ChangeTerminalPwdActivity.class)
                .addMenuItem(getString(R.string.pwd_void), R.drawable.app_void,
                        ChangeVoidPwdActivity.class)
                //The icon of refund contains Chinese character, need to be modified.
                .addMenuItem(getString(R.string.pwd_refund), R.drawable.app_refund,
                        ChangeRefundPwdActivity.class)
                //The icon of adjust need to be modified


                .addMenuItem(getString(R.string.pwd_adjust), R.drawable.app_adjust,
                        ChangeAdjustPwdActivity.class)
                .addMenuItem(getString(R.string.pwd_settle), R.drawable.app_settle,
                        ChangeSettlePwdActivity.class)
                .addMenuItem(getString(R.string.pwd_tle), R.drawable.app_void,
                        ChangeTlePwdActivity.class)
                .addMenuItem(getString(R.string.pwd_cancel), R.drawable.app_void,
                        ChangeCancelPwdActivity.class)
                ;

        return builder.create();
    }

}
