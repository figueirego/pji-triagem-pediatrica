package com.pji.triagem.service.impl;

import com.pji.triagem.base.repository.BaseRepository;
import com.pji.triagem.base.service.impl.BaseServiceImpl;
import com.pji.triagem.base.utils.ReplaceUtils;
import com.pji.triagem.base.validator.DocumentValidator;
import com.pji.triagem.dto.request.UpdateUserProfileRequest;
import com.pji.triagem.dto.response.UserAuth;
import com.pji.triagem.exception.InformationFoundExeption;
import com.pji.triagem.exception.InvalidLoginException;
import com.pji.triagem.exception.ResourceNotFoundException;
import com.pji.triagem.exception.ValidationException;
import com.pji.triagem.factory.DocumentValidatorFactory;
import com.pji.triagem.factory.UserFactory;
import com.pji.triagem.model.TypeUser;
import com.pji.triagem.model.User;
import com.pji.triagem.repository.UserRepository;
import com.pji.triagem.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
@Slf4j
public class UserServiceImpl extends BaseServiceImpl<User> implements UserService {

    private final UserRepository userRepository;

    @Override
    protected BaseRepository<User, Long> getRepository() {
        return userRepository;
    }

    @Override
    public String getResourceName() {
        return "User";
    }


    public User findByLoginAndType(String login, TypeUser typeUser) {
        return userRepository.findByLoginAndType(login,typeUser)
                .orElseThrow(() -> new ResourceNotFoundException(getResourceName()));
    }

    public User findByUserAndType(Long id, TypeUser typeUser) {
        return userRepository.findByIdType(id,typeUser)
                .orElseThrow(() -> new ResourceNotFoundException(getResourceName()));
    }

    public void resetLoginAttempts(String login, TypeUser typeUser) {
        userRepository.updateAttemptsByLoginAndType(login,typeUser,0);
    }

    public void addLoginAttemps(String login, TypeUser typeUser) {
        User user = getSelf().findByLoginAndType(login, typeUser);
        user.setAttemptsCount(user.getAttemptsCount() + 1);
        if(user.getAttemptsCount() > 5){
            user.setIsBlocked(true);
        }
        getSelf().save(user);
    }

    public void isActive(Long id){
        User user = getSelf().find(id);
        user.setIsActive(!user.getIsActive());
        getSelf().save(user);
    }

    public void validateRegisterUser(String login, TypeUser type) {
        DocumentValidator validator = DocumentValidatorFactory.getValidator(login);
        if(!validator.isValid(login)){
            throw new ValidationException("Não foi possivel criar o usuário , pois o CPF/CNPJ não é válido");
        }
        if(userRepository.existsByLoginAndType(login, type)){
            throw new ValidationException("Não foi possivel criar o usuário , pois o CPF já está cadastrado");
        }
    }

    private UserServiceImpl getSelf() {
        return (UserServiceImpl) AopContext.currentProxy();
    }

    public UserAuth loadUserById(Long id) {
        User userEntity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (userEntity.getIsBlocked()) {
            throw new InvalidLoginException("Usuário bloqueado");
        }

        return UserAuth.fromEntity(userEntity);
    }

    @Override
    public User registerClientUser(String login, String password, TypeUser typeUser, String email, String name) {
        login = ReplaceUtils.refactoryString(login);
        password = ReplaceUtils.removeSpacesEmpty(password);
        String normalizedEmail = normalizeRequiredEmail(email);

        DocumentValidator validator = DocumentValidatorFactory.getValidator(login);
        if(!validator.isValid(login)){
            throw new ValidationException("Não foi possivel criar o usuário , pois o CPF/CNPJ não é válido");
        }
        if(userRepository.existsByLoginAndType(login, typeUser)){
            throw new InformationFoundExeption("CPF já cadastrado");
        }
        if(userRepository.existsByEmailAndType(normalizedEmail, typeUser)){
            throw new InformationFoundExeption("email já cadastrado");
        }

        User newUser = UserFactory.createUserTypeClient(login, password, normalizedEmail, name.trim());
        return getSelf().save(newUser);
    }

    @Override
    @Transactional
    public User updateProfile(Long userId, UpdateUserProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        String name = normalizeOptional(request.getName());
        String email = normalizeOptional(request.getEmail());

        if (name != null) {
            user.setName(name);
        }
        if (email != null) {
            user.setEmail(email);
        }
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isBlank()) {
            throw new ValidationException("Campo informado não pode ser vazio");
        }
        return trimmed;
    }

    private String normalizeRequiredEmail(String value) {
        String email = normalizeOptional(value);
        if (email == null) {
            throw new ValidationException("Email é obrigatório");
        }
        return email.toLowerCase();
    }

}
