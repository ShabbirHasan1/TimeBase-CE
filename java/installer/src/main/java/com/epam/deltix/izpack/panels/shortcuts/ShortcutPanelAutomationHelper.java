package com.epam.deltix.izpack.panels.shortcuts;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.event.InstallerListeners;
import com.izforge.izpack.panels.shortcut.ShortcutPanelLogic;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.PlatformModelMatcher;
import com.izforge.izpack.util.TargetFactory;
import com.epam.deltix.izpack.Utils;

/**
 *
 */
public class ShortcutPanelAutomationHelper extends com.izforge.izpack.panels.shortcut.ShortcutPanelAutomationHelper {

    private ShortcutHelper shortcutHelper = new ShortcutHelper();

    public ShortcutPanelAutomationHelper(
            AutomatedInstallData installData,
            Resources resources,
            UninstallData uninstallData,
            Housekeeper housekeeper,
            TargetFactory factory,
            InstallerListeners listeners,
            PlatformModelMatcher matcher) throws Exception
    {
        super(installData, resources, uninstallData, housekeeper, factory, listeners, matcher);
    }

    @Override
    public void runAutomated(InstallData installData, IXMLElement panelRoot) {
        try {
            if (Utils.isExpress(installData)) {
                return;
            }

            ShortcutPanelLogic shortcutPanelLogic = ShortcutHelper.getShortcutPanelLogic(
                    com.izforge.izpack.panels.shortcut.ShortcutPanelAutomationHelper.class,
                    this
            );

            if (shortcutPanelLogic == null)
                throw new Exception("Can't find shortcut panel logic.");

            shortcutPanelLogic.setAutoinstallXMLData(panelRoot);
            if (shortcutPanelLogic.isCreateShortcutsImmediately()) {
                shortcutPanelLogic.refreshShortcutData();
                shortcutHelper.createDesktopDirectory(
                        shortcutPanelLogic,
                        Utils.getPrivateValue(ShortcutPanelLogic.class, shortcutPanelLogic, "groupName")
                );
                shortcutPanelLogic.createAndRegisterShortcuts();
            }
        } catch (Exception e) {
            // ignore exception
        }
    }
}
