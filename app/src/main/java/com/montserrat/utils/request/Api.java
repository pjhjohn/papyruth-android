package com.montserrat.utils.request;

/**
 * Created by pjhjohn on 2015-05-04.
 *
 */
public class Api {
    private static Api instance = null;
    /**
     * Singleton
     * @return instance of Api class. returns null if not built via Api.Builder.
     */
    public static Api getInstance() {
        return instance;
    }
    private String endpoint;
    private Api(String endpoint) {
        this.endpoint = endpoint;
    }

    public String url(String action) {
        return this.endpoint + action;
    }

    /* Api Builder */
    public static class Builder {
        private String root, version;
        private boolean ssl;
        public Builder() {
            this.root = "";
            this.version = "";
            this.ssl = false;
        }
        public Builder setRoot(String root) {
            this.root = root;
            return this;
        }
        public Builder setVersion(String version) {
            this.version = version;
            return this;
        }
        public Builder enableSSL(boolean enable) {
            this.ssl = enable;
            return this;
        }
        public Api build() { // TODO : needs error handling
            String endpoint = String.format("%s://%s/api/%s/",
                ssl ? "https" : "http",
                root.isEmpty() ? "" : root.charAt(root.length()-1) == '/' ? root.substring(0, root.length()-1) : root,
                version.isEmpty() ? "" : version.charAt(version.length()-1) == '/' ? version.substring(0, root.length()-1) : version
            );
            Api.instance = new Api(endpoint);
            return Api.instance;
        }
    }
}
