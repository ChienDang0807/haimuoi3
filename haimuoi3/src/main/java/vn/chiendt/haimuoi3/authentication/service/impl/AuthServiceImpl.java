package vn.chiendt.haimuoi3.authentication.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.chiendt.haimuoi3.authentication.dto.request.LoginRequest;
import vn.chiendt.haimuoi3.authentication.dto.request.RegisterRequest;
import vn.chiendt.haimuoi3.authentication.dto.response.AuthResponse;
import vn.chiendt.haimuoi3.authentication.mapper.AuthMapper;
import vn.chiendt.haimuoi3.authentication.service.AuthService;
import vn.chiendt.haimuoi3.authentication.validator.LoginValidator;
import vn.chiendt.haimuoi3.authentication.validator.RegisterRequestValidator;
import vn.chiendt.haimuoi3.common.config.JwtTokenProvider;
import vn.chiendt.haimuoi3.user.model.postgres.UserEntity;
import vn.chiendt.haimuoi3.user.model.postgres.UserRole;
import vn.chiendt.haimuoi3.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RegisterRequestValidator registerRequestValidator;
    private final LoginValidator loginValidator;
    private final AuthMapper authMapper;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        registerRequestValidator.validate(request);

        UserEntity user = UserEntity.builder()
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(UserRole.CUSTOMER)
                .isActive(true)
                .isVerified(false)
                .build();

        UserEntity savedUser = userRepository.save(user);

        String token = jwtTokenProvider.generateToken(savedUser);
        long expiresIn = jwtTokenProvider.getExpirationTime();

        return authMapper.toAuthResponse(savedUser, token, expiresIn);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        loginValidator.validate(request);

        UserEntity user = userRepository.findByEmail(request.getEmailOrPhone())
                .or(() -> userRepository.findByPhone(request.getEmailOrPhone()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid email/phone or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email/phone or password");
        }

        if (!user.getIsActive()) {
            throw new IllegalArgumentException("Account is inactive");
        }

        String token = jwtTokenProvider.generateToken(user);
        long expiresIn = jwtTokenProvider.getExpirationTime();

        return authMapper.toAuthResponse(user, token, expiresIn);
    }

    @Override
    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }

}
