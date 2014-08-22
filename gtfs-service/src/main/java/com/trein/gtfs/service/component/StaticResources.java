package com.trein.gtfs.service.component;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

@Component
public class StaticResources {

    private static final String TERMS_OF_USE_FILE = "terms_br.html";

    private final String terms;

    public StaticResources() {
        try {
            URL resource = getClass().getClassLoader().getResource(TERMS_OF_USE_FILE);
            this.terms = FileUtils.readFileToString(new File(resource.getPath()));
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public String getTermsOfUse() {
        return this.terms;
    }

}
