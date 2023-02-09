package com.appsdeveloperblog.photoapp.api.users.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.appsdeveloperblog.photoapp.api.users.data.AlbumsServiceClient;
import com.appsdeveloperblog.photoapp.api.users.data.UserEntity;
import com.appsdeveloperblog.photoapp.api.users.repositories.UsersRepository;
import com.appsdeveloperblog.photoapp.api.users.shared.UserDto;
import com.appsdeveloperblog.photoapp.api.users.ui.model.AlbumResponseModel;

import feign.FeignException;

@Service
public class UserServiceImpl implements UsersService {

	private final UsersRepository usersRepository;
	private final BCryptPasswordEncoder encoder;
	private final Environment environment;
	//private final RestTemplate restTemplate;
	private final AlbumsServiceClient albumsService;
	
	Logger logger = LoggerFactory.getLogger(this.getClass());

	public UserServiceImpl(UsersRepository usersRepository, BCryptPasswordEncoder encoder,
			Environment environment/* , RestTemplate restTemplate */, AlbumsServiceClient albumsService) {
		super();
		this.usersRepository = usersRepository;
		this.encoder = encoder;
		this.environment = environment;
		// this.restTemplate = restTemplate;
		this.albumsService = albumsService;
	}

	@Override
	public UserDto createUser(UserDto userDetails) {

		userDetails.setUserId(UUID.randomUUID().toString());
		userDetails.setEncryptedPassword(encoder.encode(userDetails.getPassword()));

		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		UserEntity userEntity = modelMapper.map(userDetails, UserEntity.class);

		usersRepository.save(userEntity);

		UserDto returnValue = modelMapper.map(userEntity, UserDto.class);

		return returnValue;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		UserEntity userEntity = usersRepository.findByEmail(username);

		if (userEntity == null)
			throw new UsernameNotFoundException(username);

		return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), true, true, true, true,
				new ArrayList<>());
	}

	@Override
	public UserDto getUserDetailsByEmail(String email) {
		
		UserEntity userEntity = usersRepository.findByEmail(email);
		if(userEntity==null)
			throw new UsernameNotFoundException(email);
		
		return new ModelMapper().map(userEntity, UserDto.class);
	}

	@Override
	public UserDto getUserByUserId(String userId) {

		UserEntity userEntity = usersRepository.findByUserId(userId);
		if (userEntity == null)
			throw new UsernameNotFoundException("User not found");

		UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);


		// call microservice - PhotoAppApiAlbums - using resttemplate
		/*
		String albumsUrl = String.format(environment.getProperty("albums.url"), userId);
		
		ResponseEntity<List<AlbumResponseModel>> albumsListResponse = restTemplate.exchange(albumsUrl, HttpMethod.GET,
				null, new ParameterizedTypeReference<List<AlbumResponseModel>>() {
				});

		List<AlbumResponseModel> albumsList = albumsListResponse.getBody();
		*/
		
        logger.info("Before calling albums Microservice");
        List<AlbumResponseModel> albumsList = null;
        try {
            albumsList = albumsService.getAlbums(userId);
        }catch (FeignException e) {
            logger.error(e.getLocalizedMessage());
        }
        logger.info("After calling albums Microservice");		
		
		userDto.setAlbums(albumsList);

		return userDto;
	}

}
