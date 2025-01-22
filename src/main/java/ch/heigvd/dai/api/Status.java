package ch.heigvd.dai.api;

public class Status {
    public String status;

    private Status(String status) {
        this.status = status;
    }

    public static Status ok() {
        return new Status("ok");
    }
}
