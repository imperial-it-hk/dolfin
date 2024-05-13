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
package com.pax.pay.trans.receipt;

public interface PrintListener {
    enum Status {
        OK,
        CONTINUE,
        CANCEL,
    }

    /**
     * print prompt
     *
     * @param title
     * @param message
     */
    void onShowMessage(String title, String message);

    /**
     * printer abnormal
     */
    Status onConfirm(String title, String message, int timeout);

    /**
     * printer abnormal
     */
    Status onPrintNext(String title, String message);

    void onEnd();
}
