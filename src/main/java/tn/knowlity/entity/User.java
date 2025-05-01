package tn.knowlity.entity;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

public class User {
    private int id;
    private String nom;
    private String prenom;
    private Date date_naissance;
    private String email;
    private int num_telephone   ;
    private String password;
    private String image;
    private String genre;
    private String localisation;
    private Date creted_at;
    private Date last_login;
    private  String confirm_password;
    private String verification_code;
    private int banned;
    private int deleted;
    private int grade_level;
    private String specialite;
    private String[] roles;
    private String google_id;

    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getEmail() {
        return email;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getPassword() {
        return password;
    }

    public int getNum_telephone() {
        return num_telephone;
    }

    public String getImage() {
        return image;
    }

    public String getGenre() {
        return genre;
    }

    public String getLocalisation() {
        return localisation;
    }

    public Date getCreted_at() {
        return creted_at;
    }

    public Date getLast_login() {
        return last_login;
    }

    public String getConfirm_password() {
        return confirm_password;
    }

    public String getVerification_code() {
        return verification_code;
    }

    public int getBanned() {
        return banned;
    }

    public int getDeleted() {
        return deleted;
    }

    public int getGrade_level() {
        return grade_level;
    }

    public String getSpecialite() {
        return specialite;
    }

    public String[] getRoles() {
        return roles;
    }

    public Date getDate_naissance() {
        return date_naissance;
    }

    public String getGoogle_id() {
        return google_id;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setDate_naissance(Date date_naissance) {
        this.date_naissance = date_naissance;
    }

    public void setNum_telephone(int num_telephone) {
        this.num_telephone = num_telephone;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    public void setCreted_at(Date creted_at) {
        this.creted_at = creted_at;
    }

    public void setLast_login(Date last_login) {
        this.last_login = last_login;
    }

    public void setConfirm_password(String confirm_password) {
        this.confirm_password = confirm_password;
    }

    public void setVerification_code(String verification_code) {
        this.verification_code = verification_code;
    }

    public void setBanned(int banned) {
        this.banned = banned;
    }

    public void setDeleted(int deleted) {
        this.deleted = deleted;
    }

    public void setGrade_level(int grade_level) {
        this.grade_level = grade_level;
    }

    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }

    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    public void setGoogle_id(String google_id) {
        this.google_id = google_id;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", num_telephone=" + num_telephone +
                ", password='" + password + '\'' +
                ", image='" + image + '\'' +
                ", genre='" + genre + '\'' +
                ", localisation='" + localisation + '\'' +
                ", creted_at=" + creted_at +
                ", last_login=" + last_login +
                ", confirm_password='" + confirm_password + '\'' +
                ", verification_code='" + verification_code + '\'' +
                ", banned=" + banned +
                ", deleted=" + deleted +
                ", grade_level=" + grade_level +
                ", specialite='" + specialite + '\'' +
                ", roles=" + Arrays.toString(roles) +
                ", google_id='" + google_id + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id && num_telephone == user.num_telephone && banned == user.banned && deleted == user.deleted && grade_level == user.grade_level && Objects.equals(nom, user.nom) && Objects.equals(prenom, user.prenom) && Objects.equals(email, user.email) && Objects.equals(password, user.password) && Objects.equals(image, user.image) && Objects.equals(genre, user.genre) && Objects.equals(localisation, user.localisation) && Objects.equals(creted_at, user.creted_at) && Objects.equals(last_login, user.last_login) && Objects.equals(confirm_password, user.confirm_password) && Objects.equals(verification_code, user.verification_code) && Objects.equals(specialite, user.specialite) && Objects.deepEquals(roles, user.roles) && Objects.equals(google_id, user.google_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nom, prenom, email, num_telephone, password, image, genre ,localisation, creted_at, last_login, confirm_password, verification_code, banned, deleted, grade_level, specialite, Arrays.hashCode(roles), google_id);
    }

    public User(String prenom, String email,Date date_naissance ,int num_telephone, String password, String image, String genre, String localisation,  String confirm_password,  int grade_level, String specialite, String[] roles, String nom) {
        this.prenom = prenom;
        this.email = email;
        this.date_naissance=date_naissance;
        this.num_telephone = num_telephone;
        this.password = password;
        this.image = image;
        this.genre = genre;
        this.localisation = localisation;

        this.last_login = null;
        this.confirm_password = confirm_password;
        this.verification_code = null;
        this.banned = 0;
        this.deleted = 0;
        this.grade_level = grade_level;
        this.specialite = specialite;
        this.roles = roles;
        this.google_id = null;
        this.nom = nom;
    }

    public User() {}
}
