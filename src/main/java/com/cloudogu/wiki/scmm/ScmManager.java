/**
 * Copyright (c) 2016 Cloudogu GmbH. All Rights Reserved.
 *
 * Copyright notice
 */
package com.cloudogu.wiki.scmm;

import com.cloudogu.wiki.Account;
import com.cloudogu.wiki.Wiki;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.io.InputStream;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Sebastian Sdorra
 */
public final class ScmManager {

    private ScmManager() {
    }

    public static List<Wiki> getPotentialWikis(Account account, String scmInstanceUrl) {
        try {
            HttpResponse<JsonNode> response = Unirest.get(apiUrl(scmInstanceUrl, "/repositories.json"))
                    .basicAuth(account.getUsername(), new String(account.getPassword()))
                    .header("accept", "application/json")
                    .asJson();

            if (response.getStatus() != 200) {
                throw new RuntimeException("could not find repositories, scm-manager returned status code " + response.getStatus());
            }

            List<Wiki> wikis = Lists.newArrayList();

            JSONArray repositories = response.getBody().getArray();
            for (int i = 0; i < repositories.length(); i++) {
                JSONObject repository = repositories.getJSONObject(i);
                if ("git".equals(repository.getString("type"))) {
                    wikis.add(convertToWiki(repository, repository.getString("id")));
                }
            }

            return wikis;
        } catch (UnirestException ex) {
            throw Throwables.propagate(ex);
        }
    }

    public static List<Wiki> getBranches(Account account, String scmInstanceUrl, String repository) {
        try {
            HttpResponse<JsonNode> response = Unirest.get(apiUrl(scmInstanceUrl, "/repositories/" + repository + "/branches.json"))
                    .basicAuth(account.getUsername(), new String(account.getPassword()))
                    .header("accept", "application/json")
                    .asJson();

            if (response.getStatus() != 200) {
                return null;
            }

            List<Wiki> wikis = Lists.newArrayList();
            JSONArray branches = (JSONArray) response.getBody().getArray().getJSONObject(0).get("branch");
            for (int i = 0; i < branches.length(); i++) {
                JSONObject branch = branches.getJSONObject(i);
                wikis.add(new Wiki(repository + "/" + branch.getString("name"), branch.getString("name"), null));
            }

            return wikis;
        } catch (UnirestException ex) {
            throw Throwables.propagate(ex);
        }
    }

    public static ScmWiki getWiki(Account account, String scmInstanceUrl, String name) {
        try {
            int index = name.indexOf('/');
            String repository;
            String branch;
            if (index > 0) {
                repository = name.substring(0, index);
                branch = name.substring(index + 1);
            } else {
                return null;
            }
            HttpResponse<InputStream> content = Unirest.get(apiUrl(scmInstanceUrl, "/repositories/" + repository + "/content?path=.smeagol.yml"))
                    .basicAuth(account.getUsername(), new String(account.getPassword()))
                    .asBinary();

            if (content.getStatus() != 200) {
                return null;
            }

            HttpResponse<JsonNode> response = Unirest.get(apiUrl(scmInstanceUrl, "/repositories/" + repository + ".json"))
                    .basicAuth(account.getUsername(), new String(account.getPassword()))
                    .header("accept", "application/json")
                    .asJson();

            if (response.getStatus() != 200) {
                return null;
            }

            return convertToWiki(response.getBody().getObject(), name);
        } catch (UnirestException ex) {
            throw Throwables.propagate(ex);
        }
    }

    private static ScmWiki convertToWiki(JSONObject repository, String id) {
        return new ScmWiki(
                id,
                repository.getString("name"),
                repository.getString("description"),
                repository.getString("url")
        );
    }

    private static String apiUrl(String scmInstanceUrl, String suffix) {
        String endpoint = scmInstanceUrl;
        if (!endpoint.endsWith("/")) {
            endpoint = endpoint.concat("/");
        }
        endpoint = endpoint.concat("api/rest");
        if (!suffix.startsWith("/")) {
            endpoint = endpoint.concat("/");
        }
        return endpoint.concat(suffix);
    }

}
