package org.mirent.skills.util.cli;

public enum OsType {
    WINDOWS, MAC, LINUX, OTHER;

    public static OsType detect() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) return WINDOWS;
        if (os.contains("mac")) return MAC;
        if (os.contains("nix") || os.contains("nux") || os.contains("aix")) return LINUX;
        return OTHER;
    }
}
