package org.hildan.fxlog.version.bintray;

import java.util.List;

@SuppressWarnings("unused")
public class Package {

    private String name;

    private String repository;

    private String owner;

    private String description;

    private List<String> labels;

    private List<String> attributeNames;

    private Integer rating;

    private Integer ratingCount;

    private Integer followersCount;

    private String created;

    private List<String> versions;

    private String latestVersion;

    private String updated;

    private List<String> linkedToRepos;

    private List<String> systemIds;

    public String getName() {
        return name;
    }

    public String getRepository() {
        return repository;
    }

    public String getOwner() {
        return owner;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getLabels() {
        return labels;
    }

    public List<String> getAttributeNames() {
        return attributeNames;
    }

    public Integer getRating() {
        return rating;
    }

    public Integer getRatingCount() {
        return ratingCount;
    }

    public Integer getFollowersCount() {
        return followersCount;
    }

    public String getCreated() {
        return created;
    }

    public List<String> getVersions() {
        return versions;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public String getUpdated() {
        return updated;
    }

    public List<String> getLinkedToRepos() {
        return linkedToRepos;
    }

    public List<String> getSystemIds() {
        return systemIds;
    }
}
