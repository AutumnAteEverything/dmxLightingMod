package dmx.lighting.client;

import dmx.lighting.LightingConsole;

/**
 * Remembers the user's current Lighting Console layout.
 *
 * This class contains only client-side UI state.
 *
 * It deliberately contains no fixture data.
 *
 * Fixture data is supplied by FixtureBrowserClientStore.
 */
public final class LightingConsoleState {

    /**
     * Which tab is currently visible.
     */
    public enum ConsoleTab {
        FIXTURES,
        GROUPS,
        UNIVERSES,
        PATCH,
        OUTPUT
    }

    private static ConsoleTab selectedTab =
            ConsoleTab.FIXTURES;

    private static LightingConsole.FixtureSortMode sortMode =
            LightingConsole.FixtureSortMode.PATCH;

    private static String searchText = "";

    private static int selectedRow = -1;

    private static int scrollOffset = 0;

    private LightingConsoleState() {
    }

    public static ConsoleTab getSelectedTab() {
        return selectedTab;
    }

    public static void setSelectedTab(
            ConsoleTab tab
    ) {
        if (tab != null) {
            selectedTab = tab;
        }
    }

    public static LightingConsole.FixtureSortMode getSortMode() {
        return sortMode;
    }

    public static void setSortMode(
            LightingConsole.FixtureSortMode mode
    ) {
        if (mode != null) {
            sortMode = mode;
        }
    }

    public static String getSearchText() {
        return searchText;
    }

    public static void setSearchText(
            String text
    ) {
        searchText =
                text == null
                        ? ""
                        : text;
    }

    public static int getSelectedRow() {
        return selectedRow;
    }

    public static void setSelectedRow(
            int row
    ) {
        selectedRow = row;
    }

    public static int getScrollOffset() {
        return scrollOffset;
    }

    public static void setScrollOffset(
            int offset
    ) {
        scrollOffset = Math.max(0, offset);
    }

    /**
     * Restores the default console layout.
     */
    public static void reset() {

        selectedTab =
                ConsoleTab.FIXTURES;

        sortMode =
                LightingConsole.FixtureSortMode.PATCH;

        searchText = "";

        selectedRow = -1;

        scrollOffset = 0;
    }
}