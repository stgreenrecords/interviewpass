package interviewpass.apibuilder.binary.impl;

public enum Type {
    AVATAR("avatar", "avatar", true),
    PORTFOLIO("portfolio", "portfolio", false);

    private final String name;
    private final String relPath;
    private final boolean singleton;

    Type(String name, String relPath, boolean singleton) {
        this.name = name;
        this.relPath = relPath;
        this.singleton = singleton;
    }

    public String getName() {
        return name;
    }

    public String getRelPath() {
        return relPath;
    }

    public boolean isSingleton() {
        return singleton;
    }
}
