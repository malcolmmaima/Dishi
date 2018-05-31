package malcolmmaima.dishi.Model;

public class User {
    private String Account_type;
    private String Email;
    private String Gender;
    private String Name;
    private String Notifications;
    private String Verified;

    public User() {
        //Empty constructor required by firebase
        // serialize the data retrieved and convert
        // it to an object of this class
    }

    public String GetAccount_type() {
        return Account_type;
    }

    public void setAccount_type(String Account_type) {
        this.Account_type = Account_type;
    }

    public String GetEmail() {
        return Email;
    }

    public void setEmail_(String Email) {
        this.Email = Email;
    }

    public String getGender_(){
        return Gender;
    }

    public void setGender_(String Gender){
        this.Gender = Gender;
    }

    public String getName_(){
        return Name;
    }

    public void setName_(String Name){
        this.Name = Name;
    }

    public String getNotifications_(){
        return Notifications;
    }

    public void setNotifications_(String Notifications){
        this.Notifications = Notifications;
    }

    public String getVerified_(){
        return Verified;
    }

    public void setVerified(String Verified){
        this.Verified = Verified;
    }
}