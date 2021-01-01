/*
 * * Copyright (C) 2014-2018 Matt Baxter http://kitteh.org
 * * Copyright (C) 2020-2021 Emeric Werner https://geolykt.de
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.kitteh.craftirc.irc;

import org.jetbrains.annotations.NotNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.feature.auth.NickServ;

class NicklessServ extends NickServ { //TODO name?
    NicklessServ(@NotNull Client client, @NotNull String accountName, @NotNull String password) {
        super(client, "NickServ", accountName, password);
    }

    @NotNull
    @Override
    protected String getAuthentication() {
        return "PRIVMSG " + this.getAccountName()+ " :IDENTIFY " + this.getPassword();
    }
}
