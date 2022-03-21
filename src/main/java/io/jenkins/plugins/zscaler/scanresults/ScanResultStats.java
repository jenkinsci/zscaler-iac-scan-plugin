package io.jenkins.plugins.zscaler.scanresults;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ScanResultStats {
    private int total;

    @JsonProperty("severity_counts")
    private SeverityCounts policyCounts;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public SeverityCounts getPolicyCounts() {
        return policyCounts;
    }

    public void setPolicyCounts(SeverityCounts policyCounts) {
        this.policyCounts = policyCounts;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public class SeverityCounts {
        @JsonProperty("HIGH")
        private int high;

        @JsonProperty("LOW")
        private int low;

        @JsonProperty("MEDIUM")
        private int medium;

        @JsonProperty("CRITICAL")
        private int critical;

        public int getHigh() {
            return high;
        }

        public void setHigh(int high) {
            this.high = high;
        }

        public int getLow() {
            return low;
        }

        public void setLow(int low) {
            this.low = low;
        }

        public int getMedium() {
            return medium;
        }

        public void setMedium(int medium) {
            this.medium = medium;
        }

        public int getCritical() {
            return critical;
        }

        public void setCritical(int critical) {
            this.critical = critical;
        }
    }

}
