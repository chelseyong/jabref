/*  Copyright (C) 2015 JabRef contributors.
    Copyright (C) 2015 Oscar Gustafsson.
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package net.sf.jabref.external.push;

import java.io.IOException;

import javax.swing.*;

import net.sf.jabref.*;

import com.jgoodies.forms.builder.FormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import net.sf.jabref.gui.BasePanel;
import net.sf.jabref.gui.actions.BrowseAction;
import net.sf.jabref.logic.l10n.Localization;
import net.sf.jabref.model.database.BibtexDatabase;
import net.sf.jabref.model.entry.BibtexEntry;

/**
 * Abstract class for pushing entries into different editors.
 */
public abstract class AbstractPushToApplication implements PushToApplication {

    protected boolean couldNotCall;
    protected boolean couldNotConnect;
    protected boolean notDefined;
    protected JPanel settings;
    protected final JTextField Path = new JTextField(30);
    protected String commandPath;
    protected String commandPathPreferenceKey;
    protected String citeCommand = Globals.prefs.get(JabRefPreferences.CITE_COMMAND);
    protected FormBuilder builder;


    @Override
    public String getName() {
        return Localization.menuTitle("Insert selected citations into %d", getApplicationName());
    }

    @Override
    public String getTooltip() {
        return Localization.lang("Push to %0", getApplicationName());
    }

    @Override
    public String getKeyStrokeName() {
        return Localization.lang("Push to %0", getApplicationName());
    }

    @Override
    public void pushEntries(BibtexDatabase database, BibtexEntry[] entries, String keyString, MetaData metaData) {

        couldNotConnect = false;
        couldNotCall = false;
        notDefined = false;

        initParameters();
        commandPath = Globals.prefs.get(commandPathPreferenceKey);

        if ((commandPath == null) || commandPath.trim().isEmpty()) {
            notDefined = true;
            return;
        }

        try {
            Runtime.getRuntime().exec(getCommandLine(keyString));
        }

        catch (IOException excep) {
            couldNotCall = true;
            excep.printStackTrace();
        }
    }

    @Override
    public void operationCompleted(BasePanel panel) {
        if (notDefined) {
            // @formatter:off
            panel.output(Localization.lang("Error") + ": " 
                    + Localization.lang("Path to %0 not defined", getApplicationName()) + ".");
            // @formatter:on
        } else if (couldNotCall) {
            panel.output(getCouldNotCall());
        } else if (couldNotConnect) {
            panel.output(getCouldNotConnect());
        } else {
            panel.output(Localization.lang("Pushed citations to %0", getApplicationName()) + ".");
        }
    }

    @Override
    public boolean requiresBibtexKeys() {
        return true;
    }

    protected String[] getCommandLine(String keyString) {
        return null;
    }

    protected String getCommandName() {
        return null;
    }

    @Override
    public JPanel getSettingsPanel() {
        initParameters();
        commandPath = Globals.prefs.get(commandPathPreferenceKey);
        if (settings == null) {
            initSettingsPanel();
        }
        Path.setText(commandPath);
        return settings;
    }

    abstract protected void initParameters();

    protected void initSettingsPanel() {
        builder = FormBuilder.create();
        builder.layout(new FormLayout("left:pref, 4dlu, fill:pref:grow, 4dlu, fill:pref", "p"));
        String label = Localization.lang("Path to %0", getApplicationName());
        // In case the application name and the actual command is not the same, add the command in brackets
        if (getCommandName() != null) {
            label += " (" + getCommandName() + "):";
        } else {
            label += ":";
        }
        builder.add(label).xy(1, 1);
        builder.add(Path).xy(3, 1);
        BrowseAction action = BrowseAction.buildForFile(Path);
        JButton browse = new JButton(Localization.lang("Browse"));
        browse.addActionListener(action);
        builder.add(browse).xy(5, 1);
        settings = builder.build();
    }

    @Override
    public void storeSettings() {
        Globals.prefs.put(commandPathPreferenceKey, Path.getText());
    }

    protected String getCouldNotCall() {
        // @formatter:off
        return Localization.lang("Error") + ": "
                + Localization.lang("Could not call executable") + " '" + commandPath + "'.";
        // @formatter:on
    }

    protected String getCouldNotConnect() {
        // @formatter:off
        return Localization.lang("Error") + ": "
                + Localization.lang("Could not connect to ") + getApplicationName() + ".";
        // @formatter:on
    }

}
