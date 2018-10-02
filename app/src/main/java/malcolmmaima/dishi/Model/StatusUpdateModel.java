package malcolmmaima.dishi.Model;

public class StatusUpdateModel {

    public String status;
    public String timePosted;
    public String key;
    public String author;
    public String postedTo;

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setTimePosted(String timePosted) {
        this.timePosted = timePosted;
    }

    public String getTimePosted() {
        return timePosted;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthor() {
        return author;
    }

    public void setPostedTo(String postedTo) {
        this.postedTo = postedTo;
    }

    public String getPostedTo() {
        return postedTo;
    }
}
