package ru.itmo.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@SpringBootApplication
public class ClientApplication implements ApplicationRunner {

    @Value("${auth.url}")
    private String authServerUrl;

    @Value("${target.url}")
    private String targetServerUrl;

    private AuthenticationResponseDto token;

    private final RestTemplate rest = new RestTemplate();

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        boolean working = true;

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (working) {
            System.out.print("> ");
            System.out.flush();

            String line = reader.readLine();
            if (line == null) {
                System.out.println();
                working = false;
                continue;
            }

            try {
                String command = line.trim();
                switch (command) {
                    case "register":
                        Role role = readRole(reader);
                        if (role == null) {
                            break;
                        }

                        switch (role) {
                            case USER:
                                registerUser(reader);
                                break;

                            case ADMIN:
                                registerAdmin(reader);
                                break;
                        }
                        break;

                    case "login":
                        login(reader);
                        break;

                    case "logout":
                        logout();
                        break;

                    case "everybodyEndpoint":
                        if (requestWithToken(HttpMethod.GET, targetServerUrl + "api/endpoint0", null, HttpStatus.class) != null) {
                            System.out.println("Success");
                        }

                        break;

                    case "adminsOnlyEndpoint":
                        try {
                             if (requestWithToken(HttpMethod.GET, targetServerUrl + "api/endpoint1", null, HttpStatus.class) != null) {
                                 System.out.println("Success");
                             }
                        } catch (HttpStatusCodeException e) {
                            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                                System.out.println("You must have admin role to have access to this endpoint");
                            }
                        }
                        break;

                    case "nobodyEndpoint":
                        try {
                            if (requestWithToken(HttpMethod.GET, targetServerUrl + "api/endpoint2", null, HttpStatus.class) != null) {
                                System.out.println("Success");
                            }
                        } catch (HttpStatusCodeException e) {
                            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                                System.out.println("Nobody have access to this endpoint");
                            }
                        }
                        break;

                    case "":
                        break;

                    case "help":
                        System.out.println("supported commands:\nregister\nlogin\nlogout\neverybodyEndpoint\nadminsOnlyEndpoint\nnobodyEndpoint\nquit");
                        break;

                    case "quit":
                        working = false;
                        break;

                    default:
                        System.out.printf("Unknown command \"%s\"\n", command);
                }
            } catch (Exception e) {
                System.err.println("An unknown error occurred: ");
                e.printStackTrace();
            }
        }

        System.out.println("Bye!");
    }

    private static Role readRole(BufferedReader reader) throws IOException {
        while (true) {
            String role = readField(reader, "Choose role (user, admin): ");
            if (role == null) {
                return null;
            }

            try {
                return Role.valueOf(role.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Wrong role name provided");
            }
        }
    }

    private static String readFieldNonEmpty(BufferedReader reader, String prompt) throws IOException {
        while (true) {
            String value = readField(reader, prompt);

            if (value == null) {
                return null;
            }

            if (value.isEmpty()) {
                System.out.println("Empty value provided");
                continue;
            }

            return value;
        }
    }

    private static String readField(BufferedReader reader, String prompt) throws IOException {
        System.out.print(prompt);
        System.out.flush();

        String value = reader.readLine();
        if (value == null) {
            return null;
        }

        return value.trim();
    }

    private static String readPassword(String prompt) {
        while (true) {
            char[] value = System.console().readPassword(prompt);

            if (value == null) {
                return null;
            }

            if (value.length == 0) {
                System.out.println("Empty password provided");
                continue;
            }

            return String.valueOf(value);
        }
    }
    
    private static AuthenticationRequestDto readRegisterData(BufferedReader reader) throws IOException {
        AuthenticationRequestDto dto = readLoginData(reader);

        if (dto == null) {
            return null;
        }

        String repeatedPassword = readPassword("Repeat password: ");
        if (repeatedPassword == null) {
            return null;
        }

        if (!dto.getPassword().equals(repeatedPassword)) {
            System.out.println("Passwords are different");
            return null;
        }

        return dto;
    }

    private static AuthenticationRequestDto readLoginData(BufferedReader reader) throws IOException {
        String username = readFieldNonEmpty(reader, "Choose username: ");
        if (username == null) {
            return null;
        }

        String password = readPassword("Choose password: ");
        if (password == null) {
            return null;
        }

        return new AuthenticationRequestDto(username, password);
    }


    private void refreshTokens() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("refreshToken", token.getRefreshToken());
        HttpEntity<MultiValueMap<String, String>> refreshEntity = new HttpEntity<>(form, headers);

        try {
            ResponseEntity<AuthenticationResponseDto> refreshResp = rest.postForEntity(authServerUrl + "api/auth/refresh",
                    refreshEntity, AuthenticationResponseDto.class);

            token = refreshResp.getBody();
        } catch (HttpStatusCodeException e) {
            System.out.println("Refresh token is invalid or expired");
            token = null;
        }
    }

    private void registerUser(BufferedReader reader) throws IOException {
        AuthenticationRequestDto authReq = readRegisterData(reader);
        if (authReq == null) {
            return;
        }

        try {
            rest.postForEntity(authServerUrl + "api/auth/register/user", authReq, String.class);
            System.out.println("Registered successfully");
        } catch (HttpStatusCodeException e) {
            System.out.println("Username is busy");
        }
    }

    private void registerAdmin(BufferedReader reader) throws IOException {
        AuthenticationRequestDto authReq = readRegisterData(reader);
        if (authReq == null) {
            return;
        }

        try {
            ResponseEntity<String> resp = requestWithToken(HttpMethod.POST, authServerUrl + "api/auth/register/admin", authReq, String.class);

            if (resp != null && authReq.getUsername().equals(resp.getBody())) {
                System.out.println("Registered successfully");
            }
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode()) {
                case FORBIDDEN:
                    System.out.println("You don't have permissions");
                    break;

                case CONFLICT:
                    System.out.println("Username is busy");
                    break;

                default:
                    throw e;
            }
        }
    }

    private void login(BufferedReader reader) throws IOException {
        AuthenticationRequestDto authReq = readLoginData(reader);
        if (authReq == null) {
            return;
        }

        try {
            ResponseEntity<AuthenticationResponseDto> resp = rest.postForEntity(authServerUrl + "api/auth/login", authReq, AuthenticationResponseDto.class);
            System.out.println("You`ve logged in successfully");
            token = resp.getBody();
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                System.out.println("Invalid username or password");
            }
        }
    }

    private void logout() {
        if (requestWithToken(HttpMethod.POST, authServerUrl + "api/auth/logout", null, Object.class) != null) {
            System.out.println("Legged out successfully");
            token = null;
        }
    }

    private <T, U> ResponseEntity<T> requestWithToken(HttpMethod method, String url, U requestBody, Class<T> responseClass) {
        if (token == null) {
            System.out.println("You need to login before");
            return null;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token.getAccessToken());
        HttpEntity<U> entity = new HttpEntity<>(requestBody, headers);

        try {
            return rest.exchange(url, method, entity, responseClass);
        } catch (HttpStatusCodeException e) {
            refreshTokens();

            if (token == null) {
                return null;
            }

            headers.setBearerAuth(token.getAccessToken());
        }

        return rest.exchange(url, method, entity, responseClass);
    }
}
