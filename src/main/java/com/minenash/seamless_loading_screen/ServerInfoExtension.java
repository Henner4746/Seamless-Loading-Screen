package com.minenash.seamless_loading_screen;

public interface ServerInfoExtension {

    /**
     * The mode to use when joining, which is {@link DisplayMode#DISABLED} for blacklisted addresses.
     */
    DisplayMode getDisplayMode();

    /**
     * The mode chosen for this server, regardless of the blacklist.
     */
    DisplayMode getStoredDisplayMode();

    void setDisplayMode(DisplayMode mode);

}
