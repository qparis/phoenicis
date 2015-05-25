/*
 * Copyright (C) 2015 PÂRIS Quentin
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.playonlinux.common.messages;

import com.playonlinux.domain.CancelException;

public abstract class CancelerSynchroneousMessage<RESULT_TYPE> extends SynchroneousMessage<RESULT_TYPE>
        implements CancelerMessage {
    private Boolean processCanceled = false;

    public RESULT_TYPE getResponse() throws CancelException {
        RESULT_TYPE response = super.getResponse();

        if(this.processCanceled) {
            throw new CancelException();
        }

        return response;
    }

    public void sendCancelSignal() {
        this.processCanceled = true;
        super.semaphore.release();
    }
}
