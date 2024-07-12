package app.ecosynergy.api.services;

import app.ecosynergy.api.controllers.UserController;
import app.ecosynergy.api.data.vo.v1.UserVO;
import app.ecosynergy.api.exceptions.RequiredObjectIsNullException;
import app.ecosynergy.api.exceptions.ResourceNotFoundException;
import app.ecosynergy.api.mapper.DozerMapper;
import app.ecosynergy.api.models.User;
import app.ecosynergy.api.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class UserServices implements UserDetailsService {
    @Autowired
    UserRepository repository;

    @Autowired
    PagedResourcesAssembler<UserVO> assembler;

    private static final Logger logger = Logger.getLogger(UserServices.class.getName());

    public PagedModel<EntityModel<UserVO>> findAll(Pageable pageable){
        logger.info("Finding all Users!");

        Page<User> userPage = repository.findAll(pageable);
        Page<UserVO> voPage = userPage.map(u -> DozerMapper.parseObject(u, UserVO.class));

        voPage.map(user -> {
            try{
                return user.add(linkTo(methodOn(UserController.class).findById(user.getKey())).withSelfRel());
            } catch(Exception e){
                throw new RuntimeException(e);
            }
        });

        Link link = linkTo(methodOn(UserController.class)
                .findAll(pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSort().toString()
                ))
                .withSelfRel();

        return assembler.toModel(voPage, link);
    }

    public UserVO findById(Long id){
        if(id == null) throw new RequiredObjectIsNullException();

        logger.info("Finding User by Id!");

        User entity = repository.findById(id)
                .orElseThrow();

        UserVO vo = DozerMapper.parseObject(entity, UserVO.class);
        vo.add(linkTo(methodOn(UserController.class).findById(id)).withSelfRel());
        return vo;
    }

    public UserVO findByUsername(String username){
        if(username == null) throw new RequiredObjectIsNullException();

        logger.info("Finding User by Username!");

        User entity = repository.findByUsername(username);

        UserVO vo = DozerMapper.parseObject(entity, UserVO.class);
        vo.add(linkTo(methodOn(UserController.class).findById(vo.getKey())).withSelfRel());
        return vo;
    }

    public UserVO create(UserVO user){
        if(user == null) throw new RequiredObjectIsNullException();

        User entity = DozerMapper.parseObject(user, User.class);

        logger.info("Creating user!");

        UserVO vo = DozerMapper.parseObject(repository.save(entity), UserVO.class);
        vo.add(linkTo(methodOn(UserController.class).findById(vo.getKey())).withSelfRel());

        return vo;
    }

    public UserVO update(UserVO user){
        if(user == null) throw new RequiredObjectIsNullException();

        User entity = repository.findById(user.getKey())
                .orElseThrow();
        entity.setFullName(user.getFullName() != null ? user.getFullName() : entity.getFullName());
        entity.setEmail(user.getEmail() != null ? user.getEmail() : entity.getEmail());
        entity.setGender(user.getGender() != null ? user.getGender() : entity.getGender());
        entity.setNationality(user.getNationality() != null ? user.getNationality() : entity.getNationality());

        logger.info("Updating user!");

        UserVO vo = DozerMapper.parseObject(repository.save(entity), UserVO.class);
        vo.add(linkTo(methodOn(UserController.class).findById(vo.getKey())).withSelfRel());

        return vo;
    }

    public void delete(Long id){
        if(id == null) throw new RequiredObjectIsNullException();

        User entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(""));

        logger.info("Deleting user: " + entity.getId());

        repository.delete(entity);
    }

    public UserVO recoverPassword(UserVO user){
        if(user.getUserName() == null || user.getPassword() == null) throw new RequiredObjectIsNullException();

        logger.info("Recovering password");

        User entity = repository.findByUsername(user.getUserName());

        entity.setPassword(passwordEncode(user.getPassword()));

        UserVO vo = DozerMapper.parseObject(repository.save(entity), UserVO.class);
        vo.add(linkTo(methodOn(UserController.class).findById(vo.getKey())).withSelfRel());

        return vo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Finding one User by name " + username + "!");

        var user = repository.findByUsername(username);

        if(user != null){
            return user;
        } else {
            throw new UsernameNotFoundException("Username " + username + " not found!");
        }
    }

    private String passwordEncode(String password){
        Map<String, PasswordEncoder> encoders = new HashMap<>();

        Pbkdf2PasswordEncoder pbkdf2PasswordEncoder = new Pbkdf2PasswordEncoder(
                "",
                8,
                185000,
                Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256
        );

        encoders.put("pbkdf2", pbkdf2PasswordEncoder);
        DelegatingPasswordEncoder passwordEncoder = new DelegatingPasswordEncoder("pbkdf2", encoders);
        passwordEncoder.setDefaultPasswordEncoderForMatches(pbkdf2PasswordEncoder);

        return passwordEncoder.encode(password);
    }
}
