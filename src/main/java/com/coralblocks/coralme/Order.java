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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.coralblocks.coralme;

import java.util.ArrayList;
import java.util.List;

import com.coralblocks.coralme.util.DoubleUtils;
import com.coralblocks.coralme.Side;
import com.coralblocks.coralme.TimeInForce;
import com.coralblocks.coralme.Type;
import com.coralblocks.coralme.RejectReason;
import com.coralblocks.coralme.CancelReason;
import com.coralblocks.coralme.ExecuteSide;

public class Order {

    final static String EMPTY_CLIENT_ORDER_ID = "NULL";

    public final static int CLIENT_ORDER_ID_MAX_LENGTH = 64;

    private final List<OrderListener> listeners = new ArrayList<OrderListener>(64);

    private Side side;

    private long originalSize;

    private long totalSize;

    private long executedSize;

    private PriceLevel priceLevel;

    private long clientId;

    private final StringBuilder clientOrderId = new StringBuilder(CLIENT_ORDER_ID_MAX_LENGTH);

    private long price;

    private long acceptTime;

    private long restTime;

    private long cancelTime;

    private long rejectTime;

    private long reduceTime;

    private long executeTime;

    private long id;

    private String security;

    private TimeInForce tif;

    private Type type;

    Order next = null;

    Order prev = null;

    private boolean isResting;

    private boolean isPendingCancel;

    private long pendingSize;

    public Order() {
    }

    public void init(long clientId, CharSequence clientOrderId, long exchangeOrderId, String security, Side side, long size, long price, Type type, TimeInForce tif) {
        // Implementation
    }

    public final void setPendingCancel() {
        this.isPendingCancel = true;
    }

    public final void setPendingSize(long size) {
        this.pendingSize = size;
    }

    public final long getPendingSize() {
        return pendingSize;
    }

    public final boolean isPendingCancel() {
        return isPendingCancel;
    }

    public final boolean isResting() {
        return isResting;
    }

    public final double getPriceAsDouble() {
        return DoubleUtils.toDouble(price);
    }

    public final void setPriceLevel(PriceLevel priceLevel) {
        this.priceLevel = priceLevel;
    }

    public final PriceLevel getPriceLevel() {
        return priceLevel;
    }

    public final Type getType() {
        return type;
    }

    public final boolean isLimit() {
        return type == Type.LIMIT;
    }

    public final boolean isMarket() {
        return type == Type.MARKET;
    }

    public final long getOriginalSize() {
        return originalSize;
    }

    public final long getExecutedSize() {
        return executedSize;
    }

    public final long getFilledSize() {
        return executedSize;
    }

    public final long getOpenSize() {
        return totalSize - executedSize;
    }

    public final long getTotalSize() {
        return totalSize;
    }

    public final long getAcceptTime() {
        return acceptTime;
    }

    public final long getRestTime() {
        return restTime;
    }

    public final long getReduceTime() {
        return reduceTime;
    }

    public final long getExecuteTime() {
        return executeTime;
    }

    public final long getCancelTime() {
        return cancelTime;
    }

    public final long getRejectTime() {
        return rejectTime;
    }

    public final long getCanceledSize() {
        return originalSize - getOpenSize() - executedSize;
    }

    public final boolean isTerminal() {
        return getOpenSize() == 0;
    }

    public final TimeInForce getTimeInForce() {
        return tif;
    }

    public final boolean isAccepted() {
        return id > 0;
    }

    public final boolean isIoC() {
        return tif == TimeInForce.IOC;
    }

    public final boolean isDay() {
        return tif == TimeInForce.DAY;
    }

    public final boolean isGTC() {
        return tif == TimeInForce.GTC;
    }

    public final long getPrice() {
        return price;
    }

    public final Side getSide() {
        return side;
    }

    public final Side getOtherSide() {
        return side == Side.BUY ? Side.SELL : Side.BUY;
    }

    public final long getId() {
        return id;
    }

    public final long getExchangeOrderId() {
        return id;
    }

    public final long getClientId() {
        return clientId;
    }

    public final CharSequence getClientOrderId() {
        return clientOrderId;
    }

    public final String getSecurity() {
        return security;
    }

    public void addListener(OrderListener listener) {
        listeners.add(listener);
    }

    public void accept(long time, long id) {
        // Implementation
    }

    public void rest(long time) {
        // Implementation
    }

    public void reject(long time, RejectReason reason) {
        // Implementation
    }

    public void reduceTo(long time, long newTotalSize) {
        // Implementation
    }

    public void cancel(long time, long sizeToCancel) {
        cancel(time, sizeToCancel, CancelReason.USER);
    }

    public void cancel(long time, long sizeToCancel, CancelReason reason) {
        // Implementation
    }

    public void cancel(long time) {
        cancel(time, CancelReason.USER);
    }

    public void cancel(long time, CancelReason reason) {
        // Implementation
    }

    public void execute(long time, long sizeToExecute) {
        execute(time, ExecuteSide.TAKER, sizeToExecute, this.price, -1, -1);
    }

    public void execute(long time, ExecuteSide execSide, long sizeToExecute, long priceExecuted, long executionId, long matchId) {
        // Implementation
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(256);
        return toCharSequence(sb).toString();
    }

    public CharSequence toCharSequence(StringBuilder sb) {
        // Implementation
        return sb;
    }
}
