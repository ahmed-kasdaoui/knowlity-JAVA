package tn.knowlity;

import tn.knowlity.entity.User;
import tn.knowlity.service.userService;

import java.sql.SQLException;
import java.util.Date;

public class Main {
    public static void main(String[] args) throws SQLException {
        userService userService = new userService();
       String[]  roles = new String[]{"Etudiant"};

        User user  = new User( "ahmed","ahmed@gmail.com",  java.sql.Date.valueOf("2002-11-11"),222222,"brahim","test.jpg","homme","tunis","brahim",0,"math" , roles          ,  "brhaim");
        userService.ajouterEtudiant(user);
    }
}
