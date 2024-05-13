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
package com.pax.pay.trans.pack;


import androidx.annotation.NonNull;
import com.pax.abl.core.ipacker.PackListener;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;

public class PackBatchUpNotice extends PackIso8583 {

    public PackBatchUpNotice(PackListener listener) {
        super(listener);
    }

    @Override
    @NonNull
    public byte[] pack(@NonNull TransData transData) {
        // 当前交易类型，通知类批上送
        ETransType transType = transData.getTransType();
        // 原交易类型
        ETransType origTransType = transData.getOrigTransType();
        // 切换交易类型(退货， 预授权完成通知)
        transData.setTransType(origTransType);
        if (origTransType == null) {
            return "".getBytes();
        }

        byte[] buf = "".getBytes();
        byte[] temp = origTransType.getPackager(listener).pack(transData);
        if (temp.length - 8 >= 20) {
            buf = new byte[temp.length - 8];
            System.arraycopy(temp, 0, buf, 0, temp.length - 8);
            // 把0220转成0320
            buf[11] = 0x03;
            // 去掉bit64
            buf[11 + 2 + 8 - 1] &= 0xfe;
        }
        // 恢复交易类型
        transData.setTransType(transType);

        return buf;
    }

}
