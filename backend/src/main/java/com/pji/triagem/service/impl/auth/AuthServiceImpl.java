package com.pji.triagem.service.impl.auth;

import com.pji.triagem.base.provider.JwtTokenProvider;
import com.pji.triagem.config.ApplicationProperties;
import com.pji.triagem.dto.response.InfoLoginDTO;
import com.pji.triagem.dto.response.TokenPair;
import com.pji.triagem.dto.response.TokenResponse;
import com.pji.triagem.dto.response.UserAuth;
import com.pji.triagem.exception.BlockedUserException;
import com.pji.triagem.exception.InvalidLoginException;
import com.pji.triagem.exception.InvalidTokenException;
import com.pji.triagem.exception.ResourceNotFoundException;
import com.pji.triagem.factory.LoginProcessorFactory;
import com.pji.triagem.model.TypeUser;
import com.pji.triagem.service.AuthService;
import com.pji.triagem.service.LoginProcessor;
import com.pji.triagem.service.UserService;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final LoginProcessorFactory processorFactory;
    private final ApplicationProperties applicationProperties;

    @Override
    public TokenResponse login(String login, String password, TypeUser typeUser) {
        Authentication authentication = authenticateUser(login, password, typeUser);
        //resetar tentativas de login
        userService.resetLoginAttempts(login, typeUser);
        UserAuth userDetails = loadAndGetUser(authentication);
        LoginProcessor processor = processorFactory.getProcessor(typeUser);
        InfoLoginDTO i = processor.resolveName(userDetails);
        return tokenResponse(processor.generateToken(userDetails, i));
    }

    TokenResponse tokenResponse(TokenPair tokens){
        return TokenResponse.builder()
                .accessToken(tokens.accessToken())
                .refreshToken(tokens.refreshToken())
                .accessTokenExpiration(applicationProperties.getToken().getAccess().getExpiration())
                .refreshTokenExpiration(applicationProperties.getToken().getRefresh().getExpiration())
                .build();
    }

    private UserAuth loadAndGetUser(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserAuth)) {
            throw new IllegalStateException("Principal não é um UserAuth! Tipo real: " + principal.getClass().getName());
        }

        return (UserAuth) principal;
    }

    private Authentication authenticateUser(String login, String password, TypeUser typeUser) {
        Authentication authentication;
        try {
            authentication =  authenticationManager.authenticate(
                    new CustomUsernamePasswordToken(login, password, typeUser));
        } catch (BadCredentialsException e) {
            userService.addLoginAttemps(login, typeUser);
            throw new InvalidLoginException("Usuário ou senha inválidos");
        } catch (ResourceNotFoundException e) {
            throw new InvalidLoginException("Usuário ou senha inválidos");
        } catch (BlockedUserException e) {
            throw new InvalidLoginException(e.getMessage());
        }
        return authentication;
    }

    public TokenResponse refreshTokens(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new InvalidTokenException("Refresh token inválido ou expirado");
        }
        Claims claims = tokenProvider.getClaims(refreshToken);
        if (!"refresh".equals(claims.get("token_type"))) {
            throw new InvalidTokenException("Token não é do tipo refresh");
        }

        Long userId = Long.valueOf(claims.get("id").toString());
        TypeUser typeUser = TypeUser.valueOf(claims.get("type").toString());

        UserAuth userDetails = userService.loadUserById(userId);
        LoginProcessor processor = processorFactory.getProcessor(typeUser);
        InfoLoginDTO d = processor.resolveName(userDetails);
        TokenPair tokens = processor.generateToken(userDetails, d);
        return tokenResponse(tokens);
    }
}
