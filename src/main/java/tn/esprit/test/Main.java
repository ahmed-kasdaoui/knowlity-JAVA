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
        se.checkEvents();

    }
}
