package pl.kontomatik.challenge.navigator.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BaseRequest {
    private final int version;
    private final int seq;
    private final String location;
    private final Map<String, Object> data;

    public BaseRequest(int version, int seq, String location, Map<String, Object> data) {
        this.version = version;
        this.seq = seq;
        this.location = location;
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseRequest that = (BaseRequest) o;
        return version == that.version && seq == that.seq && location.equals(that.location) && data.equals(that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, seq, location, data);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int version;
        private int seq;
        private String location;
        private Map<String, Object> data;

        public Builder() {
            data = new HashMap<>();
        }

        public Builder setVersion(int version) {
            this.version = version;
            return this;
        }

        public Builder setSeq(int seq) {
            this.seq = seq;
            return this;
        }

        public Builder setLocation(String location) {
            this.location = location;
            return this;
        }

        public Builder putData(String key, Object value) {
            this.data.put(key, value);
            return this;
        }

        public BaseRequest build() {
            return new BaseRequest(version, seq, location, data);
        }
    }
}
