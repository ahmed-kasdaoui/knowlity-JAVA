package tn.esprit.test;

import tn.esprit.models.*;
import tn.esprit.services.*;

import java.time.LocalDateTime;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        ServiceEvents se = new ServiceEvents();
        ServiceEventRegistration ser = new ServiceEventRegistration();
        ServiceUserEventPreference suep = new ServiceUserEventPreference();
        int choice=-1 ;
        while (choice != 0) {
        System.out.println("1-List\n2-Add\n3-Update\n4-Delete\n0-Exit");
        System.out.print("Enter your choice: ");
        choice = scanner.nextInt();

        if (choice == 1) {
            System.out.println("Événements : " + se.getAll());
            System.out.println("Inscriptions : " + ser.getAll());
            System.out.println("Préférences : " + suep.getAll());

        } else if (choice == 2) {
            Events event = new Events("Conférence Tech", "Une conférence sur la tech",LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), "Conférence", 100, 100, "Paris", "tech.jpg", "Technologie",30F,30F);
            event.setOrganizerId(1);
            se.add(event);


            // Ajouter une inscription
            EventRegistration registration = new EventRegistration(event,1, "Lyon", "Jean Dupont", 2);
            ser.add(registration);

            // Ajouter une préférence
            UserEventPreference preference = new UserEventPreference(event, "Technologie", 5);
            suep.add(preference);
        } else if (choice == 3) {
            Events event1 = new Events("test test", "Une conférence sur la tech",
                    LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                    "Conférence", 100, 100, "Paris", "tech.jpg", "Technologie", 30.00F,30.00F);
            event1.setId(5);
            event1.setOrganizerId(1);
            se.update(event1);


            // Ajouter une inscription
            EventRegistration registration = new EventRegistration(event1,1, "Bizerte", "Jean Dupont", 5);
            registration.setId(2);
            ser.update(registration);

            // Ajouter une préférence
            UserEventPreference preference = new UserEventPreference(event1, "Technologie", 20);
            preference.setId(2);
            suep.update(preference);
        } else if (choice==4) {
            UserEventPreference preference = new UserEventPreference();
            preference.setId(11);
            suep.delete(preference);
        }else {
            System.out.println("Exiting...");
            System.exit(0);
        }
        }
        scanner.close();

    }
}
