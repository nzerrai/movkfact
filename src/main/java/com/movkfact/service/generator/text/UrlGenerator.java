package com.movkfact.service.generator.text;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.service.generator.DataTypeGenerator;

import java.util.Random;

/**
 * Générateur d'URLs réalistes aléatoires.
 */
public class UrlGenerator extends DataTypeGenerator {
    private static final Random random = new Random();
    private static final String[] DOMAINS = {
        "example.com", "testdata.io", "mockapi.dev", "sample.net", "demosite.org",
        "fakedata.com", "testenv.io", "mockserver.net", "devdata.org", "staging.app"
    };
    private static final String[] PATHS = {
        "users", "products", "orders", "api/v1", "dashboard", "reports",
        "admin", "catalog", "search", "profile", "settings", "docs"
    };
    private static final String[] SCHEMES = {"https", "http"};

    public UrlGenerator(ColumnConfigDTO columnConfig) {
        super(columnConfig);
    }

    @Override
    public Object generate() {
        String scheme = SCHEMES[random.nextInt(SCHEMES.length)];
        String domain = DOMAINS[random.nextInt(DOMAINS.length)];
        String path = PATHS[random.nextInt(PATHS.length)];
        int id = random.nextInt(9000) + 1000;
        return scheme + "://" + domain + "/" + path + "/" + id;
    }
}
