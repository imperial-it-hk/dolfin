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
package com.pax.pay.trans.action;

import com.pax.abl.core.AAction;

public class ActionSelectAuthMode extends AAction {

    public ActionSelectAuthMode(ActionStartListener listener) {
        super(listener);
    }

    @Override
    protected void process() {
        throw new UnsupportedOperationException();
    }

}
