/*
 * Copyright (C) 2015-2017 PÂRIS Quentin
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

package org.phoenicis.library;

import org.graalvm.polyglot.Value;
import org.phoenicis.library.dto.ShortcutDTO;
import org.phoenicis.scripts.session.InteractiveScriptSession;
import org.phoenicis.scripts.interpreter.ScriptInterpreter;

import java.util.List;
import java.util.function.Consumer;

public class ShortcutRunner {
    private final ScriptInterpreter scriptInterpreter;
    private final LibraryManager libraryManager;

    public ShortcutRunner(ScriptInterpreter scriptInterpreter, LibraryManager libraryManager) {
        this.scriptInterpreter = scriptInterpreter;
        this.libraryManager = libraryManager;
    }

    public void run(String shortcutName, List<String> arguments, Consumer<Exception> errorCallback) {
        run(libraryManager.fetchShortcutsFromName(shortcutName), arguments, errorCallback);
    }

    public void run(ShortcutDTO shortcutDTO, List<String> arguments, Consumer<Exception> errorCallback) {
        final InteractiveScriptSession interactiveScriptSession = scriptInterpreter.createInteractiveSession();

        interactiveScriptSession.eval("include(\"engines.wine.shortcuts.reader\");",
                ignored -> interactiveScriptSession.eval("new ShortcutReader()", output -> {
                    final Value shortcutReader = (Value) output;

                    shortcutReader.invokeMember("of", shortcutDTO);
                    shortcutReader.invokeMember("run", arguments);
                }, errorCallback), errorCallback);
    }

    public void stop(ShortcutDTO shortcutDTO, Consumer<Exception> errorCallback) {
        final InteractiveScriptSession interactiveScriptSession = scriptInterpreter.createInteractiveSession();

        interactiveScriptSession.eval("include(\"engines.wine.shortcuts.reader\");",
                ignored -> interactiveScriptSession.eval("new ShortcutReader()", output -> {
                    final Value shortcutReader = (Value) output;

                    shortcutReader.invokeMember("of", shortcutDTO);
                    shortcutReader.invokeMember("stop");
                }, errorCallback), errorCallback);
    }

}
