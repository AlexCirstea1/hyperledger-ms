
package org.example;

import java.nio.file.Paths;
import java.security.PrivateKey;
import java.util.Properties;
import java.util.Set;

import org.hyperledger.fabric.gateway.Identities;
import org.hyperledger.fabric.gateway.Identity;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory;
import org.hyperledger.fabric_ca.sdk.EnrollmentRequest;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;

public class EnrollAppUser {

    public static void main(String[] args) throws Exception {
        // Create a CA client for interacting with the CA.
        Properties props = new Properties();
        props.put("pemFile", "../fabric-ca/org1/ca-cert.pem");
        props.put("allowAllHostNames", "true");
        HFCAClient caClient = HFCAClient.createNewInstance("https://localhost:7054", props);
        CryptoSuite cryptoSuite = CryptoSuiteFactory.getDefault().getCryptoSuite();
        caClient.setCryptoSuite(cryptoSuite);

        // Create a wallet for managing identities
        Wallet wallet = Wallets.newFileSystemWallet(Paths.get("wallet"));

        // Check if admin already exists in wallet
        if (wallet.get("admin") != null) {
            System.out.println("An identity for the admin user \"admin\" already exists in the wallet");
        } else {
            // Enroll the admin user, and import the new identity into the wallet.
            final EnrollmentRequest enrollmentRequestTLS = new EnrollmentRequest();
            enrollmentRequestTLS.addHost("localhost");
            enrollmentRequestTLS.setProfile("tls");
            Enrollment enrollment = caClient.enroll("admin", "adminpw", enrollmentRequestTLS);
            Identity user = Identities.newX509Identity("Org1MSP", enrollment);
            wallet.put("admin", user);
            System.out.println("Successfully enrolled user \"admin\" and imported it into the wallet");
        }
        
        // Check if appUser already exists in wallet
        if (wallet.get("appUser") != null) {
            System.out.println("An identity for the user \"appUser\" already exists in the wallet");
            return;
        }

        // Register the user, enroll the user, and import the new identity into the wallet
        // Must be a user with admin privileges to register new users
        Identity adminIdentity = wallet.get("admin");
        
        // Create registration request
        RegistrationRequest registrationRequest = new RegistrationRequest("appUser");
        registrationRequest.setAffiliation("org1.department1");
        registrationRequest.setEnrollmentID("appUser");
        registrationRequest.setSecret("appUserSecret");
        
        // Register and enroll user
        String enrollmentSecret = caClient.register(registrationRequest, AdminUser.createInstance(adminIdentity));
        Enrollment enrollment = caClient.enroll("appUser", enrollmentSecret);
        Identity user = Identities.newX509Identity("Org1MSP", enrollment);
        wallet.put("appUser", user);
        System.out.println("Successfully enrolled user \"appUser\" and imported it into the wallet");
    }
    
    // Helper class for admin user
    static class AdminUser implements User {
        private final Identity identity;
        
        private AdminUser(Identity identity) {
            this.identity = identity;
        }
        
        public static User createInstance(Identity identity) {
            return new AdminUser(identity);
        }

        @Override
        public String getName() {
            return "admin";
        }

        @Override
        public Set<String> getRoles() {
            return null;
        }

        @Override
        public String getAccount() {
            return null;
        }

        @Override
        public String getAffiliation() {
            return "org1.department1";
        }

        @Override
        public Enrollment getEnrollment() {
            return new Enrollment() {
                @Override
                public PrivateKey getKey() {
                    return identity.getPrivateKey();
                }

                @Override
                public String getCert() {
                    return Identities.toPemString(identity.getCertificate());
                }
            };
        }

        @Override
        public String getMspId() {
            return "Org1MSP";
        }
    }
}
