package pl.kontomatik.challenge.navigator.dto;

import java.util.Map;
import java.util.Objects;

public class BaseRequest {
    private int version;
    private int seq;
    private String location;
    private Map<String, Object> data;

    public BaseRequest(int version, int seq, String location, Map<String, Object> data) {
        this.version = version;
        this.seq = seq;
        this.location = location;
        this.data = data;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
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
}
