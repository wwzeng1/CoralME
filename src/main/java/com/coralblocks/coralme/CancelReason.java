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

public enum CancelReason implements CharEnum {
    MISSED('M'),
    USER('U'),
    NO_LIQUIDITY('L'),
    PRICE('E'),
    CROSSED('C'),
    PURGED('P'),
    EXPIRED('D'),
    ROLLED('R');

    private final char b;
    public static final CharMap<CancelReason> ALL = new CharMap<CancelReason>();

    static {
        for (CancelReason cr : CancelReason.values()) {
            if (ALL.put(cr.getChar(), cr) != null)
                throw new IllegalStateException("Duplicate: " + cr);
        }
    }

    private CancelReason(char b) {
        this.b = b;
    }

    @Override
    public final char getChar() {
        return b;
    }
}
