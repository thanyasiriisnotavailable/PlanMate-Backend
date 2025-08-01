package senior.project.security.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import senior.project.entity.User;
import senior.project.service.UserService;

import java.io.IOException;
import java.util.Optional;

@Component
public class FirebaseTokenVerifier extends OncePerRequestFilter {

    @Autowired
    private UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String idToken = authHeader.replace("Bearer ", "");

            try {
                FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
                request.setAttribute("firebaseUser", decodedToken);

                // Check if user exists in DB
                String uid = decodedToken.getUid();
                Optional<User> existingUser = userService.findByUid(uid);
                if (existingUser.isEmpty()) {
                    User newUser = new User();
                    newUser.setUid(uid);
                    newUser.setEmail(decodedToken.getEmail());
                    newUser.setDisplayName(decodedToken.getName());
                    newUser.setProfileImage(decodedToken.getPicture());
                    userService.saveUser(newUser);
                }

            } catch (FirebaseAuthException e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}