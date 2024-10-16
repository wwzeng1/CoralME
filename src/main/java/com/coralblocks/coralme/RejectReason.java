/*
 * Copyright 2023 (c) CoralBlocks - http://www.coralblocks.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.coralblocks.coralme;

import com.coralblocks.coralme.util.CharEnum;
import com.coralblocks.coralme.util.CharMap;

public enum RejectReason implements CharEnum {
    MISSING_FIELD('1'),
    BAD_TYPE('2'),
    BAD_TIF('3'),
    BAD_SIDE('4'),
    BAD_SYMBOL('5'),

    BAD_PRICE('P'),
    BAD_SIZE('S'),
    TRADING_HALTED('H'),
    BAD_LOT('L'),
    UNKNOWN_SYMBOL('U'),
    DUPLICATE_EXCHANGE_ORDER_ID('E'),
    DUPLICATE_CLIENT_ORDER_ID('C');

    private final char b;
    public static final CharMap<RejectReason> ALL = new CharMap<RejectReason>();

    static {
        for (RejectReason rr : RejectReason.values()) {
            if (ALL.put(rr.getChar(), rr) != null)
                throw new IllegalStateException("Duplicate: " + rr);
        }
    }

    private RejectReason(char b) {
        this.b = b;
    }

    @Override
    public final char getChar() {
        return b;
    }
}
