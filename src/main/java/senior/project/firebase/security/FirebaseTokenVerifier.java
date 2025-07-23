package senior.project.firebase.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import senior.project.entity.User;
import senior.project.service.UserService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class FirebaseTokenVerifier extends OncePerRequestFilter {

    private final UserService userService;

    public FirebaseTokenVerifier(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        System.out.println("Authorization Header: " + authHeader);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String idToken = authHeader.replace("Bearer ", "");

            try {
                FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
                request.setAttribute("firebaseUser", decodedToken);

                // Fetch the full user record
                UserRecord userRecord = FirebaseAuth.getInstance().getUser(decodedToken.getUid());

                // Extract 'auth_time' from the token's claims
                Object authTimeObj = decodedToken.getClaims().get("auth_time");
                if (authTimeObj == null) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
                long tokenAuthTime = ((Number) authTimeObj).longValue() * 1000L; // Convert to ms
                long tokensValidAfter = userRecord.getTokensValidAfterTimestamp(); // Already in ms

                // Invalidate if token was issued before session revocation
                if (tokenAuthTime < tokensValidAfter) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                String uid = decodedToken.getUid();
                User existingUser = userService.findByUid(uid);
                if (existingUser == null) {
                    User newUser = new User();
                    newUser.setUid(uid);
                    newUser.setEmail(decodedToken.getEmail());
                    userService.save(newUser);
                }

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        uid,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (FirebaseAuthException e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}