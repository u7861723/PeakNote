package com.peaknote.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name ="users")
public class User {
    @Id
    @Column(name = "oid")
    private String oid;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String displayname;

    @Override
    public String toString() {
        return "User [oid=" + oid + ", email=" + email + ", displayname=" + displayname + "]";
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayname() {
        return displayname;
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }
}
